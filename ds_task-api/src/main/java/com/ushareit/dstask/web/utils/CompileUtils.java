package com.ushareit.dstask.web.utils;

import com.ushareit.dstask.bean.ArtifactVersion;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.web.compile.CompileBase;
import com.ushareit.dstask.web.compile.JavaCompile;
import com.ushareit.dstask.web.compile.ScalaCompile;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.util.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;

/**
 * @author: licg
 * @create: 2020-12-08
 **/
@Slf4j
public class CompileUtils {

    /**
     * URLClassLoader的addURL方法
     */
    private static Method addURL = initAddMethod();
    /**
     * 初始化addUrl 方法.
     *
     * @return 可访问addUrl方法的Method对象
     */
    private static Method initAddMethod() {
        try {
            Method add = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            add.setAccessible(true);
            return add;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void addLocalCompileDependJar(String... flinkJarPaths) {
        ClassLoader clazzLoader = getClass().getClassLoader();
        Arrays.stream(flinkJarPaths).forEach(flinkJarPath ->{
            if (StringUtils.isEmpty(flinkJarPath) || !new File(flinkJarPath).exists()) {
                throw new RuntimeException("path " + flinkJarPath + " is not exist");
            }
            File[] jars = new File(flinkJarPath).listFiles();
            if (jars == null || jars.length == 0) {
                throw new RuntimeException(flinkJarPath + " no file exist !");
            }

            for (File file : jars) {
                try {
                    if (file.getName().startsWith("flink-dist") || file.getName().startsWith("flink-table-blink")) {
                        addURL.invoke(clazzLoader, file.toURI().toURL());
                    }
                    if (file.getName().startsWith("flink-sql-client")) {
                        addURL.invoke(clazzLoader, file.toURI().toURL());
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(),e);
                }
            }
        });
    }

    public static File doCompileAndPackage(List<ArtifactVersion> onlineUdfs, String classPath) {
        String classTargetDir = DsTaskConstant.LOCAL_UPLOAD_DIR + UUID.randomUUID();
        File targetJar = null;
        if (!new File(classTargetDir).exists()) {
            new File(classTargetDir).mkdirs();
        }
        try {
            //1、遍历编译
            for (ArtifactVersion udf : onlineUdfs) {
                doCompile(udf, classPath, classTargetDir);
            }
            //2、打包jar到job资源目录
            String targetPath = DsTaskConstant.LOCAL_UPLOAD_DIR + UUID.randomUUID();
            if (!new File(targetPath).exists()) {
                new File(targetPath).mkdirs();
            }
            targetJar = new File(targetPath, DsTaskConstant.DEFAULT_UDF_JAR_NAME);
            FileOutputStream jarPath = new FileOutputStream(targetJar);
            ZipUtils.toZip(classTargetDir, jarPath, false);
            log.info(String.format("ZIP jarPath=%s", targetJar.getAbsolutePath()));
        } catch (IOException e) {
            throw new ServiceException(BaseResponseCodeEnum.FILE_COMPILE_FAIL);
        } finally {
            //3、清理class文件目录
            try {
                FileUtils.deleteFileOrDirectory(new File(classTargetDir));
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }

        return targetJar;
    }

    private static void doCompile(ArtifactVersion artifactVersion, String classPath, String classTargetDir) throws UnsupportedEncodingException {
        //1、初始化对应实例
        CompileBase compile = null;
        if (artifactVersion.getTypeCode().equalsIgnoreCase(DsTaskConstant.APPLICATION_TYPE_JAVA)) {
            compile = new JavaCompile();
        } else {
            compile = new ScalaCompile();
        }
        //2、组装成完整类文件
        String classBody = URLDecoder.decode(new String(Base64.getDecoder().decode(artifactVersion.getContent().getBytes())), "UTF-8");
        String className = getClassNameFromClass(classBody);
        log.info(String.format("\nCompile params \nclassName=%s \nclassBody=%s \nclassPath=%s \nclassTargetDir %s\n", className, classBody, classPath, classTargetDir));
        //3、编译成clss文件
        compile.compile(className, classBody, classPath, classTargetDir);
    }

    private static String getClassNameFromClass(String classBody) {
        Matcher m = UrlUtil.getMatcher(classBody, DsTaskConstant.CLASS_NAME_PATTERN);
        return m.group(2);
    }
}

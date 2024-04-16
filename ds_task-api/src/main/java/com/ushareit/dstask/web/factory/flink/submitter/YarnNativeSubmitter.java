package com.ushareit.dstask.web.factory.flink.submitter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.AccessGroup;
import com.ushareit.dstask.bean.AccessTenant;
import com.ushareit.dstask.bean.DependentInformation;
import com.ushareit.dstask.bean.FlinkVersion;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.constant.FlinkExecModeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.service.impl.TaskServiceImpl;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.utils.HttpUtil;
import com.ushareit.dstask.web.factory.Job;
import com.ushareit.dstask.web.factory.flink.FlinkRunCommand;
import com.ushareit.dstask.web.factory.flink.param.RuntimeConfig;
import com.ushareit.dstask.web.utils.*;
import com.ushareit.dstask.web.vo.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * @author xuebotao
 * @date 2023/08/04
 */
@Slf4j
public class YarnNativeSubmitter extends BaseSubmitter {
    public YarnNativeSubmitter(TaskServiceImpl taskServiceImp, Job job) {
        super(taskServiceImp, job);
    }

    @Override
    public void submit() {
        try {
            doExec();
        } catch (Exception e) {
            log.error("yarn submitAsync error");
            processException();
            log.error(e.getMessage(), e);
            throw new ServiceException(BaseResponseCodeEnum.TASK_SUBMIT_FAIL, "flink任务提交到YARN");
        }
    }

    @Override
    public void processException() {
        log.info("YarnNativeSubmitter processException");
        updateTaskAndInstance();
    }

    public void doExec() throws Exception {
        long startMs = System.currentTimeMillis();
        String command = this.getCommand();
        boolean success = this.yarnDoProcess(command);
        log.info("Process completed " + (success ? "successfully" : "unsuccessfully") + " in " + (System.currentTimeMillis() - startMs) / 1000L + " seconds.");
    }


    private Boolean yarnDoProcess(String execCmd) throws Exception {

        String user = new String(org.apache.commons.codec.binary.Base64.encodeBase64(baseJob.runTimeTaskBase.getCreateBy().getBytes()));
        HashMap<String, String> headers = new HashMap<>(1);
        headers.put("Authorization", "Basic " + user);
        log.info("yarn execCmd  config is :" + execCmd + " user is :" + baseJob.runTimeTaskBase.getCreateBy());

        String gatewayHost = DataCakeConfigUtil.getDataCakeConfig().getGatewayRestHost();
        //http://jdbc:hive2://gateway-test.ushareit.org:10009
        BaseResponse response = HttpUtil.postWithJson(gatewayHost + "/api/v1/batches", execCmd, headers);
        JSONObject jsonObject = response.get();
        if (response.getCode() != 0 || jsonObject.get("id") == null) {
            throw new ServiceException(BaseResponseCodeEnum.TASK_SUBMIT_FAIL);
        }
        Object id = jsonObject.get("id");

        log.info(" start flink task uuid is :" + id);

        //获取UI  等5s
        Thread.sleep(10000);
        log.info(" sleep arfter ");
        String logUrl = MessageFormat.format("/api/v1/batches/{0}/localLog?from=0&size=2000", id.toString());
        log.info(" request is : " + gatewayHost + logUrl);
        BaseResponse logResponse = HttpUtil.get(gatewayHost + logUrl);

        Object logRowSet = logResponse.get().get("logRowSet");
        log.info(" frist logRowSet is : " + logRowSet);
        if (logResponse.getCode() != 0 || logRowSet == null) {
            throw new ServiceException(BaseResponseCodeEnum.TASK_GET_LOG_FAIL);
        }

        //Found Web Interface
        String logs = logRowSet.toString();
        String webUi = parseLog(logs);

        log.info(" webUi is : " + webUi);
        if (webUi == null) {
            Thread.sleep(10000);
            BaseResponse baseResponse = HttpUtil.get(gatewayHost + logUrl);
            logRowSet = baseResponse.get().get("logRowSet");
            log.info(" two logRowSet is : " + logRowSet);
            webUi = parseLog(logRowSet.toString());
        }

        updateInstanceUI(id.toString());
        if (webUi != null) {
            baseJob.setWebUi("http://" + webUi);
        }
        return true;
    }

    public static String parseLog(String log) {
        String webUi;
        try {
            String UI_LOG_PATTERN = "(.+) - Found Web Interface (.+) of application (.+)";
            Matcher matcher = UrlUtil.getMatcher(log, UI_LOG_PATTERN);
            String group = matcher.group(2);
            System.out.println(group);

            if(group.contains(".")){
                return group;
            }
            String[] webUiArr = group.split(":");
            String[] IPStr = webUiArr[0].split("\\.");
            String[] IPArr = IPStr[0].split("-");
            webUi = IPArr[1] + "." + IPArr[2] + "." + IPArr[3] + "." + IPArr[4] + ":" + webUiArr[1];
        } catch (Exception e) {
            return null;
        }
        return webUi;
    }

    public static void main(String[] args) {
        String ss="{\n" +
                "\"logRowSet\": [\n" +
                "\"2023-10-27 11:11:33.157 INFO org.apache.kyuubi.operation.FlinkJobSubmission: Submitting FLINK batch[990d288f-91e8-4f46-b931-139a45b95232] job:\",\n" +
                "\"/opt/flink/flink-1.14.6/bin/flink run-application \\\\\",\n" +
                "\"\\t-t yarn-application \\\\\",\n" +
                "\"\\t-Dkyuubi.session.real.user=user_test3 \\\\\",\n" +
                "\"\\t-Dkyuubi.session.command.tags=type:flink,version:1.14.6 \\\\\",\n" +
                "\"\\t-Dexecution.checkpointing.mode=AT_LEAST_ONCE \\\\\",\n" +
                "\"\\t-Dkyuubi.session.tenant=qiyuan \\\\\",\n" +
                "\"\\t-Dkyuubi.batch.resource.uploaded=false \\\\\",\n" +
                "\"\\t-Dyarn.application.name=test-mqtt-kafka \\\\\",\n" +
                "\"\\t-Dexecution.checkpointing.interval=30 \\\\\",\n" +
                "\"\\t-Dkyuubi.session.group=dpm \\\\\",\n" +
                "\"\\t-Dstate.savepoints.dir=hdfs:///ninebot/flink/prod/22/savepoint \\\\\",\n" +
                "\"\\t-Dkyuubi.session.cluster.tags=type:yarn,region:cn-beijing-6,sla:normal,rbac.cluster:bdp-prod,provider:ksyun \\\\\",\n" +
                "\"\\t-Dexecution.checkpointing.timeout=100 \\\\\",\n" +
                "\"\\t-Dkyuubi.batch.id=990d288f-91e8-4f46-b931-139a45b95232 \\\\\",\n" +
                "\"\\t-Dkyuubi.client.ipAddress=172.18.0.4 \\\\\",\n" +
                "\"\\t-Dkyuubi.session.connection.url=103.59.148.180:10099 \\\\\",\n" +
                "\"\\t-Dkyuubi.server.ipAddress=103.59.148.180 \\\\\",\n" +
                "\"\\t-Dstate.checkpoints.dir=hdfs:///ninebot/flink/prod/22/checkpoint \\\\\",\n" +
                "\"\\t-nm test-mqtt-kafka \\\\\",\n" +
                "\"\\t-ytm 4096m \\\\\",\n" +
                "\"\\t-yjm 4096m \\\\\",\n" +
                "\"\\t-ys 1 \\\\\",\n" +
                "\"\\t-c com.ushareit.flink.StreamingClient /opt/flink/flink-1.14.6/lib/sql-submit-1.0-SNAPSHOT.jar \\\\\",\n" +
                "\"\\t-n test-mqtt-kafka \\\\\",\n" +
                "\"\\t-p 1 \\\\\",\n" +
                "\"\\t-ci 30 \\\\\",\n" +
                "\"\\t-cm AT_LEAST_ONCE \\\\\",\n" +
                "\"\\t-ct 100 \\\\\",\n" +
                "\"\\t-sql U0VUJTIwZmxpbmsuZXhlY3V0aW9uLnBhY2thZ2VzJTNEb3JnLmFwYWNoZS5mbGluayUzQWZsaW5rLWNvbm5lY3Rvci1rYWZrYV8yLjExJTNBMS4xNC42JTJDb3JnLmFwYWNoZS5mbGluayUzQWZsaW5rLWpzb24lM0ExLjE0LjYlM0IlMEFDUkVBVEUlMjBUQUJMRSUyMHRlc3RfbXF0dF9tc2clMjAobXNnJTIwc3RyaW5nKSUyMCUwQVdJVEglMjAoJTBBJTIwJTIwJ2Nvbm5lY3RvciclMjAlM0QlMjAna2Fma2EnJTJDJTBBJTIwJTIwJ3RvcGljJyUyMCUzRCUyMCd0ZXN0MSclMkMlMEElMjAlMjAncHJvcGVydGllcy5ncm91cC5pZCclMjAlM0QlMjAndGVzdF9tcXR0X2thZmthJyUyQyUwQSUyMCUyMCdwcm9wZXJ0aWVzLmJvb3RzdHJhcC5zZXJ2ZXJzJyUyMCUzRCUyMCcxMDMuNTkuMTQ4LjE4MCUzQTkwOTInJTJDJTBBJTIwJTIwJ3Byb3BlcnRpZXMubWF4LnJlcXVlc3Quc2l6ZSclMjAlM0QlMjAnMTA0ODU3NjAwJyUyQyUwQSUyMCUyMCdwcm9wZXJ0aWVzLmJ1ZmZlci5tZW1vcnknJTIwJTNEJTIwJzMzNTU0NDMyJyUyQyUwQSUyMCUyMCdzY2FuLnN0YXJ0dXAubW9kZSclMjAlM0QlMjAnbGF0ZXN0LW9mZnNldCclMkMlMEElMjAlMjAncHJvcGVydGllcy5saW5nZXIubXMnJTIwJTNEJTIwJzEwJyUyQyUwQSUyMCUyMCdmb3JtYXQnJTIwJTNEJTIwJ2pzb24nJTBBKSUzQiUwQSUwQWNyZWF0ZSUyMHRhYmxlJTIwY29uc29sZV9wcmludCUyMChtc2clMjBzdHJpbmcpJTBBd2l0aCUyMCgnY29ubmVjdG9yJyUyMCUzRCUyMCdwcmludCcpJTNCJTBBJTBBaW5zZXJ0JTIwaW50byUyMGNvbnNvbGVfcHJpbnQlMEFzZWxlY3QlMjAqJTIwZnJvbSUyMHRlc3RfbXF0dF9tc2clM0I=\",\n" +
                "\"2023-10-27 11:11:33.158 INFO org.apache.kyuubi.operation.FlinkJobSubmission: Launching engine env:\",\n" +
                "\"Map(PATH -> /root/.local/bin:/root/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/opt/hadoop/hadoop-3.3.6/bin:/opt/hadoop/hadoop-3.3.6/sbin:/opt/hive/apache-hive-2.3.7-bin/bin, HADOOP_CONF_DIR -> /opt/hadoop/hadoop-3.3.6/etc/hadoop, HISTCONTROL -> ignoredups, KYUUBI_HOME -> /opt/gateway/apache-kyuubi-1.7.0-bin, HISTSIZE -> 1000, JAVA_HOME -> /usr/lib/jvm/java-1.8.0-openjdk-1.8.0.382.b05-2.el9.x86_64, BASH_FUNC_which%% -> () {  ( alias;\",\n" +
                "\" eval ${which_declare} ) | /usr/bin/which --tty-only --read-alias --read-functions --show-tilde --show-dot $@\",\n" +
                "\"}, KYUUBI_SCALA_VERSION -> 2.12, TERM -> xterm-256color, KYUUBI_PID_DIR -> /opt/gateway/apache-kyuubi-1.7.0-bin/pid, LANG -> zh_CN.UTF-8, JRE_HOME -> /usr/lib/jvm/java-1.8.0-openjdk-1.8.0.382.b05-2.el9.x86_64/jre, HADOOP_CLASSPATH -> /opt/gateway/apache-kyuubi-1.7.0-bin/cluster_config/yarn/yarn-ksyun:/opt/hadoop/hadoop-3.3.6/etc/hadoop:/opt/hadoop/hadoop-3.3.6/share/hadoop/common/lib/*:/opt/hadoop/hadoop-3.3.6/share/hadoop/common/*:/opt/hadoop/hadoop-3.3.6/share/hadoop/hdfs:/opt/hadoop/hadoop-3.3.6/share/hadoop/hdfs/lib/*:/opt/hadoop/hadoop-3.3.6/share/hadoop/hdfs/*:/opt/hadoop/hadoop-3.3.6/share/hadoop/mapreduce/*:/opt/hadoop/hadoop-3.3.6/share/hadoop/yarn:/opt/hadoop/hadoop-3.3.6/share/hadoop/yarn/lib/*:/opt/hadoop/hadoop-3.3.6/share/hadoop/yarn/*, MAIL -> /var/spool/mail/root, SPARK_HOME -> /opt/spark, SPARK_ENGINE_HOME -> /opt/gateway/apache-kyuubi-1.7.0-bin/externals/engines/spark, YARN_HOME -> /opt/hadoop/hadoop-3.3.6, FLINK_ENGINE_HOME -> /opt/gateway/apache-kyuubi-1.7.0-bin/externals/engines/flink, which_declare -> declare -f, KYUUBI_LOG_DIR -> /opt/gateway/apache-kyuubi-1.7.0-bin/logs, LOGNAME -> root, PWD -> /opt/gateway/apache-kyuubi-1.7.0-bin, _ -> /usr/bin/nohup, S_COLORS -> auto, HIVE_ENGINE_HOME -> /opt/gateway/apache-kyuubi-1.7.0-bin/externals/engines/hive, HIVE_HADOOP_CLASSPATH -> /opt/hadoop/hadoop-3.3.6/share/hadoop/common/lib/commons-collections-3.2.2.jar:/opt/hadoop/hadoop-3.3.6/share/hadoop/client/hadoop-client-runtime-3.1.1.jar:/opt/hadoop/hadoop-3.3.6/share/hadoop/client/hadoop-client-api-3.1.1.jar:/opt/hadoop/hadoop-3.3.6/share/hadoop/common/lib/htrace-core4-4.1.0-incubating.jar, LESSOPEN -> ||/usr/bin/lesspipe.sh %s, HIVE_CONF_DIR -> , SHELL -> /bin/bash, TRINO_ENGINE_HOME -> /opt/gateway/apache-kyuubi-1.7.0-bin/externals/engines/trino, FLINK_HOME -> /opt/flink/flink-1.14.6, HIVE_HOME -> /opt/hive/apache-hive-2.3.7-bin, OLDPWD -> /root/gateway/apache-kyuubi-1.7.0-bin/jars, USER -> root, KYUUBI_CONF_DIR -> /opt/gateway/apache-kyuubi-1.7.0-bin/conf, HOSTNAME -> k8s-master, PREFIX -> /opt, DEBUGINFOD_URLS -> https://debuginfod.centos.org/ , HADOOP_HOME -> /opt/hadoop/hadoop-3.3.6, KYUUBI_JAVA_OPTS -> -Xmx10g -XX:+UnlockDiagnosticVMOptions -XX:ParGCCardsPerStrideChunk=4096 -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+CMSConcurrentMTEnabled -XX:CMSInitiatingOccupancyFraction=70 -XX:+UseCMSInitiatingOccupancyOnly -XX:+CMSClassUnloadingEnabled -XX:+CMSParallelRemarkEnabled -XX:+UseCondCardMark -XX:MaxDirectMemorySize=1024m  -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./logs -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintTenuringDistribution -Xloggc:./logs/kyuubi-server-gc-%t.log -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=5M -XX:NewRatio=3 -XX:MetaspaceSize=512m, KYUUBI_WORK_DIR_ROOT -> /opt/gateway/apache-kyuubi-1.7.0-bin/work, HADOOP_COMMON_LIB_NATIVE_DIR -> /opt/hadoop/hadoop-3.3.6/lib/native, LS_COLORS -> rs=0:di=01;34:ln=01;36:mh=00:pi=40;33:so=01;35:do=01;35:bd=40;33;01:cd=40;33;01:or=40;31;01:mi=01;37;41:su=37;41:sg=30;43:ca=30;41:tw=30;42:ow=34;42:st=37;44:ex=01;32:*.tar=01;31:*.tgz=01;31:*.arc=01;31:*.arj=01;31:*.taz=01;31:*.lha=01;31:*.lz4=01;31:*.lzh=01;31:*.lzma=01;31:*.tlz=01;31:*.txz=01;31:*.tzo=01;31:*.t7z=01;31:*.zip=01;31:*.z=01;31:*.dz=01;31:*.gz=01;31:*.lrz=01;31:*.lz=01;31:*.lzo=01;31:*.xz=01;31:*.zst=01;31:*.tzst=01;31:*.bz2=01;31:*.bz=01;31:*.tbz=01;31:*.tbz2=01;31:*.tz=01;31:*.deb=01;31:*.rpm=01;31:*.jar=01;31:*.war=01;31:*.ear=01;31:*.sar=01;31:*.rar=01;31:*.alz=01;31:*.ace=01;31:*.zoo=01;31:*.cpio=01;31:*.7z=01;31:*.rz=01;31:*.cab=01;31:*.wim=01;31:*.swm=01;31:*.dwm=01;31:*.esd=01;31:*.jpg=01;35:*.jpeg=01;35:*.mjpg=01;35:*.mjpeg=01;35:*.gif=01;35:*.bmp=01;35:*.pbm=01;35:*.pgm=01;35:*.ppm=01;35:*.tga=01;35:*.xbm=01;35:*.xpm=01;35:*.tif=01;35:*.tiff=01;35:*.png=01;35:*.svg=01;35:*.svgz=01;35:*.mng=01;35:*.pcx=01;35:*.mov=01;35:*.mpg=01;35:*.mpeg=01;35:*.m2v=01;35:*.mkv=01;35:*.webm=01;35:*.webp=01;35:*.ogm=01;35:*.mp4=01;35:*.m4v=01;35:*.mp4v=01;35:*.vob=01;35:*.qt=01;35:*.nuv=01;35:*.wmv=01;35:*.asf=01;35:*.rm=01;35:*.rmvb=01;35:*.flc=01;35:*.avi=01;35:*.fli=01;35:*.flv=01;35:*.gl=01;35:*.dl=01;35:*.xcf=01;35:*.xwd=01;35:*.yuv=01;35:*.cgm=01;35:*.emf=01;35:*.ogv=01;35:*.ogx=01;35:*.aac=01;36:*.au=01;36:*.flac=01;36:*.m4a=01;36:*.mid=01;36:*.midi=01;36:*.mka=01;36:*.mp3=01;36:*.mpc=01;36:*.ogg=01;36:*.ra=01;36:*.wav=01;36:*.oga=01;36:*.opus=01;36:*.spx=01;36:*.xspf=01;36:, KYUUBI_BEELINE_OPTS -> -Xmx2g -XX:+UnlockDiagnosticVMOptions -XX:ParGCCardsPerStrideChunk=4096 -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+CMSConcurrentMTEnabled -XX:CMSInitiatingOccupancyFraction=70 -XX:+UseCMSInitiatingOccupancyOnly -XX:+CMSClassUnloadingEnabled -XX:+CMSParallelRemarkEnabled -XX:+UseCondCardMark, SHLVL -> 1, HOME -> /root)\",\n" +
                "\"2023-10-27 11:11:41.401 ERROR org.apache.kyuubi.operation.FlinkJobSubmission: Batch[990d288f-91e8-4f46-b931-139a45b95232] has not appId so can not monitor job\",\n" +
                "\"2023-10-27 11:11:41.402 INFO org.apache.kyuubi.operation.FlinkJobSubmission: Processing user_test3's query[6fe3a15d-5e00-439e-a6af-79def20600a2]: PENDING_STATE -> FINISHED_STATE, time taken: 1.698376301402E9 seconds\",\n" +
                "\"2023-10-27 11:11:41.404 INFO org.apache.kyuubi.operation.FlinkJobSubmission: Batch[990d288f-91e8-4f46-b931-139a45b95232] finished\",\n" +
                "\"2023-10-27 11:11:34,036 WARN  org.apache.flink.yarn.configuration.YarnLogConfigUtil        [] - The configuration directory ('/opt/flink/flink-1.14.6/conf') already contains a LOG4J config file.If you want to use logback, then please delete or rename the log configuration file.\",\n" +
                "\"2023-10-27 11:11:34,080 INFO  org.apache.hadoop.yarn.client.DefaultNoHARMFailoverProxyProvider [] - Connecting to ResourceManager at /103.59.148.180:8032\",\n" +
                "\"2023-10-27 11:11:34,265 INFO  org.apache.flink.yarn.YarnClusterDescriptor                  [] - No path for the flink jar passed. Using the location of class org.apache.flink.yarn.YarnClusterDescriptor to locate the jar\",\n" +
                "\"2023-10-27 11:11:34,389 INFO  org.apache.hadoop.conf.Configuration                         [] - resource-types.xml not found\",\n" +
                "\"2023-10-27 11:11:34,390 INFO  org.apache.hadoop.yarn.util.resource.ResourceUtils           [] - Unable to find 'resource-types.xml'.\",\n" +
                "\"2023-10-27 11:11:34,429 INFO  org.apache.flink.yarn.YarnClusterDescriptor                  [] - The configured JobManager memory is 1600 MB. YARN will allocate 1664 MB to make up an integer multiple of its minimum allocation memory (128 MB, configured via 'yarn.scheduler.minimum-allocation-mb'). The extra 64 MB may not be used by Flink.\",\n" +
                "\"2023-10-27 11:11:34,429 INFO  org.apache.flink.yarn.YarnClusterDescriptor                  [] - The configured TaskManager memory is 1728 MB. YARN will allocate 1792 MB to make up an integer multiple of its minimum allocation memory (128 MB, configured via 'yarn.scheduler.minimum-allocation-mb'). The extra 64 MB may not be used by Flink.\",\n" +
                "\"2023-10-27 11:11:34,429 INFO  org.apache.flink.yarn.YarnClusterDescriptor                  [] - Cluster specification: ClusterSpecification{masterMemoryMB=1600, taskManagerMemoryMB=1728, slotsPerTaskManager=1}\",\n" +
                "\"2023-10-27 11:11:36,871 INFO  org.apache.flink.yarn.YarnClusterDescriptor                  [] - Submitting application master application_1697017632728_0457\",\n" +
                "\"2023-10-27 11:11:37,099 INFO  org.apache.hadoop.yarn.client.api.impl.YarnClientImpl        [] - Submitted application application_1697017632728_0457\",\n" +
                "\"rowCount\": 51,\n" +
                "\"empty\": false\n" +
                "}";



        try {
            String s = parseLog1(ss);
            System.out.println(s);
        } catch (Exception e) {
            log.error("yarn submitAsync error");
            log.error(e.getMessage(), e);
            throw new ServiceException(BaseResponseCodeEnum.TASK_SUBMIT_FAIL, "flink任务提交到YARN");
        }
        System.out.println("111");
    }

    public static String parseLog1(String log) {
        String webUi;
        try {
            String UI_LOG_PATTERN = "(.+) - Found Web Interface (.+) of application (.+)";
            Matcher matcher = UrlUtil.getMatcher(log, UI_LOG_PATTERN);
            String group = matcher.group(2);
            System.out.println(group);

            if(group.contains(".")){
                return group;
            }
            String[] webUiArr = group.split(":");
            String[] IPStr = webUiArr[0].split("\\.");
            String[] IPArr = IPStr[0].split("-");
            webUi = IPArr[1] + "." + IPArr[2] + "." + IPArr[3] + "." + IPArr[4] + ":" + webUiArr[1];
        } catch (Exception e) {
            return null;
        }
        return webUi;
    }


//    public static void main(String[] args) {
//
//        BaseResponse baseResponse = HttpUtil.get("http://gateway-test.ushareit.org/api/v1/batches/5ebb1575-85be-4784-b2fb-05924c1d6581/localLog?from=0&size=2000");
//        Object logRowSet = baseResponse.get().get("logRowSet");
//
////        System.out.println(logRowSet.toString());
//        String ss = logRowSet.toString();
//        String AWS_ADDRESS_PATTERN = "(.+) - Found Web Interface (.+) of application (.+)";
//        Matcher matcher = UrlUtil.getMatcher(ss, AWS_ADDRESS_PATTERN);
////        System.out.println(matcher.group(0));
////        System.out.println(matcher.group(1));
//        System.out.println(matcher.group(2));
//        String group = matcher.group(2);
//        String[] webUiArr = group.split(":");
//        String[] IPStr = webUiArr[0].split("\\.");
//        String[] IPArr = IPStr[0].split("-");
//        System.out.println(IPArr[1]+"."+IPArr[2]+"."+IPArr[3]+"."+IPArr[4]+":"+webUiArr[1]);
//    }

    /**
     * 获取执行命令行
     *
     * @return
     */
    public String getCommand() {
        baseJob.command = getFlinkRunCommand();
        return baseJob.command.getCommandLine();
    }

    @Override
    public void deleteResource() {
        ZkClientUtil.deleteRunningJobRegistry(baseJob.cluster.getZookeeperQuorum(), baseJob.runTimeTaskBase.getName());
    }


    private FlinkRunCommand getFlinkRunCommand() {
        String mainClass = baseJob.runTimeTaskBase.getMainClass();
        String args = baseJob.runTimeTaskBase.getMainClassArgs();

        Integer parallelism = baseJob.runtimeConfig.getParallelism();
        String huaweiIam = baseJob.runtimeConfig.getHuaweiIam();
        String awsIam = baseJob.runtimeConfig.getAwsIam();
        Double tmCpu = baseJob.runtimeConfig.getTmCpu();
        Double tmMemory = baseJob.runtimeConfig.getTmMemory();
        List<RuntimeConfig.Kv> params = new ArrayList<>();

        String containerImage = baseJob.cluster.getContainerImage();

        StringBuffer dependJars = new StringBuffer();
        if (StringUtils.isNotEmpty(baseJob.onlineUdfJarObsUrl)) {
            String cloudLocation = convertFormat(baseJob.onlineUdfJarObsUrl);
            dependJars.append(cloudLocation);
        }
        if (StringUtils.isNotEmpty(baseJob.artifactUdfJarObsUrl)) {
            String cloudLocation = convertFormat(baseJob.artifactUdfJarObsUrl);
            if (StringUtils.isNotEmpty(dependJars)) {
                dependJars.append(",");
            }
            dependJars.append(cloudLocation);
        }

        //改变jar的
        log.info("yarn : dependJars=" + dependJars);

        String mainJar = baseJob.runTimeTaskBase.getJarUrl();

        Integer tentanId = 1;
        Integer groupId = 0;
        JSONObject runtimeConfigObject = JSON.parseObject(baseJob.runTimeTaskBase.getRuntimeConfig());
        List<AccessGroup> accessGroupList = taskServiceImp.accessGroupService.selectByName(runtimeConfigObject.getString("owner"));
        List<Integer> collect = accessGroupList.stream().map(AccessGroup::getParentId).collect(Collectors.toList());

        List<AccessGroup> groupList = taskServiceImp.accessGroupService.listByIds(collect);
        for (AccessGroup accessGroup : groupList) {
            tentanId = accessGroup.getTenantId();
            AccessTenant accessTenant = taskServiceImp.accessTenantService.checkExist(tentanId);
            Integer id = accessGroup.getId();
            String eName = taskServiceImp.accessGroupService.getRootGroup(id).getEName();
            if (StringUtils.isNotEmpty(eName) && eName.equals(accessTenant.getName())) {
                groupId = id;
                if (accessGroup.getName().contains("default")) {
                    break;
                }
            }
        }


        FlinkRunCommand command = new FlinkRunCommand()
                .setRuntimeConfig(baseJob.runtimeConfig)
                .setTaskServiceimpl(taskServiceImp)
                .setCluster(baseJob.cluster.getAddress())
                .setAppName(baseJob.runTimeTaskBase.getName().toLowerCase())
                .setMainClass(mainClass)
                .setArgs(args)
                .setContainerImage(containerImage)
                .setDependJars(dependJars.toString())
                .setMainJar(mainJar)
                .setFromSavepoint(baseJob.savepointUrl)
                .setParallelism(parallelism)
                .setContext(baseJob.cluster.getAddress())
                .setHuaweiIam(huaweiIam)
                .setAwsIam(awsIam)
                .setStatePath(baseJob.cluster.getStatePath())
                .setTmCpu(tmCpu == null ? 1 : tmCpu)
                .setTmMemory(Math.round((tmMemory == null ? 4 : tmMemory) * 1024))
                .setParams(params)
                .setZookeeperQuorum(baseJob.cluster.getZookeeperQuorum())
                .setAppId(baseJob.runTimeTaskBase.getId())
                .setFlinkExecutionPackages(baseJob.flinkExecutionPackages)
                .setVersion(baseJob.getCluster().getVersion())
                .setDependentInformationJar(porcessExecPackage(baseJob.flinkExecutionPackages))
                .setExecArgs(baseJob.execArgs)
                .setRegion(baseJob.cluster.getRegion())
                .setMode(baseJob.cluster.getTypeCode())
                .setEnv(baseJob.cluster.getEnv())
                .setIsBatchTask(baseJob.runtimeConfig.getIsBatchTask())
                .setNamespace(baseJob.cluster.getNameSpace())
                .setNodeSelector(baseJob.cluster.getNodeSelector())
                .setTolerations(baseJob.cluster.getTolerations())
                .setId(baseJob.runTimeTaskBase.getId())
                .setOwner(baseJob.runTimeTaskBase.getCreateBy())
                .setTemplate(baseJob.runTimeTaskBase.getTemplateCode())
                .setGatewayUrl(taskServiceImp.getGatewayUrl())
                .setTenantId(tentanId)
                .setGroupId(groupId);
        command.setHomePath("/data/code");
        return command;
    }

    public String porcessExecPackage(String execPackage) {
        if (StringUtils.isEmpty(execPackage)) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        try {
            String[] split = execPackage.trim().split(",");
            for (String oneUrl : split) {
                String[] urlArr = oneUrl.split(":");
                DependentInformation build = DependentInformation.builder().groupId(urlArr[0]).artifactId(urlArr[1]).version(urlArr[2]).build();
                build.setDeleteStatus(0);
                DependentInformation dependentInformation = taskServiceImp.dependentInformationService.selectOne(build);
                if (dependentInformation != null) {
                    result.append(dependentInformation.getStorageLocation()).append(",");
                }
            }
        } catch (Exception e) {
            throw new ServiceException(BaseResponseCodeEnum.TASK_DEL_FORMAT_ERROR);
        }
        if (StringUtils.isNotEmpty(result)) {
            return result.substring(0, result.length() - 1);
        }
        return null;
    }


    public String convertFormat(String cloudUrl) {
        if (StringUtils.isEmpty(cloudUrl)) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        String[] split = cloudUrl.split(",");
        for (String oneUrl : split) {
            String CLOUD_URL_PATTERN = "(http(.*).com/(.*))";
            Matcher matcher = UrlUtil.getMatcher(oneUrl, CLOUD_URL_PATTERN);
            String group = matcher.group(3);
            if (oneUrl.contains("amazonaws.com")) {
                result.append("s3://").append(group).append(",");
            } else if (oneUrl.contains("myhuaweicloud.com")) {
                result.append("obs://").append(group).append(",");
            } else if (oneUrl.contains("www.googleapis.com")) {
                result.append("gcs://").append(group).append(",");
            } else if (oneUrl.contains("ksyuncs.com")) {
                result.append("ks3://").append(group).append(",");
            }
        }
        return result.substring(0, result.length() - 1);
    }


//    public static void main(String[] args) {
//
//        String CLOUD_URL_PATTERN = "(http(.*).com/(.*))";
//        Matcher matcher = UrlUtil.getMatcher("https://ninebot.ks3-cn-beijing.asdasdas.com/hebe/cloud-sgt-test/jars/a7522490f34a43b3a2d4b560b92b854d/selenium-server-standalone-2.46.0%E7%9A%84%E5%89%AF%E6%9C%AC.jar", CLOUD_URL_PATTERN);
//
//        System.out.println(matcher.group(1));
//        System.out.println(matcher.group(2));
//        System.out.println(matcher.group(3));
//
//
//    }


}

package com.ushareit.dstask.web.utils;


import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.exception.ServiceException;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.DependencyFilterUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: licg
 * @create: 2020-05-12 15:24
 **/
@Component
public final class MavenUtil {


    public List<URL> downloadDependency(String artifactStr) throws Exception {
        return downloadDependency(artifactStr, ".m2");
    }

    public List<URL> downloadDependency(String artifactStr, String storePath) throws Exception {
        String[] artifactArr = artifactStr.split(",");
        List<URL> resultList = new ArrayList<>();
        for (String artifact : artifactArr) {
            Pattern pattern = Pattern.compile(DsTaskConstant.FLINK_SQL_DEPENDENCE_PATTERN);
            Matcher matcher = pattern.matcher(artifact);
            if (!matcher.find()) {
                throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_ILLEGAL, "Maven依赖项格式异常！");
            }
            resultList.addAll(downloadDependency(artifact, storePath, JavaScopes.COMPILE));
        }

        return resultList;
    }

    /**
     * 根据Artifact信息下载其相关依赖，并存储到指定的文件夹
     *
     * @param artifactStr Artifact信息，例如：org.apache.maven.shared:maven-invoker:3.0.1
     * @param storePath   存储的路径
     * @param theScope    Scope
     * @return 全部依赖信息
     */
    public  List<URL> downloadDependency(String artifactStr, String storePath, String theScope) throws Exception {
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        RepositorySystem system = newRepositorySystem(locator);
        RepositorySystemSession session = newSession(system, storePath);

        Artifact artifact = new DefaultArtifact(artifactStr);
        DependencyFilter theDependencyFilter = DependencyFilterUtils.classpathFilter(theScope);

        CollectRequest theCollectRequest = new CollectRequest();
        theCollectRequest.setRoot(new org.eclipse.aether.graph.Dependency(artifact, theScope));
        usingCentralRepo(theCollectRequest);

        DependencyRequest theDependencyRequest = new DependencyRequest(theCollectRequest, theDependencyFilter);

        DependencyResult theDependencyResult = system.resolveDependencies(session, theDependencyRequest);
        List<URL> resultList = new ArrayList<>();
        for (ArtifactResult theArtifactResult : theDependencyResult.getArtifactResults()) {
            Artifact theResolved = theArtifactResult.getArtifact();
            resultList.add(theResolved.getFile().toURI().toURL());
        }

        return resultList;
    }

    private static RepositorySystem newRepositorySystem(DefaultServiceLocator locator) {
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
        return locator.getService(RepositorySystem.class);
    }

    private static RepositorySystemSession newSession(RepositorySystem system, String storePath) {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
        LocalRepository localRepo = new LocalRepository(storePath);
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));

        return session;
    }

    private  void usingCentralRepo(CollectRequest theCollectRequest) {
        if (!DataCakeConfigUtil.getDataCakeConfig().getDcRole()) {
            RemoteRepository shareitReleases = new RemoteRepository.Builder("shareit", "default", "http://nexus.ushareit.me/repository/maven-releases/").build();
            RemoteRepository shareitSnapshots = new RemoteRepository.Builder("shareit", "default", "http://nexus.ushareit.me/repository/maven-snapshots/").build();
            theCollectRequest.addRepository(shareitReleases);
            theCollectRequest.addRepository(shareitSnapshots);
        }
        RemoteRepository central = new RemoteRepository.Builder("central", "default", "http://maven.aliyun.com/nexus/content/groups/public").build();
        theCollectRequest.addRepository(central);
    }
}
package com.ushareit.dstask.web.utils;

import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.utils.HttpUtil;
import com.ushareit.dstask.web.vo.BaseResponse;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.KubeConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileReader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: licg
 * @create: 2020-05-12 15:24
 **/
@Slf4j
@Component
public class ScmpUtil {

    public static Map<String, String> clusterMap = new HashMap();

    static {
        clusterMap.put("shareit-cce-test", "7");
        clusterMap.put("bdp-flink-sg1-prod", "47");
        clusterMap.put("bdp-flink-ue1-prod", "48");
        clusterMap.put("bdp-flink-sg2-prod", "51");
        clusterMap.put("bdp-service-sg3-test", "https://34.143.176.138");
    }

    public Boolean deleteK8sDeployment(String cluster, String nameSpace, String applicationName) {
        //暂时设置sg3的删除走 直接删除k8s pod
        if(StringUtils.isEmpty(clusterMap.get(cluster))){
            return deleteKDeployment(cluster, nameSpace, applicationName);
        }
        log.info("deleteK8SDeployment Cluster:" + cluster + " ApplicationName:" + applicationName);
        Map<String, String> heads = new HashMap(2);
        heads.put("cid", clusterMap.get(cluster));
        heads.put("Authorization", "Bearer " + getToken(DsTaskConstant.SCMP_CLIENT_ID));

        String url = MessageFormat.format(DsTaskConstant.SCMP_DELETE_DEPLOYMENT_URL, nameSpace, applicationName.toLowerCase());
        log.info("deleteK8SDeployment url is " + url);
        BaseResponse response = HttpUtil.delete(url, new HashMap(2), heads);
        if (response == null || !BaseResponseCodeEnum.SUCCESS.name().equals(response.getCodeStr())) {
            log.error("deleteK8SDeployment response code is " + response.getCodeStr() + ", content is " + response.getData());
        }
        log.info("deleteK8SDeployment success.Cluster:" + cluster + " ApplicationName:" + applicationName);
        return true;
    }

    public Boolean deleteKDeployment(String cluster, String namespace, String deploymentName) {
        //kubectl config view --minify | grep server
        log.info("deleteKDeployment Cluster:" + cluster + " ApplicationName:" + deploymentName);
        try {
            // 加载 kubeconfig 文件
            File kubeConfigFile;
            if (!DataCakeConfigUtil.getDataCakeConfig().getDcRole()) {
                kubeConfigFile = new File(System.getProperty("user.home"), ".kube/config");
            } else {
                kubeConfigFile = new File(System.getProperty("user.home"), "flink/config");
            }

            KubeConfig kubeConfig = KubeConfig.loadKubeConfig(new FileReader(kubeConfigFile));
            log.info("kubeConfig cluster  is :" + kubeConfig.getClusters().toString());

            // 选择要使用的 cluster
            kubeConfig.setContext(cluster);
            log.info("kubeConfig currentContextName  is :" + kubeConfig.getCurrentContext());
            log.info("kubeConfig server  is :" + kubeConfig.getServer());
            log.info("kubeConfig getUsername  is :" + kubeConfig.getUsername());
            log.info("kubeConfig toekn  is :" + kubeConfig.getAccessToken());

            // 创建 ApiClient
            ApiClient client = Config.fromConfig(kubeConfig);

            // 配置全局默认 ApiClient
            Configuration.setDefaultApiClient(client);
            AppsV1Api api = new AppsV1Api(client);
            api.deleteNamespacedDeployment(deploymentName,
                    namespace, null, null,
                    null,
                    null, null, null);
        } catch (Exception e) {
            log.info("deleteKDeployment Cluster fail ");
            log.error(e.getMessage(), e);
//            throw new RuntimeException("deleteKDeployment Cluster fail ");
        }
        return true;
    }

    /**
     * 调用gateway删除
     *
     * @param submitId
     * @param user
     * @return
     */
    public Boolean deleteKDeploymentByGateWay(String submitId, String user) {
        log.info("deleteKDeployment submitId is :" + submitId);
        log.info("deleteKDeployment user is :" + user);
        if (StringUtils.isEmpty(submitId) || StringUtils.isEmpty(user)) {
            return true;
        }
        //curl DELETE 'http://gateway.bd.ninebot.local/api/v1/batches/d1141dee-8721-46e5-a1b6-5fff4ff9805b'
        // -H 'Authorization: Basic dXNlcl90ZXN0Njo='
        String baseUser = new String(org.apache.commons.codec.binary.Base64.encodeBase64(user.getBytes()));
        HashMap<String, String> headers = new HashMap<>(1);
        headers.put("Authorization", "Basic " + baseUser);

        String gatewayHost = DataCakeConfigUtil.getDataCakeConfig().getGatewayRestHost();
        String deletUrl = gatewayHost + "/api/v1/batches/" + submitId;
        log.info("deleteKDeployment deletUrl is :" + deletUrl);
        log.info("deleteKDeployment baseUser is :" + baseUser);
        try {
            BaseResponse response = HttpUtil.delete(deletUrl, new HashMap(2), headers);
        } catch (Exception e) {
            log.info("deleteKDeployment Cluster fail ");
            log.error(e.getMessage(), e);
//            throw new RuntimeException("deleteKDeployment Cluster fail ");
        }
        return true;
    }

    private static String getToken(String clientId) {

        Map<String, String> params = new HashMap(2);
        params.put("username", DsTaskConstant.SCMP_USERNAME);
        params.put("grant_type", DsTaskConstant.SCMP_GRANT_TYPE);
        params.put("scope", DsTaskConstant.SCMP_SCOPE);
        params.put("client_id", clientId);
        params.put("password", DsTaskConstant.SCMP_PASSWORD);

        BaseResponse response = HttpUtil.doPost(DsTaskConstant.SCMP_GET_TOKEN_URL, params);
        if (response == null || !BaseResponseCodeEnum.SUCCESS.name().equals(response.getCodeStr())) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_UNA);
        }
        JSONObject result = response.get();
        log.info("getToken:" + result.getString("id_token"));
        return result.getString("id_token");
    }

    public static Object getIam(String cloud, String tenancyCode) {
        String[] groups = tenancyCode.split(",");
        HashMap<String, Object> map = new HashMap<>(2);
        map.put("cloud", cloud);
        map.put("groups", groups);

        HashMap<String, String> headers = new HashMap<>(1);
        headers.put("Authorization", "Bearer " + getToken(DsTaskConstant.SCMP_CLIENT_ID_USER));

        BaseResponse response = HttpUtil.postWithJson(DsTaskConstant.SCMP_URL_IAM, JSONObject.toJSONString(map), headers);

        if (response.getCode() != 0) {
            return BaseResponse.error(BaseResponseCodeEnum.SCMP_GET_IAM_FAIL);
        }

        return response.get().get("result");
    }
}

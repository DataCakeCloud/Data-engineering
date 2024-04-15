package com.ushareit.dstask.third.sharestore.impl;

import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.third.sharestore.ShareStoreService;
import com.ushareit.dstask.third.sharestore.vo.ShareStoreClusterVO;
import com.ushareit.dstask.third.sharestore.vo.ShareStoreSegmentVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

/**
 * @author fengxiao
 * @date 2023/2/17
 */
@Slf4j
@Service
public class ShareStoreServiceImpl implements ShareStoreService {

    private final String TEST_REST_ENDPOINT_SG1 = "http://test.sharestore.cbs.sg1.helix/admin/v2/";
    private final String TEST_REST_ENDPOINT_SG2 = "http://test.sharestore.cbs.sg2.helix/admin/v2/";
    private final String REST_ENDPOINT_SG1 = "http://prod.sharestore.cbs.sg1.helix/admin/v2";
    private final String REST_ENDPOINT_SG2 = "http://prod.sharestore.cbs.sg2.helix/admin/v2";

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public int getPartitionNum(String restEndpoint, String cluster, String segment) {
        try {
            String url = String.format("%s/clusters/%s/resources/%s", formatHost(restEndpoint), cluster, segment);
            log.info("share store url is {}", url);
            ResponseEntity<ShareStoreSegmentVO> response = restTemplate.exchange(url, HttpMethod.GET, null,
                    ShareStoreSegmentVO.class);
            if (response.getStatusCode() != HttpStatus.OK) {
                log.error("访问 ShareStore 地址 url {} 返回结果是 {}", url, response);
                throw new ServiceException(BaseResponseCodeEnum.SHARESTORE_CONNECTION_FAIL);
            }

            ShareStoreSegmentVO shareStoreSegmentVO = response.getBody();
            if (shareStoreSegmentVO == null || shareStoreSegmentVO.getNum() == null) {
                log.error("访问 ShareStore {} 返回结果是 {}", url, shareStoreSegmentVO);
                throw new ServiceException(BaseResponseCodeEnum.SHARESTORE_NOT_GET_SEGMENT.name(),
                        String.format(BaseResponseCodeEnum.SHARESTORE_NOT_GET_SEGMENT.getMessage(), cluster, segment));
            }
            return shareStoreSegmentVO.getNum();
        } catch (ResourceAccessException e) {
            log.error(e.getMessage(), e);
            throw new ServiceException(BaseResponseCodeEnum.SHARESTORE_NETWORK_FAIL.name(),
                    String.format(BaseResponseCodeEnum.SHARESTORE_NETWORK_FAIL.getMessage(), restEndpoint));
        } catch (ServiceException e) {
            log.error(e.getMessage(), e);
            throw new ServiceException(BaseResponseCodeEnum.SHARESTORE_CONNECTION_FAIL);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ServiceException(BaseResponseCodeEnum.SHARESTORE_VISIT_FAIL.name(),
                    String.format(BaseResponseCodeEnum.SHARESTORE_VISIT_FAIL.getMessage(), restEndpoint));
        }
    }

    @Override
    public boolean segmentExist(String restEndpoint, String cluster, String segment) {
        try {
            String url = String.format("%s/clusters/%s/resources/", formatHost(restEndpoint), cluster);
            log.info("share store url is {}", url);
            ResponseEntity<ShareStoreClusterVO> response = restTemplate.exchange(url, HttpMethod.GET, null,
                    ShareStoreClusterVO.class);
            if (response.getStatusCode() != HttpStatus.OK) {
                log.error("访问 ShareStore 地址 url {} 返回结果是 {}", url, response);
                throw new ServiceException(BaseResponseCodeEnum.SHARESTORE_CONNECTION_FAIL);
            }

            ShareStoreClusterVO shareStoreClusterVO = response.getBody();
            if (shareStoreClusterVO == null) {
                log.info("访问 ShareStore {} 返回结果是 {}", url, shareStoreClusterVO);
                throw new ServiceException(BaseResponseCodeEnum.SHARESTORE_CONNECTION_FAIL,
                        String.format("访问 ShareStore 服务失败, Cluster is: %s", cluster));
            }
            return shareStoreClusterVO.contains(segment);
        } catch (ResourceAccessException e) {
            log.error(e.getMessage(), e);
            throw new ServiceException(BaseResponseCodeEnum.SHARESTORE_NETWORK_FAIL.name(),
                    String.format(BaseResponseCodeEnum.SHARESTORE_NETWORK_FAIL.getMessage(), restEndpoint));
        } catch (ServiceException e) {
            log.error(e.getMessage(), e);
            throw new ServiceException(BaseResponseCodeEnum.SHARESTORE_CONNECTION_FAIL);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ServiceException(BaseResponseCodeEnum.SHARESTORE_VISIT_FAIL.name(),
                    String.format(BaseResponseCodeEnum.SHARESTORE_VISIT_FAIL.getMessage(), restEndpoint));
        }
    }

    private String formatHost(String host) {
        if (host == null) {
            throw new ServiceException(BaseResponseCodeEnum.SHARESTORE_ADDRESS_NOT_NULL);
        }

        if (host.contains("sg1")) {
            if (host.contains(DsTaskConstant.TEST)) {
                return TEST_REST_ENDPOINT_SG1;
            }
            return REST_ENDPOINT_SG1;
        }

        if (host.contains("sg2")) {
            if (host.contains(DsTaskConstant.TEST)) {
                return TEST_REST_ENDPOINT_SG2;
            }
            return REST_ENDPOINT_SG2;
        }

//        if (!host.startsWith("http://") && !host.startsWith("https://")) {
//            host = "http://" + host;
//        }
//
//        while (host.endsWith("/")) {
//            host = host.substring(NumberUtils.INTEGER_ZERO, host.length() - NumberUtils.INTEGER_ONE);
//        }
        return host;
    }
}

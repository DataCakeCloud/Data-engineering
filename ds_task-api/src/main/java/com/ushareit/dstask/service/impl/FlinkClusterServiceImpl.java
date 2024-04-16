package com.ushareit.dstask.service.impl;

import com.ushareit.dstask.bean.FlinkCluster;
import com.ushareit.dstask.bean.FlinkVersion;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.mapper.FlinkClusterMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.FlinkClusterService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
@Slf4j
@Service
public class FlinkClusterServiceImpl extends AbstractBaseServiceImpl<FlinkCluster> implements FlinkClusterService {

    @Resource
    private FlinkClusterMapper flinkClusterMapper;

    @Override
    public CrudMapper<FlinkCluster> getBaseMapper() {
        return flinkClusterMapper;
    }

    @Override
    public Object save(FlinkCluster flinkClusterFromWeb) {
        //1.参数预校验
        preCheckCommon(flinkClusterFromWeb);

        super.save(flinkClusterFromWeb);
        return flinkClusterFromWeb;
    }

    @Override
    public void update(FlinkCluster flinkClusterFromWeb) {
        //1.ID不为空校验
        if (flinkClusterFromWeb == null || flinkClusterFromWeb.getId() == null) {
            throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_REQUIRED);
        }
        //2.参数预校验
        preCheckCommon(flinkClusterFromWeb);

        super.update(flinkClusterFromWeb);
    }

    private void preCheckCommon(FlinkCluster cluster) {
        //1.校验名称
        if (!match(cluster.getName(), DsTaskConstant.CLUSTER_NAME_PATTERN)) {
            throw new ServiceException(BaseResponseCodeEnum.NAME_NOT_MATCH);
        }
        //2.校验statepath
        if (StringUtils.isNotEmpty(cluster.getStatePath()) && !match(cluster.getStatePath(), DsTaskConstant.OBS_AWS_PATH_PATTERN)) {
            throw new ServiceException(BaseResponseCodeEnum.STATE_PATH_NOT_MATCH);
        }
        //3.Name不重复校验
        super.checkOnUpdate(super.getByName(cluster.getName()), cluster);
    }

    @Override
    public void delete(Object id) {
        FlinkCluster flinkCluster = checkExist(id);
        flinkCluster.setDeleteStatus(1);
        super.update(flinkCluster);
    }

    private FlinkCluster checkExist(Object id) {
        FlinkCluster flinkCluster = super.getById(id);
        if (flinkCluster == null || flinkCluster.getDeleteStatus() == 1) {
            throw new ServiceException(BaseResponseCodeEnum.DATA_NOT_FOUND);
        }
        return flinkCluster;
    }

    @Override
    public List<FlinkCluster> listAutoScaleClusters() {
        List<FlinkCluster> clusters = flinkClusterMapper.selectExist();
        List<FlinkCluster> result = clusters.stream().filter(cluster -> new FlinkVersion(cluster.getVersion()).isGreaterThanFlink113()).collect(Collectors.toList());
        return result;
    }

    @Override
    public List<FlinkCluster> listNonAutoScaleClusters() {
        List<FlinkCluster> clusters = flinkClusterMapper.selectExist();
        List<FlinkCluster> result = clusters.stream().filter(cluster -> !new FlinkVersion(cluster.getVersion()).isGreaterThanFlink113()).collect(Collectors.toList());
        return result;
    }
}

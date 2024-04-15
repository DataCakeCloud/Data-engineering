package com.ushareit.dstask.third.airbyte.common.param;

import com.ushareit.dstask.bean.Actor;
import com.ushareit.dstask.bean.CloudResouce;
import com.ushareit.dstask.bean.DeleteEntity;
import com.ushareit.dstask.constant.ActorTypeEnum;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;
import tk.mybatis.mapper.entity.Example;

import javax.validation.constraints.Pattern;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author fengxiao
 * @date 2022/7/22
 */
@Data
public class DestinationSearch {

    private Integer destinationDefinitionId;
    private Integer destinationId;
    private String name;
    private String destinationName;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date createDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date updateDate;

    private String createBy;

    private String region;

    @Pattern(regexp = "name|createTime|updateTime", message = "排序字段不支持")
    private String sortKey;

    @Pattern(regexp = "asc|desc", message = "排序类别不支持")
    private String sortOrder;

    public Example toExample(List<Integer> definitionIds) {

        CloudResouce cloudResource = DataCakeConfigUtil.getCloudResourcesService().getCloudResource();
        List<String> collect = cloudResource.getList().stream().map(CloudResouce.DataResource::getRegionAlias).collect(Collectors.toList());

        if (StringUtils.isNotEmpty(region) && !collect.contains(region)) {
            throw new ServiceException(BaseResponseCodeEnum.NOT_SUPPORT_REGION);
        }

        Example example = new Example(Actor.class);
        Example.Criteria criteria = example.or();
        if (destinationId != null) {
            criteria.andEqualTo("id", destinationId);
        }

        if (destinationDefinitionId != null) {
            criteria.andEqualTo("actorDefinitionId", destinationDefinitionId);
        }

        if (StringUtils.isNotBlank(name)) {
            criteria.andLike("name", "%" + name + "%");
        }

        if (CollectionUtils.isNotEmpty(definitionIds)) {
            criteria.andIn("actorDefinitionId", definitionIds);
        }

        if (createDate != null) {
            criteria.andGreaterThanOrEqualTo("createTime", createDate);

            Calendar c = Calendar.getInstance();
            c.setTime(createDate);
            c.add(Calendar.DATE, 1);
            Date endDate = c.getTime();
            criteria.andLessThan("createTime", endDate);
        }

        if (updateDate != null) {
            criteria.andGreaterThanOrEqualTo("updateTime", updateDate);

            Calendar c = Calendar.getInstance();
            c.setTime(updateDate);
            c.add(Calendar.DATE, 1);
            Date endUpdateDate = c.getTime();
            criteria.andLessThan("updateTime", endUpdateDate);
        }

        if (StringUtils.isNotBlank(region)) {
            criteria.andEqualTo("region", region);
        }

        if (StringUtils.isNotBlank(createBy)) {
            criteria.andEqualTo("createBy", createBy);
        }

        if (!InfTraceContextHolder.get().getAdmin()) {
            criteria.andIn("createBy", Arrays.asList(InfTraceContextHolder.get().getUserName(), "system"));
        }

        if (StringUtils.isNotBlank(sortKey) && StringUtils.isNotBlank(sortOrder)) {
            switch (sortKey) {
                case "name":
                    example.setOrderByClause("name " + sortOrder);
                    break;
                case "createTime":
                    example.setOrderByClause("create_time " + sortOrder);
                    break;
                case "updateTime":
                    example.setOrderByClause("update_time " + sortOrder);
                    break;
            }
        }

        criteria.andEqualTo("actorType", ActorTypeEnum.destination.name());
        criteria.andEqualTo("deleteStatus", DeleteEntity.NOT_DELETE);
        return example;
    }

}

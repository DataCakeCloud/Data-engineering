package com.ushareit.dstask.third.airbyte.common.param;

import com.ushareit.dstask.bean.ActorDefinition;
import com.ushareit.dstask.bean.DeleteEntity;
import com.ushareit.dstask.constant.ActorTypeEnum;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;
import tk.mybatis.mapper.entity.Example;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import java.util.Calendar;
import java.util.Date;

/**
 * @author fengxiao
 * @date 2022/7/26
 */
@Data
public class SourceDefinitionSearch {

    private Integer sourceDefinitionId;
    private String name;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date createDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date updateDate;

    private String dockerRepository;

    private String createBy;

    @Max(value = 1, message = "是否开启取值不存在")
    @Min(value = 0, message = "是否开启取值不存在")
    private Integer isOpen;

    @Pattern(regexp = "name|createTime|updateTime", message = "排序字段不支持")
    private String sortKey;

    @Pattern(regexp = "asc|desc", message = "排序类别不支持")
    private String sortOrder;

    public Example toExample() {
        Example example = new Example(ActorDefinition.class);
        Example.Criteria criteria = example.or();

        if (sourceDefinitionId != null) {
            criteria.andEqualTo("id", sourceDefinitionId);
        }

        if (StringUtils.isNotBlank(name)) {
            criteria.andLike("name", "%" + name + "%");
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

        if (StringUtils.isNotBlank(dockerRepository)) {
            criteria.andLike("dockerRepository", "%" + dockerRepository + "%");
        }

        if (StringUtils.isNotBlank(createBy)) {
            criteria.andEqualTo("createBy", createBy);
        }

        if (isOpen != null) {
            criteria.andEqualTo("isOpen", isOpen);
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

        criteria.andEqualTo("actorType", ActorTypeEnum.source.name());
        criteria.andEqualTo("deleteStatus", DeleteEntity.NOT_DELETE);
        return example;
    }
}

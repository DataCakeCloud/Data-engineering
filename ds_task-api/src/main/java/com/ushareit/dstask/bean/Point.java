package com.ushareit.dstask.bean;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import java.util.Objects;

/**
 * @author wuyan
 * @date 2022/6/6
 */
@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("point类")
public class Point {
    private String name;

    /**
     * event/crontab/table/task
     */
    private String type;

    /**
     * true表示外部 false表示内部
     */
    private Boolean isExternal = true;

    /**
     * 哪个任务产生这个数据集，只有table时才有
     */
    private String produceDataSetTask;

    @JSONField(serialzeFeatures = {
            SerializerFeature.DisableCircularReferenceDetect
    })
    private Task task;

    private String crontab;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        return name.equals(point.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}

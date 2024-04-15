package com.ushareit.engine.seatunnel.adapter.bean;



import com.ushareit.engine.param.Column;
import com.ushareit.engine.param.RuntimeConfig;
import com.ushareit.engine.param.Table;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * author: xuebtao
 * date: 2023-07-06
 */

@Data
public class Schema implements Serializable {

    private Map<String, String> columns = new HashMap<>();

    // "partitions":"dt={{ yesterday_ds_nodash }}",
    private Map<String, String> partitions = new HashMap<>();


    public Schema(RuntimeConfig runtimeConfig) {
        List<Table> tables = runtimeConfig.getCatalog().getTables();
        Table table = tables.get(0);
        String partitions = table.getPartitions();

        List<Column> columnsList = table.getColumns();
        List<String> collect = columnsList.stream()
                .map(data -> columns.put(data.getName(), data.getColumnType())).collect(Collectors.toList());

        addPartition(partitions);
    }


    public void addPartition(String partitions) {
        if(StringUtils.isEmpty(partitions)){
            return;
        }
        String[] split = partitions.split(",");
        List<String> list = Arrays.asList(split);
        List<String> collect = list.stream().map(data -> {
            String[] Kv = data.split("=");
            this.partitions.put(Kv[0].trim(), Kv[1].trim());
            return data;
        }).collect(Collectors.toList());
    }


}

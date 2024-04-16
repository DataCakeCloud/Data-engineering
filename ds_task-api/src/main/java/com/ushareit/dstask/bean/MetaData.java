package com.ushareit.dstask.bean;


import com.ushareit.dstask.web.ddl.metadata.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import java.util.List;

/**
 * @author xuebotao
 * @date 2022-08-01
 */
@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MetaData {

    private List<Table> data;

    public Integer count;


}

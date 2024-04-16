package com.ushareit.engine.seatunnel.job;

import com.ushareit.engine.seatunnel.adapter.Adapter;
import com.ushareit.engine.seatunnel.adapter.transform.Transform;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import javax.persistence.Entity;
import java.util.Map;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class JobInfo {
    private Env env;
    private Map<String, Adapter> source;
    private Map<String, Adapter> transform;
    private Map<String, Adapter> sink;
}

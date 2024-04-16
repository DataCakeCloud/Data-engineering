package com.ushareit.engine.datax;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class Job {
    private List<Content> content;
    private Map<String,Object> setting;

    public Job(){
        Content c = new Content();
        content= new ArrayList<>();
        content.add(c);
    }
}

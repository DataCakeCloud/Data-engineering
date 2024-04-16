package com.ushareit.engine.datax;

import lombok.Data;

import java.util.List;
@Data
public class Content {
    private Reader reader;
    private Writer writer;
    private List<Transformer> transformer;

    @Data
    public static class Transformer{
        String name;
        Parameter parameter;
    }
    @Data
    public static class Parameter{
        Integer columnIndex;
        List<String> paras;
    }
}

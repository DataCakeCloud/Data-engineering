package com.ushareit.dstask.bean;


import lombok.Data;

import java.util.List;

@Data
public class HawKeyeResult {

    private String status;

    private String isPartial;

    private Data data;

    @lombok.Data
    public class Data {
        private String resultType;
        private List<CoreResult> result;

        @lombok.Data
        public class CoreResult {
            private Metric metric;

            private String[][] values;

            private String[] value;

            @lombok.Data
            public class Metric {
                private String topic;

                private String instance;
            }
        }
    }
}

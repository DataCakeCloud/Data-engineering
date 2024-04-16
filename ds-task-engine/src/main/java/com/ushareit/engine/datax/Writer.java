package com.ushareit.engine.datax;

import lombok.Data;

@Data
public class Writer {
    public static final String KERBEROS_FILE_PATH = "/data-kfs/home/{0}/.keytab/{1}.keytab";
    public static final String KERBEROS_PRINCIPAL = "{0}@HADOOP.COM";
    private String name;
}

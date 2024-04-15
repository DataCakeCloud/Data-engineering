package com.ushareit.dstask.web.ddl.model;

import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.web.ddl.SqlDdl;
import com.ushareit.dstask.web.ddl.metadata.Table;
import com.ushareit.dstask.web.utils.EncryptUtil;
import com.ushareit.dstask.web.utils.UrlUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
public class MysqlCDCDdl extends SqlDdl{
    private final String INFO_DEMO =
            "   'connector' = 'mysql-cdc',\n" +
            "   'hostname' = '%s',\n" +
            "   'port' = '%s',\n" +
            "   'username' = '%s',\n" +
            "   'password' = '%s',\n" +
            "   'database-name' = '%s',\n" +
            "   'table-name' = '%s',\n" +
            "   'scan.startup.mode' = '%s'";

    private String mysqlCdcType;
    public MysqlCDCDdl(Table table, String mysqlCdcType) {
        super(table);
        this.mysqlCdcType = mysqlCdcType;
    }

    @Override
    public String getSchema() throws Exception {
        String schema = super.getSchema() + ",\n   proc_time as PROCTIME()";
        if (StringUtils.isNoneEmpty(table.getPrimaryKey())){
            schema += ",\n  PRIMARY KEY (`" + table.getPrimaryKey() +"`)  NOT ENFORCED";
        }
        return schema;
    }

    @Override
    public String getInfo() throws Exception {
        String url = table.getUrl();
        Matcher m = UrlUtil.getMatcher(url, DsTaskConstant.JDBC_URL_PATTERN);
        String hostname = m.group(1);
        String port = m.group(2);
        String database_name = table.getDbName();
        String tableName = table.getSourceTable();
        String userName = table.getUsername();
        String passwdEncryptKey = table.getPassword();
        String password = EncryptUtil.decrypt(passwdEncryptKey, DsTaskConstant.METADATA_PASSWDKEY);

        return String.format(INFO_DEMO, hostname, port, userName, password, database_name, tableName, mysqlCdcType);
    }
}

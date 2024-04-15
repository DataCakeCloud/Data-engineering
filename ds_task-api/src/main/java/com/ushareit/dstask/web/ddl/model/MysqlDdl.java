package com.ushareit.dstask.web.ddl.model;

import com.google.common.base.Joiner;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.constant.SymbolEnum;
import com.ushareit.dstask.web.ddl.SqlDdl;
import com.ushareit.dstask.web.ddl.metadata.Table;
import com.ushareit.dstask.web.utils.EncryptUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
public class MysqlDdl extends SqlDdl{
    private final String INFO_DEMO =
            "   'connector' = 'jdbc',\n" +
            "   'url' = '%s',\n" +
            "   'table-name' = '%s',\n" +
            "   'username' = '%s',\n" +
            "   'password' = '%s',\n" +
            "   'driver' = 'com.mysql.jdbc.Driver'";

    public MysqlDdl(Table table) {
        super(table);
    }

    @Override
    public String getInfo() throws Exception {
        String url = table.getParameters().get("url");
        String tableName = table.getName();
        String userName = table.getParameters().get("username");
        String dbName = table.getParameters().get("db_name");
        String passwdEncryptKey = table.getParameters().get("password");

        String password = EncryptUtil.decrypt(passwdEncryptKey, DsTaskConstant.METADATA_PASSWDKEY);

        return String.format(INFO_DEMO, assmbleDatabase(url, dbName), tableName, userName, password);
    }

    private String  assmbleDatabase(String url, String dbName){
        String[] strs = url.split("\\?");
        if (strs[0].endsWith("//")){
            return url;
        }
        return strs[0] + dbName + "?zeroDateTimeBehavior=convertToNull";
    }
    @Override
    public String getDisplayTableName()  throws Exception {
        return Joiner.on(SymbolEnum.UNDERLINE.getSymbol())
                .skipNulls()
                .join("mysql", getName(),"inc_hourly").toLowerCase();
    }
}

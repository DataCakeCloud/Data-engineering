package com.ushareit.dstask.airbyte;

import com.fasterxml.jackson.databind.JsonNode;
import com.ushareit.dstask.third.airbyte.connector.MySQLConnector;
import com.ushareit.dstask.third.airbyte.connector.impl.MySQLConnectorImpl;
import com.ushareit.dstask.third.airbyte.json.Jsons;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author fengxiao
 * @date 2022/8/18
 */
@RunWith(SpringRunner.class)
public class TestMySQL extends TestCase {

    private static final String config = "{\"jdbc_url_params\":\"autoReconnect=true&useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai\",\"database\":\"ds_task\",\"password\":\"dTaKBkzFHRnwSwfhLL^pXIJWbR%3INLl\",\"port\":3306,\"replication_method\":\"STANDARD\",\"host\":\"test.inf-common.cbs.sg2.mysql\",\"username\":\"CBS_Developer\",\"tunnel_method\":{\"tunnel_method\":\"NO_TUNNEL\"}}";

    @Test
    public void testGetColumns() throws Exception {
        JsonNode jsonNode = Jsons.deserialize(config);
        MySQLConnector mySQLConnector = new MySQLConnectorImpl();
        System.out.println(mySQLConnector.getColumnMap(jsonNode, "access_group1","mysql"));

    }

    @Test
    public void testGetTables() throws Exception {
        JsonNode jsonNode = Jsons.deserialize(config);
        MySQLConnector mySQLConnector = new MySQLConnectorImpl();
        System.out.println(mySQLConnector.getTableList(jsonNode,"mysql"));
    }

}

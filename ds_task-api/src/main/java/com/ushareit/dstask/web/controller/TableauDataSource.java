package com.ushareit.dstask.web.controller;

import com.alibaba.druid.pool.DruidDataSource;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class TableauDataSource {

    public static Map<String, String> readProperties(String fileName) {
        Map<String, String> resultMap = new HashMap<String, String>();
        try {
            Properties props = PropertiesLoaderUtils.loadAllProperties(fileName);
            for (Object key : props.keySet()) {
                resultMap.put(key.toString(), props.get(key).toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultMap;
    }

    public DataSource dataSource() {
        Map<String, String> paramMap = readProperties("tableau-jdbc.properties");
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(paramMap.get("url"));
        dataSource.setUsername(paramMap.get("username"));
        dataSource.setPassword(paramMap.get("password"));
        return dataSource;
    }

    public Set<String> getAdsOwner(){
        Set<String> ownerSet = new HashSet<>();
        try {
            Connection conn = dataSource().getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement("select owner from tableau.bill_owner_department where department = '广告工程' or department = '广告算法'");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                ownerSet.add(resultSet.getString("owner"));
            }
        } catch (SQLException e) {
            throw new ServiceException(BaseResponseCodeEnum.IT_GET_DEPARTMENT_LIST_FAIL,"数据查询失败");
        }
        return ownerSet;
    }

    public boolean isAdsOwener(String owner){
        if (StringUtils.isEmpty(InfTraceContextHolder.get().getEnv()) ||
                DsTaskConstant.TEST.equalsIgnoreCase(InfTraceContextHolder.get().getEnv()) ||
                DsTaskConstant.CLOUD_TEST.equalsIgnoreCase(InfTraceContextHolder.get().getEnv()) ||
                DsTaskConstant.DEV.equalsIgnoreCase(InfTraceContextHolder.get().getEnv())) {
            return false;
        }
        Set<String> adsOwner = getAdsOwner();
        return adsOwner.contains(owner);
    }
}

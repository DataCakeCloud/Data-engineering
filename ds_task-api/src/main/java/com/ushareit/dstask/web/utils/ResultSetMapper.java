package com.ushareit.dstask.web.utils;

import org.apache.commons.beanutils.BeanUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: licg
 * @create: 2022-01-20 15:24
 **/
public class ResultSetMapper<T> {
    @SuppressWarnings("unchecked")
    public List<T> mapRersultSetToObject(ResultSet rs, Class outputClass) {
        List<T> outputList = null;
        try {
            // check if outputClass has 'Entity' annotation
            if (!outputClass.isAnnotationPresent(Entity.class)) {
                return null;
            }
            // get the resultset metadata
            ResultSetMetaData rsmd = rs.getMetaData();
            // get all the attributes of outputClass
            Field[] fields = outputClass.getDeclaredFields();
            while (rs.next()) {
                T bean = (T) outputClass.newInstance();
                for (int _iterator = 0; _iterator < rsmd.getColumnCount(); _iterator++) {
                    // getting the SQL column name
                    String columnName = rsmd.getColumnName(_iterator + 1);
                    // reading the value of the SQL column
                    Object columnValue = rs.getObject(_iterator + 1);
                    // iterating over outputClass attributes to check if
                    // any attribute has 'Column' annotation with
                    // matching 'name' value
                    for (Field field : fields) {
                        if (field.isAnnotationPresent(Column.class)) {
                            Column column = field.getAnnotation(Column.class);
                            if (column.name().equalsIgnoreCase(columnName) && columnValue != null) {
                                BeanUtils.setProperty(bean, field.getName(), columnValue);
                                break;
                            }
                        }
                    }
                }
                if (outputList == null) {
                    outputList = new ArrayList<T>();
                }
                outputList.add(bean);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return outputList;
    }
}

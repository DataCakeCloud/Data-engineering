package com.ushareit.dstask.validator.impl;

import com.amazonaws.util.StringUtils;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.validator.TaskContext;
import com.ushareit.dstask.validator.ValidFor;
import com.ushareit.dstask.validator.ValidType;
import com.ushareit.dstask.validator.Validator;
import com.ushareit.dstask.web.utils.FlinkSqlParseUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.sql.parser.SqlParseException;
import org.springframework.stereotype.Component;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Base64;

/**
 * Flink sql末尾未加；导致无法构建拓扑关系，构建流图
 * 需加缓存
 *
 * @author fengxiao
 * @date 2023/2/9
 */
@Slf4j
@Component
@ValidFor(type = ValidType.FLINK_SQL_SYNTAX)
public class FlinkSqlSyntaxValidator implements Validator {

    @Override
    public void validateImpl(Task task, TaskContext context) {
        String sqlContent = task.getContent();
        if (StringUtils.isNullOrEmpty(sqlContent)){
            return;
        }

        try {
            //从指定位置读取sql或读取传入的sql参数
            String sql = getSql(sqlContent);
            //解析sql
            FlinkSqlParseUtil.parse(sql);
            if (!sql.trim().endsWith(";")) {
                throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "Flink Sql格式不正确,结尾必须为【;】");
            }
        } catch (SqlParseException | UnsupportedOperationException e) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "Flink Sql Parse异常：" + e.getMessage());
        } catch (Exception e) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "Flink Sql格式不正确");
        }
    }
    private String getSql(String sqlContent) throws Exception{
        String decode = decode(new String(Base64.getDecoder().decode(sqlContent.getBytes())));
        return decode;
    }

    private String decode(String str) throws UnsupportedEncodingException {
        return URLDecoder.decode(str, "UTF-8");
    }
}

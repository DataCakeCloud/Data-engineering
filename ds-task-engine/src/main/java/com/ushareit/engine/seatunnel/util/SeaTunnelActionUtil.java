package com.ushareit.engine.seatunnel.util;

import com.alibaba.fastjson.JSONArray;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class SeaTunnelActionUtil {
    private static final String SEATUNNEL_ACTION_RESULT= "Seatunnel Action result:";
    private static final String SEATUNNEL_ACTION_FALSE= "Seatunnel Action false:";
    private static final String SEATUNNEL_ACTION_SHELL_DEMO = " $SEATUNNEL_HOME/bin/action-seatunnel-flink-13-connector-v2.sh --config %s %s";

     public static Boolean check( String configStr) {
         String command = "--check";
         String result = localExecute(configStr, command);
         return Boolean.valueOf(result);
     }

    public static List<Map<String, String>> getSourceSchema(String configStr) {
        String command = "--get-source-schema";
        String result = localExecute(configStr, command);
        return (List<Map<String,String>>) JSONArray.parse(result);
    }

    public static List<Map<String, String>> getSourceSample(String configStr) {
        String command = "--get-source-demo";
        String result = localExecute(configStr, command);
        return (List<Map<String,String>>) JSONArray.parse(result);
    }

    public static List<Map<String, Object>> getTables(String configStr) {
        String command = "--get-source-tables";
        String result = localExecute(configStr, command);
        return (List<Map<String, Object>>) JSONArray.parse(result);
    }

    public static Boolean createTable(String configStr) {
        String command = "--createTable";
        String result = localExecute(configStr, command);
        return Boolean.valueOf(result);
    }

    private static String localExecute(String configStr, String command){
        File file = null;
        try {
            file = createTempFile(configStr);
            String shell = String.format(SEATUNNEL_ACTION_SHELL_DEMO, file.getAbsolutePath(), command);
            return doProcess(shell);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(BaseResponseCodeEnum.JOB_RESULT_PARSE_FAIL,e.getMessage());
        }finally {
            if (file != null) {
                file.delete();
            }
        }
    }

    private static File createTempFile(String configStr) throws IOException {
        File tmpFile = File.createTempFile("job_", ".conf");
        tmpFile.deleteOnExit();
        BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile));
        writer.write(configStr);
        writer.flush();
        writer.close();

        return tmpFile;
    }
    private static String doProcess(String execCmd) throws Exception {
        ShellUtil.CommandResult response = ShellUtil.execCmd(execCmd, false);
        if (response == null || !response.result ) {
            throw new ServiceException(BaseResponseCodeEnum.JOB_SUBMIT_TO_LOCAL_FAIL, response.errorMsg);
        }
        if (response.successMsg.contains(SEATUNNEL_ACTION_FALSE)){
            throw new RuntimeException(response.successMsg.split(SEATUNNEL_ACTION_FALSE)[1]);
        }
        return response.successMsg.split(SEATUNNEL_ACTION_RESULT)[1];
    }

}

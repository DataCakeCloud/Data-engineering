package com.ushareit.dstask.web.factory.flink.job;

import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.bean.TemplateRegionImp;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.constant.TemplateEnum;
import com.ushareit.dstask.service.impl.TaskServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
@Slf4j
public class FlinkSqlJob extends FlinkBaseJob {
    public FlinkSqlJob(Task task, Integer tagId, Integer savepointId, TaskServiceImpl taskServiceImp) {
        super(task, tagId, savepointId, taskServiceImp);
    }

    @Override
    public void beforeExec() throws Exception {
        String region = taskServiceImp.flinkClusterService.getById(runTimeTaskBase.getFlinkClusterId()).getRegion();

        TemplateRegionImp build = TemplateRegionImp.builder().templateCode(task.getTemplateCode()).regionCode(region).build();
        TemplateRegionImp templateRegionImp = taskServiceImp.templateRegionImpService.selectOne(build);
        runTimeTaskBase.setJarUrl(templateRegionImp.getUrl());
        runTimeTaskBase.setMainClass(templateRegionImp.getMainClass());

        runTimeTaskBase.setMainClassArgs(getSqlJobArgs());

        flinkExecutionPackages = super.getFlinkExecutionPackages(runTimeTaskBase.getContent());
        execArgs = getSqlJobArgsArr();

        super.beforeExec();
    }

    private String getSqlJobArgs() {
        StringBuffer sb = new StringBuffer();

        sb.append("-n " + runTimeTaskBase.getName());

        sb.append(" -p " + runtimeConfig.getParallelism());

        if (runtimeConfig.getCheckpoint()) {
            sb.append(" -ci " + runtimeConfig.getCheckpointInterval());
            sb.append(" -cm " + runtimeConfig.getCheckpointMode());
            if (runtimeConfig.getCheckpointTimeout() != null){
                sb.append(" -ct " + runtimeConfig.getCheckpointTimeout());
            }
        }

        if (!StringUtils.isBlank(runTimeTaskBase.getContent())) {
            sb.append(" -sql " + runTimeTaskBase.getContent());
        }
        log.info("sql提交参数:" + sb.toString());
        return sb.toString();
    }

    private List<String> getSqlJobArgsArr() {
        List<String> args = new ArrayList<>();
        args.add("-n");
        args.add(runTimeTaskBase.getName());
        args.add("-p");
        args.add(runtimeConfig.getParallelism().toString());
        if (runtimeConfig.getCheckpoint()) {
            args.add("-ci");
            args.add(runtimeConfig.getCheckpointInterval().toString());
            args.add("-cm");
            args.add(runtimeConfig.getCheckpointMode());
            if (runtimeConfig.getCheckpointTimeout() != null) {
                args.add("-ct");
                args.add(runtimeConfig.getCheckpointTimeout().toString());
            }
        }

        if (!StringUtils.isBlank(runTimeTaskBase.getContent())) {
            args.add("-sql");
            args.add(runTimeTaskBase.getContent());
        }
        return args;
    }

    public String getSqlJobArgsForAutoScale() {
        StringBuffer sb = new StringBuffer();

        sb.append("--job-name " + runTimeTaskBase.getName());

        sb.append(" --parallism " + runtimeConfig.getParallelism());

        if (runtimeConfig.getCheckpoint()) {
            sb.append(" --checkpoint-interval " + runtimeConfig.getCheckpointInterval());
            sb.append(" --checkpoint-mode " + runtimeConfig.getCheckpointMode());
        }

        if (!StringUtils.isBlank(runTimeTaskBase.getContent())) {
            sb.append(" --sql-content " + runTimeTaskBase.getContent());
        }
        log.info("getSqlJobArgsForAutoScale提交参数:" + sb.toString());
        return sb.toString();
    }

}

package com.ushareit.dstask.service;

import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.*;
import com.ushareit.dstask.common.vo.TaskNameVO;
import com.ushareit.dstask.entity.ScheduleJobOuterClass;
import com.ushareit.dstask.web.factory.scheduled.param.Dataset;
import com.ushareit.dstask.web.factory.scheduled.param.EventDepend;
import com.ushareit.dstask.web.vo.BaseResponse;
import org.apache.commons.math3.util.Pair;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
public interface TaskService extends BaseService<Task> {
    /**
     * 打标签application
     *
     * @param task
     * @return
     */
    void tag(Task task);

    /**
     * 启动
     *
     * @param taskId
     * @return
     */
    void start(Integer taskId, Integer tagId, Integer savepointId) throws Exception;

    /**
     * 添加审计和版本信息
     */
    Integer addTaskVersionAndAudit(Task task, String operation);

    /**
     * 获取传给aiflow的task的task_code
     *
     * @param task
     * @return
     */
    Map<String, String> getTaskCode(Task task);

    Integer addTaskVersion(Task task);

    List<TaskVersion> addTaskVersions(List<Task> tasks);

    /**
     * 检测应用是否存在
     *
     * @param depId
     * @return
     */
    Task checkExist(Object depId);

    /**
     * 修改应用当前状态
     *
     * @param task
     * @param state
     */
    void changeStateCode(Task task, String state);

    List<Task> checkExist(List<Integer> taskIds);

    /**
     * 查询服务Ui地址
     *
     * @param taskId
     * @param genieJobId
     * @return
     */
    String getServiceUi(Integer taskId, String genieJobId, String state, String logFileUrl) throws Exception;

    /**
     * 查询Metrics地址
     *
     * @param taskId
     * @return
     */
    String getMetricsUi(Integer taskId, String genieJobId) throws Exception;

    /**
     * 查询Logs地址
     *
     * @param taskId
     * @return
     */
    String getlogsUi(Integer taskId, String executionDate) throws Exception;

    /**
     * 下载实例日志
     *
     * @param url
     * @return
     */
    ResponseEntity<Object> getInstanceLog(String url) throws IOException;

    /**
     * 补数
     *
     * @param id
     * @param startDate
     * @param endDate
     * @return
     */
    Object backFill(Integer id, String startDate, String endDate, Integer[] childIds, String isSendNotify, String isCheckDependency);

    String process(Integer userActionId);

    /**
     * 获取目录树
     *
     * @return
     */
    List<Map<String, Object>> getCatalog();

    /**
     * @return
     */
    Map<String, Object> page(Integer pageNum, Integer pageSize, Map<String, String> paramMap);

    /**
     * 终止
     *
     * @param id
     * @return
     */
    void stop(Integer id);

    /**
     * 取消
     *
     * @param id
     */
    void cancel(Integer id);

    /**
     * 取消
     *
     * @param
     */
    void statusHook(String taskName, String status);

    Map<String, Object> getChildDependencies(Integer id);

    Map<String, Object> getDependencies(Integer id, Integer level, String executionDate, Integer upDown);

    /**
     * 获取DDL
     *
     * @param guId
     * @return
     */
    String getDdl(String guId);

    String templateRendering(String modelName,String modelType);

    Map<String, Object> getDisplayDdl(String guId, Boolean isSql);

    /**
     * 校验SQL
     *
     * @param task
     * @return
     */
    BaseResponse check(Task task) throws Exception;

    void cacheCheck(Task task,Integer id,String md5Sql,String region,String owner) throws Exception;

    void onlineAndOffline(Integer id, Integer status, Boolean ifNotify);

    Map<String, List<Task>> listTasks(String name);

    List<TaskNameVO> searchByName(String name);

    void batchUpdateRole(String owner, String[] collaborators, String[] taskNames);

    /**
     * 获取
     *
     * @param groups
     * @return
     */
    BaseResponse getIams(String groups);

    /**
     * 任务一键复制
     *
     * @param id
     * @param name
     * @return
     */
    void copy(Integer id, String name);

    /**
     * 获取统计信息
     *
     * @return
     */
    BaseResponse getStatistic();

    /**
     * @param id
     * @param upDown 0-上下游  1-上游  2-下游
     * @return
     */
    Map<String, Object> upAndDown(Integer id, Integer upDown);

    List<Task> backUpdate();

    void autoScaleTm(Integer id, Integer count);

    Integer getAutoScaleTaskParal(Integer id);

    /**
     * 查询当天任务指标
     *
     * @return
     */
    HashMap<String, Integer> getTaskIndicators();

    Map<String, Object> getDependenciesOverview(String tableName);

    Task selectByOutputGuid(String output);

    Map<String, Object> getDatasetInfo(String type, String region, String source, String db, String tbl, String qualifyname);

    String renderContent(String content,String taskName);

    Map<String,Object> getBackFillDateDetail(Integer taskId);

    Map<Integer, List<TaskInstance>> getLast7State(String ids);

    Boolean isStreaming(Task task);

    List<Task> queryByIds(List<Integer> ids);

    Map<String, String> getGitFileSql(String projectName, String filePath) throws IOException;

    Map<String, Object> getEtlSqlTbl(String sql, String region);

    List<Map<String, Object>> getTaskInfo(String name);

    String getOffset(String table, String granularity, String sql);

    Boolean checkPath(String path);

    /**
     * 获取crontab语句
     */
    String getCrontab(CrontabParam crontabParam);

    void batchUpdateOwnerOrCollaborator(String taskIds, String owner, String collaborator);

    void dateTransform(Integer id, String airflowCrontab, String newTaskName) throws Exception;


    Boolean updateSeniorParam(Integer id, String sparkConfParam);

    void updateByid(Task task);

    TaskVersion getTaskVersionInfo(Integer id, Integer version);

    Task getTaskByVersion(Integer taskId, Integer version);

    List<Task> listByNames(List<String> names);

    List<Task> selectParentTask(String names);

    JSONObject getDatePreview(String taskGra, String taskCrontab, Dataset dataDepend, EventDepend taskDepend);

    void setCanEdit(Task task);

    void statics();

    List<Task> selectDayOnlinedTasks(String start, String end, String userGroup);

    List<Task> sumAllOnlinedTasks(String createBy);

    void deleteAndNotify(Integer id, Boolean ifNotify);

    List<Task> downstreamTask(Integer id);

    void realtimeExecute(Integer taskd, String newArgs, String callbackUrl);

    /**
     * 将数据库中的任务转换为调度需要的格式
     *
     * @param taskVersionIds 任务ID与版本集合
     * @param preExecute     是否要调用预执行程序
     * @return 调度系统所需要的数据格式
     */
    List<ScheduleJobOuterClass.ScheduleJob> toScheduleJobByIds(Collection<Pair<Integer, Integer>> taskVersionIds,
                                                               boolean preExecute);

    /**
     * 将任务转换为调度需要的格式
     *
     * @param task       任务信息
     * @param preExecute 是否要调用预执行程序
     * @return 调度系统所需要的数据格式
     */
    ScheduleJobOuterClass.ScheduleJob toScheduleJob(Task task, boolean preExecute);

    /**
     * 获取用户是负责人或协作者的所有任务
     *
     * @param userName 用户名
     * @return 任务列表
     */
    Map<Integer, Task> getScheduleTaskListByUser(String userName);

    /**
     * 将预先保存在数据库中的 taskKey 转换为 taskId
     *
     * @param taskKeyIdPairs     taskId 列表
     * @param eventDependsParser eventDepends 转换函数
     */
    void swapTaskKeys(List<Pair<Optional<String>, Integer>> taskKeyIdPairs,
                      BiFunction<String, Map<String, Integer>, List<EventDepend>> eventDependsParser);

    /**
     * 检查生成数据集是否存在
     *
     * @param taskId        任务ID
     * @param outputDataset 生成数据集
     */
    void checkOutputDataset(Integer taskId, String outputDataset);

    void superUpdate(Task data);

    List<String> getResultSet();

    void fastBackfill(Integer taskd,String args, String callbackUrl,String startDate,String endDate);

    String eventTrigger(EventTrigger et);

    /**
     * 获取任务运行时的command
     * @param taskId 任务ID
     * @return
     */
    String getCommand(Integer taskId);

    void changeName(Integer taskId,String newName);
}

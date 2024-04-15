package com.ushareit.dstask.mapper;

import com.github.pagehelper.Page;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;


/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
@Mapper
public interface TaskMapper extends CrudMapper<Task> {
    /**
     * 根据name查询
     *
     * @param name name
     * @return
     */
    @Override
    @Select({"SELECT * FROM task WHERE name=#{name} AND delete_status=0"})
    Task selectByName(@Param("name") String name);

    @Override
    @Select({"<script>" +
            "SELECT * FROM task " +
            "WHERE delete_status=0 " +
            "<if test='task.outputGuids!=null and \"\" neq task.outputGuids'> AND output_guids REGEXP REPLACE(#{task.outputGuids},',','|') </if>" +
            " ORDER BY create_time DESC" +
            "</script>"})
    List<Task> select(@Param("task") Task task);

    /**
     * 根据name查询
     *
     * @param name name
     * @return
     */
    @Select({"<script>" +
            "SELECT id, name, create_by FROM task " +
            "WHERE delete_status=0 AND workflow_id = 0 AND user_group = #{userGroup}" +
            "<if test='name!=null and \"\" neq name'> AND (LOCATE(#{name},name) &gt; 0 " +
            " OR LOCATE(#{name},create_by) &gt; 0)" +
            " OR id = #{name} " +
            "</if>" +
            " ORDER BY create_time DESC" +
            "</script>"})
    List<Task> selectLikeName(@Param("name") String name,@Param("userGroup") String userGroup);

    @Select({"select * from task where JSON_EXTRACT(output_dataset,'$[0].id') = #{metadataId} and delete_status = 0 and online = 1"})
    List<Task> selectWithMatadataid(@Param("metadataId") String metadataId);

    @Select({"select * from task where id = #{id} and delete_status = 0 "})
    Task getTaskNameById(@Param("id") int id);


    @Select({"<script>" +
            "select * from task where delete_status = 0 and template_code not in (\"StreamingSQL\", \"StreamingJAR\",\"Metis2Hive\",\"MysqlCDC2Hive\", \"Db2Hive\") and (name like concat('%', #{name}, '%') or JSON_EXTRACT(output_dataset,'$[0].id') like concat('%', #{name}, '%') or id = #{name})" +
            "</script>"})
    List<Task> selectByMetadataIdOrName(@Param("name") String name);

    /**
     * 根据name查询
     *
     * @param name name
     * @return
     */
    @Select({"<script>" +
            "SELECT  name FROM task " +
            "WHERE delete_status=0 " +
            "<if test='name!=null and \"\" neq name'> AND (LOCATE(#{name},name) &gt; 0 OR LOCATE(#{name},create_by) &gt; 0)</if>" +
            " limit 50" +
            "</script>"})
    List<String> selectLikeNameLimit50(@Param("name") String name);

    @Select({"<script>" +
            "SELECT distinct create_by,tenancy_code FROM task " +
            "WHERE delete_status=0 " +
            "</script>"})
    List<Task> selectForCatalog();

    @Select({"<script>" +
            "select * from task where delete_status=0 and online=1 and template_code not in ('StreamingSQL', 'StreamingJAR', 'Metis2Hive', 'MysqlCDC2Hive', 'Db2Hive') " +
            "and JSON_CONTAINS(JSON_EXTRACT(output_dataset, '$[*].id'), <![CDATA[ '\"${id}\"' ]]>,  '$')" +
            "</script>"})
    List<Task> selectOfflineTaskWithSameOutputId(@Param("id") String outputId);

    @Select({"<script>" +
            "SELECT *  FROM task t  " +
            "WHERE t.delete_status=0 and t.online=1 and t.status_code is not null and t.id<![CDATA[ <> ]]>#{id} " +
            "<if test='reg!=null and \"\" neq reg'> AND t.input_guids is not null AND t.input_guids REGEXP REPLACE(#{reg},',','|') </if> " +
            " ORDER BY t.create_time DESC" +
            "</script>"})
    List<Task> selectChildDependendcies(@Param("id") Integer id, @Param("reg") String reg);

    @Select({"<script>" +
            "SELECT *  FROM task t  " +
            "WHERE t.delete_status=0 and t.online=1 and t.status_code is not null and JSON_CONTAINS(JSON_EXTRACT(event_depends, '$[*].taskId'),  CAST(#{id} AS CHAR))" +
            "</script>"})
    List<Task> selectChildrenByEventDepends(@Param("id") Integer id);

    @Select({"<script>" +
            "SELECT *  FROM task t  " +
            "WHERE t.delete_status=0 and t.online=1 and t.status_code is not null and JSON_CONTAINS(JSON_EXTRACT(event_depends, '$[*].taskId'),  CAST(#{id} AS CHAR))"+
            "</script>"})
    List<Task> selectChildrenByEventDependsInGroup(@Param("id") Integer id);

    @Select({"<script>" +
            "SELECT * FROM task t  " +
            "WHERE t.delete_status=0 and t.online=1 and t.status_code is not null and t.id<![CDATA[ <> ]]>#{id} " +
            "<if test='reg!=null'> AND t.output_guids REGEXP REPLACE(#{reg},',','|') </if> " +
            " ORDER BY t.create_time DESC" +
            "</script>"})
    List<Task> selectParentDependendcies(@Param("id") Integer id, @Param("reg") String reg);

    @Select({"<script>" +
            "SELECT * FROM task t  " +
            "WHERE t.delete_status=0 and t.online = 1 and t.status_code is not null " +
            "and t.output_guids <![CDATA[ <> ]]> 'test.a@ue1' " +
            "<if test='reg!=null'> AND t.output_guids REGEXP REPLACE(#{reg},',','|') </if> " +
            " ORDER BY t.create_time DESC" +
            "</script>"})
    List<Task> selectParentTask(@Param("reg") String reg);


    @Select({"<script>" +
            "SELECT count(id) FROM task t  " +
            "WHERE t.delete_status=0 and t.online=1 and t.status_code is not null and (t.input_guids<![CDATA[ <> ]]>'test.a@ue1' or t.output_guids<![CDATA[ <> ]]>'test.a@ue1') " +
            "<if test='reg!=null'> AND t.output_guids REGEXP REPLACE(#{reg},',','|') </if> " +
            " ORDER BY t.create_time DESC" +
            "</script>"})
    Integer countParentTasks(@Param("id") Integer id, @Param("reg") String reg);

    @Select({"<script>" +
            "SELECT count(id)  FROM task t  " +
            "WHERE t.delete_status=0 and t.online=1 and t.status_code is not null and (t.input_guids<![CDATA[ <> ]]>'test.a@ue1' or t.output_guids<![CDATA[ <> ]]>'test.a@ue1') " +
            "<if test='reg!=null'> AND t.input_guids REGEXP REPLACE(#{reg},',','|') </if> " +
            " ORDER BY t.create_time DESC" +
            "</script>"})
    Integer countChildTasks(@Param("id") Integer id, @Param("reg") String reg);

    @Select({"<script>" +
            "SELECT * FROM task " +
            "WHERE delete_status=0 and status_code is not null " +
            "<if test='list!=null'> AND id in " +
            "   <foreach collection='list' item='id' open='(' separator=',' close=')'>",
            "   #{id}",
            "   </foreach>" +
                    "</if>" +
                    " order by create_time desc" +
                    "</script>"})
    List<Task> selectWithIds(@Param("list") List<Integer> list);

    @Select({"<script>" +
            "SELECT * FROM task " +
            "WHERE delete_status=0 and status_code is not null " +
            "<if test='list!=null'> AND name in " +
            "   <foreach collection='list' item='name' open='(' separator=',' close=')'>",
            "   #{name}",
            "   </foreach>" +
                    "</if>" +
                    " order by create_time desc" +
                    "</script>"})
    List<Task> selectWithNames(@Param("list") List<String> list);

    /**
     * 根据MAP参数查询
     *
     * @param paramMap paramMap
     * @return
     */
    @Select({"<script>" +
            "SELECT t.*,d.`value` as displayTemplateCode FROM task t,sys_dict d " +
            "WHERE t.delete_status=0 and t.status_code is not null and d.parent_code !='TEMPLATE_DEP'  and t.template_code = d.code " +
            "<if test='list!=null'> AND t.id in " +
            "   <foreach collection='list' item='id' open='(' separator=',' close=')'>",
            "   #{id}",
            "   </foreach>" +
                    "</if>" +
                    "<if test='paramMap.startTime!=null and paramMap.endTime!=null'> AND t.create_time &lt;= #{paramMap.endTime} AND t.create_time &gt;= #{paramMap.startTime} </if>" +
                    "<if test='keyWordId!=null and \"\" neq keyWordId'> AND t.id = #{keyWordId} </if>" +
                    "<if test='paramMap.keyWord!=null and \"\" neq paramMap.keyWord'> AND ( LOCATE(#{paramMap.keyWord},t.name) &gt; 0 OR LOCATE(#{paramMap.keyWord},t.description) &gt; 0 ) </if>" +
                    "<if test='paramMap.templateCode!=null and \"\" neq paramMap.templateCode'> AND t.template_code = #{paramMap.templateCode} </if>" +
                    "<if test='paramMap.online!=null and \"\" neq paramMap.online'> " +
                    "AND ((t.template_code in ${paramMap.streamingTemplateList} and t.status_code  ${paramMap.statusParam} ) or (t.template_code not in ${paramMap.streamingTemplateList} and t.online=#{paramMap.online} ))" +
                    " </if>" +
                    "<if test='paramMap.tenancyCode!=null and \"\" neq paramMap.tenancyCode'> AND t.tenancy_code = #{paramMap.tenancyCode} </if>" +
                    "<if test='paramMap.statusCode!=null and \"\" neq paramMap.statusCode'> AND t.status_code REGEXP REPLACE(#{paramMap.statusCode},',','|') </if>" +
                    "<if test='paramMap.createBy!=null and \"\" neq paramMap.createBy'> AND t.create_by = #{paramMap.createBy} </if>" +
                    "<if test='paramMap.userGroupDetail!=null and \"\" neq paramMap.userGroupDetail'> AND t.user_group = #{paramMap.userGroupDetail} </if>" +
                    "<if test='paramMap.tableName!=null and \"\" neq paramMap.tableName'> " +
                        "AND t.output_guids like concat(concat(\"%\",#{paramMap.tableName}),\"%\") " +
                    "</if>" +
                    "<if test='paramMap.inputGuids!=null and \"\" neq paramMap.inputGuids'> " +
                        "AND t.input_guids like concat(concat(\"%\",#{paramMap.inputGuids}),\"%\") " +
                    "</if>" +
                    "And workflow_id=0  ORDER BY t.update_time DESC" +
                    "</script>"})
    Page<Task> listByMap(@Param("list") List<Integer> list, @Param("keyWordId") Integer keyWordId, @Param("paramMap") Map<String, String> paramMap);

    @Select({"<script>" +
            "SELECT status_code,count(1) as num FROM task t,sys_dict d " +
            "WHERE t.delete_status=0 and t.status_code is not null and d.parent_code !='TEMPLATE_DEP' and t.template_code = d.code  " +
            "<if test='list!=null'> AND t.id in " +
            "   <foreach collection='list' item='id' open='(' separator=',' close=')'>",
            "   #{id}",
            "   </foreach>" +
                    "</if>" +
                    "<if test='paramMap.startTime!=null and paramMap.endTime!=null'> AND t.create_time &lt;= #{paramMap.endTime} AND t.create_time &gt;= #{paramMap.startTime} </if>" +
                    "<if test='keyWordId!=null and \"\" neq keyWordId'> AND t.id = #{keyWordId} </if>" +
                    "<if test='paramMap.keyWord!=null and \"\" neq paramMap.keyWord'> AND ( LOCATE(#{paramMap.keyWord},t.name) &gt; 0 OR LOCATE(#{paramMap.keyWord},t.description) &gt; 0 ) </if>" +
                    "<if test='paramMap.templateCode!=null and \"\" neq paramMap.templateCode'> AND t.template_code = #{paramMap.templateCode} </if>" +
                    "<if test='paramMap.online!=null and \"\" neq paramMap.online'> " +
                    "AND ((t.template_code in ${paramMap.streamingTemplateList} and t.status_code  ${paramMap.statusParam} ) or (t.template_code not in ${paramMap.streamingTemplateList} and t.online=#{paramMap.online} ))" +
                    " </if>" +
                    "<if test='paramMap.tenancyCode!=null and \"\" neq paramMap.tenancyCode'> AND t.tenancy_code = #{paramMap.tenancyCode} </if>" +
                    "<if test='paramMap.statusCode!=null and \"\" neq paramMap.statusCode'> AND t.status_code REGEXP REPLACE(#{paramMap.statusCode},',','|') </if>" +
                    "<if test='paramMap.userGroupDetail!=null and \"\" neq paramMap.userGroupDetail'> AND t.user_group = #{paramMap.userGroupDetail} </if>" +
                    "<if test='paramMap.tableName!=null and \"\" neq paramMap.tableName'> " +
                        "AND t.output_guids like concat(concat(\"%\",#{paramMap.tableName}),\"%\") " +
                    "</if>" +
                    "<if test='paramMap.inputGuids!=null and \"\" neq paramMap.inputGuids'> " +
                        "AND t.input_guids like concat(concat(\"%\",#{paramMap.inputGuids}),\"%\") " +
                    "</if>" +
                    "And workflow_id=0  group by status_code" +
            "</script>"})
    List<Map<String, Object>> taskStatusCount(@Param("list") List<Integer> list, @Param("keyWordId") Integer keyWordId, @Param("paramMap") Map<String, String> paramMap);
    @Select({"SELECT * FROM task WHERE delete_status=1 AND delete_status=0"})
    Page<Task> listByBlankTasks();


    @Select({"select count(*) from task where create_time >= DATE_FORMAT(CURDATE(),'%Y-%m-%d %H:%i:%s')"})
    Integer getNewTaskCount();


    @Select({"SELECT * FROM task WHERE delete_status=0"})
    List<Task> getAll();

    @Select({"<script>" +
            "SELECT t.*,d.`value` as displayTemplateCode FROM task t,sys_dict d   " +
            "WHERE t.delete_status=0 and t.status_code is not null  and t.template_code = d.code " +
            "<if test='list!=null'> AND t.id in " +
            "   <foreach collection='list' item='id' open='(' separator=',' close=')'>",
            "   #{id}",
            "   </foreach>" +
                    "</if>" +
                    "</script>"})
    List<Task> getLast7State(@Param("list") List<String> list);

    @Select({"select count(*) from task "})
    Integer getAccTaskCount();

    /**
     * 累计上线任务数
     *
     * @return
     */
    @Select({"select count(DISTINCT  id) \n" +
            "from task \n" +
            "where delete_status = 0 \n" +
            "and  online = 1 or (status_code in( 'RUNNING' ,'FINISHED') and template_code in('StreamingJAR', 'StreamingSQL', 'Metis2Hive','MysqlCDC2Hive','Db2Hive')     ) \n" +
            "and date_format(release_time, '%Y-%m-%d') <= #{time}"})
    Integer getAccumulativeTasks(@Param("time") String time);


    /**
     * 累计用户数
     *
     * @return
     */
    @Select({"select count(DISTINCT tmp.owner) as num from \n" +
            "(\n" +
            "select JSON_EXTRACT(runtime_config, '$.owner') as owner\n" +
            "from task \n" +
            "where delete_status = 0 \n" +
            "and online = 1 or (status_code in( 'RUNNING' ,'FINISHED') and template_code in('StreamingJAR', 'StreamingSQL', 'Metis2Hive','MysqlCDC2Hive','Db2Hive')     ) \n" +
            "and date_format(release_time, '%Y-%m-%d') <= #{time}\n" +
            ") as tmp"})
    Integer getAccumulativeUser(@Param("time") String time);

    @Select({"<script>" +
            "SELECT * FROM task " +
            "WHERE 1= 1 " +
            "<if test='idList!=null'> AND id in " +
            "   <foreach collection='idList' item='state' open='(' separator=',' close=')'>",
            "   #{state}",
            "   </foreach>" +
                    "</if>" +
                    "</script>"})
    List<Task> queryByIds(@Param("idList") List<Integer> idList);


    /**
     * 获取实时任务非运行中的状态
     *
     * @return
     */
    @Select({"<script>" +
            " select distinct(status_code) as `status_code` " +
            " FROM task " +
            " where 1=1  " +
            "<if test='templateList!=null'> AND template_code in " +
            "   <foreach collection='templateList' item='template' open='(' separator=',' close=')'>",
            "   #{template}",
            "   </foreach>" +
                    "</if>" +
                    "<if test='statusList!=null'> AND status_code not in " +
                    "   <foreach collection='statusList' item='status' open='(' separator=',' close=')'>",
            "   #{status}",
            "   </foreach>" +
                    "</if>" +
                    " </script>"})
    List<String> getStraimgStatus(@Param("templateList") List<String> templateList, @Param("statusList") List<String> statusList);


    @Select({"<script>" +
            "SELECT id, status_code, create_time FROM task " +
            "where  delete_status = 0 and release_time is not null \n" +
            "and online = 1 and !(template_code in('StreamingJAR', 'StreamingSQL', 'Metis2Hive','MysqlCDC2Hive','Db2Hive')     ) \n" +
            "and (date(create_time) &lt; #{end} and date(create_time) &gt;= #{start} ) \n" +
            " <if test='userGroup!=null '> AND user_group = #{userGroup} </if> </script>"})
    List<Task> selectDayOnlinedTasks(@Param("start") String start, @Param("end") String end, @Param("userGroup") String userGroup);

    @Select({"<script>" +
            "SELECT * FROM task " +
            "WHERE  delete_status = 0 and release_time is not null \n" +
            "and online = 1 and !(template_code in('StreamingJAR', 'StreamingSQL', 'Metis2Hive','MysqlCDC2Hive','Db2Hive')     ) \n" +
            " <if test='userGroup!=null '> AND user_group = #{userGroup} </if></script>"})
    List<Task> sumAllOnlinedTasks(@Param("userGroup") String userGroup);

    @Select({"<script>" +
            "select * from task where delete_status = 0 and online =1 \n" +
            "<if test='id!=null and \"\" neq id'> AND id != #{id} </if>" +
            " and JSON_EXTRACT(output_dataset,'$[0].fileName')=#{fileName}\n" +
            " and JSON_EXTRACT(output_dataset,'$[0].location')=#{location} \n" +
            "</script>"})
    List<Task> getTaskBySuccessPath( @Param("id") Integer id, @Param("fileName") String fileName, @Param("location") String location);

    @Select({"<script>" +
            "select * from task where delete_status = 0 \n" +
            " and (JSON_EXTRACT(runtime_config,'$.sourceId')=#{sourceId} \n" +
            " or JSON_EXTRACT(output_dataset,'$.destinationId')=#{destinationId}) \n" +
            "</script>"})
    List<Task> selectTaskBySourceId(@Param("sourceId") String sourceId, @Param("destinationId") Integer destinationId);
}

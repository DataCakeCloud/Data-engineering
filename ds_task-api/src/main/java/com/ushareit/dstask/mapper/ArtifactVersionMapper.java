package com.ushareit.dstask.mapper;

import com.github.pagehelper.Page;
import com.ushareit.dstask.bean.ArtifactVersion;
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
public interface ArtifactVersionMapper extends CrudMapper<ArtifactVersion> {
    /**
     * 根据MAP参数查询
     *
     * @param paramMap paramMap
     * @return
     */
    @Override
    @Select({"<script>" +
            "SELECT * FROM artifact_version " +
            "WHERE 1=1 " +
            "<if test='paramMap.artifactId!=null'> AND artifact_id = #{paramMap.artifactId} </if> " +
            " ORDER BY version DESC" +
            "</script>"})
    Page<ArtifactVersion> listByMap(@Param("paramMap") Map<String, String> paramMap);

    /**
     * jar list
     *
     * @param artifactVersion
     * @return
     */
    @Override
    @Select({"<script>" +
            "SELECT * FROM artifact_version WHERE 1=1 " +
            "<if test='artifactVersion.artifactId!=null'> AND artifact_id = #{artifactVersion.artifactId} </if> " +
            "<if test='artifactVersion.region!=null'> AND region = #{artifactVersion.region} </if> " +
            " ORDER BY version DESC" +
            "</script>"})
    List<ArtifactVersion> select(@Param("artifactVersion") ArtifactVersion artifactVersion);


    @Select({"SELECT * FROM artifact_version WHERE artifact_id = #{artifactVersion.artifactId} " +
            " AND region = #{artifactVersion.region} ORDER BY version DESC"})
    List<ArtifactVersion> selectByIdAndRegion(@Param("artifactVersion") ArtifactVersion artifactVersion);

    /**
     * 根据应用ID查询最大版本号
     *
     * @param id
     * @return
     */
    @Select("select max(version) from artifact_version where artifact_id=#{id} ")
    Integer getMaxVersionById(@Param("id") Integer id);

    /**
     * 通过工件id查询最大的版本
     *
     * @param artifactId
     * @return
     */
    @Select({"<script>" +
            "SELECT * FROM artifact_version" +
            " WHERE 1=1  " +
            "<if test='region!=null'> AND region = #{region} </if> " +
            "<if test='artifactId!=null'> AND artifact_id = #{artifactId} </if> " +
            " ORDER BY version DESC " +
            "</script>"})
    List<ArtifactVersion> getMaxVersionByArtifactId(@Param("artifactId") Integer artifactId, @Param("region") String region);

//    /**
//     * 通过工件id查询最大的版本
//     *
//     * @param artifactId
//     * @return
//     */
//    @Select({"<script>" +
//            "SELECT * FROM artifact_version" +
//            " WHERE 1=1  " +
//            "<if test='artifactId!=null'> AND artifact_id = #{artifactId} </if> " +
//            " ORDER BY version DESC limit 1" +
//            "</script>"})
//    ArtifactVersion getMaxVersionByArtifactId(@Param("artifactId") Integer artifactId);

    @Select({"select count(*) from artifact_version where create_time >= DATE_FORMAT(CURDATE(),'%Y-%m-%d %H:%i:%s')"})
    Integer getNewArtifactVersionCount();

    /**
     * 根据多个工件版本id，批量查询工件版本
     *
     * @param artifactId
     * @param artifactVersionId
     * @return
     */
    @Select({"<script>" +
            "SELECT * FROM artifact_version " +
            "WHERE 1=1 " +
            " AND artifact_id = #{artifactId} " +
            "<if test='artifactVersionId!=-1'> AND id = #{artifactVersionId} </if> " +
            "<if test='artifactVersionId==-1'> order by id desc limit 1 </if> " +
            "</script>"})
    ArtifactVersion selectByArtiIdAndArtiVersionId(@Param("artifactId") Integer artifactId, @Param("artifactVersionId") Integer artifactVersionId);

    @Select({"select count(*) from artifact_version "})
    Integer getAccArtifactVersionCount();
}

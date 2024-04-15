package com.ushareit.dstask.out;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.ushareit.dstask.common.vo.cost.CostDictionaryVo;
import com.ushareit.dstask.common.vo.cost.CostJobVo;
import com.ushareit.dstask.common.vo.cost.CostRequestVo;
import com.ushareit.dstask.common.vo.cost.CostResponseVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
@DS("outer")
public interface CostMapper  {
    List<CostResponseVo> selectCost(@Param("vo") CostRequestVo costRequestVo);
    List<CostResponseVo> selectCostDp(@Param("vo") CostRequestVo costRequestVo);
    List<CostResponseVo> selectCostPu(@Param("vo") CostRequestVo costRequestVo);
    List<CostResponseVo> selectCumulativeCost(@Param("vo") CostRequestVo costRequestVo);
    List<CostResponseVo> selectCumulativeCostDp(@Param("vo") CostRequestVo costRequestVo);
    List<String> selectPuDepartment(@Param("vo") CostRequestVo costRequestVo);
    List<CostResponseVo> selectDepartmentAndPu(@Param("vo") CostRequestVo costRequestVo);
    List<CostResponseVo> selectNewJob(@Param("vo") CostRequestVo costRequestVo);
    List<String> selectDepartment(@Param("vo")CostDictionaryVo costDictionaryVo);
    List<String> selectJobs(@Param("vo")CostDictionaryVo costDictionaryVo);
    List<String> selectRegions(@Param("vo")CostDictionaryVo costDictionaryVo);
    List<String> selectProducts(@Param("vo")CostDictionaryVo costDictionaryVo);
    List<String> selectOwners(@Param("vo")CostDictionaryVo costDictionaryVo);
    List<CostResponseVo> selectJobOwner();
    void deleteJobDetail();
    void insertBatchJobDetail(@Param("list") List<CostResponseVo> costResponseVoList);
}

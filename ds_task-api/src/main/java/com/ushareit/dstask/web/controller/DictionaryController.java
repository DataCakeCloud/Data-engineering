package com.ushareit.dstask.web.controller;

import com.ushareit.dstask.bean.Dictionary;
import com.ushareit.dstask.common.vo.DictionaryNameVO;
import com.ushareit.dstask.service.BaseService;
import com.ushareit.dstask.service.DictionaryService;
import com.ushareit.dstask.web.vo.BaseResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
@Api(tags = "字典管理")
@RestController
@RequestMapping("/dict")
public class DictionaryController extends BaseBusinessController<Dictionary> {

    @Autowired
    private DictionaryService dictionaryService;

    @Override
    public BaseService<Dictionary> getBaseService() {
        return dictionaryService;
    }

    @ApiOperation(value = "字典名模糊查询")
    @GetMapping("name/search")
    public BaseResponse<List<DictionaryNameVO>> search(String keywords) {
        return BaseResponse.success(dictionaryService.searchByName(keywords));
    }
}

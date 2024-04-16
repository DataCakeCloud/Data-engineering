package com.ushareit.dstask.web.controller;

import com.ushareit.dstask.bean.*;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.service.AccessTenantService;
import com.ushareit.dstask.service.BaseService;
import com.ushareit.dstask.web.vo.BaseResponse;
import io.swagger.annotations.*;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.Map;

/**
 * @author wuyan
 * @date 2022/4/7
 */
@Api(tags = "租户管理")
@RestController
@RequestMapping("/tenant")
public class AccessTenantController extends BaseBusinessController<AccessTenant> {
    @Resource
    private AccessTenantService accessTenantService;

    @Override
    public BaseService getBaseService() {
        return accessTenantService;
    }


    @Override
    @ApiResponses({@ApiResponse(code = 200, response = BaseResponse.class, message = "成功")})
    @PostMapping("/add")
    public BaseResponse<?> add(@RequestBody @Valid AccessTenant accessTenant) {
        // 添加user信息
        accessTenant.setCreateTime(new Timestamp(System.currentTimeMillis()));
        accessTenant.setCreateBy(getCurrentUser().getUserName());
        accessTenant.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        accessTenant.setUpdateBy(getCurrentUser().getUserName());

        AccessTenant save = (AccessTenant) accessTenantService.save(accessTenant);
        accessTenantService.insertAdminUser(accessTenant, save);
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS, save);
    }

    @ApiOperation(value = "冻结")
    @GetMapping("/freeze")
    public BaseResponse freeze(@RequestParam("id") Integer id, @RequestParam("freeze") Integer freeze) throws Exception {
        accessTenantService.freeze(id, freeze);
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS);
    }

    @ApiOperation(value = "配置服务")
    @GetMapping("/config")
    public BaseResponse config(@RequestParam("id") Integer id, @RequestParam("productIds") String productIds) throws Exception {
        accessTenantService.config(id, productIds);
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS);
    }

    @ApiOperation(value = "新建角色时列出当期用户所在的租户接口")
    @GetMapping("/current")
    public BaseResponse current(@RequestParam("tenantId") Integer tenantId) throws Exception {
        AccessTenant current = accessTenantService.current(tenantId);
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS, current);
    }

    @ApiOperation(value = "获取aksk")
    @GetMapping("/aksk")
    public BaseResponse getAkSk() {
        AkSk aksk = accessTenantService.getAkSk(getCurrentUser());
        AkSkResponse akSkResponse = new AkSkResponse();
        BeanUtils.copyProperties(aksk, akSkResponse);
        return BaseResponse.success(akSkResponse);
    }

    @ApiOperation(value = "更新aksk")
    @PostMapping("/aksk/update")
    public BaseResponse updateAkSk(@RequestBody @Valid AkSkRequest akskRequest) {
        AkSk akSk = accessTenantService.updateAkSk(akskRequest, getCurrentUser());
        AkSkResponse akSkResponse = new AkSkResponse();
        BeanUtils.copyProperties(akSk, akSkResponse);
        return BaseResponse.success(akSkResponse);
    }

    @ApiOperation(value = "aksk生成的租户token")
    @PostMapping("/aksk/token")
    public BaseResponse generateAkSkToken(@RequestBody @Valid AkSkTokenRequest akSkTokenRequest) throws UnsupportedEncodingException {
        String token = accessTenantService.generateAkSkToken(akSkTokenRequest);
        if (token == null) {
            return BaseResponse.error(BaseResponseCodeEnum.AK_SK_INVALID);
        }
        return BaseResponse.success(token);
    }

    @ApiOperation(value = "生成个人token")
    @PostMapping("/aksk/personalToken")
    public BaseResponse generateAkSkPersonalToken(@RequestBody @Valid AkSkTokenRequest akSkTokenRequest) throws UnsupportedEncodingException {
        String token = accessTenantService.generateAkSkPersonalToken(akSkTokenRequest, getCurrentUser());
        return BaseResponse.success(token);
    }

    @ApiOperation(value = "获取个人token")
    @GetMapping("/aksk/personalToken/info")
    public BaseResponse getAkSkPersonalToken() {
        AkSkResponse akSkResponse = accessTenantService.getAkSkPersonalToken(getCurrentUser());
        return BaseResponse.success(akSkResponse);
    }

    @ApiOperation(value = "获取用户信息")
    @GetMapping("/aksk/userInfo")
    public BaseResponse getUserInfo(HttpServletRequest request) {
        String token = request.getHeader("datacake_token");
        Map<String, Object> user = accessTenantService.getUserInfo(token);
        return BaseResponse.success(user);
    }
}

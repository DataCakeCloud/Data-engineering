package com.ushareit.dstask.web.controller;

import com.ushareit.dstask.bean.ShortUrlEntity;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.mapper.ShortUrlMapper;
import com.ushareit.dstask.web.utils.IdUtils;
import com.ushareit.dstask.web.vo.BaseResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@Controller
public class ShortUrlController {

    @Autowired
    private ShortUrlMapper shortUrlMapper;

    /**
     * 生成短链
     *
     * @param realUrl
     * @return
     */
    @RequestMapping("/short/create")
    @ResponseBody
    public BaseResponse createShortUrl(String realUrl) {
        if (StringUtils.isNoneBlank(realUrl)) {
            ShortUrlEntity shortUrlEntity = shortUrlMapper.selectByRealUrl(realUrl);
            if (shortUrlEntity != null) {
                return BaseResponse.success(shortUrlEntity.getUrlId());
            }
            shortUrlEntity = new ShortUrlEntity();
            shortUrlEntity.setUrlId(IdUtils.getLenthId(6));
            shortUrlEntity.setRealUrl(realUrl);
            shortUrlEntity.setCreateTime(new Date());
            shortUrlMapper.insert(shortUrlEntity);
            return BaseResponse.success("/api/" + shortUrlEntity.getUrlId());
        }
        return BaseResponse.error("204", "请输入有效的链接");
    }

    /**
     * 短链的请求
     *
     * @param urlId
     * @return
     */
    @RequestMapping("/api/{urlId}")
    public String redirect(@PathVariable("urlId") String urlId) {
        ShortUrlEntity shortUrlEntity = shortUrlMapper.selectByUrlId(urlId);
        if (shortUrlEntity != null) {
            if (shortUrlEntity.getRealUrl().startsWith("/")) {
                return "forward:" + shortUrlEntity.getRealUrl();
            }
            return "forward:/" + shortUrlEntity.getRealUrl();
        }
        return "forward:/api/404";
    }

    /**
     * 短链的请求
     *
     * @param
     * @return
     */
    @RequestMapping("/api/404")
    @ResponseBody
    public BaseResponse forward404() {
        return BaseResponse.error(BaseResponseCodeEnum.BAD_SHORTURL);
    }

}

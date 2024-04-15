package com.ushareit.dstask.web.utils;


import com.ushareit.dstask.bean.AccessUser;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.service.AccessUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.List;


/**
 * @ClassName EmailUtil
 * author xuebotao
 * date 2022-11-14
 */
@Component
public class EmailUtils {

    private static EmailUtils emailUtils;

    @Value("${spring.mail.from}") // 从application.yml配置文件中获取
    private String from;

    @Value("${spring.mail.password}")
    private String password;

    @Resource
    private AccessUserService accessUserService;

    @Autowired
    private JavaMailSender mailSender;

    @PostConstruct
    public void init() {
        emailUtils = this;
        emailUtils.accessUserService = this.accessUserService;
        emailUtils.mailSender = this.mailSender;
    }

    public void sendMessage(String receiver, String topic, String content) {
        MimeMessage mimeMessage = emailUtils.mailSender.createMimeMessage();
        MimeMessageHelper msg = null;
        try {
            msg = new MimeMessageHelper(mimeMessage, true, "GBK");
            // 创建一个邮件对象
            msg.setFrom(from); // 设置发送发
            msg.setTo(receiver); // 设置接收方
            msg.setSubject(topic); // 设置邮件主题
            msg.setText(content, true); // 设置邮件内容
            // 发送邮件
            emailUtils.mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new ServiceException(BaseResponseCodeEnum.USER_EMAIL_ERROR, e);
        }

    }

    public String getFrom() {
        return from;
    }

    public String getPassword(){
        return password;
    }

    public void setFrom(String from) {
        this.from = from;
    }


    public void sendMessage(List<String> shareIdList, String topic, String message) {
        List<AccessUser> accessUsers = emailUtils.accessUserService.selectByNames(shareIdList);
        for (AccessUser accessUser : accessUsers) {
            sendMessage(accessUser.getEmail(), topic, message);
        }
    }

}


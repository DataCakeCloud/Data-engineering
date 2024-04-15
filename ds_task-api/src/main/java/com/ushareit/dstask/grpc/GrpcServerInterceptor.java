package com.ushareit.dstask.grpc;

import com.ushareit.dstask.bean.AccessTenant;
import com.ushareit.dstask.bean.CurrentUser;
import com.ushareit.dstask.constant.CommonConstant;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.service.AccessTenantService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.utils.ProtoUtils;
import com.ushareit.dstask.web.utils.UuidUtil;
import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author fengxiao
 * @date 2022/12/26
 */
@Slf4j
@GrpcGlobalServerInterceptor
public class GrpcServerInterceptor implements ServerInterceptor {

    @Autowired
    private AccessTenantService accessTenantService;

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers,
                                                                 ServerCallHandler<ReqT, RespT> next) {
        ServerCall<ReqT, RespT> listener = new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {

            @Override
            public void sendMessage(RespT message) {
                log.info("invoke method {} by user {}, result is {}", call.getMethodDescriptor().getFullMethodName(),
                        InfTraceContextHolder.get().getUserInfo(), ProtoUtils.print(message));
                super.sendMessage(message);
            }
        };

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(next.startCall(listener, headers)) {

            @Override
            public void onMessage(ReqT message) {
                String userBase64Str = headers.get(Metadata.Key.of(CommonConstant.CURRENT_LOGIN_USER, Metadata.ASCII_STRING_MARSHALLER));
                CurrentUser currentUser = CurrentUser.parse(userBase64Str);
                if (currentUser != null && currentUser.getTenantId() != null) {
                    AccessTenant accessTenant = accessTenantService.checkExist(currentUser.getTenantId());
                    InfTraceContextHolder.get().setUserInfo(currentUser);
                    InfTraceContextHolder.get().setTenantId(currentUser.getTenantId());
                    InfTraceContextHolder.get().setTenantName(accessTenant.getName());
                }

                String traceId = UuidUtil.getUuid32();

                // put slf4j mdc
                MDC.put(DsTaskConstant.LOG_TRACE_ID, traceId);

                log.debug("invoke method {} by user {}, param is {}", call.getMethodDescriptor().getFullMethodName(),
                        InfTraceContextHolder.get().getUserInfo(), ProtoUtils.print(message));
                super.onMessage(message);
            }
        };
    }


}

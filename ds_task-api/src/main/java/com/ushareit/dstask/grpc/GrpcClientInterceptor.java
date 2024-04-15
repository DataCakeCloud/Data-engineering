package com.ushareit.dstask.grpc;

import com.google.common.io.BaseEncoding;
import com.ushareit.dstask.bean.CurrentUser;
import com.ushareit.dstask.constant.CommonConstant;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.utils.ProtoUtils;
import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor;

/**
 * @author fengxiao
 * @date 2022/12/4
 */
@Slf4j
@GrpcGlobalClientInterceptor
public class GrpcClientInterceptor implements ClientInterceptor {

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
                                                               CallOptions callOptions, Channel channel) {
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
                channel.newCall(method, callOptions)) {
            final long start = System.currentTimeMillis();

            @Override
            public void sendMessage(ReqT message) {
                //log.info("invoke method {} with user info {} and params {}", method.getFullMethodName(),
                //        InfTraceContextHolder.get().getUserInfo(), ProtoUtils.print(message));
                super.sendMessage(message);
            }

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                CurrentUser currentUser = InfTraceContextHolder.get().getUserInfo();
                if (currentUser != null) {
                    headers.put(Metadata.Key.of(CommonConstant.CURRENT_LOGIN_USER, Metadata.ASCII_STRING_MARSHALLER),
                            BaseEncoding.base64().encode(ProtoUtils.print(currentUser.toUserInfo()).getBytes()));
                }

                super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
                    @Override
                    public void onMessage(RespT message) {
                        //log.info("invoke method {} by user {}, response is {}", method.getFullMethodName(), currentUser,
                        //        ProtoUtils.print(message));
                        super.onMessage(message);
                    }

                    @Override
                    public void onClose(Status status, Metadata trailers) {
                        GrpcMetricUtils.count(method.getFullMethodName(), status);
                        GrpcMetricUtils.grpcCostHistogram(method.getFullMethodName(), System.currentTimeMillis() - start);
                        if (status.getCode() != Status.OK.getCode()) {
                            log.error("invoke method {} by user {}, status code is {}, exception is {}",
                                    method.getFullMethodName(), currentUser, status.getCode(), status.getDescription());
                        }
                        super.onClose(status, trailers);
                    }

                }, headers);


            }
        };
    }
}

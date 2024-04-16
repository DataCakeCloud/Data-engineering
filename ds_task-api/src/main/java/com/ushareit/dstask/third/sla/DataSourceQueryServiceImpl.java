package com.ushareit.dstask.third.sla;

import com.fasterxml.jackson.databind.JsonNode;
import com.ushareit.dstask.api.DataSourceQueryApi;
import com.ushareit.dstask.api.DataSourceQueryServiceGrpc;
import com.ushareit.dstask.bean.Actor;
import com.ushareit.dstask.service.ActorService;
import com.ushareit.dstask.third.airbyte.connector.MySQLConnector;
import com.ushareit.dstask.third.airbyte.json.Jsons;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author fengxiao
 * @date 2022/9/21
 */
@Slf4j
@GrpcService
public class DataSourceQueryServiceImpl extends DataSourceQueryServiceGrpc.DataSourceQueryServiceImplBase {

    @Autowired
    private ActorService actorService;
    @Autowired
    private MySQLConnector mySQLConnector;

    @Override
    public void query(DataSourceQueryApi.QueryRequest request, StreamObserver<DataSourceQueryApi.QueryResponse> responseObserver) {
        DataSourceQueryApi.QueryResponse.Builder responseBuilder = DataSourceQueryApi.QueryResponse.newBuilder();
        responseBuilder.setCode(NumberUtils.INTEGER_ZERO);
        try {
            Actor actor = actorService.getById(request.getActorId());
            if (actor == null) {
                throw new RuntimeException(String.format("数据实例不存在，actorId = %s", request.getActorId()));
            }

            switch (request.getSourceType()) {
                case clickhouse:
                case mysql:
                    List<DataSourceQueryApi.Item> resultList = mySQLConnector.query(Jsons.deserialize(actor.getConfiguration()),
                            request.getSourceType().name(), request.getSql(), request.getQueryContext());
                    responseBuilder.addAllResult(resultList);
                    break;
            }
        } catch (Exception e) {
            responseBuilder.setCode(NumberUtils.INTEGER_MINUS_ONE);
            responseBuilder.setMessage(e.getMessage());
            log.error(e.getMessage(), e);
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getInfo(DataSourceQueryApi.ActorRequest request, StreamObserver<DataSourceQueryApi.ClusterResponse> responseObserver) {
        DataSourceQueryApi.ClusterResponse.Builder responseBuilder = DataSourceQueryApi.ClusterResponse.newBuilder();
        responseBuilder.setCode(NumberUtils.INTEGER_ZERO);
        try {
            Actor actor = actorService.getById(request.getActorId());
            if (actor == null) {
                throw new RuntimeException(String.format("数据实例不存在，actorId = %s", request.getActorId()));
            }

            JsonNode jsonNode = Jsons.deserialize(actor.getConfiguration());
            responseBuilder.setClusterInfo(DataSourceQueryApi.ClusterInfo.newBuilder()
                    .setHost(String.format("%s:%s", jsonNode.get("host").asText(), jsonNode.get("port").asText()))
                    .setUsername(jsonNode.get("username").asText())
                    .setPassword(jsonNode.has("password") ? jsonNode.get("password").asText() : null)
                    .build());

        } catch (Exception e) {
            responseBuilder.setCode(NumberUtils.INTEGER_MINUS_ONE);
            responseBuilder.setMessage(e.getMessage());
            log.error(e.getMessage(), e);
        }
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }
}

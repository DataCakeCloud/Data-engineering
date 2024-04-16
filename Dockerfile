# stage1 add flink-1.11-scala_2.11
# FROM flink:1.11.1-scala_2.11 as flink
FROM swr.ap-southeast-3.myhuaweicloud.com/shareit-cbs/sflink:dc75951 as flink

#stage2 runtime image
FROM swr.ap-southeast-3.myhuaweicloud.com/shareit-common/java-base:oracle-jdk-1.8.0_202

RUN sudo mkdir -p /data/flink/          && \
    sudo mkdir -p /data/code/k8s/          && \
    sudo mkdir -p /data/code/sql/          && \
    sudo chown -R ${AppUser}:${AppGroup} /data

COPY --from=flink /opt/flink/ /data/flink/

#安装 kubectl 客户端
RUN cd /tmp           && \
    curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl" && \
    sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl

#从build_image.sh传进来工程目录
ARG PROJECT_NAME

# copy run jar file
COPY ${PROJECT_NAME}/target/*.jar /data/code/
COPY ${PROJECT_NAME}/run.sh  /data/code/run.sh
COPY ${PROJECT_NAME}/kubernetes_application.sh  /data/code/kubernetes_application.sh
COPY ${PROJECT_NAME}/k8s/*  /data/code/k8s/
COPY ${PROJECT_NAME}/flink-conf.yaml  /data/flink/conf/flink-conf.yaml
COPY ${PROJECT_NAME}/log4j.properties  /data/flink/conf/log4j.properties
COPY doc/sql/*.sql  /data/code/sql/

RUN sudo chmod o+x /data/code/run.sh    && \
    sudo chmod o+x /data/code/kubernetes_application.sh    && \
    sudo mkdir -p /data/logs/gc         && \
    sudo chown -R ${AppUser}:${AppGroup} /data

# set workdir
WORKDIR /data
# 在环境中加一标记 如果是fast deployment就走 fast.run脚本
CMD ["/bin/bash","-c","bash /data/code/run.sh start $Env"]

# 在一键部署中使用下面的sh
#CMD ["/bin/bash","-c","bash /data/code/run_fast_deployment.sh start $Env"]

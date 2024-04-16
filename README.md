# DataStudio

## 介绍
```
茄子大数据体系平台众多、功能繁杂、任务调度编程复杂、数据处理低效等问题已经在一定程度影响公司的产研迭代及业务发展效率。借此，希望能够统一各大数据平台及工具，提供全链路一体化大数据解决方案，【数据开发平台-DataStudio】产品应运而生。
```

## 系统定位
```
【数据开发平台-DataPipeline】以全链路一体化原则，提供一站式大数据开发管理平台，可以一站式完成数据治理、数据开发 、数据服务。
```
## 其核心功能包括：
*  统一的数仓规划及建模
*  *  提供数据组织、模型及字典管理等功能，建立统一规范、统一数据口径。
*  统一的元数据管理功能
*  *  多云、多区域、多存储目标的元数据汇聚及管理
*  *  可通过全文检索的方式完成数据字段级别的查询，通过图形化界面提供表、字段级别的详情分析、血缘关系、数据预览等功能。
*  多源异构数据同步
*  *  包括HTTP\File\Kafka\对象存储\RDS\NoSql\MPP数仓等数据源的实时、离线、存量、增量同步。
*  实时\离线SQL任务的在线编辑、测试、提交、运维、监控等
*  *  通过SQL的方式统一批流编程模型。
*  支持提交Flink、Spark任务来完成复杂逻辑的处理
*  *  支持通过界面化完成运行资源配置及参数调优。同时，提供完善的运维管理功能，包括生命周期、运行状态、实时监控等。
*  以画布方式提供DAG的任务调度的配置【待定】
*  *  支持画布到编码的相互转换，提供任务预警、告警功能。
*  完善的数据质量检测、通知、调度阻断
*  *  提供数据质量的规则配置、在线测试及指标、值的监控检查以及非预期的预警、告警
*  高效便捷的数据API生成及生命周期管理
*  *  数据服务将以界面配置的方式提供快速将数据表生成数据API的能力，完成对API的统一管理和发布
*  完善的数据安全管理控制
*  *  字段级别的数据访问控制，提供统一数据权限管控功能和可视化的申请、审批流程。

## 相关文档
```
产品原型
https://6g1hlm.axshare.com

功能架构
https://www.processon.com/view/link/604b4ae71e08537ac5bf58f4#outline

PRD
https://wiki.ushareit.me/pages/viewpage.action?pageId=46979039

```

## Building 

* Git
* Maven (recommend version 3.2.5 and require at least 3.1.1)
* Java 8 or 11 (Java 9 or 10 may work)

```bash
git clone git@gitlab.ushareit.me:cbs/stream/ds_task.git
cd ds_task
mvn clean package -DskipTests # this will take up to 3 minutes

```

##切换一键部署注意
1.修改dockerfile CMD ["/bin/bash","-c","bash /data/code/run_fast_deployment.sh start $Env"]
2.修改 application.yml  datacake.external-role:  false 为ture 
3.修改常量 CommonConstant下 INSIDE_SUPPER_TENANT_NAME 为admin租户
4.修改application.yml  datacake:namespace: datacake
create table if not exists ds_task_${tenantName}.access_group
(
	id int auto_increment comment '主键'
		primary key,
	name varchar(128) null comment '名称',
	e_name varchar(128) null comment '部门别名',
	director varchar(128) null comment '负责人',
	tenant_id int null comment '租户id',
	parent_id int null comment '父id',
	user_id int null comment '用户id',
	type int(4) default 0 null comment '0是组，1是人',
	hierarchy int(4) default 1 null comment '层级',
	is_leader int(4) null comment '0:是 ，1：不是',
	description varchar(512) null comment '备注',
	delete_status varchar(1) default '0' not null comment '是否删除 0：未删除；1：已删除',
	CREATE_BY varchar(32) not null comment '创建人',
	CREATE_TIME datetime default CURRENT_TIMESTAMP not null comment '创建时间',
	UPDATE_BY varchar(32) null comment '更新人',
	UPDATE_TIME datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
comment 'group表 ' charset=utf8;

create table if not exists ds_task_${tenantName}.access_menu
(
	id int auto_increment comment '主键'
		primary key,
	code varchar(128) null comment '代码',
	name varchar(128) null comment '名称',
	level varchar(1) null comment '级别',
	parent_menu_id int null comment '父id',
	description varchar(512) null comment '备注',
	url varchar(512) null comment '链接',
	delete_status varchar(1) default '0' not null comment '是否删除 0：有效；1：无效',
	CREATE_BY varchar(32) not null comment '创建人',
	CREATE_TIME datetime default CURRENT_TIMESTAMP not null comment '创建时间',
	UPDATE_BY varchar(32) null comment '更新人',
	UPDATE_TIME datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
	valid varchar(1) default '0' null,
	product_id int default 1 null
)
comment '菜单表 ' charset=utf8;

create table if not exists ds_task_${tenantName}.access_product
(
	id int auto_increment comment '产品id'
		primary key,
	name varchar(128) null comment '产品名称',
	description varchar(512) null comment '备注',
	delete_status varchar(1) default '0' not null comment '是否删除 0：有效；1：无效',
	CREATE_BY varchar(32) not null comment '创建人',
	CREATE_TIME datetime default CURRENT_TIMESTAMP not null comment '创建时间',
	UPDATE_BY varchar(32) null comment '更新人',
	UPDATE_TIME datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
comment '产品表 ' charset=utf8;

create table if not exists ds_task_${tenantName}.access_role
(
	id int auto_increment comment '主键'
		primary key,
	name varchar(128) null comment '名称',
	description varchar(512) null comment '备注',
	delete_status varchar(1) default '0' not null comment '是否删除 0：未删除；1：已删除',
	CREATE_BY varchar(32) not null comment '创建人',
	CREATE_TIME datetime default CURRENT_TIMESTAMP not null comment '创建时间',
	UPDATE_BY varchar(32) null comment '更新人',
	UPDATE_TIME datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
comment '角色表 ' charset=utf8;

create table if not exists ds_task_${tenantName}.access_role_menu
(
	id int auto_increment comment '主键'
		primary key,
	role_id int not null comment '角色id',
	menu_id varchar(512) null comment '菜单id',
	CREATE_BY varchar(32) not null comment '创建人',
	CREATE_TIME datetime default CURRENT_TIMESTAMP not null comment '创建时间',
	UPDATE_BY varchar(32) null comment '更新人',
	UPDATE_TIME datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
comment '角色与菜单表 ' charset=utf8;

create table if not exists ds_task_${tenantName}.access_user
(
	id int auto_increment comment '主键'
		primary key,
	name varchar(128) null comment '名称',
	email varchar(128) null comment '用户邮箱',
	password varchar(1000) null comment '用户密码',
	description text null comment '描述',
	company_id varchar(128) null comment '企业id',
	tenant_id int not null comment '租户id',
	tenancy_code varchar(128) null comment '企业部门',
	source varchar(128) null comment '来源',
	latest_code varchar(128) null comment '最新验证码',
	mfa_secret varchar(128) null comment 'MFA秘钥',
	is_bindmfa varchar(1) default '1' null comment '1是没绑定 0是绑定',
	freeze_status varchar(1) default '0' not null comment '是否删除 0：启动；1：冻结',
	delete_status varchar(1) default '0' not null comment '是否删除 0：未删除；1：已删除',
	CREATE_BY varchar(32) not null comment '创建人',
	CREATE_TIME datetime default CURRENT_TIMESTAMP not null comment '创建时间',
	UPDATE_BY varchar(32) null comment '更新人',
	UPDATE_TIME datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
	org text null,
    `phone` varchar(255) DEFAULT NULL COMMENT '手机号',
    `we_chat_id` varchar(255) DEFAULT NULL COMMENT '企业微信id',
    `groups` varchar(255) DEFAULT NULL COMMENT '用户组',
    `e_name` varchar(255) DEFAULT NULL
)
comment '用户表 ' charset=utf8;

create table if not exists ds_task_${tenantName}.access_user_group
(
	id int auto_increment comment '主键'
		primary key,
	user_id int not null comment '用户id',
	group_id varchar(512) null comment '角色id',
	CREATE_BY varchar(32) not null comment '创建人',
	CREATE_TIME datetime default CURRENT_TIMESTAMP not null comment '创建时间',
	UPDATE_BY varchar(32) null comment '更新人',
	UPDATE_TIME datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
comment '用户与group表 ' charset=utf8;

create table if not exists ds_task_${tenantName}.access_user_role
(
	id int auto_increment comment '主键'
		primary key,
	user_id int not null comment '用户id',
	role_id varchar(512) null comment '角色id',
	CREATE_BY varchar(32) not null comment '创建人',
	CREATE_TIME datetime default CURRENT_TIMESTAMP not null comment '创建时间',
	UPDATE_BY varchar(32) null comment '更新人',
	UPDATE_TIME datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
comment '用户与角色表 ' charset=utf8;

create table if not exists ds_task_${tenantName}.access_tenant_group(
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` int(11) NOT NULL COMMENT '租户id',
  `CREATE_BY` varchar(32) NOT NULL COMMENT '创建人',
  `CREATE_TIME` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `UPDATE_BY` varchar(32) DEFAULT NULL COMMENT '更新人',
  `UPDATE_TIME` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `group_id` int(11) DEFAULT NULL COMMENT 'goup id',
  PRIMARY KEY (`id`)
)
comment '租户与group表 ' charset=utf8;

create table if not exists ds_task_${tenantName}.access_tenant_product
(
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` int(11) NOT NULL COMMENT '租户id',
  `CREATE_BY` varchar(32) NOT NULL COMMENT '创建人',
  `CREATE_TIME` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `UPDATE_BY` varchar(32) DEFAULT NULL COMMENT '更新人',
  `UPDATE_TIME` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `product_id` int(11) DEFAULT NULL COMMENT '产品id',
  PRIMARY KEY (`id`)
)
comment '租户与产品表 ' charset=utf8;


create table if not exists ds_task_${tenantName}.access_tenant_role
(
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` int(11) NOT NULL COMMENT '租户id',
  `CREATE_BY` varchar(32) NOT NULL COMMENT '创建人',
  `CREATE_TIME` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `UPDATE_BY` varchar(32) DEFAULT NULL COMMENT '更新人',
  `UPDATE_TIME` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `role_id` int(11) DEFAULT NULL COMMENT '产品id',
  PRIMARY KEY (`id`)
)
comment '租户与角色表 ' charset=utf8;


create table if not exists ds_task_${tenantName}.account
(
	id int auto_increment comment '主键'
		primary key,
	user_group varchar(256) null comment '用户组',
	username varchar(256) null comment '用户名',
	password varchar(256) null comment '密码',
	create_by varchar(32) not null comment '创建人',
	create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
	update_by varchar(32) null comment '更新人',
	update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
comment '公共账号表' charset=utf8;

create table if not exists ds_task_${tenantName}.accumulate_online_task
(
	id int auto_increment comment '主键'
		primary key,
	date varchar(128) null comment '日期',
	num int not null comment '任务数量'
)
comment '累计上线任务' charset=utf8;

create table if not exists ds_task_${tenantName}.accumulate_user
(
	id int auto_increment comment '主键'
		primary key,
	date varchar(128) null comment '日期',
	num int not null comment '用户数量'
)
comment '累计用户数' charset=utf8;

create table if not exists ds_task_${tenantName}.actor
(
	id int auto_increment
		primary key,
	actor_definition_id int default 0 null comment '数据源定义ID',
	name varchar(255) default '' null comment '数据源实例名字',
	region varchar(200) default '' null comment 'region 信息 ue1-aws美东 sg1-aws新加坡 sg2-华为新加坡',
	configuration longtext not null comment '数据源参数配置',
	actor_type varchar(100) default '' null comment '角色类别 source/destination',
	description varchar(2000) default '' null comment '附件描述',
    groups varchar(2000) default '' null comment '部门信息',
	delete_status varchar(1) default '0' null comment '是否删除 0：未删除；1：已删除',
	create_by varchar(32) default '' null comment '创建人',
	create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
	update_by varchar(32) null comment '更新人',
	`uuid` varchar(100) DEFAULT NULL COMMENT '生成uuid用来兼容catalog',
	update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
comment '数据源实例表' charset=utf8mb4;

create table if not exists ds_task_${tenantName}.actor_share (
  `id` int(18) unsigned zerofill NOT NULL AUTO_INCREMENT,
  `actor_id` int(18) DEFAULT NULL,
  `share_id` varchar(100) COLLATE utf8mb4_bin DEFAULT NULL,
  `name` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `type` int(2) DEFAULT '1',
  PRIMARY KEY (`id`)
) comment '数据源实例分享表' DEFAULT CHARSET=utf8mb4 ;

create table if not exists ds_task_${tenantName}.actor_catalog
(
	id int auto_increment
		primary key,
	actor_id int default 0 null comment '数据源实例 ID',
	catalog longtext not null comment '数据源目录',
	catalog_hash varchar(100) default '' null comment '数据目录hash',
	description varchar(2000) default '' null comment '附件描述',
	delete_status varchar(1) default '0' null comment '是否删除 0：未删除；1：已删除',
	create_by varchar(32) default '' null comment '创建人',
	create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
	update_by varchar(32) null comment '更新人',
	update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
comment '数据源目录表' charset=utf8mb4;

create table if not exists ds_task_${tenantName}.actor_definition
(
	id int auto_increment
		primary key,
	name varchar(512) default '' null comment '数据 source/destination 名称',
	docker_repository varchar(255) default '' null comment '镜像地址',
	docker_image_tag varchar(255) default '' null comment '镜像版本',
	documentation_url varchar(255) default '' null comment '文档地址',
	icon varchar(255) default '' null comment '图标地址',
	actor_type varchar(100) default '' null comment '角色类别 source/destination',
	source_type varchar(100) default '' null comment '数据源类别 api, file, database, custom',
	spec longtext not null comment '数据源参数定义',
	release_stage varchar(100) default '' null comment '发布阶段 alpha, beta, generally_available, custom',
	release_date datetime null comment '发布日期',
	resource_requirements longtext null comment '资源限制条件',
	for_ds_template tinyint(1) default 0 null comment '是否DS普通模板的数据源 0.否 1.是',
	is_open tinyint(1) default 0 null comment '是否开启 0.否 1.是',
	description varchar(2000) default '' null comment '附件描述',
	delete_status varchar(1) default '0' null comment '是否删除 0：未删除；1：已删除',
	create_by varchar(32) default '' null comment '创建人',
	create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
	update_by varchar(32) null comment '更新人',
	update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
comment '数据源定义表' charset=utf8mb4;

create table if not exists ds_task_${tenantName}.advice
(
	id int auto_increment
		primary key,
	app_name varchar(500) default 'DS_TASK' null comment '产品名称',
	score int default 0 null comment '用户满意度',
	advice_info varchar(6000) default '' null comment '建议信息',
	attachment_ids varchar(500) default '' null comment '附件 ID 列表，逗号分隔',
	delete_status varchar(1) default '0' null comment '是否删除 0：未删除；1：已删除',
	create_by varchar(32) default '' null comment '创建人',
	create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
	update_by varchar(32) default '' null comment '更新人',
	update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
comment '用户建议表' charset=utf8mb4;

create table if not exists ds_task_${tenantName}.announcement
(
	id int auto_increment comment '主键'
		primary key,
	name varchar(128) not null comment '公告名称',
	content longtext null comment '内容',
	online varchar(1) default '0' not null comment '是否上线 0：下线；1：上线',
	CREATE_BY varchar(32) not null comment '创建人',
	CREATE_TIME datetime default CURRENT_TIMESTAMP not null comment '创建时间',
	UPDATE_BY varchar(32) null comment '更新人',
	UPDATE_TIME datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
	delete_status varchar(1) default '0' null comment '0 未删除 1 已删除'
)
comment '公告表' charset=utf8;

create table if not exists ds_task_${tenantName}.artifact
(
	id int auto_increment comment '主键'
		primary key,
	name varchar(64) not null comment '名称',
	description varchar(512) null comment '备注',
	mode_code varchar(32) null comment '模式 ONLINE/UPLOAD',
	type_code varchar(32) null comment '类型 JAR/CSV/JSON',
	file_name varchar(255) null comment '文件名',
	file_size int null comment '文件大小',
	is_public int(4) default 0 null comment '0公共的 1是私有的',
	content text null comment '内容',
	delete_status varchar(1) default '0' null comment '是否删除 0：未删除；1：已删除',
	create_by varchar(32) not null comment '创建人',
	create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
	update_by varchar(32) null comment '更新人',
	update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
	user_group varchar(100) DEFAULT NULL,
	`folder_id` int(20) DEFAULT NULL
)
comment '工件表 ' charset=utf8;

create table if not exists ds_task_${tenantName}.artifact_version
(
	id int auto_increment comment '主键'
		primary key,
	artifact_id int not null comment '工件ID',
	version int default -1 not null comment '版本标签 -1为当前草稿，其他为TAG标签号',
	name varchar(64) null comment '命名',
	description varchar(512) null comment '备注',
	mode_code varchar(32) null comment '模式 ONLINE/UPLOAD',
	type_code varchar(32) null comment '类型 JAR/CSV/JSON',
	file_name varchar(255) null comment '文件名',
	file_size int null comment '文件大小',
	content text null comment '内容 ONLINE：编辑内容；UPLOAD：文件线上地址',
	region varchar(64) default 'sg2' null comment '区域',
	delete_status varchar(1) default '0' null comment '是否删除 0：未删除；1：已删除',
	CREATE_BY varchar(32) not null comment '创建人',
	CREATE_TIME datetime default CURRENT_TIMESTAMP not null comment '创建时间',
	UPDATE_BY varchar(32) null comment '更新人',
	UPDATE_TIME datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
comment '工件版本表 存储工件镜像' charset=utf8;

create table if not exists ds_task_${tenantName}.attachment
(
	id int auto_increment
		primary key,
	file_url varchar(512) default '' null comment '附件地址',
	file_name varchar(256) default '' null comment '附件文件名',
	file_size int default 0 null comment '附件大小',
	content_type varchar(100) default '' null comment '附件的类型',
	description varchar(2000) default '' null comment '附件描述',
	delete_status varchar(1) default '0' null comment '是否删除 0：未删除；1：已删除',
	create_by varchar(32) default '' null comment '创建人',
	create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
	update_by varchar(32) null comment '更新人',
	update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
comment '附件表' charset=utf8mb4;

create table if not exists ds_task_${tenantName}.auditlog
(
	id int auto_increment
		primary key,
	module varchar(64) not null comment 'event code',
	event_id int null comment '所属task',
	event_version int null comment '所属task',
	trace_id varchar(256) null comment '日志追踪id',
	event_code varchar(64) not null comment 'event code',
	event_message longtext not null comment 'event 描述',
	event_snapshot longtext not null comment '时间发生时的信息快照',
	CREATE_BY varchar(255) null,
	CREATE_TIME timestamp default CURRENT_TIMESTAMP not null
)
charset=utf8mb4;

create table if not exists ds_task_${tenantName}.cost_allocation
(
	id int(11) unsigned auto_increment
		primary key,
	task_id int(11) unsigned not null comment '任务ID',
	group_id int(11) unsigned not null comment 'access_group',
	value decimal(4,2) null,
	description varchar(512) null comment '备注',
	delete_status varchar(1) default '0' not null comment '是否删除 0：未删除；1：已删除',
	CREATE_BY varchar(32) null,
	CREATE_TIME datetime null,
	UPDATE_BY varchar(32) null,
	UPDATE_TIME datetime null
)
charset=utf8mb4;

create table if not exists ds_task_${tenantName}.cost_monitor
(
	id bigint auto_increment
		primary key,
	create_shareit_id varchar(50) null,
	execute_time varchar(50) null,
	create_time varchar(50) null,
	dps text null,
	jobs text null,
	frep varchar(200) not null,
	monitor_level int not null,
	name varchar(255) null,
	notice_persons text null,
	notice_self varchar(50) null,
	notice_type varchar(10) null,
	owners text null,
	pus text null,
	ratio varchar(10) null,
	trial_range int not null,
	type int not null,
	valid bit not null,
	send_notice bit not null
)
comment '成本监控表' charset=utf8;

create table if not exists ds_task_${tenantName}.cost_monitor_notice
(
	id bigint auto_increment
		primary key,
	cost_monitor_id bigint null,
	create_time varchar(50) null,
	name varchar(255) null,
	notice_time varchar(50) null,
	content text null,
	create_shareit_id varchar(255) null
)
comment '成本监控通知表' charset=utf8;

create table if not exists ds_task_${tenantName}.dept_info
(
	id int auto_increment comment '主键'
		primary key,
	organization_name varchar(64) not null comment '部门真实名称',
	cost_name varchar(512) null comment '成本统计部门名称',
	parent_id int null comment '父部门ID',
	is_effective_cost int(4) default 0 null comment '是否是成本统计有效部门 0：无效；1：有效',
	delete_status varchar(1) default '0' null comment '是否删除 0：未删除；1：已删除',
	create_by varchar(32) not null comment '创建人',
	create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
	update_by varchar(32) null comment '更新人',
	update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
comment '部门信息表' charset=utf8;

create table if not exists ds_task_${tenantName}.dictionary
(
	id int auto_increment comment '主键'
		primary key,
	component_code varchar(32) not null,
	chinese_name varchar(255) null comment '中文名称',
	english_name varchar(255) null comment '英文名称',
	description varchar(512) null comment '备注',
	delete_status varchar(1) default '0' null comment '是否删除 0：未删除；1：已删除',
	CREATE_BY varchar(32) not null comment '创建人',
	CREATE_TIME datetime default CURRENT_TIMESTAMP not null comment '创建时间',
	UPDATE_BY varchar(32) null comment '更新人',
	UPDATE_TIME datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
charset=utf8mb4;

create table if not exists ds_task_${tenantName}.ds_indicators
(
	id int auto_increment comment '自增id'
		primary key,
	project varchar(100) default 'dataStudio' null comment '项目名称',
	dt varchar(256) default '' not null comment '天',
	indicators varchar(256) default '' not null comment '指标名称',
	value bigint default 0 null comment '指标value',
	CREATE_TIME datetime default CURRENT_TIMESTAMP not null comment '创建时间',
	UPDATE_TIME datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
	constraint uniquekey
		unique (project, dt, indicators)
)
comment 'DS指标统计表' charset=utf8;

create table if not exists ds_task_${tenantName}.ds_indicators_statistical_new
(
	id int auto_increment comment '主键'
		primary key,
	dt varchar(256) not null comment '天粒度',
	indicators varchar(256) null comment '指标名称',
	value int default 0 null comment '指标value',
	CREATE_TIME datetime default CURRENT_TIMESTAMP not null comment '创建时间',
	UPDATE_TIME datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
charset=utf8;

create table if not exists ds_task_${tenantName}.duty_info
(
	id int auto_increment
		primary key,
	tenant_id int null comment '租户id',
	module varchar(500) default '' null comment '值班模块',
	user_id int null comment '用户id，值班人',
	serial_number int null comment '编号，排班顺序',
	is_duty varchar(1) null comment '当前是否值班 0：值班；1：不值班',
	duty_date varchar(100) null comment '值班日期',
	description varchar(512) null comment '备注',
	delete_status varchar(1) default '0' null comment '是否删除 0：未删除；1：已删除',
	create_by varchar(32) default '' null comment '创建人',
	create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
	update_by varchar(32) default '' null comment '更新人',
	update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
comment '值班信息表' charset=utf8mb4;

create table if not exists ds_task_${tenantName}.dynamic_form
(
	id int auto_increment comment '主键'
		primary key,
	component_code varchar(32) not null comment '组件编码',
	element_key varchar(1024) not null comment '参数关键字',
	element_type varchar(32) not null comment '参数类型 RidoCheckbox',
	pattern varchar(128) null comment '参数约束正则表达式',
	`option` tinyint unsigned null comment '是否必须 0：选填；1：必填',
	value varchar(32) null comment '默认值',
	unit varchar(32) null comment '单位',
	default_value varchar(255) null,
	element_classify varchar(64) not null comment 'ARGCONFIGPARAM',
	description varchar(512) null comment '备注',
	parent_id varchar(32) null comment '父节点ID',
	`rank` int null comment '序列',
	CREATE_BY varchar(32) not null comment '创建人',
	CREATE_TIME datetime default CURRENT_TIMESTAMP not null comment '创建时间',
	UPDATE_BY varchar(32) null comment '更新人',
	UPDATE_TIME datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
comment '动态表单表 ' charset=utf8;

create table if not exists ds_task_${tenantName}.feedback
(
	id int auto_increment
		primary key,
	app_name varchar(500) default '' null comment '产品名称',
	module varchar(100) default '' null comment '问题产品',
	type varchar(100) default '' null comment '问题类别',
	title varchar(200) default '' null comment '标题',
	description varchar(6000) default '' null comment '问题描述',
	page_uri varchar(2000) default '' null comment '页面地址',
	task_id varchar(50) default '' null comment '任务id',
	status varchar(100) default '' null comment '状态 UN_ACCEPT.待接单 ACCEPTED.处理中 SOLVED.已完成 UN_SCORE.未打分 SCORED 已打分',
	attachment_ids varchar(500) default '' null comment '附件 ID 列表，逗号分隔',
	charge_person varchar(100) default '' null comment '负责人',
	handle_by varchar(32) default '' null comment '当前处理人',
	resolve_time datetime null comment '问题解决时间',
	resolve_reply varchar(5000) default '' null comment '解决方案',
	score int default 0 null comment '评分',
	first_accept_time datetime default CURRENT_TIMESTAMP null comment '首次受理时间',
	first_close_time datetime default CURRENT_TIMESTAMP null comment '首次关闭时间',
	feedback_level varchar(50) default 'GENERAL' null comment '工单级别',
	delete_status varchar(1) default '0' null comment '是否删除 0：未删除；1：已删除',
	create_by varchar(32) default '' null comment '创建人',
	create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
	update_by varchar(32) default '' null comment '更新人',
	update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
	second_notify varchar(1) default '0' null comment '0 未通知 1 已通知',
	first_notify varchar(1) default '0' null comment '0 未通知 1 已通知',
	user_group varchar(100) DEFAULT NULL
)
comment '用户反馈表' charset=utf8mb4;

create table if not exists ds_task_${tenantName}.feedback_process_item
(
	id int auto_increment
		primary key,
	feedback_id int default 0 null comment '反馈（工单）ID',
	detail varchar(2000) default '' null comment '处理明细信息',
	attachment_ids varchar(500) default '' null comment '附件 ID 列表，逗号分隔',
	description varchar(500) default '' null comment '描述',
	delete_status varchar(1) default '0' null comment '是否删除 0：未删除；1：已删除',
	create_by varchar(32) default '' null comment '创建人',
	create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
	update_by varchar(32) null comment '更新人',
	update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
comment '反馈（工单）受理详情表' charset=utf8mb4;

create table if not exists ds_task_${tenantName}.flink_cluster
(
	id int auto_increment comment '主键'
		primary key,
	name varchar(128) not null comment '名称',
	description varchar(512) null comment '备注',
	type_code varchar(32) default 'K8S' not null comment '类型 SESSION/K8S/EMR/MRS',
	address varchar(128) null comment '集群服务基地址',
	state_path varchar(128) null comment '状态存储路径',
	zookeeper_quorum varchar(255) null comment 'ZK服务地址',
	log_es_source varchar(128) null comment '日志对应es数据源',
	container_image varchar(128) null comment '容器镜像地址',
	region varchar(128) null comment '域',
	env varchar(128) null comment '环境',
	delete_status varchar(1) default '0' not null comment '是否删除 0：未删除；1：已删除',
	create_by varchar(32) not null comment '创建人',
	create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
	update_by varchar(32) null comment '更新人',
	update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
	namespace varchar(128) null,
	node_selector varchar(128) null,
	tolerations varchar(128) null,
	version varchar(100) null,
	cluster_id varchar(128) null comment '集群实例ID'
)
comment 'Flink集群信息表 ' charset=utf8;

create table if not exists ds_task_${tenantName}.hive_tables
(
	id int auto_increment comment '主键'
		primary key,
	path text not null comment '模板编码',
	region varchar(128) null comment '名称',
	provider varchar(512) null comment '备注',
	mode longtext null comment '内容 JAR则是url，其他在线编辑为原内容（base64）',
	name varchar(128) null comment '主类路径',
	delete_status varchar(1) default '0' not null comment '是否删除 0：未删除；1：已删除',
	CREATE_BY varchar(32) not null comment '创建人',
	CREATE_TIME datetime default CURRENT_TIMESTAMP not null comment '创建时间',
	UPDATE_BY varchar(32) null comment '更新人',
	UPDATE_TIME datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
	`database` varchar(100) null,
	task_id int null,
	catalog varchar(100) null
)
comment 'iceberg表 ' charset=utf8;

create table if not exists ds_task_${tenantName}.label
(
	id int auto_increment comment '主键'
		primary key,
	name text not null comment '标签名',
	description text null comment '描述',
	tasks text null comment '任务id列表',
	delete_status varchar(1) default '0' not null comment '是否删除 0：未删除；1：已删除',
	CREATE_BY varchar(32) not null comment '创建人',
	CREATE_TIME datetime default CURRENT_TIMESTAMP not null comment '创建时间',
	UPDATE_BY varchar(32) null comment '更新人',
	UPDATE_TIME datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    workflows text COMMENT '工作流id，多个用逗号分割',
    publish int(1) NOT NULL DEFAULT '0' COMMENT '是否公开',
    publishers text COMMENT '公开人员',
	tenancy_code text null
)
comment 'iceberg表 ' charset=utf8;

create table if not exists ds_task_${tenantName}.label_collect
(
	id int auto_increment comment '主键'
		primary key,
	label_ids varchar(4096) not null comment '标签id',
	CREATE_BY varchar(32) not null comment '创建人',
	UPDATE_TIME datetime default CURRENT_TIMESTAMP not null comment '操作时间'
)
comment '标签收藏表 ' charset=utf8;

create table if not exists ds_task_${tenantName}.lock_info
(
	id int auto_increment comment '主键'
		primary key,
	tag varchar(256) not null comment '锁唯一标签',
	expirationTime datetime default CURRENT_TIMESTAMP not null comment '失效时间',
	status int default 0 not null comment '锁状态',
	CREATE_TIME datetime default CURRENT_TIMESTAMP not null comment '创建时间',
	UPDATE_TIME datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
	hostname varchar(255) null comment '持有锁的主机名',
	constraint tag_status
		unique (tag, status)
)
charset=utf8;

create table if not exists ds_task_${tenantName}.operate_log
(
	id int auto_increment
		primary key,
	user_name varchar(256) default '' null comment '用户ID',
	source varchar(100) default '' null comment '来源',
	trace_id varchar(256) default '' null comment '日志ID',
	type varchar(32) default '' null comment '操作类型',
	uri varchar(256) default '' null comment '请求路径',
	params longtext null comment '请求参数',
	result_code varchar(512) default '' null comment '响应状态码',
	result_message varchar(256) default '' null comment '响应码不为0时的错误信息',
	result_data longtext null comment '响应码为0时，返回数据',
	cost_time bigint default 0 null comment '耗时，单位毫秒',
	request_time datetime default CURRENT_TIMESTAMP null comment '请求时间',
	response_time datetime default CURRENT_TIMESTAMP null comment '返回日期'
)
comment '操作日志表' charset=utf8mb4;

create index request_time
	on ds_task_${tenantName}.operate_log (request_time);

create index result_code
	on ds_task_${tenantName}.operate_log (result_code);

create index type
	on ds_task_${tenantName}.operate_log (type);

create index uri
	on ds_task_${tenantName}.operate_log (uri);

create index user_name
	on ds_task_${tenantName}.operate_log (user_name);

create table if not exists ds_task_${tenantName}.spark_log_url
(
	id int auto_increment comment '主键'
		primary key,
	index_pattern varchar(32) not null comment 'kibana上index pattern名',
	index_id varchar(512) null comment 'kibana上index 对应id',
	region varchar(32) null comment '所在区域',
	`group` varchar(32) null comment '组名',
	url varchar(2048) not null comment 'kibana地址'
)
comment 'spark_log域名' charset=utf8;

create table if not exists ds_task_${tenantName}.sys_dict
(
	id int auto_increment comment '主键'
		primary key,
	code varchar(32) not null comment '唯一编码',
	value varchar(512) null comment '对应值',
	parent_code varchar(32) null comment '父编码',
	description varchar(512) null comment '备注',
	status smallint(5) unsigned default 0 null comment '是否启用 0：未启用；1：启用',
	`source` varchar(32) DEFAULT NULL COMMENT '请求模块',
	CREATE_BY varchar(32) not null comment '创建人',
	CREATE_TIME datetime default CURRENT_TIMESTAMP not null comment '创建时间',
	UPDATE_BY varchar(32) null comment '更新人',
	UPDATE_TIME datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
comment '字典表 ' charset=utf8;

create table if not exists ds_task_${tenantName}.task
(
	id int auto_increment comment '主键'
		primary key,
	template_code varchar(32) not null comment '模板编码',
	name varchar(128) null comment '名称',
	description varchar(512) null comment '备注',
	content longtext null comment '内容 JAR则是url，其他在线编辑为原内容（base64）',
	main_class varchar(128) null comment '主类路径',
	main_class_args text null comment '参数',
	runtime_config text null comment '任务运行配置 json格式存储',
	input_dataset text null comment '输入源数据集 json格式存储',
	output_dataset varchar(1024) null comment '输出源数据集 json格式存储',
	depend_artifacts varchar(32) null comment '依赖工件',
	status_code varchar(32) default 'CREATED' not null comment '任务当前状态 CREATED|RUNNING|CANCELED|SUSPENDED|FINISHED|TRANSITIONING|FAILED',
	type_code varchar(64) null comment 'artifact/online',
	jar_url varchar(255) null comment '工件地址',
	flink_cluster_id int null comment 'flink任务集群ID',
	tenancy_code varchar(128) null comment '应用所属租户组',
	input_guids text null,
	output_guids varchar(1024) null,
	online varchar(1) default '0' not null comment '是否上线 0：下线；1：上线',
	delete_status varchar(1) default '0' not null comment '是否删除 0：未删除；1：已删除',
	CREATE_BY varchar(32) not null comment '创建人',
	CREATE_TIME datetime default CURRENT_TIMESTAMP not null comment '创建时间',
	UPDATE_BY varchar(32) null comment '更新人',
	UPDATE_TIME datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
	collaborators varchar(512) null,
	release_time datetime null,
	restart_time datetime default CURRENT_TIMESTAMP null comment '自动伸缩重启时间',
	trigger_param varchar(512) default '' not null comment '任务的触发规则参数',
	event_depends text null comment '事件依赖',
	depend_types varchar(64) default '' not null comment '依赖的类型,dataset,event',
	current_version int default 1 null comment '任务当前版本',
	workflow_id int default 0 null comment '关联工作流ID',
	source int default 0 null comment '来源 0.单任务添加 1.工作流添加',
	user_group varchar(100) DEFAULT NULL
)
comment '任务表 ' charset=utf8;

create table if not exists ds_task_${tenantName}.task_instance
(
	id int auto_increment comment '主键'
		primary key,
	version_id int not null comment '任务版本ID',
	snapshot_id int null comment '保存点ID',
	engine_instance_id varchar(64) null comment '运行实例ID flinkjob id或者genie id',
	service_address varchar(128) null comment '服务UI地址',
	task_id int null,
	cluster_id int null comment '集群ID',
	status_code varchar(32) null comment '任务当前状态 CREATED|RUNNING|CANCELED|SUSPENDED|FINISHED|TRANSITIONING|FAILED',
	CREATE_BY varchar(32) not null comment '创建人',
	CREATE_TIME datetime default CURRENT_TIMESTAMP not null comment '创建时间',
	update_by varchar(32) null comment '终止人',
	update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '终止时间'
)
comment '运行实例表 ' charset=utf8;

create table if not exists ds_task_${tenantName}.task_par_change
(
	id int auto_increment comment '主键'
		primary key,
	task_id int null comment '任务ID',
	strategy_name varchar(128) null comment '策略名称',
	original_par int default 0 null comment '原始并行度',
	update_par int default 0 null comment '更新并行度',
	CREATE_TIME datetime default CURRENT_TIMESTAMP not null comment '创建时间',
	UPDATE_TIME datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
	restart_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '任务重启时间'
)
comment '任务并行度变化记录表' charset=utf8;

create table if not exists ds_task_${tenantName}.task_scale_strategy
(
	id int auto_increment comment '主键'
		primary key,
	task_id int null comment '任务ID',
	name varchar(128) null comment '策略名称',
	description varchar(512) null comment '备注',
	specific_strategy varchar(1000) default '' null comment '具体策略',
	delete_status varchar(1) default '0' not null,
	CREATE_BY varchar(32) not null comment '创建人',
	CREATE_TIME datetime default CURRENT_TIMESTAMP not null comment '创建时间',
	UPDATE_BY varchar(32) null comment '更新人',
	UPDATE_TIME datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
	cooling_time int default 0 null comment '扩缩冷却时间单位S'
)
comment '任务自动伸缩策略表' charset=utf8;

create table if not exists ds_task_${tenantName}.task_snapshot
(
	id int(11) unsigned auto_increment
		primary key,
	task_id int(11) unsigned not null comment '所属任务',
	name varchar(255) not null,
	description varchar(255) null,
	trigger_kind varchar(32) default 'SAVEPOINT' not null comment '保持点触发方式',
	url varchar(4096) null comment '保存点url',
	CREATE_BY varchar(32) not null comment '创建人',
	CREATE_TIME datetime default CURRENT_TIMESTAMP not null comment '创建时间',
	UPDATE_BY varchar(32) null comment '更新人',
	UPDATE_TIME datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
	delete_status varchar(1) default '0' null comment '是否删除 0：未删除；1：已删除;'
)
charset=utf8mb4;

create table if not exists ds_task_${tenantName}.task_version
(
	id int auto_increment comment '主键'
		primary key,
	task_id int not null comment '任务ID',
	template_code varchar(32) null comment '模板编码',
	name varchar(128) not null,
	description varchar(512) null comment '备注',
	version int default -1 not null comment '版本标签 -1为当前草稿，其他为TAG标签号',
	content text null comment '内容 JAR则是url，其他在线编辑为原内容（base64）',
	main_class varchar(128) null comment '主类路径',
	main_class_args text null comment '任务启动参数',
	runtime_config text null comment '任务运行配置 json格式存储',
	input_dataset text null comment '输入源数据集 json格式存储',
	output_dataset text null comment '输出源数据集 json格式存储',
	depend_artifacts varchar(32) null comment '依赖工件',
	type_code varchar(64) null comment 'artifact/online',
	jar_url varchar(255) null comment '工件地址',
	flink_cluster_id int null comment 'flink任务集群ID',
	tenancy_code varchar(128) null comment '应用所属租户组',
	CREATE_BY varchar(32) not null comment '创建人',
	CREATE_TIME datetime default CURRENT_TIMESTAMP not null comment '创建时间',
	UPDATE_BY varchar(32) null comment '更新人',
	UPDATE_TIME datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
	collaborators varchar(512) null,
	input_guids text null,
	output_guids text null,
	online varchar(1) null,
	release_time datetime null,
	trigger_param varchar(512) default '' not null comment '任务的触发规则参数',
	event_depends text null comment '事件依赖',
	depend_types varchar(64) default '' not null comment '依赖的类型,dataset,event',
	workflow_id int default 0 null comment '关联工作流ID',
	source int default 0 null comment '来源 0.单任务添加 1.工作流添加',
	user_group varchar(100) DEFAULT NULL
)
comment '任务版本表 用于存储任务TAG镜像' charset=utf8;

create table if not exists ds_task_${tenantName}.template_region_imp
(
	id int auto_increment comment '主键'
		primary key,
	template_code varchar(32) null comment '任务模板编码',
	region_code varchar(32) null comment '区域编码',
	url varchar(255) null comment '模板实现包存储地址',
	main_class varchar(255) null comment '模板实现包主类路径',
	CREATE_BY varchar(32) not null comment '创建人',
	CREATE_TIME datetime default CURRENT_TIMESTAMP not null comment '创建时间',
	UPDATE_BY varchar(32) null comment '更新人',
	UPDATE_TIME datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
	description varchar(512) null comment '备注',
	image varchar(100) null
)
comment '模板分region具体实现 ' charset=utf8;

create table if not exists ds_task_${tenantName}.workflow
(
	id int auto_increment comment '主键'
		primary key,
	name varchar(200) default '' null comment '工作流名字',
	source varchar(100) default '' null comment '工作流来源 new.全部新建 history.历史创建',
	current_version int default 0 null comment '工作流当前版本',
	status int default 0 null comment '工作流状态 0.新建 1.已上线 2.已下线',
	owner varchar(100) default '' null comment '负责人',
	collaborators varchar(2000) default '' null comment '协作者，逗号分隔',
	granularity varchar(100) default '' null comment '粒度 hourly.小时，dayly.天， weekly.周',
	cron_config varchar(1000) default '' null comment '定时调度配置',
	user_group varchar(500) default '' null comment '用户组',
	description varchar(512) default '' null comment '描述',
	delete_status varchar(1) default '0' null comment '是否删除 0：未删除；1：已删除',
	create_by varchar(32) default '' null comment '创建人',
	create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
	update_by varchar(32) default '' null comment '更新人',
	update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
comment '工作流表' charset=utf8;

create table if not exists ds_task_${tenantName}.workflow_task
(
	id int auto_increment comment '主键'
		primary key,
	workflow_id int not null comment '工作流ID',
	workflow_version_id int not null comment '工作流版本ID',
	task_id int not null comment '任务ID',
	task_version int not null comment '任务版本，非DB自增ID',
	description varchar(512) default '' null comment '描述',
	delete_status varchar(1) default '0' null comment '是否删除 0：未删除；1：已删除',
	create_by varchar(32) default '' null comment '创建人',
	create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
	update_by varchar(32) default '' null comment '更新人',
	update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
comment '工作流关联任务表' charset=utf8;

create table if not exists ds_task_${tenantName}.workflow_version
(
	id int auto_increment comment '主键，版本ID'
		primary key,
	workflow_id int not null comment '工作流ID',
	name varchar(200) default '' null comment '工作流名字',
	version int(10) default 1 null comment '版本',
	status int default 0 null comment '工作流状态 0.新建 1.已上线 2.已下线',
	owner varchar(100) default '' null comment '负责人',
	collaborators varchar(2000) default '' null comment '协作者，逗号分隔',
	granularity varchar(100) default '' null comment '粒度 hourly.小时，dayly.天， weekly.周',
	cron_config varchar(1000) default '' null comment '定时调度配置',
	user_group  varchar(500) default '' null comment '用户组',
	description varchar(512) default '' null comment '描述',
	delete_status varchar(1) default '0' null comment '是否删除 0：未删除；1：已删除',
	create_by varchar(32) default '' null comment '创建人',
	create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
	update_by varchar(32) default '' null comment '更新人',
	update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
comment '工作流表版本表' charset=utf8;

create table if not exists ds_task_${tenantName}.spark_param_restrict
(
	 `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
      `spark_version` varchar(300) DEFAULT '3' COMMENT 'spark版本',
      `prefix` varchar(300) DEFAULT NULL COMMENT '参数前缀',
      `name` varchar(300) DEFAULT NULL COMMENT '参数名称',
      `description` varchar(512) DEFAULT NULL COMMENT '参数描述',
      `type` varchar(50) DEFAULT NULL COMMENT '参数类型',
      `is_value_check` varchar(4) DEFAULT '0' COMMENT '0:校验 | 1:不校验，参数值是否进行值校验',
      `param_strategy` varchar(1000) DEFAULT '' COMMENT '参数限制策略',
      `delete_status` varchar(1) NOT NULL DEFAULT '0',
      `CREATE_BY` varchar(32) NOT NULL COMMENT '创建人',
      `CREATE_TIME` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
      `UPDATE_BY` varchar(32) DEFAULT NULL COMMENT '更新人',
      `UPDATE_TIME` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
      PRIMARY KEY (`id`) USING BTREE
)
CHARSET=utf8 COMMENT='spark参数约定表';

CREATE TABLE  if not exists ds_task_${tenantName}.`user_group` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(200) DEFAULT '' COMMENT '用户组名称',
  `uuid` varchar(100) DEFAULT '' COMMENT '后续用户组名称',
  `parent_id` int(11) DEFAULT NULL COMMENT '挂在哪个组织架构',
  `default_hive_db_name` varchar(100) DEFAULT NULL COMMENT '默认的hive库',
  `token` varchar(100) DEFAULT '' COMMENT '负责人',
  `description` varchar(2000) DEFAULT '' COMMENT '描述',
  `delete_status` varchar(1) NOT NULL DEFAULT '0' COMMENT '是否删除 0：未删除；1：已删除',
  `create_by` varchar(32) DEFAULT '' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT '' COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8 COMMENT='用户组';

CREATE TABLE  if not exists ds_task_${tenantName}.`user_group_relation` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_name` varchar(200) DEFAULT '' COMMENT '用户名称',
  `owner` int(1) DEFAULT NULL COMMENT '是不是owner',
  `user_id` int(11) DEFAULT NULL COMMENT '用户id',
  `user_group_id` int(11) DEFAULT NULL COMMENT '用户组id',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1071 DEFAULT CHARSET=utf8 COMMENT='用户和用户组的关系表';


CREATE TABLE  if not exists ds_task_${tenantName}.`task_folder` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(200) DEFAULT '' COMMENT '用户名称',
  `parent_id` int(11) DEFAULT NULL COMMENT '父节点',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='任务文件夹表';

CREATE TABLE  if not exists ds_task_${tenantName}.`task_folder_relation` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `task_name` varchar(200) DEFAULT '' COMMENT '任务名称',
  `task_id` int(11) DEFAULT NULL COMMENT '任务id',
	`task_folder_id` int(11) DEFAULT NULL COMMENT '任务文件夹id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='任务与文件夹关系表';


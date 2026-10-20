-- =====================================================================
-- sudutech 快速开发平台 数据库 Schema v1.0
-- 兼容 MySQL 5.5+ / 推荐 MySQL 8.0+
-- 说明: JSON 列统一使用 LONGTEXT（兼容 5.5），时间戳由应用层维护
-- =====================================================================
CREATE DATABASE IF NOT EXISTS sudutech_lite DEFAULT CHARACTER SET utf8mb4;
USE sudutech_lite;

-- ----------------------------
-- 1. 部门表
-- ----------------------------
DROP TABLE IF EXISTS sys_dept;
CREATE TABLE sys_dept (
  dept_id     BIGINT NOT NULL AUTO_INCREMENT COMMENT '部门ID',
  parent_id   BIGINT      DEFAULT 0  COMMENT '父部门ID',
  ancestors   VARCHAR(500) DEFAULT '' COMMENT '祖级列表',
  dept_name   VARCHAR(30) NOT NULL COMMENT '部门名称',
  order_num   INT         DEFAULT 0  COMMENT '显示顺序',
  leader_user_id BIGINT   DEFAULT NULL COMMENT '负责人ID',
  phone       VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  email       VARCHAR(50) DEFAULT NULL COMMENT '邮箱',
  status      CHAR(1)     DEFAULT '0' COMMENT '状态 0正常 1停用',
  del_flag    CHAR(1)     DEFAULT '0' COMMENT '删除标志 0存在 2删除',
  remark      VARCHAR(500) DEFAULT NULL,
  create_by   VARCHAR(64) DEFAULT '',
  create_time DATETIME    DEFAULT NULL,
  update_by   VARCHAR(64) DEFAULT '',
  update_time DATETIME    DEFAULT NULL,
  PRIMARY KEY (dept_id)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- ----------------------------
-- 2. 用户表
-- ----------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
  user_id     BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  dept_id     BIGINT    DEFAULT NULL COMMENT '部门ID',
  user_name   VARCHAR(30) NOT NULL COMMENT '登录账号',
  nick_name   VARCHAR(30) NOT NULL COMMENT '用户昵称',
  email       VARCHAR(50)  DEFAULT '' COMMENT '邮箱',
  phone       VARCHAR(11)  DEFAULT '' COMMENT '手机号',
  sex         CHAR(1)      DEFAULT '0' COMMENT '性别 0男 1女 2未知',
  avatar      VARCHAR(200) DEFAULT '' COMMENT '头像',
  password    VARCHAR(100) DEFAULT '' COMMENT '密码 BCrypt',
  status      CHAR(1)      DEFAULT '0' COMMENT '状态 0正常 1停用',
  del_flag    CHAR(1)      DEFAULT '0' COMMENT '删除标志 0存在 2删除',
  login_ip    VARCHAR(128) DEFAULT '' COMMENT '最后登录IP',
  login_date  DATETIME     DEFAULT NULL COMMENT '最后登录时间',
  remark      VARCHAR(500) DEFAULT NULL,
  create_by   VARCHAR(64) DEFAULT '',
  create_time DATETIME    DEFAULT NULL,
  update_by   VARCHAR(64) DEFAULT '',
  update_time DATETIME    DEFAULT NULL,
  PRIMARY KEY (user_id),
  UNIQUE KEY uk_user_name (user_name)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ----------------------------
-- 3. 岗位表
-- ----------------------------
DROP TABLE IF EXISTS sys_post;
CREATE TABLE sys_post (
  post_id     BIGINT NOT NULL AUTO_INCREMENT COMMENT '岗位ID',
  post_code   VARCHAR(64) NOT NULL COMMENT '岗位编码',
  post_name   VARCHAR(50) NOT NULL COMMENT '岗位名称',
  post_sort   INT NOT NULL DEFAULT 0 COMMENT '显示顺序',
  status      CHAR(1)     DEFAULT '0' COMMENT '状态 0正常 1停用',
  remark      VARCHAR(500) DEFAULT NULL,
  create_by   VARCHAR(64) DEFAULT '',
  create_time DATETIME    DEFAULT NULL,
  update_by   VARCHAR(64) DEFAULT '',
  update_time DATETIME    DEFAULT NULL,
  PRIMARY KEY (post_id)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='岗位表';

-- ----------------------------
-- 4. 角色表
-- ----------------------------
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
  role_id     BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  role_name   VARCHAR(30) NOT NULL COMMENT '角色名称',
  role_key    VARCHAR(100) NOT NULL COMMENT '角色权限字符串',
  role_sort   INT NOT NULL DEFAULT 0 COMMENT '显示顺序',
  data_scope  CHAR(1)     DEFAULT '1' COMMENT '数据范围 1全部 2自定义 3本部门 4本部门及以下 5仅本人',
  status      CHAR(1)     DEFAULT '0' COMMENT '状态 0正常 1停用',
  del_flag    CHAR(1)     DEFAULT '0' COMMENT '删除标志',
  remark      VARCHAR(500) DEFAULT NULL,
  create_by   VARCHAR(64) DEFAULT '',
  create_time DATETIME    DEFAULT NULL,
  update_by   VARCHAR(64) DEFAULT '',
  update_time DATETIME    DEFAULT NULL,
  PRIMARY KEY (role_id),
  UNIQUE KEY uk_role_key (role_key)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ----------------------------
-- 5. 菜单权限表
-- ----------------------------
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
  menu_id     BIGINT NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  menu_name   VARCHAR(50) NOT NULL COMMENT '菜单名称',
  parent_id   BIGINT      DEFAULT 0 COMMENT '父菜单ID',
  order_num   INT         DEFAULT 0 COMMENT '显示顺序',
  path        VARCHAR(200) DEFAULT '' COMMENT '路由地址',
  component   VARCHAR(255) DEFAULT NULL COMMENT '组件路径',
  is_frame    INT         DEFAULT 1 COMMENT '是否外链 0是 1否',
  menu_type   CHAR(1)     DEFAULT '' COMMENT '类型 M目录 C菜单 F按钮',
  visible     CHAR(1)     DEFAULT '0' COMMENT '显示 0显示 1隐藏',
  status      CHAR(1)     DEFAULT '0' COMMENT '状态 0正常 1停用',
  perms       VARCHAR(100) DEFAULT NULL COMMENT '权限标识',
  icon        VARCHAR(100) DEFAULT '#' COMMENT '菜单图标',
  remark      VARCHAR(500) DEFAULT NULL,
  create_by   VARCHAR(64) DEFAULT '',
  create_time DATETIME    DEFAULT NULL,
  update_by   VARCHAR(64) DEFAULT '',
  update_time DATETIME    DEFAULT NULL,
  PRIMARY KEY (menu_id)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';

-- ----------------------------
-- 6. 用户-角色关联
-- ----------------------------
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
  user_id BIGINT NOT NULL COMMENT '用户ID',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  PRIMARY KEY (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- ----------------------------
-- 7. 用户-岗位关联
-- ----------------------------
DROP TABLE IF EXISTS sys_user_post;
CREATE TABLE sys_user_post (
  user_id BIGINT NOT NULL COMMENT '用户ID',
  post_id BIGINT NOT NULL COMMENT '岗位ID',
  PRIMARY KEY (user_id, post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户岗位关联表';

-- ----------------------------
-- 8. 角色-菜单关联
-- ----------------------------
DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
  role_id BIGINT NOT NULL COMMENT '角色ID',
  menu_id BIGINT NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- ----------------------------
-- 9. 角色-部门关联（自定义数据权限）
-- ----------------------------
DROP TABLE IF EXISTS sys_role_dept;
CREATE TABLE sys_role_dept (
  role_id BIGINT NOT NULL COMMENT '角色ID',
  dept_id BIGINT NOT NULL COMMENT '部门ID',
  PRIMARY KEY (role_id, dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色部门关联表';

-- ----------------------------
-- 10. 字典类型表
-- ----------------------------
DROP TABLE IF EXISTS sys_dict_type;
CREATE TABLE sys_dict_type (
  dict_id     BIGINT NOT NULL AUTO_INCREMENT COMMENT '字典主键',
  dict_name   VARCHAR(100) DEFAULT '' COMMENT '字典名称',
  dict_type   VARCHAR(100) DEFAULT '' COMMENT '字典类型',
  status      CHAR(1)      DEFAULT '0' COMMENT '状态 0正常 1停用',
  remark      VARCHAR(500) DEFAULT NULL,
  create_by   VARCHAR(64) DEFAULT '',
  create_time DATETIME    DEFAULT NULL,
  update_by   VARCHAR(64) DEFAULT '',
  update_time DATETIME    DEFAULT NULL,
  PRIMARY KEY (dict_id),
  UNIQUE KEY uk_dict_type (dict_type)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='字典类型表';

-- ----------------------------
-- 11. 字典数据表
-- ----------------------------
DROP TABLE IF EXISTS sys_dict_data;
CREATE TABLE sys_dict_data (
  dict_code   BIGINT NOT NULL AUTO_INCREMENT COMMENT '字典编码',
  dict_sort   INT         DEFAULT 0 COMMENT '字典排序',
  dict_label  VARCHAR(100) DEFAULT '' COMMENT '字典标签',
  dict_value  VARCHAR(100) DEFAULT '' COMMENT '字典键值',
  dict_type   VARCHAR(100) DEFAULT '' COMMENT '字典类型',
  css_class   VARCHAR(100) DEFAULT NULL COMMENT '样式属性',
  list_class  VARCHAR(100) DEFAULT NULL COMMENT '表格回显样式 success/danger等',
  is_default  CHAR(1)     DEFAULT 'N' COMMENT '是否默认 Y是 N否',
  status      CHAR(1)     DEFAULT '0' COMMENT '状态',
  remark      VARCHAR(500) DEFAULT NULL,
  create_by   VARCHAR(64) DEFAULT '',
  create_time DATETIME    DEFAULT NULL,
  update_by   VARCHAR(64) DEFAULT '',
  update_time DATETIME    DEFAULT NULL,
  PRIMARY KEY (dict_code)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='字典数据表';

-- ----------------------------
-- 12. 参数配置表
-- ----------------------------
DROP TABLE IF EXISTS sys_config;
CREATE TABLE sys_config (
  config_id   BIGINT NOT NULL AUTO_INCREMENT COMMENT '参数主键',
  config_name VARCHAR(100) DEFAULT '' COMMENT '参数名称',
  config_key  VARCHAR(100) DEFAULT '' COMMENT '参数键名',
  config_value VARCHAR(500) DEFAULT '' COMMENT '参数键值',
  config_type CHAR(1)      DEFAULT 'N' COMMENT '系统内置 Y是 N否',
  remark      VARCHAR(500) DEFAULT NULL,
  create_by   VARCHAR(64) DEFAULT '',
  create_time DATETIME    DEFAULT NULL,
  update_by   VARCHAR(64) DEFAULT '',
  update_time DATETIME    DEFAULT NULL,
  PRIMARY KEY (config_id),
  UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='参数配置表';

-- ----------------------------
-- 13. 通知公告表
-- ----------------------------
DROP TABLE IF EXISTS sys_notice;
CREATE TABLE sys_notice (
  notice_id     BIGINT NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  notice_title  VARCHAR(100) NOT NULL COMMENT '公告标题',
  notice_type   CHAR(1)      NOT NULL COMMENT '公告类型 1通知 2公告',
  notice_content LONGTEXT    DEFAULT NULL COMMENT '公告内容',
  status        CHAR(1)      DEFAULT '0' COMMENT '状态 0正常 1关闭',
  create_by     VARCHAR(64) DEFAULT '',
  create_time   DATETIME    DEFAULT NULL,
  update_by     VARCHAR(64) DEFAULT '',
  update_time   DATETIME    DEFAULT NULL,
  remark        VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (notice_id)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='通知公告表';

-- ----------------------------
-- 14. 操作日志表
-- ----------------------------
DROP TABLE IF EXISTS sys_oper_log;
CREATE TABLE sys_oper_log (
  oper_id        BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  title          VARCHAR(50)  DEFAULT '' COMMENT '模块标题',
  business_type  INT          DEFAULT 0 COMMENT '业务类型 0其它 1新增 2修改 3删除',
  method         VARCHAR(200) DEFAULT '' COMMENT '方法名称',
  request_method VARCHAR(10)  DEFAULT '' COMMENT '请求方式',
  operator_type  INT          DEFAULT 0 COMMENT '操作类别 0其它 1后台用户',
  oper_name      VARCHAR(50)  DEFAULT '' COMMENT '操作人员',
  oper_url       VARCHAR(255) DEFAULT '' COMMENT '请求URL',
  oper_ip        VARCHAR(128) DEFAULT '' COMMENT '主机地址',
  oper_param     TEXT         DEFAULT NULL COMMENT '请求参数',
  json_result    TEXT         DEFAULT NULL COMMENT '返回参数',
  status         INT          DEFAULT 0 COMMENT '操作状态 0正常 1异常',
  error_msg      TEXT         DEFAULT NULL COMMENT '错误消息',
  cost_time      BIGINT       DEFAULT 0 COMMENT '耗时毫秒',
  oper_time      DATETIME     DEFAULT NULL COMMENT '操作时间',
  PRIMARY KEY (oper_id),
  KEY idx_oper_time (oper_time)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ----------------------------
-- 15. 登录日志表
-- ----------------------------
DROP TABLE IF EXISTS sys_login_log;
CREATE TABLE sys_login_log (
  info_id        BIGINT NOT NULL AUTO_INCREMENT COMMENT '访问ID',
  user_name      VARCHAR(50)  DEFAULT '' COMMENT '登录账号',
  ipaddr         VARCHAR(128) DEFAULT '' COMMENT '登录IP',
  login_location VARCHAR(255) DEFAULT '' COMMENT '登录地点',
  browser        VARCHAR(50)  DEFAULT '' COMMENT '浏览器',
  os             VARCHAR(50)  DEFAULT '' COMMENT '操作系统',
  status         CHAR(1)      DEFAULT '0' COMMENT '登录状态 0成功 1失败',
  msg            VARCHAR(255) DEFAULT '' COMMENT '提示消息',
  login_time     DATETIME     DEFAULT NULL COMMENT '登录时间',
  PRIMARY KEY (info_id),
  KEY idx_login_time (login_time)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

-- ----------------------------
-- 16. 定时任务表
-- ----------------------------
DROP TABLE IF EXISTS sys_job;
CREATE TABLE sys_job (
  job_id        BIGINT NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  job_name      VARCHAR(64)  NOT NULL COMMENT '任务名称',
  job_group     VARCHAR(64)  NOT NULL DEFAULT 'DEFAULT' COMMENT '任务组名',
  invoke_target VARCHAR(500) NOT NULL COMMENT '调用目标字符串 beanName.method(args)',
  cron_expression VARCHAR(100) DEFAULT '' COMMENT 'cron执行表达式',
  misfire_policy VARCHAR(20)  DEFAULT '3' COMMENT '计划执行错误策略 1立即 2执行一次 3放弃',
  concurrent    CHAR(1)      DEFAULT '1' COMMENT '是否并发执行 0允许 1禁止',
  status        CHAR(1)      DEFAULT '0' COMMENT '状态 0正常 1暂停',
  remark        VARCHAR(500) DEFAULT NULL,
  create_by     VARCHAR(64) DEFAULT '',
  create_time   DATETIME    DEFAULT NULL,
  update_by     VARCHAR(64) DEFAULT '',
  update_time   DATETIME    DEFAULT NULL,
  PRIMARY KEY (job_id, job_name, job_group)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='定时任务调度表';

-- ----------------------------
-- 17. 定时任务日志表
-- ----------------------------
DROP TABLE IF EXISTS sys_job_log;
CREATE TABLE sys_job_log (
  job_log_id    BIGINT NOT NULL AUTO_INCREMENT COMMENT '任务日志ID',
  job_name      VARCHAR(64)  NOT NULL COMMENT '任务名称',
  job_group     VARCHAR(64)  NOT NULL COMMENT '任务组名',
  invoke_target VARCHAR(500) NOT NULL COMMENT '调用目标字符串',
  job_message   VARCHAR(500) DEFAULT NULL COMMENT '日志信息',
  status        CHAR(1)      DEFAULT '0' COMMENT '执行状态 0正常 1失败',
  exception_info TEXT        DEFAULT NULL COMMENT '异常信息',
  job_time      DATETIME     DEFAULT NULL COMMENT '执行时间',
  PRIMARY KEY (job_log_id)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='定时任务日志表';

-- ----------------------------
-- 18. 流程定义扩展表
-- ----------------------------
DROP TABLE IF EXISTS wf_definition_ext;
CREATE TABLE wf_definition_ext (
  id            BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  process_key   VARCHAR(100) NOT NULL COMMENT '流程唯一key',
  process_name  VARCHAR(100) DEFAULT '' COMMENT '流程名称',
  version       INT NOT NULL DEFAULT 1 COMMENT '版本号',
  business_type VARCHAR(100) DEFAULT NULL COMMENT '对应业务类型',
  form_key      VARCHAR(100) DEFAULT NULL COMMENT '关联动态表单key',
  category      VARCHAR(50)  DEFAULT NULL COMMENT '流程分类',
  bpmn_xml      LONGTEXT     DEFAULT NULL COMMENT 'BPMN 2.0 XML',
  deployment_id VARCHAR(64)  DEFAULT NULL COMMENT '部署ID',
  deploy_status CHAR(1)      DEFAULT '0' COMMENT '部署状态 0未部署 1已部署',
  status        CHAR(1)      DEFAULT '0' COMMENT '状态 0启用 1停用',
  remark        VARCHAR(500) DEFAULT NULL,
  create_by     VARCHAR(64) DEFAULT '',
  create_time   DATETIME    DEFAULT NULL,
  update_by     VARCHAR(64) DEFAULT '',
  update_time   DATETIME    DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_key_version (process_key, version)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='流程定义扩展表';

-- ----------------------------
-- 19. 动态表单表
-- ----------------------------
DROP TABLE IF EXISTS wf_form;
CREATE TABLE wf_form (
  id           BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  form_name    VARCHAR(100) DEFAULT '' COMMENT '表单名称',
  form_key     VARCHAR(100) NOT NULL COMMENT '表单唯一key',
  form_content LONGTEXT     DEFAULT NULL COMMENT '表单 Schema JSON',
  status       CHAR(1)      DEFAULT '0' COMMENT '状态 0启用 1停用',
  remark       VARCHAR(500) DEFAULT NULL,
  create_by    VARCHAR(64) DEFAULT '',
  create_time  DATETIME    DEFAULT NULL,
  update_by    VARCHAR(64) DEFAULT '',
  update_time  DATETIME    DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_form_key (form_key)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='动态表单表';

-- ----------------------------
-- 20. 业务流程实例关联表
-- ----------------------------
DROP TABLE IF EXISTS wf_business;
CREATE TABLE wf_business (
  id                  BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  process_key         VARCHAR(100) NOT NULL COMMENT '流程key',
  process_instance_id VARCHAR(64)  NOT NULL COMMENT '流程实例ID(唯一)',
  business_id         VARCHAR(64)  NOT NULL COMMENT '业务表主键',
  business_title      VARCHAR(200) DEFAULT '' COMMENT '业务标题',
  status              VARCHAR(20)  NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/RUNNING/APPROVED/REJECTED/CANCELED',
  current_node        VARCHAR(100) DEFAULT '' COMMENT '当前节点key',
  applicant_user_id   BIGINT       NOT NULL COMMENT '发起人',
  form_data           LONGTEXT     DEFAULT NULL COMMENT '表单数据JSON',
  create_time         DATETIME     DEFAULT NULL,
  update_time         DATETIME     DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_instance_id (process_instance_id)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='业务流程实例关联表';

-- ----------------------------
-- 21. 流程节点扩展配置表
-- ----------------------------
DROP TABLE IF EXISTS wf_node_config;
CREATE TABLE wf_node_config (
  id             BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  definition_id  BIGINT NOT NULL COMMENT '流程定义ID',
  node_key       VARCHAR(100) NOT NULL COMMENT '节点key',
  node_name      VARCHAR(100) DEFAULT '' COMMENT '节点名称',
  node_order     INT          DEFAULT 0 COMMENT '节点顺序(简易引擎按此顺序流转)',
  assignee_type  VARCHAR(20)  DEFAULT NULL COMMENT 'USER/ROLE/DEPT_LEADER/EXPRESSION',
  assignee_value VARCHAR(200) DEFAULT NULL COMMENT '审批人/角色key/表达式',
  approve_mode   VARCHAR(20)  DEFAULT 'ANY' COMMENT 'ANY任一通过 ALL会签',
  form_permission LONGTEXT    DEFAULT NULL COMMENT '字段权限JSON {field:read/write/hidden}',
  PRIMARY KEY (id),
  UNIQUE KEY uk_def_node (definition_id, node_key)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='流程节点扩展配置表';

-- ----------------------------
-- 22. 流程任务运行表（简化引擎运行时任务）
-- ----------------------------
DROP TABLE IF EXISTS wf_task;
CREATE TABLE wf_task (
  id                  BIGINT NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  process_instance_id VARCHAR(64) NOT NULL COMMENT '流程实例ID',
  business_id         BIGINT      NOT NULL COMMENT 'wf_business.id',
  node_key            VARCHAR(100) DEFAULT '' COMMENT '节点key',
  node_name           VARCHAR(100) DEFAULT '' COMMENT '节点名称',
  assignee_id         BIGINT      DEFAULT NULL COMMENT '办理人ID',
  status              VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/DONE/CANCELED',
  result              VARCHAR(20) DEFAULT NULL COMMENT 'APPROVE/REJECT',
  comment             VARCHAR(500) DEFAULT NULL COMMENT '审批意见',
  create_time         DATETIME    DEFAULT NULL,
  finish_time         DATETIME    DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_assignee (assignee_id, status)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='流程任务运行表';

-- ----------------------------
-- 23. 流程操作日志表
-- ----------------------------
DROP TABLE IF EXISTS wf_operation_log;
CREATE TABLE wf_operation_log (
  id                  BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  process_instance_id VARCHAR(64) NOT NULL COMMENT '流程实例ID',
  task_id             BIGINT      DEFAULT NULL COMMENT '任务ID',
  node_key            VARCHAR(100) DEFAULT '' COMMENT '节点key',
  operator_user_id    BIGINT      NOT NULL COMMENT '操作人',
  operation           VARCHAR(20) NOT NULL COMMENT 'START/APPROVE/REJECT/DELEGATE/TRANSFER/CANCEL',
  opinion             VARCHAR(500) DEFAULT NULL COMMENT '意见',
  create_time         DATETIME    DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_instance (process_instance_id)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='流程操作日志表';

-- ----------------------------
-- 24. 大屏项目表
-- ----------------------------
DROP TABLE IF EXISTS screen_project;
CREATE TABLE screen_project (
  project_id   BIGINT NOT NULL AUTO_INCREMENT COMMENT '项目ID',
  project_name VARCHAR(100) NOT NULL COMMENT '项目名称',
  project_code VARCHAR(100) DEFAULT '' COMMENT '项目编码',
  description  VARCHAR(500) DEFAULT '' COMMENT '描述',
  cover_url    VARCHAR(500) DEFAULT '' COMMENT '封面图',
  status       CHAR(1)      DEFAULT '0' COMMENT '状态 0草稿 1已发布',
  publish_url  VARCHAR(500) DEFAULT '' COMMENT '分享链接标识',
  create_by    VARCHAR(64) DEFAULT '',
  create_time  DATETIME    DEFAULT NULL,
  update_by    VARCHAR(64) DEFAULT '',
  update_time  DATETIME    DEFAULT NULL,
  del_flag     CHAR(1)     DEFAULT '0' COMMENT '删除标志',
  PRIMARY KEY (project_id)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='大屏项目表';

-- ----------------------------
-- 25. 大屏页面表
-- ----------------------------
DROP TABLE IF EXISTS screen_page;
CREATE TABLE screen_page (
  page_id        BIGINT NOT NULL AUTO_INCREMENT COMMENT '页面ID',
  project_id     BIGINT NOT NULL COMMENT '项目ID',
  page_name      VARCHAR(100) DEFAULT '主页面' COMMENT '页面名称',
  canvas_config  LONGTEXT NOT NULL COMMENT '画布配置JSON 宽高/背景/缩放',
  component_tree LONGTEXT NOT NULL COMMENT '组件树JSON',
  create_by      VARCHAR(64) DEFAULT '',
  create_time    DATETIME    DEFAULT NULL,
  update_by      VARCHAR(64) DEFAULT '',
  update_time    DATETIME    DEFAULT NULL,
  PRIMARY KEY (page_id),
  KEY idx_project (project_id)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='大屏页面表';

-- ----------------------------
-- 26. 大屏数据集表
-- ----------------------------
DROP TABLE IF EXISTS screen_dataset;
CREATE TABLE screen_dataset (
  dataset_id       BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据集ID',
  dataset_name     VARCHAR(100) NOT NULL COMMENT '数据集名称',
  project_id       BIGINT DEFAULT NULL COMMENT '所属项目',
  datasource_id    BIGINT NOT NULL COMMENT '数据源ID(ai_datasource)',
  sql_text         TEXT   NOT NULL COMMENT 'SQL语句 支持:param占位',
  params_schema    LONGTEXT DEFAULT NULL COMMENT '参数声明JSON',
  refresh_interval INT    DEFAULT 0 COMMENT '自动刷新秒数 0不刷新',
  remark           VARCHAR(500) DEFAULT NULL,
  create_by        VARCHAR(64) DEFAULT '',
  create_time      DATETIME    DEFAULT NULL,
  update_by        VARCHAR(64) DEFAULT '',
  update_time      DATETIME    DEFAULT NULL,
  PRIMARY KEY (dataset_id)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='大屏数据集表';

-- ----------------------------
-- 27. 大屏组件注册表
-- ----------------------------
DROP TABLE IF EXISTS screen_component;
CREATE TABLE screen_component (
  component_id    BIGINT NOT NULL AUTO_INCREMENT COMMENT '组件ID',
  component_code  VARCHAR(100) NOT NULL COMMENT '组件编码 前端注册key',
  component_name  VARCHAR(100) NOT NULL COMMENT '组件名称',
  component_type  VARCHAR(50)  DEFAULT 'chart' COMMENT '类型 chart/text/border/media',
  icon            VARCHAR(200) DEFAULT '' COMMENT '图标',
  default_config  LONGTEXT     DEFAULT NULL COMMENT '默认配置JSON',
  default_width   INT DEFAULT 400 COMMENT '默认宽度',
  default_height  INT DEFAULT 300 COMMENT '默认高度',
  order_num       INT DEFAULT 0 COMMENT '排序',
  status          CHAR(1) DEFAULT '0' COMMENT '状态 0启用 1停用',
  PRIMARY KEY (component_id),
  UNIQUE KEY uk_component_code (component_code)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='大屏组件注册表';

-- ----------------------------
-- 28. 报表页表
-- ----------------------------
DROP TABLE IF EXISTS report_page;
CREATE TABLE report_page (
  page_id     BIGINT NOT NULL AUTO_INCREMENT COMMENT '报表页ID',
  page_name   VARCHAR(100) NOT NULL COMMENT '报表名称',
  page_code   VARCHAR(100) DEFAULT '' COMMENT '报表编码',
  layout_config LONGTEXT   DEFAULT NULL COMMENT '栅格布局JSON',
  remark      VARCHAR(500) DEFAULT NULL,
  status      CHAR(1)      DEFAULT '0' COMMENT '状态 0草稿 1已发布',
  create_by   VARCHAR(64) DEFAULT '',
  create_time DATETIME    DEFAULT NULL,
  update_by   VARCHAR(64) DEFAULT '',
  update_time DATETIME    DEFAULT NULL,
  del_flag    CHAR(1)      DEFAULT '0' COMMENT '删除标志',
  PRIMARY KEY (page_id)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='报表页表';

-- ----------------------------
-- 29. 报表数据集表
-- ----------------------------
DROP TABLE IF EXISTS report_dataset;
CREATE TABLE report_dataset (
  dataset_id    BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据集ID',
  dataset_name  VARCHAR(100) NOT NULL COMMENT '数据集名称',
  datasource_id BIGINT NOT NULL COMMENT '数据源ID',
  sql_text      TEXT NOT NULL COMMENT '只读SELECT语句',
  row_limit     INT DEFAULT 1000 COMMENT '强制LIMIT防大查询',
  params_schema LONGTEXT DEFAULT NULL COMMENT '参数声明JSON',
  remark        VARCHAR(500) DEFAULT NULL,
  create_by     VARCHAR(64) DEFAULT '',
  create_time   DATETIME    DEFAULT NULL,
  update_by     VARCHAR(64) DEFAULT '',
  update_time   DATETIME    DEFAULT NULL,
  PRIMARY KEY (dataset_id)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='报表数据集表';

-- ----------------------------
-- 30. 报表图表配置表
-- ----------------------------
DROP TABLE IF EXISTS report_chart;
CREATE TABLE report_chart (
  chart_id     BIGINT NOT NULL AUTO_INCREMENT COMMENT '图表ID',
  page_id      BIGINT NOT NULL COMMENT '所属报表页',
  chart_name   VARCHAR(100) DEFAULT '' COMMENT '图表名称',
  chart_type   VARCHAR(30) NOT NULL COMMENT 'BAR/LINE/PIE/TABLE/MAP/FUNNEL',
  dataset_id   BIGINT      DEFAULT NULL COMMENT '绑定数据集',
  chart_option LONGTEXT    DEFAULT NULL COMMENT 'ECharts option JSON',
  x_field      VARCHAR(100) DEFAULT '' COMMENT 'X轴/维度字段',
  y_field      VARCHAR(100) DEFAULT '' COMMENT 'Y轴/度量字段',
  width        INT DEFAULT 6 COMMENT '栅格宽 1-24',
  height       INT DEFAULT 300 COMMENT '高度px',
  sort_num     INT DEFAULT 0 COMMENT '排序',
  remark       VARCHAR(500) DEFAULT NULL,
  create_by    VARCHAR(64) DEFAULT '',
  create_time  DATETIME    DEFAULT NULL,
  update_by    VARCHAR(64) DEFAULT '',
  update_time  DATETIME    DEFAULT NULL,
  PRIMARY KEY (chart_id),
  KEY idx_page (page_id)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='报表图表配置表';

-- ----------------------------
-- 31. 代码生成业务表
-- ----------------------------
DROP TABLE IF EXISTS gen_table;
CREATE TABLE gen_table (
  table_id      BIGINT NOT NULL AUTO_INCREMENT COMMENT '编号',
  table_name    VARCHAR(200) DEFAULT '' COMMENT '表名称',
  table_comment VARCHAR(500) DEFAULT '' COMMENT '表描述',
  class_name    VARCHAR(100) DEFAULT '' COMMENT '实体类名称',
  module_name   VARCHAR(30)  DEFAULT NULL COMMENT '生成模块名',
  business_name VARCHAR(30)  DEFAULT NULL COMMENT '生成业务名',
  function_name VARCHAR(50)  DEFAULT NULL COMMENT '生成功能名',
  author_name   VARCHAR(50)  DEFAULT NULL COMMENT '作者',
  tpl_category  VARCHAR(200) DEFAULT 'crud' COMMENT '使用的模板 crud/tree/sub',
  package_name  VARCHAR(100) DEFAULT NULL COMMENT '生成包路径',
  gen_type      CHAR(1)      DEFAULT '0' COMMENT '生成代码方式 0zip 1自定义路径',
  gen_path      VARCHAR(200) DEFAULT '/' COMMENT '生成路径',
  options       VARCHAR(1000) DEFAULT NULL COMMENT '其它生成选项',
  remark        VARCHAR(500) DEFAULT NULL,
  create_by     VARCHAR(64) DEFAULT '',
  create_time   DATETIME    DEFAULT NULL,
  update_by     VARCHAR(64) DEFAULT '',
  update_time   DATETIME    DEFAULT NULL,
  PRIMARY KEY (table_id),
  UNIQUE KEY uk_table_name (table_name(190))
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='代码生成业务表';

-- ----------------------------
-- 32. 代码生成业务表字段
-- ----------------------------
DROP TABLE IF EXISTS gen_table_column;
CREATE TABLE gen_table_column (
  column_id     BIGINT NOT NULL AUTO_INCREMENT COMMENT '编号',
  table_id      BIGINT DEFAULT NULL COMMENT '归属表编号',
  column_name   VARCHAR(200) DEFAULT '' COMMENT '列名称',
  column_comment VARCHAR(500) DEFAULT '' COMMENT '列描述',
  column_type   VARCHAR(100) DEFAULT '' COMMENT '列类型',
  java_type     VARCHAR(500) DEFAULT NULL COMMENT 'JAVA类型',
  java_field    VARCHAR(200) DEFAULT NULL COMMENT 'JAVA字段名',
  is_pk         CHAR(1)      DEFAULT '0' COMMENT '是否主键 1是',
  is_increment  CHAR(1)      DEFAULT '1' COMMENT '是否自增 1是',
  is_required   CHAR(1)      DEFAULT '0' COMMENT '是否必填 1是',
  is_insert     CHAR(1)      DEFAULT '1' COMMENT '是否为插入字段',
  is_edit       CHAR(1)      DEFAULT '1' COMMENT '是否编辑字段',
  is_list       CHAR(1)      DEFAULT '1' COMMENT '是否列表字段',
  is_query      CHAR(1)      DEFAULT '0' COMMENT '是否查询字段',
  query_type    VARCHAR(200) DEFAULT 'EQ' COMMENT '查询方式 EQ/LIKE/BETWEEN/NE/GT/LT',
  html_type     VARCHAR(200) DEFAULT NULL COMMENT '显示类型 input/textarea/select/radio/checkbox/datetime',
  dict_type     VARCHAR(200) DEFAULT '' COMMENT '字典类型',
  sort_num      INT DEFAULT 0 COMMENT '排序',
  remark        VARCHAR(500) DEFAULT NULL,
  create_by     VARCHAR(64) DEFAULT '',
  create_time   DATETIME    DEFAULT NULL,
  update_by     VARCHAR(64) DEFAULT '',
  update_time   DATETIME    DEFAULT NULL,
  PRIMARY KEY (column_id),
  KEY idx_table_id (table_id)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='代码生成业务表字段';

-- ----------------------------
-- 33. AI 数据源表
-- ----------------------------
DROP TABLE IF EXISTS ai_datasource;
CREATE TABLE ai_datasource (
  datasource_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据源ID',
  datasource_name VARCHAR(100) NOT NULL COMMENT '数据源名称',
  db_type       VARCHAR(20)  NOT NULL COMMENT '数据库类型 mysql/pg/clickhouse',
  jdbc_url      VARCHAR(500) NOT NULL COMMENT 'JDBC连接串',
  username      VARCHAR(100) DEFAULT '' COMMENT '用户名',
  password      VARCHAR(200) DEFAULT '' COMMENT '密码 AES加密存储',
  db_name       VARCHAR(100) DEFAULT '' COMMENT 'NL2SQL默认库',
  readonly_user CHAR(1)      DEFAULT '1' COMMENT '是否只读账号 1是 0否',
  status        CHAR(1)      DEFAULT '0' COMMENT '状态 0启用 1停用',
  remark        VARCHAR(500) DEFAULT NULL,
  create_by     VARCHAR(64) DEFAULT '',
  create_time   DATETIME    DEFAULT NULL,
  update_by     VARCHAR(64) DEFAULT '',
  update_time   DATETIME    DEFAULT NULL,
  PRIMARY KEY (datasource_id)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='AI数据源表';

-- ----------------------------
-- 34. AI 会话表
-- ----------------------------
DROP TABLE IF EXISTS ai_chat_session;
CREATE TABLE ai_chat_session (
  session_id    BIGINT NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  session_title VARCHAR(200) DEFAULT '新会话' COMMENT '会话标题',
  datasource_id BIGINT DEFAULT NULL COMMENT '默认数据源',
  user_id       BIGINT NOT NULL COMMENT '所属用户',
  create_time   DATETIME DEFAULT NULL,
  update_time   DATETIME DEFAULT NULL,
  PRIMARY KEY (session_id)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='AI会话表';

-- ----------------------------
-- 35. AI 消息表
-- ----------------------------
DROP TABLE IF EXISTS ai_chat_message;
CREATE TABLE ai_chat_message (
  message_id    BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  session_id    BIGINT NOT NULL COMMENT '会话ID',
  role          VARCHAR(20) NOT NULL COMMENT 'user/assistant',
  content       MEDIUMTEXT  NOT NULL COMMENT '消息内容',
  generated_sql TEXT        DEFAULT NULL COMMENT 'AI产出SQL',
  chart_type    VARCHAR(30) DEFAULT NULL COMMENT '推荐图表类型',
  chart_data    MEDIUMTEXT  DEFAULT NULL COMMENT '结果数据缓存JSON',
  status        VARCHAR(20) DEFAULT 'OK' COMMENT 'OK/ERROR',
  create_time   DATETIME    DEFAULT NULL,
  PRIMARY KEY (message_id),
  KEY idx_session (session_id)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='AI消息表';

-- ----------------------------
-- 36. AI Schema 缓存表
-- ----------------------------
DROP TABLE IF EXISTS ai_schema_cache;
CREATE TABLE ai_schema_cache (
  cache_id       BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  datasource_id  BIGINT NOT NULL COMMENT '数据源ID',
  table_name     VARCHAR(200) NOT NULL COMMENT '表名',
  table_comment  VARCHAR(500) DEFAULT '' COMMENT '表注释',
  columns_json   MEDIUMTEXT NOT NULL COMMENT '字段JSON [{name,type,comment}]',
  sample_rows    TEXT       DEFAULT NULL COMMENT '脱敏样例数据',
  refresh_time   DATETIME   DEFAULT NULL COMMENT '采集时间',
  PRIMARY KEY (cache_id),
  UNIQUE KEY uk_ds_table (datasource_id, table_name(190))
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='AI Schema缓存表';

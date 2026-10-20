-- =====================================================================
-- sudutech 快速开发平台 初始化数据
-- 默认管理员: admin / admin123
-- =====================================================================
USE sudutech_lite;

-- ----------------------------
-- 1. 部门
-- ----------------------------
INSERT INTO sys_dept VALUES(100, 0, '0',   'sudutech科技', 0, NULL, '', '', '0', '0', 'admin', NOW(), '', NULL);
INSERT INTO sys_dept VALUES(101, 100, '0,100', '研发部门', 1, NULL, '', '', '0', '0', 'admin', NOW(), '', NULL);
INSERT INTO sys_dept VALUES(102, 100, '0,100', '市场部门', 2, NULL, '', '', '0', '0', 'admin', NOW(), '', NULL);
INSERT INTO sys_dept VALUES(103, 100, '0,100', '财务部门', 3, NULL, '', '', '0', '0', 'admin', NOW(), '', NULL);
INSERT INTO sys_dept VALUES(104, 100, '0,100', '人力资源部', 4, NULL, '', '', '0', '0', 'admin', NOW(), '', NULL);

-- ----------------------------
-- 2. 用户 (admin/admin123)
-- ----------------------------
INSERT INTO sys_user VALUES(1, 101, 'admin', '平台管理员', 'admin@sudutech.com', '15888888888', '0', '', '$2a$10$sRD6Pq/X3t6SvyvO/3Z8aOwJwpvDLcqEks30dKp2byynRCCNAqWc6', '0', '0', '127.0.0.1', NOW(), '管理员', 'admin', NOW(), '', NULL);
INSERT INTO sys_user VALUES(2, 101, 'demo', '演示用户', 'demo@sudutech.com', '15666666666', '1', '', '$2a$10$sRD6Pq/X3t6SvyvO/3Z8aOwJwpvDLcqEks30dKp2byynRCCNAqWc6', '0', '0', '127.0.0.1', NOW(), '演示账号', 'admin', NOW(), '', NULL);

-- ----------------------------
-- 3. 岗位
-- ----------------------------
INSERT INTO sys_post VALUES(1, 'ceo',  '董事长',   1, '0', '', 'admin', NOW(), '', NULL);
INSERT INTO sys_post VALUES(2, 'pm',   '项目经理', 2, '0', '', 'admin', NOW(), '', NULL);
INSERT INTO sys_post VALUES(3, 'hr',   '人力资源', 3, '0', '', 'admin', NOW(), '', NULL);
INSERT INTO sys_post VALUES(4, 'staff','普通员工', 4, '0', '', 'admin', NOW(), '', NULL);

-- ----------------------------
-- 4. 角色
-- ----------------------------
INSERT INTO sys_role VALUES(1, '超级管理员', 'admin',  1, '1', '0', '0', '超级管理员(内置)', 'admin', NOW(), '', NULL);
INSERT INTO sys_role VALUES(2, '普通角色',   'common', 2, '3', '0', '0', '普通角色', 'admin', NOW(), '', NULL);

-- ----------------------------
-- 5. 用户角色/岗位关联
-- ----------------------------
INSERT INTO sys_user_role VALUES(1, 1);
INSERT INTO sys_user_role VALUES(2, 2);
INSERT INTO sys_user_post VALUES(1, 1);
INSERT INTO sys_user_post VALUES(2, 4);

-- ----------------------------
-- 6. 菜单 (目录)
-- ----------------------------
INSERT INTO sys_menu VALUES(1, '系统管理', 0, 1, 'system',    NULL, 1, 'M', '0', '0', '', 'system',   'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(2, '系统监控', 0, 2, 'monitor',   NULL, 1, 'M', '0', '0', '', 'monitor',  'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(3, '系统工具', 0, 3, 'tool',      NULL, 1, 'M', '0', '0', '', 'tool',     'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(4, '工作流',   0, 4, 'workflow',  NULL, 1, 'M', '0', '0', '', 'workflow', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(5, '数据大屏', 0, 5, 'screen',    NULL, 1, 'M', '0', '0', '', 'screen',   'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(6, '报表中心', 0, 6, 'report',    NULL, 1, 'M', '0', '0', '', 'report',   'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(7, 'AI助手',   0, 7, 'ai',        NULL, 1, 'M', '0', '0', '', 'ai',       'admin', NOW(), '', NULL);

-- ----------------------------
-- 7. 菜单 (页面)
-- ----------------------------
INSERT INTO sys_menu VALUES(100, '用户管理', 1, 1, 'user',     'system/user/index',     1, 'C', '0', '0', 'system:user:list',      'user',     'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(101, '角色管理', 1, 2, 'role',     'system/role/index',     1, 'C', '0', '0', 'system:role:list',      'peoples',  'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(102, '菜单管理', 1, 3, 'menu',     'system/menu/index',     1, 'C', '0', '0', 'system:menu:list',      'tree-table','admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(103, '部门管理', 1, 4, 'dept',     'system/dept/index',     1, 'C', '0', '0', 'system:dept:list',      'tree',     'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(104, '岗位管理', 1, 5, 'post',     'system/post/index',     1, 'C', '0', '0', 'system:post:list',      'post',     'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(105, '字典管理', 1, 6, 'dict',     'system/dict/index',     1, 'C', '0', '0', 'system:dict:list',      'dict',     'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(106, '参数设置', 1, 7, 'config',   'system/config/index',   1, 'C', '0', '0', 'system:config:list',    'edit',     'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(107, '通知公告', 1, 8, 'notice',   'system/notice/index',   1, 'C', '0', '0', 'system:notice:list',    'message',  'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(108, '在线用户', 2, 1, 'online',   'monitor/online/index',  1, 'C', '0', '0', 'monitor:online:list',   'online',   'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(109, '定时任务', 2, 2, 'job',      'monitor/job/index',     1, 'C', '0', '0', 'monitor:job:list',      'job',      'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(110, '操作日志', 2, 3, 'operlog',  'monitor/operlog/index', 1, 'C', '0', '0', 'system:oper:list',      'form',     'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(111, '登录日志', 2, 4, 'loginlog', 'monitor/loginlog/index',1, 'C', '0', '0', 'system:loginlog:list',  'logininfor','admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(112, '代码生成', 3, 1, 'gentable', 'gen/table/index',       1, 'C', '0', '0', 'gen:table:list',        'code',     'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(113, '数据源管理', 3, 2, 'datasource','ai/datasource/index', 1, 'C', '0', '0', 'ai:datasource:list',    'database', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(114, '流程定义', 4, 1, 'definition','workflow/definition/index', 1, 'C', '0', '0', 'wf:def:list',       'tree-table','admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(115, '流程表单', 4, 2, 'wfform',   'workflow/form/index',   1, 'C', '0', '0', 'wf:form:list',          'form',     'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(116, '大屏项目', 5, 1, 'project',  'screen/project/index',  1, 'C', '0', '0', 'screen:project:list',   'screen',   'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(117, '报表管理', 6, 1, 'reportpage','report/page/index',    1, 'C', '0', '0', 'report:page:list',      'chart',    'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(118, 'AI问数',   7, 1, 'chat',     'ai/chat/index',         1, 'C', '0', '0', 'ai:chat:list',          'robot',    'admin', NOW(), '', NULL);

-- ----------------------------
-- 8. 菜单 (按钮)
-- ----------------------------
INSERT INTO sys_menu VALUES(1001, '用户查询', 100, 1, '', '', 1, 'F', '0', '0', 'system:user:query',    '#', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(1002, '用户新增', 100, 2, '', '', 1, 'F', '0', '0', 'system:user:add',      '#', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(1003, '用户修改', 100, 3, '', '', 1, 'F', '0', '0', 'system:user:edit',     '#', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(1004, '用户删除', 100, 4, '', '', 1, 'F', '0', '0', 'system:user:remove',   '#', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(1005, '重置密码', 100, 5, '', '', 1, 'F', '0', '0', 'system:user:resetPwd', '#', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(1006, '角色查询', 101, 1, '', '', 1, 'F', '0', '0', 'system:role:query',    '#', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(1007, '角色新增', 101, 2, '', '', 1, 'F', '0', '0', 'system:role:add',      '#', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(1008, '角色修改', 101, 3, '', '', 1, 'F', '0', '0', 'system:role:edit',     '#', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(1009, '角色删除', 101, 4, '', '', 1, 'F', '0', '0', 'system:role:remove',   '#', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(1010, '菜单查询', 102, 1, '', '', 1, 'F', '0', '0', 'system:menu:query',    '#', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(1011, '菜单新增', 102, 2, '', '', 1, 'F', '0', '0', 'system:menu:add',      '#', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(1012, '菜单修改', 102, 3, '', '', 1, 'F', '0', '0', 'system:menu:edit',     '#', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(1013, '菜单删除', 102, 4, '', '', 1, 'F', '0', '0', 'system:menu:remove',   '#', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(1014, '部门查询', 103, 1, '', '', 1, 'F', '0', '0', 'system:dept:query',    '#', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(1015, '部门新增', 103, 2, '', '', 1, 'F', '0', '0', 'system:dept:add',      '#', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(1016, '部门修改', 103, 3, '', '', 1, 'F', '0', '0', 'system:dept:edit',     '#', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(1017, '部门删除', 103, 4, '', '', 1, 'F', '0', '0', 'system:dept:remove',   '#', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(1018, '岗位查询', 104, 1, '', '', 1, 'F', '0', '0', 'system:post:query',    '#', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(1019, '岗位新增', 104, 2, '', '', 1, 'F', '0', '0', 'system:post:add',      '#', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(1020, '岗位修改', 104, 3, '', '', 1, 'F', '0', '0', 'system:post:edit',     '#', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(1021, '岗位删除', 104, 4, '', '', 1, 'F', '0', '0', 'system:post:remove',   '#', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(1022, '字典查询', 105, 1, '', '', 1, 'F', '0', '0', 'system:dict:query',    '#', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(1023, '字典新增', 105, 2, '', '', 1, 'F', '0', '0', 'system:dict:add',      '#', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(1024, '字典修改', 105, 3, '', '', 1, 'F', '0', '0', 'system:dict:edit',     '#', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(1025, '字典删除', 105, 4, '', '', 1, 'F', '0', '0', 'system:dict:remove',   '#', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(1026, '参数查询', 106, 1, '', '', 1, 'F', '0', '0', 'system:config:query',  '#', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(1027, '参数新增', 106, 2, '', '', 1, 'F', '0', '0', 'system:config:add',    '#', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(1028, '参数修改', 106, 3, '', '', 1, 'F', '0', '0', 'system:config:edit',   '#', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(1029, '参数删除', 106, 4, '', '', 1, 'F', '0', '0', 'system:config:remove', '#', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(1030, '公告查询', 107, 1, '', '', 1, 'F', '0', '0', 'system:notice:query',  '#', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(1031, '公告新增', 107, 2, '', '', 1, 'F', '0', '0', 'system:notice:add',    '#', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(1032, '公告修改', 107, 3, '', '', 1, 'F', '0', '0', 'system:notice:edit',   '#', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(1033, '公告删除', 107, 4, '', '', 1, 'F', '0', '0', 'system:notice:remove', '#', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(1034, '任务查询', 109, 1, '', '', 1, 'F', '0', '0', 'monitor:job:query',    '#', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(1035, '任务新增', 109, 2, '', '', 1, 'F', '0', '0', 'monitor:job:add',      '#', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(1036, '任务修改', 109, 3, '', '', 1, 'F', '0', '0', 'monitor:job:edit',     '#', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(1037, '任务删除', 109, 4, '', '', 1, 'F', '0', '0', 'monitor:job:remove',   '#', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(1038, '任务执行', 109, 5, '', '', 1, 'F', '0', '0', 'monitor:job:run',      '#', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(1039, '生成查询', 112, 1, '', '', 1, 'F', '0', '0', 'gen:table:query',      '#', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(1040, '导入表',   112, 2, '', '', 1, 'F', '0', '0', 'gen:table:import',     '#', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(1041, '生成修改', 112, 3, '', '', 1, 'F', '0', '0', 'gen:table:edit',       '#', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(1042, '生成删除', 112, 4, '', '', 1, 'F', '0', '0', 'gen:table:remove',     '#', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(1043, '预览代码', 112, 5, '', '', 1, 'F', '0', '0', 'gen:table:preview',    '#', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(1044, '生成代码', 112, 6, '', '', 1, 'F', '0', '0', 'gen:table:genCode',    '#', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(1045, '数据源查询', 113, 1, '', '', 1, 'F', '0', '0', 'ai:datasource:query',   '#', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(1046, '数据源新增', 113, 2, '', '', 1, 'F', '0', '0', 'ai:datasource:add',     '#', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(1047, '数据源修改', 113, 3, '', '', 1, 'F', '0', '0', 'ai:datasource:edit',    '#', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(1048, '数据源删除', 113, 4, '', '', 1, 'F', '0', '0', 'ai:datasource:remove',  '#', 'admin', NOW(), '', NULL);
INSERT INTO sys_menu VALUES(1049, 'Schema采集', 113, 5, '', '', 1, 'F', '0', '0', 'ai:datasource:refresh','#', 'admin', NOW(), '', NULL);

-- ----------------------------
-- 9. 普通角色菜单授权 (系统管理只读示例)
-- ----------------------------
INSERT INTO sys_role_menu VALUES(2, 1);
INSERT INTO sys_role_menu VALUES(2, 100);

-- ----------------------------
-- 10. 字典类型
-- ----------------------------
INSERT INTO sys_dict_type VALUES(1, '系统状态', 'sys_normal_status',   '0', '', 'admin', NOW(), '', NULL);
INSERT INTO sys_dict_type VALUES(2, '用户性别', 'sys_user_sex',        '0', '', 'admin', NOW(), '', NULL);
INSERT INTO sys_dict_type VALUES(3, '通知类型', 'sys_notice_type',     '0', '', 'admin', NOW(), '', NULL);
INSERT INTO sys_dict_type VALUES(4, '通知状态', 'sys_notice_status',   '0', '', 'admin', NOW(), '', NULL);
INSERT INTO sys_dict_type VALUES(5, '操作类型', 'sys_oper_type',       '0', '', 'admin', NOW(), '', NULL);
INSERT INTO sys_dict_type VALUES(6, '系统是否', 'sys_yes_no',          '0', '', 'admin', NOW(), '', NULL);
INSERT INTO sys_dict_type VALUES(7, '图表类型', 'sys_chart_type',      '0', '', 'admin', NOW(), '', NULL);
INSERT INTO sys_dict_type VALUES(8, '审批人类型', 'wf_assignee_type',  '0', '', 'admin', NOW(), '', NULL);
INSERT INTO sys_dict_type VALUES(9, '数据库类型', 'ai_db_type',        '0', '', 'admin', NOW(), '', NULL);

-- ----------------------------
-- 11. 字典数据
-- ----------------------------
INSERT INTO sys_dict_data VALUES(1, 1, '正常', '0', 'sys_normal_status', '', 'primary', 'Y', '0', '', 'admin', NOW(), '', NULL);
INSERT INTO sys_dict_data VALUES(2, 2, '停用', '1', 'sys_normal_status', '', 'danger',  'N', '0', '', 'admin', NOW(), '', NULL);
INSERT INTO sys_dict_data VALUES(3, 1, '男',   '0', 'sys_user_sex', '', '',       'Y', '0', '', 'admin', NOW(), '', NULL);
INSERT INTO sys_dict_data VALUES(4, 2, '女',   '1', 'sys_user_sex', '', '',       'N', '0', '', 'admin', NOW(), '', NULL);
INSERT INTO sys_dict_data VALUES(5, 3, '未知', '2', 'sys_user_sex', '', '',       'N', '0', '', 'admin', NOW(), '', NULL);
INSERT INTO sys_dict_data VALUES(6, 1, '通知', '1', 'sys_notice_type', '', 'warning', 'Y', '0', '', 'admin', NOW(), '', NULL);
INSERT INTO sys_dict_data VALUES(7, 2, '公告', '2', 'sys_notice_type', '', 'success', 'N', '0', '', 'admin', NOW(), '', NULL);
INSERT INTO sys_dict_data VALUES(8, 1, '正常', '0', 'sys_notice_status', '', 'primary', 'Y', '0', '', 'admin', NOW(), '', NULL);
INSERT INTO sys_dict_data VALUES(9, 2, '关闭', '1', 'sys_notice_status', '', 'danger',  'N', '0', '', 'admin', NOW(), '', NULL);
INSERT INTO sys_dict_data VALUES(10, 1, '新增', '1', 'sys_oper_type', '', 'info',    'N', '0', '', 'admin', NOW(), '', NULL);
INSERT INTO sys_dict_data VALUES(11, 2, '修改', '2', 'sys_oper_type', '', 'info',    'N', '0', '', 'admin', NOW(), '', NULL);
INSERT INTO sys_dict_data VALUES(12, 3, '删除', '3', 'sys_oper_type', '', 'danger',  'N', '0', '', 'admin', NOW(), '', NULL);
INSERT INTO sys_dict_data VALUES(13, 4, '授权', '4', 'sys_oper_type', '', 'primary', 'N', '0', '', 'admin', NOW(), '', NULL);
INSERT INTO sys_dict_data VALUES(14, 5, '导出', '5', 'sys_oper_type', '', 'warning', 'N', '0', '', 'admin', NOW(), '', NULL);
INSERT INTO sys_dict_data VALUES(15, 6, '导入', '6', 'sys_oper_type', '', 'warning', 'N', '0', '', 'admin', NOW(), '', NULL);
INSERT INTO sys_dict_data VALUES(16, 7, '强退', '7', 'sys_oper_type', '', 'danger',  'N', '0', '', 'admin', NOW(), '', NULL);
INSERT INTO sys_dict_data VALUES(17, 8, '清空', '8', 'sys_oper_type', '', 'danger',  'N', '0', '', 'admin', NOW(), '', NULL);
INSERT INTO sys_dict_data VALUES(18, 1, '是', 'Y', 'sys_yes_no', '', 'primary', 'Y', '0', '', 'admin', NOW(), '', NULL);
INSERT INTO sys_dict_data VALUES(19, 2, '否', 'N', 'sys_yes_no', '', 'danger',  'N', '0', '', 'admin', NOW(), '', NULL);
INSERT INTO sys_dict_data VALUES(20, 1, '柱状图', 'BAR',    'sys_chart_type', '', 'primary', 'Y', '0', '', 'admin', NOW(), '', NULL);
INSERT INTO sys_dict_data VALUES(21, 2, '折线图', 'LINE',   'sys_chart_type', '', 'success', 'N', '0', '', 'admin', NOW(), '', NULL);
INSERT INTO sys_dict_data VALUES(22, 3, '饼图',   'PIE',    'sys_chart_type', '', 'warning', 'N', '0', '', 'admin', NOW(), '', NULL);
INSERT INTO sys_dict_data VALUES(23, 4, '表格',   'TABLE',  'sys_chart_type', '', 'info',    'N', '0', '', 'admin', NOW(), '', NULL);
INSERT INTO sys_dict_data VALUES(24, 5, '地图',   'MAP',    'sys_chart_type', '', 'primary', 'N', '0', '', 'admin', NOW(), '', NULL);
INSERT INTO sys_dict_data VALUES(25, 6, '漏斗图', 'FUNNEL', 'sys_chart_type', '', 'danger',  'N', '0', '', 'admin', NOW(), '', NULL);
INSERT INTO sys_dict_data VALUES(26, 1, '指定用户',   'USER',        'wf_assignee_type', '', 'primary', 'Y', '0', '', 'admin', NOW(), '', NULL);
INSERT INTO sys_dict_data VALUES(27, 2, '指定角色',   'ROLE',        'wf_assignee_type', '', 'success', 'N', '0', '', 'admin', NOW(), '', NULL);
INSERT INTO sys_dict_data VALUES(28, 3, '部门负责人', 'DEPT_LEADER', 'wf_assignee_type', '', 'warning', 'N', '0', '', 'admin', NOW(), '', NULL);
INSERT INTO sys_dict_data VALUES(29, 4, '表达式',     'EXPRESSION',  'wf_assignee_type', '', 'info',    'N', '0', '', 'admin', NOW(), '', NULL);
INSERT INTO sys_dict_data VALUES(30, 1, 'MySQL',      'mysql',     'ai_db_type', '', 'primary', 'Y', '0', '', 'admin', NOW(), '', NULL);
INSERT INTO sys_dict_data VALUES(31, 2, 'PostgreSQL', 'pg',        'ai_db_type', '', 'success', 'N', '0', '', 'admin', NOW(), '', NULL);
INSERT INTO sys_dict_data VALUES(32, 3, 'ClickHouse', 'clickhouse','ai_db_type', '', 'warning', 'N', '0', '', 'admin', NOW(), '', NULL);

-- ----------------------------
-- 12. 参数配置
-- ----------------------------
INSERT INTO sys_config VALUES(1, '初始密码', 'sys.user.initPassword', '123456', 'Y', '新用户初始密码', 'admin', NOW(), '', NULL);
INSERT INTO sys_config VALUES(2, '是否开启注册', 'sys.account.registerUser', 'false', 'Y', '是否允许自注册', 'admin', NOW(), '', NULL);
INSERT INTO sys_config VALUES(3, '大屏分享开关', 'screen.share.enabled', 'true', 'Y', '发布大屏是否允许匿名访问', 'admin', NOW(), '', NULL);

-- ----------------------------
-- 13. 通知公告
-- ----------------------------
INSERT INTO sys_notice VALUES(1, 'sudutech快速开发平台 v1.0 正式上线', '2', '<p>平台已上线，支持系统管理、代码生成、工作流、大屏可视化、报表与AI问数。</p>', '0', 'admin', NOW(), '', NULL, NULL);

-- ----------------------------
-- 14. 大屏内置组件注册
-- ----------------------------
INSERT INTO screen_component VALUES(1, 'chart-line',   '折线图', 'chart', '', '{"dataType":"dataset","chartType":"line"}', 400, 300, 1, '0');
INSERT INTO screen_component VALUES(2, 'chart-bar',    '柱状图', 'chart', '', '{"dataType":"dataset","chartType":"bar"}',  400, 300, 2, '0');
INSERT INTO screen_component VALUES(3, 'chart-pie',    '饼图',   'chart', '', '{"dataType":"dataset","chartType":"pie"}',  400, 300, 3, '0');
INSERT INTO screen_component VALUES(4, 'chart-gauge',  '仪表盘', 'chart', '', '{"dataType":"dataset","chartType":"gauge"}',300, 300, 4, '0');
INSERT INTO screen_component VALUES(5, 'chart-funnel', '漏斗图', 'chart', '', '{"dataType":"dataset","chartType":"funnel"}',400, 300, 5, '0');
INSERT INTO screen_component VALUES(6, 'chart-table',  '数据表格','chart', '', '{"dataType":"dataset","chartType":"table"}',400, 300, 6, '0');
INSERT INTO screen_component VALUES(7, 'text',         '标题文本','text',  '', '{"text":"标题文本","fontSize":32,"color":"#ffffff","fontWeight":"bold"}', 300, 60, 7, '0');
INSERT INTO screen_component VALUES(8, 'time',         '当前时间','time',  '', '{"fontSize":24,"color":"#00eaff","format":"YYYY-MM-DD HH:mm:ss"}', 300, 60, 8, '0');
INSERT INTO screen_component VALUES(9, 'border',       '边框装饰','border','', '{"borderStyle":"solid","borderColor":"#00eaff","borderWidth":1,"borderRadius":4}', 400, 200, 9, '0');

-- ----------------------------
-- 15. 演示数据源 (指向本平台库自身)
-- ----------------------------
INSERT INTO ai_datasource VALUES(1, '本机演示库', 'mysql', 'jdbc:mysql://127.0.0.1:3306/sudutech_lite?useSSL=false&characterEncoding=utf8&serverTimezone=Asia/Shanghai', 'root', '', 'sudutech_lite', '1', '0', '平台内置演示数据源，可执行SELECT自查元数据', 'admin', NOW(), '', NULL);

-- ----------------------------
-- 16. 演示大屏项目与报表
-- ----------------------------
INSERT INTO screen_project VALUES(1, '业务运营监控大屏', 'ops-demo', '演示大屏：业务运营监控', '', '0', '', 'admin', NOW(), '', NULL, '0');
INSERT INTO screen_page VALUES(1, 1, '主页面', '{"width":1920,"height":1080,"background":"#0b1c33","scaleMode":"fit"}', '[]', 'admin', NOW(), '', NULL);
INSERT INTO report_page VALUES(1, '销售分析报表', 'sales-demo', '[]', '演示报表', '0', 'admin', NOW(), '', NULL, '0');

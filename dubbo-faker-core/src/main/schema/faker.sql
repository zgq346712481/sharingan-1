CREATE TABLE `method_invoke` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `app_id` int(11) DEFAULT NULL COMMENT '项目编号',
  `app_name` varchar(100) DEFAULT NULL COMMENT '项目名称',
  `class_name` varchar(255) DEFAULT NULL COMMENT '接口名称',
  `method_name` varchar(255) DEFAULT NULL COMMENT '方法名称',
  `param_type` text COMMENT '参数类型以,分隔',
  `return_type` varchar(255) DEFAULT NULL COMMENT '返回值类型',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='调用方法表';

CREATE TABLE `invoke_param` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `app_id` int(11) DEFAULT NULL COMMENT '项目编号',
  `type` varchar(11) DEFAULT NULL COMMENT '参数类别',
  `param_value` varchar(255) DEFAULT NULL COMMENT '参数值',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`app_id`,`type`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='调用参数表';

CREATE TABLE `faker_log` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `faker_id` varchar(32) DEFAULT NULL COMMENT '测试序号',
  `invoke_id` int(11) DEFAULT NULL COMMENT '调用方法编号',
  `real_param` varchar(255) DEFAULT NULL COMMENT '实际请求参数',
  `code` smallint(3) unsigned DEFAULT NULL COMMENT '请求结果码',
  `result` text COMMENT '请求返回信息',
  `message` text COMMENT '失败异常',
  `spend_time` int(11) DEFAULT NULL COMMENT '耗时',
  `invoke_time` timestamp NULL DEFAULT NULL COMMENT '请求时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='测试请求表';
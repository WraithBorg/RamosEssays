# 答案
`Author:zxu`  
#### 查看sql查询计划
```
id	select_type	table	partitions	type	possible_keys	key		 key_len	ref		rows	filtered	Extra
1	SIMPLE		lh					ref			status		status	 1		    const	18249	100.00		Using temporary; Using filesort
1	SIMPLE		rd					ALL												    731		100.00		Using where; Using join buffer (Block Nested Loop)

发现没有用到serial索引，继续排查索引失效原因
```
#### 查看原有表结构
```
CREATE TABLE `a` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `serial` char(21) NOT NULL COMMENT '编号',
  `rd_serial` varchar(255) NOT NULL DEFAULT '' COMMENT 'B表编号',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态：1可用，2禁用',
  `create_time` char(11) NOT NULL DEFAULT '0' COMMENT '创建时间',
  `update_time` char(11) NOT NULL DEFAULT '0' COMMENT '更新时间',
  `content` int(11) NOT NULL DEFAULT '0' COMMENT '内容',
  PRIMARY KEY (`id`),
  KEY `serial` (`serial`),
  KEY `status` (`status`,`update_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=36555 DEFAULT CHARSET=utf8mb4 COMMENT='A表'

CREATE TABLE `b` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `serial` char(21) NOT NULL,
  `name` varchar(20) NOT NULL DEFAULT '' COMMENT '名称',
  `create_time` char(11) NOT NULL DEFAULT '0' COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `serial` (`serial`)
) ENGINE=InnoDB AUTO_INCREMENT=734 DEFAULT CHARSET=utf8 COMMENT='B表'

```
#### 发现问题两处
```
a表rd_serial字段和b表serial字段 类型不匹配
a表编码和b表编码不匹配
```
#### 表结构优化调整后

```
CREATE TABLE `a` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `serial` char(21) NOT NULL COMMENT '编号',
  `rd_serial` char(21) NOT NULL DEFAULT '' COMMENT 'B表编号',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态：1可用，2禁用',
  `create_time` char(11) NOT NULL DEFAULT '0' COMMENT '创建时间',
  `update_time` char(11) NOT NULL DEFAULT '0' COMMENT '更新时间',
  `content` int(11) NOT NULL DEFAULT '0' COMMENT '内容',
  PRIMARY KEY (`id`),
  KEY `serial` (`serial`),
  KEY `status` (`status`,`update_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=36555 DEFAULT CHARSET=utf8mb4 COMMENT='A表'

CREATE TABLE `b` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `serial` char(21) NOT NULL,
  `name` varchar(20) NOT NULL DEFAULT '' COMMENT '名称',
  `create_time` char(11) NOT NULL DEFAULT '0' COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `serial` (`serial`)
) ENGINE=InnoDB AUTO_INCREMENT=734 DEFAULT CHARSET=utf8mb4 COMMENT='B表'
```

#### 优化后，执行SQL

```
-- 查询时间在0.02s左右，sql没有任何改变，也没有加任何索引
SELECT
        `lh`.`serial`,
        `lh`.`update_time`,
        rd. NAME rd_name
FROM
        `a` `lh`
INNER JOIN `b` `rd` ON `rd`.`serial` = `lh`.`rd_serial`
WHERE
        `lh`.`status` = 1
ORDER BY
        `lh`.`update_time` DESC
LIMIT 0,
 20
```
#### 如果不想改变字段类型和表编码，直接用以下sql，时间在0.03s以内,但是数据量变大的时候，速度会很慢
*这是用谎言去弥补谎言，相当于挖坑了*
```
SELECT
 a.* 
FROM
 (
 SELECT
  `lh`.`serial`,
  lh.rd_serial,
  `lh`.`update_time` 
 FROM
  `a` `lh` 
 WHERE
  `lh`.`status` = 1 
  AND EXISTS ( SELECT 1 FROM `b` `rd` WHERE `rd`.`serial` = `lh`.`rd_serial` ) 
 ORDER BY
  `lh`.`update_time` DESC 
  LIMIT 0,
  20 
 ) a
 JOIN b ON a.rd_serial = b.serial
```
#### 极致优化
```
修改
1、字段类型，
2、修改编码，
3、时间戳排序的，用bigint，
4、加上索引，（status，update_time）
速度就基本在0.001秒
```
```
CREATE TABLE `a` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `serial` char(21) NOT NULL COMMENT '编号',
  `rd_serial` char(21) NOT NULL DEFAULT '' COMMENT 'B表编号',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态：1可用，2禁用',
  `create_time` bigint(11) NOT NULL DEFAULT '0' COMMENT '创建时间',
  `update_time` bigint(11) NOT NULL DEFAULT '0' COMMENT '更新时间',
  `content` int(11) NOT NULL DEFAULT '0' COMMENT '内容',
  PRIMARY KEY (`id`),
  KEY `k_serial` (`serial`),
  KEY `k_status_update_time` (`status`,`update_time`)
) ENGINE=InnoDB AUTO_INCREMENT=36555 DEFAULT CHARSET=utf8mb4 COMMENT='A表'

CREATE TABLE `b` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `serial` char(21) NOT NULL,
  `name` varchar(20) NOT NULL DEFAULT '' COMMENT '名称',
  `create_time` char(11) NOT NULL DEFAULT '0' COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `serial` (`serial`)
) ENGINE=InnoDB AUTO_INCREMENT=734 DEFAULT CHARSET=utf8mb4 COMMENT='B表'
```

#### 查看Mysql执行时间
```
SELECT
	`lh`.`serial`,
	`lh`.`update_time`,
	rd.NAME rd_name 
FROM
	`a` `lh`
	INNER JOIN `b` `rd` ON `rd`.`serial` = `lh`.`rd_serial` 
WHERE
	`lh`.`status` = 1 
ORDER BY
	`lh`.`update_time` DESC 
	LIMIT 0,
	20;
	
-- show variables like '%profiling%';
--  set profiling=1;

SHOW PROFILES;
```

```
Query_ID	Duration	Query
71	0.0008465	SHOW STATUS
72	0.00034175	SELECT
	`lh`.`serial`,
	`lh`.`update_time`,
	rd.NAME rd_name 
FROM
	`a` `lh`
	INNER JOIN `b` `rd` ON `rd`.`serial` = `lh`.`rd_serial` 
WHERE
	`lh`.`status` = 1 
ORDER BY
	`lh`.`update_time` DESC 
	LIMIT 0,
	20
73	0.0006455	SHOW STATUS
```

#### 具体sql查询时间还可以参照下cmd命令行返回时间，Navicat返回时间算上了数据返回时间

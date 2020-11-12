# 利用表分区和表空间传输的方式进行数据转储
> storebill4part表中有约191w条数据,希望将一月份的数据（约70w条）移到历史表或者进行备份

### 将分区表的数据转移到历史表，缩减当前表体积
```
@mysql 命令
SELECT COUNT(*) FROM `storebill4part`; -- 1916861条
drop table if EXISTS storebill4bak;
CREATE TABLE storebill4bak LIKE `storebill4part`;
ALTER  TABLE storebill4bak REMOVE PARTITIONING;
ALTER  TABLE storebill4part EXCHANGE PARTITION p202001 WITH TABLE storebill4test;
SELECT COUNT(*) FROM storebill4test; -- 71w条数据
SELECT COUNT(*) FROM storebill4part; -- 120w条数据

-- 如果将历史表的数据还原到当前表，只需要再次执行
ALTER  TABLE storebill4part EXCHANGE PARTITION p202001 WITH TABLE storebill4test; -- 耗时9s
```
### 将历史表的数据进行表空间备份，数据备份目录 C:\databasebak
```
@mysql 命令
show variables like '%datadir%';
flush tables a_share.storebill4test for     export;
copy "C:\ProgramData\MySQL\MySQL Server 5.7\Data\a_share\storebill4test.cfg" "C:\databasebak\a_share\" & copy "C:\ProgramData\MySQL\MySQL Server 5.7\Data\a_share\storebill4test.ibd" "C:\databasebak\a_share\"
unlock tables;
```
### 根据需要还原数据到新数据库 newdb
1: 创建新数据库newdb,并复制表结构和删除表空间
```
@mysql 命令
drop database if EXISTS newdb;
create database newdb; 
use newdb;
create table storebill4test like a_share.storebill4test;
alter table  newdb.storebill4test discard tablespace;
```
2: 表空间文件传输
```
@windows 命令
copy "C:\databasebak\a_share\storebill4test.cfg" "C:\ProgramData\MySQL\MySQL Server 5.7\Data\newdb\" & copy "C:\databasebak\a_share\storebill4test.ibd" "C:\ProgramData\MySQL\MySQL Server 5.7\Data\newdb\"
```
3: 根据需要将历史表的数据进行还原
```
@mysql 命令
alter table newdb.storebill4test import tablespace; -- 耗时4到7s
```
4: 校验数据结构和内容
```
@mysql 命令
use newdb;
SELECT COUNT(*) FROM newdb.storebill4test;
show create table newdb.storebill4test;
```

# 通过create table的方式备份表数据
> 复制表storebill4test中所有数据到 storebill4bak表 (约71w条数据)  

### 方案一
```
@mysql 命令
drop table if exists storebill4bak2;
create table storebill4bak2 select * from storebill4test; -- 执行时间29s
-- 校验数据
select count(*) from storebill4bak2;
show full columns from storebill4bak2;
```

### 方案二
```
@mysql 命令
drop table if exists storebill4bak3;
create table storebill4bak3 like  storebill4test;
insert into storebill4bak3 select * from storebill4test; -- 执行时间75s
```

#### 方案一为什么比方案二快那么多
方案一复制表的时候没有复制索引信息，方案二复制了索引信息






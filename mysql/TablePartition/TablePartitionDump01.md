# 表分区转储
业务需求,将storebill4partb表某个月的数据转储到历史表(storebill4test)。  
实现方式有两种，对比下表分区置换和insert select的速度  
1：`insert into storebill4test select * from storebill4part where ...
2：表分区置换的方式
## 准备数据
#### 创建分区表，根据日期分区
```sql
CREATE TABLE `storebill4part` (
          `id` varchar(32) NOT NULL COMMENT '',
          `createDate` timestamp NULL DEFAULT NULL COMMENT '',
          `modifyDate` timestamp NULL DEFAULT NULL COMMENT '',
          `APMonth` int(11) DEFAULT NULL COMMENT '',
          `APYear` int(11) DEFAULT NULL COMMENT '',
          `AuditDate` timestamp NULL DEFAULT NULL COMMENT '',
          `AuditUser` varchar(32) DEFAULT NULL COMMENT '',
          `BillAbstract` varchar(500) DEFAULT NULL COMMENT '',
          `BillAbstractSign` varchar(500) DEFAULT NULL COMMENT '',
          `BillBarCode` varchar(50) DEFAULT NULL COMMENT '',
          `BillDTCount` int(11) NOT NULL COMMENT '',
          `BillNO` varchar(32) NOT NULL COMMENT '',
          `BillState` tinyint(1) NOT NULL COMMENT '',
          `ReState` tinyint(1) NOT NULL DEFAULT '0' COMMENT '',
          `iconTypeId` varchar(32) DEFAULT NULL COMMENT '',
          `BusDate` datetime DEFAULT '2020-01-01' NOT NULL COMMENT '',
          `BusUser` varchar(32) DEFAULT NULL COMMENT '',
          `CreateUser` varchar(32) NOT NULL COMMENT '',
          `EffectStoreFlag` tinyint(1) NOT NULL COMMENT '',
          `GetOrganType` tinyint(2) DEFAULT NULL COMMENT '',
          `HandNO` varchar(50) DEFAULT NULL COMMENT '',
          `OutStoreSumMoney` decimal(20,8) NOT NULL COMMENT '',
          `InStoreSumMoney` decimal(20,8) NOT NULL COMMENT '',
          `MakeDate` datetime DEFAULT NULL COMMENT '',
          `MakeUser` varchar(32) DEFAULT NULL COMMENT '',
          `Opinion` varchar(500) DEFAULT NULL COMMENT '',
          `ReturnSumMoney` decimal(18,8) NOT NULL COMMENT '',
          `ParentBillID` varchar(32) DEFAULT NULL COMMENT '',
          `ParentBillTable` varchar(50) DEFAULT NULL COMMENT '',
          `RedBillFlag` tinyint(1) NOT NULL COMMENT '',
          `Remark` varchar(500) DEFAULT NULL COMMENT '',
          `ReturnDate` datetime DEFAULT NULL COMMENT '',
          `ReturnFlag` tinyint(1) NOT NULL COMMENT '',
          `ReturnUser` varchar(32) DEFAULT NULL COMMENT '',
          `SendOrganType` tinyint(2) DEFAULT NULL COMMENT '',
          `SysMakeFlag` tinyint(1) NOT NULL COMMENT ' ',
          `SysRemark` varchar(500) DEFAULT NULL COMMENT '',
          `UnAuditDate` datetime DEFAULT NULL COMMENT '',
          `UnAuditFlag` tinyint(1) DEFAULT NULL COMMENT '',
          `UnAuditUser` varchar(32) DEFAULT NULL COMMENT '',
          `BillBusTypeID` varchar(32) DEFAULT NULL COMMENT '',
          `BillTypeID` varchar(32) NOT NULL COMMENT '',
          `GetOrganID` varchar(32) DEFAULT NULL COMMENT '',
          `SendOrganID` varchar(32) DEFAULT NULL COMMENT '',
          `DelFlag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '',
          `PostFlag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '',
          `InLocationID` varchar(32) DEFAULT NULL COMMENT '',
          `OutLocationID` varchar(32) DEFAULT NULL COMMENT '',
          `InStoreID` varchar(32) DEFAULT NULL COMMENT '',
          `OutStoreID` varchar(32) DEFAULT NULL COMMENT '',
          `ParentBillNO` varchar(50) DEFAULT NULL COMMENT '',
          `urgentFlag` tinyint(1) DEFAULT NULL COMMENT '',
          `agreeFlag` tinyint(1) DEFAULT NULL COMMENT '',
          `CenterOrganID` varchar(32) DEFAULT NULL COMMENT '',
          `ContractNO` varchar(32) DEFAULT NULL COMMENT '',
          `FileLogicalName` varchar(1000) DEFAULT NULL,
          `FilePhysicalName` varchar(1000) DEFAULT NULL,
          `ArrivalDate` datetime DEFAULT NULL COMMENT '',
          `IncludeTaxSumMoney` decimal(20,8) DEFAULT '0.00000000' COMMENT '',
          `importFromWay` int(1) DEFAULT NULL COMMENT '',
          `IsEDIFlag` int(11) DEFAULT '0' COMMENT '',
          `IsEMAILFlag` int(11) DEFAULT '0' COMMENT '',
          `EmailNo` int(11) DEFAULT '0' COMMENT '。',
          `sellSumMoney` decimal(20,8) DEFAULT '0.00000000' COMMENT '',
          `printCs` varchar(50) DEFAULT '0',
          `BillDTCountDown` int(11) DEFAULT '0',
          `assignState` tinyint(1) DEFAULT '0' COMMENT '',
          `sellIncludeTaxSumMoney` decimal(20,8) DEFAULT '0.00000000' COMMENT '',
          `actualSumMoney` decimal(20,8) DEFAULT '0.00000000' COMMENT '',
          `theorySumMoney` decimal(20,8) DEFAULT '0.00000000' COMMENT '',
          `createFlag` tinyint(1) DEFAULT '0' COMMENT '',
          `dispatchLockFlag` tinyint(1) DEFAULT '0' COMMENT '',
          `chainId` varchar(50) DEFAULT NULL COMMENT '',
          `referenceMoney` decimal(18,8) DEFAULT '0.00000000' COMMENT '',
          `payStateBySSSF` tinyint(4) DEFAULT '0' COMMENT '',
          `weighState` tinyint(1) NOT NULL DEFAULT '0' COMMENT '',
          `weighId` varchar(32) DEFAULT NULL COMMENT '',
          `sumWeighCount` int(11) DEFAULT '0' COMMENT '',
          `subCustomer` varchar(32) DEFAULT NULL COMMENT '',
          `payState` tinyint(1) NOT NULL DEFAULT '0' COMMENT '',
          `transMoney` decimal(18,8) NOT NULL DEFAULT '0.00000000' COMMENT '',
          `platformBillNo` varchar(32) DEFAULT NULL COMMENT '',
          `platformPhone` varchar(32) DEFAULT NULL COMMENT '',
          `platformBusDate` datetime DEFAULT NULL COMMENT '',
          `customerId` varchar(50) DEFAULT NULL COMMENT '',
          `sendToSHFlag` tinyint(1) unsigned zerofill NOT NULL DEFAULT '0' COMMENT '',
          `SHOrderNo` varchar(50) DEFAULT NULL COMMENT '',
          `paymentState` tinyint(1) DEFAULT NULL COMMENT '',
          `source_id` varchar(32) DEFAULT NULL COMMENT '',
          `submit_date` datetime DEFAULT NULL COMMENT '',
          `submit_user` varchar(32) DEFAULT NULL COMMENT '',
          `supply_flag` tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT '',
          `examine_flag` tinyint(1) unsigned NOT NULL DEFAULT '1' COMMENT '',
          `update_flag` tinyint(1) unsigned NOT NULL DEFAULT '1' COMMENT '',
          `workshop_id` varchar(32) DEFAULT NULL COMMENT '',
          `vehicle_id` varchar(32) DEFAULT NULL COMMENT '',
          `reaudit_user` varchar(32) DEFAULT NULL COMMENT '',
          `reaudit_date` datetime DEFAULT NULL COMMENT '',
          `sum_money_no_trans` decimal(18,8) unsigned NOT NULL DEFAULT '0.00000000' COMMENT '',
          `sum_tax_money_no_trans` decimal(18,8) unsigned NOT NULL DEFAULT '0.00000000' COMMENT '',
          PRIMARY KEY (`id`,`BusDate`),
          KEY `index_fk_billBusTyeId` (`BillBusTypeID`),
          KEY `index_fk_billTypeId` (`BillTypeID`),
          KEY `index_fk_getOrganId` (`GetOrganID`),
          KEY `index_fk_sendOrganId` (`SendOrganID`),
          KEY `index_fk_parentBillId` (`ParentBillID`),
          KEY `index_fk_auditUser` (`AuditUser`),
          KEY `index_fk_busUser` (`BusUser`),
          KEY `index_fk_createUser` (`CreateUser`),
          KEY `index_fk_makeUser` (`MakeUser`),
          KEY `index_fk_returnUser` (`ReturnUser`),
          KEY `index_fk_unAuditUser` (`UnAuditUser`),
          KEY `index_fk_inStoreId` (`InStoreID`),
          KEY `index_fk_outStoreId` (`OutStoreID`),
          KEY `Index_busDate` (`BusDate`),
          KEY `FK_submit_user` (`submit_user`),
          KEY `FK_storebill_workshop_organ` (`workshop_id`),
          KEY `FK_reaudit_user` (`reaudit_user`),
          KEY `index_chainid` (`chainId`),
          KEY `index_postFlag` (`PostFlag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT=''
    PARTITION BY RANGE (to_days(BusDate)) (
        PARTITION p202001 VALUES LESS THAN (to_days('2020-02-01')),
        PARTITION p202002 VALUES LESS THAN (to_days('2020-03-01')),
        PARTITION p202003 VALUES LESS THAN (to_days('2020-04-01')),
        PARTITION p202004 VALUES LESS THAN (to_days('2020-05-01')),
        PARTITION p202005 VALUES LESS THAN (to_days('2020-06-01')),
        PARTITION p202006 VALUES LESS THAN (to_days('2020-07-01')),
        PARTITION p202007 VALUES LESS THAN (to_days('2020-08-01')),
        PARTITION p2020 VALUES LESS THAN (MAXVALUE) );

```
#### 插入数据
略
#### 性能比较
```
1： 创建两个非分区表作为历史表
CREATE TABLE storebill4test01 LIKE `storebill4part`;
CREATE TABLE storebill4test02 LIKE `storebill4part`;
ALTER TABLE storebill4test01 REMOVE PARTITIONING;
ALTER TABLE storebill4test02 REMOVE PARTITIONING;

SELECT COUNT(*) FROM storebill4part; -- 共1916861条数据
SELECT COUNT(*) FROM storebill4part WHERE BusDate < '2020-02-01'; -- 一月份共716860条数据

2： 采用insert...select的方式，将一月份的数据转储到历史表 storebill4test01
INSERT INTO storebill4test01 SELECT * FROM storebill4part WHERE BusDate < '2020-02-01'; -- 耗时超过一分钟

3： 采用表分区置换的方式，将一月份的数据转储到历史表 storebill4test02
-- 将storebill4part表数据转储到历史表storebill4test02
ALTER TABLE storebill4part EXCHANGE PARTITION p202001 WITH TABLE storebill4test02; -- 耗时0.158s
select count(*) from storebill4test02; -- 转储了716860条数据 
-- 将历史表storebill4test02数据转回到storebill4part表
ALTER TABLE storebill4part EXCHANGE PARTITION p202001 WITH TABLE storebill4test02; -- 耗时9.8s
```
`insert...select`方式可以根据业务需求灵活运用，但是效率极低  
利用表分区置换的方式转储数据，速度快但是需要前期根据业务设计表分区

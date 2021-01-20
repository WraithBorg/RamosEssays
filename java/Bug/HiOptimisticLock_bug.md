# HibernateOptimisticLockingFailureException
  org.springframework.orm.hibernate4.HibernateOptimisticLockingFailureException: 
  Batch update returned unexpected row count from update [0]; actual row count: 0; expected: 1; 
  nested exception is org.hibernate.StaleStateException: 
      Batch update returned unexpected row count from update [0]; actual row count: 0; expected: 1
```
 // 发送单据到第三方系统业务精简代码
    public void sendBill(String billids) {
        String[] ids = billids.split(",");
        for (String id : ids) {
            try (SprLock lock = new SprLock(id)) {
                OrderBill orderBill = orderBillService.get(id);
                // 更新单据状态为发送中
                billStatusService.updateBillStatus(billId, BillStatusEnum.SENDING);//根据日志显示此处代码异常
                // 发送单据，并更新单据状态为已发送
                orderBillService.sendBill(billId);
                Log.recordLog("单据发送成功");
            } catch (BusinessException e) {
                Log.recordLog("单据发送失败：" + e.getMessage());
            } catch (Exception e) {
                logger.error("单据发送失败：", e);
                billStatus = billStatusService.get(billId);
                if (billStatus != null && !getStatus().equals(BillStatusEnum.SYNCED)) {
                    billStatusService.delete(s);//如果不是已发送状态，则删除单据状态
                }
                Log.recordLog("单据发送失败：" + e.getMessage());
            }
        }
    }
    class SprLock {
        public SprLock(String id) {
            this.lock(id);
        }
        public void lock(String id) {
            if (StringUtil.isNotEmpty(id)) {
                if (RedisLock.put(billId, "") != null) {
                    throw new BusinessException("其他人正在操作该单据，请重新打开单据!");
                }
            }else{
                // TODO
            }
        }
    }
```
经分析得知,多人操作的情况下,第一个人获取到redis锁后进行业务操作,此时第二个人也进入这个方法,但是由于并发控制,
        所以抛异常进入catch了,这时候第二个人会把billStatus删掉,此时第一个人业务操作完毕,提交事务,update更新billStatus,
        但是billStatus已经不存在了,就抛异常HibernateOptimisticLockingFailureException了

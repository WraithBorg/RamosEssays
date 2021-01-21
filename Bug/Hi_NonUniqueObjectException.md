## 解决Hibernate异常

#### 异常信息
```
org.hibernate.NonUniqueObjectException: a different object with the same identifier value was already associated with the session: 
[com.xxx.entity.StoreBill#ff80808176608e14017661327bb000d7]
	at org.hibernate.engine.internal.StatefulPersistenceContext.checkUniqueness(StatefulPersistenceContext.java:696)
	at org.hibernate.event.internal.DefaultSaveOrUpdateEventListener.performUpdate(DefaultSaveOrUpdateEventListener.java:296)
	at org.hibernate.event.internal.DefaultSaveOrUpdateEventListener.entityIsDetached(DefaultSaveOrUpdateEventListener.java:241)
	at org.hibernate.event.internal.DefaultUpdateEventListener.performSaveOrUpdate(DefaultUpdateEventListener.java:55)
	at org.hibernate.event.internal.DefaultSaveOrUpdateEventListener.onSaveOrUpdate(DefaultSaveOrUpdateEventListener.java:90)
	at org.hibernate.internal.SessionImpl.fireUpdate(SessionImpl.java:705)
	at org.hibernate.internal.SessionImpl.update(SessionImpl.java:697)
	at org.hibernate.internal.SessionImpl.update(SessionImpl.java:693)
```

#### 发现持久化对象的内存地址发生改变
```

BillDTServiceImpl --> b = billDao.get(b.getId());//内存地址AAA

StoreServiceImpl#getStore --> storeDao.clear();

GroundServiceImpl#pushInfo --> billDao.get(billId);//内存地址BBB

BillDTServiceImpl --> b = billDao.update(b);//内存地址AAA
	

```
#### 结果分析  
当Hibernate查出对象AAA并放到持久化缓存后，如果进行session.clear操作后，在查询该对象的话，会重新从数据库查出一个新对象，所以内存地址发生变化  
这时候AAA不再是持久化对象，已经被BBB代替，如果后续还有对AAA操作的代码，那么就会抛出异常`a different object with the same identifier value was already associated with the session
`      

#### 解决办法
`billDao.get(billId);`之后添加`billDao.evict(billDB);`这样BBB不再属于持久化对象，当后面继续对AAA进行持久化操作时，Hibernate则会将AAA关联到持久化缓存中，持久化缓存只有一个单据对象，不会发生异常信息




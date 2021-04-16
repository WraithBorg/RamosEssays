# 快速定位JPA BUG
`Author:zxu`  
>  每次单据审核的时候,单据备注(remark)都会莫名更新,需要排查一下是哪块逻辑造成的  

### 1: 在单据对象内 setRemark字段设置断点,然后根据方法调用栈的顺序查找相关调用方法
**如果调用setXx方法的上一步是一个JPA查询方法,则该set方法是由JPA查询出数据后反射到单据实体上调用的,这时候需要用到方法2** 


### 2: 在以下代码添加条件断点,然后再根据方法栈调用记录,可以定位到问题所在,原因是某处逻辑通过执行update sql来更新属性值
```
// 断点条件 query.getQueryString().contains("ABC"),ABC为更新后的备注值
org/hibernate/internal/AbstractSessionImpl.java:218
org/hibernate/internal/AbstractSessionImpl.java:206

// 定位到更新备注的逻辑代码
String update = " UPDATE bill b SET b.remark = 'ABC' WHERE b.id = '" + id + "'  " ;
BillDao.executeSQL(update);
```



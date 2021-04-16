## 编辑器常用正则
`Author:zxu`
#### 删除所有多余空行
```regexp
# notepad
查找目标 \n[\s| ]*\r
替换为
```
#### 删除多余空行
```regexp
^[\s]*\n
```

## 利用正则查找不规范代码
####  查找没有规定小数精度的divide方法,避免小数除不尽的bug	(发现两处)	 
```regexp
// Regex divide\(.+\..+\(\)\)  
× tPrice=sdt.x().divide(sdt.x()).setScale(4, BigDecimal.ROUND_HALF_UP);
√ tPrice=sdt.x().divide(sdt.x(), 4, BigDecimal.ROUND_HALF_UP));
```
#### 查找有精度损失的BigDecimal	 (发现三处)
```regexp
BigDecimal\([0-9]+\.
System.out.println(new BigDecimal(0.01));
// 输出结果 0.01000000000000000020816681711721685132943093776702880859375
× a.x(x.x().multiply(new BigDecimal(0.01)));
√ a.x(x.x().multiply(new BigDecimal("0.01")));
```

#### java查找 finally里写return的代码
```regexp
finally.*\{\n.*return
```

#### 查找所有捕获异常但是没有输出异常日志的代码块
```
JAVA笔记->检索不规范的catch代码块
```



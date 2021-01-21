 
# mysql 时间 timestamp的坑
```
CREATE TABLE `t` (
  `a` int(11) DEFAULT NULL,
  `b` timestamp  NOT NULL,
  `c` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8
```
我们可以发现 「c列」 是有CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP，   
所以c列会随着记录更新而「更新为当前时间」。   
但是b列也会随着有记录更新为而「更新为当前时间」   
### 解决方案：  
我们可以发现 「c列」 是有CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP，   
所以c列会随着记录更新而「更新为当前时间」。   
但是b列也会随着有记录更新为而「更新为当前时间」   

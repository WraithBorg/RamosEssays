# 如何优化下面sql查询速度
#### 测试环境
Mysql5.7 (只能是5.7版本)

#### 准备数据
* [测试数据右键下载](SqlOptimise_001_test_data.sql)

#### 执行sql，发现查询速度较慢
```
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

## 如果优化查询速度？
执行查询sql后，发现执行时间为9s左右  
希望优化后，查询时间控制在0.05s以内

#### 答案
* [答案](SqlOptimise_001_answer.md)


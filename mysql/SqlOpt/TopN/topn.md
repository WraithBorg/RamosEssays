# Mysql TOPN问题三种解决方案对比
`Author:zxu`  
------
> * 用户自定义变量的使用
> * 窗口函数的使用

## 查询每个班级分数在前两名的学生,包括成绩并列的学生
* [数据源](x_student.sql)

**表结构**
```
CREATE TABLE `x_student` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '姓名',
  `grade` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '班级',
  `score` int DEFAULT NULL COMMENT '分数',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3002 DEFAULT CHARSET=utf8
```
#### 方案一 利用子查询方式
利用diff对比查询结果
```
SELECT stu.id,
       stu.name,
       stu.grade,
       stu.score
FROM `x_student` stu
WHERE (
          SELECT count(*)
          FROM (SELECT u.score
                FROM `x_student` u
                WHERE u.grade = stu.grade
                  AND stu.score < u.score
                GROUP BY u.score) a
      ) < 2
ORDER BY stu.grade,
         stu.score DESC,
         stu.id DESC;

```
#### 方案二 利用用户自定义变量
```
SELECT stu.id, stu.name, stu.grade, stu.score, stu.topnum
FROM (SELECT a.*,
             @topnum := if(@subj = grade, if(a.score < @score, @topnum + 1, @topnum), 1) topnum,
             @subj := a.grade                                                            subj,
             @score := a.score                                                           scor
      FROM x_student a,
           (SELECT @subj = '', @score = 0, @topnum = 0) r
      ORDER BY grade, score DESC) stu
WHERE stu.topnum < 3
ORDER BY stu.grade, stu.score DESC, stu.id DESC;

```
#### 方案三 利用窗口函数
```
SELECT t.id,t.name,t.score,t.grade
FROM (
         SELECT dense_rank() OVER (PARTITION BY grade ORDER BY score DESC ) AS row_num,
                id,
                name,
                grade,
                score
         FROM x_student
     ) t
WHERE t.row_num <= 2 ORDER BY t.grade,t.score DESC ,t.id DESC;
```

### 查询分析
```
SELECT count(*) from `x_student`; -- 3000条数据
子查询方式平均耗时：2.48s
用户自定义变量方式平均耗时：0.30s
窗口函数方式平均耗时：0.212s
```

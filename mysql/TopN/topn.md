# Mysql TOPN问题三种解决方案对比
------
> * 用户自定义变量的使用
> * 窗口函数的使用

## 查询每个班级分数在前两名的学生，包括成绩并列的学生
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
```
SELECT
	stu.id,stu.name,stu.grade,stu.score
FROM
	`x_student` stu
WHERE
	(
		SELECT
			COUNT(*)
		FROM
			`x_student` u
		WHERE
			u.grade = stu.grade
		AND stu.score < u.score
	) <3
ORDER BY
	stu.grade,
	stu.score DESC ,
	stu.id DESC
;
```
#### 方案二 利用用户自定义变量
```
select stu.id,stu.name,stu.grade,stu.score from 
(select a.*,
@rankid := if(@subj = grade,@rankid + 1, 1) rankid,
@tmprank := if(@subj = grade and @score = a.score,@tmprank,@rankid) tmprank,
@subj := a.grade subj ,
@score := a.score scor
from x_student a,
(select @subj = '',@rankid = 0,@score = 0,@tmprank = 0) r 
order by grade,score desc) stu where stu.tmprank <4
order by stu.grade,stu.score desc,stu.id DESC;
```
#### 方案三 利用窗口函数
```
SELECT
	stu.id,stu.name,stu.grade,stu.score
FROM
	(
		SELECT
			*, rank () over (
				PARTITION BY grade
				ORDER BY
					score DESC
			) AS ranking
		FROM
			x_student
	) stu
WHERE
	ranking < 4 order by stu.grade,stu.score desc,stu.id DESC;;
```

### 查询分析
```
SELECT count(*) from `x_student`; -- 3000条数据
子查询方式平均耗时：2.48s
用户自定义变量方式平均耗时：0.30s
窗口函数方式平均耗时：0.212s
```

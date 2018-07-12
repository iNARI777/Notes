## 工作中遇到的问题：

* 使用 Mybatis 取表中的数据的时候，返回的 `List` 中记录的顺序不确定，需要手动调整；

* 使用 Mybatis 在插入的同时返回主键的值：

让 `Mapper` 中相应的 `insert` 方法使用注解 `@Options(useGeneratedKeys = true, keyProperty = "<param>.<id>", keyColumn = "<dbcolum>")`就可以了。

这个地方踩了个坑，因为 `keycolumn` 仅仅使用了默认的 `id` ，这会导致 Mybatis 无法识别对象中要设置的属性，导致无法从 `engineInfo` 中获得数据库生成的自增值。同时还要注意，`insert` 方法返回的 `int` 型变量代表的是这条语句影响的行数，如果要获得真实的 `id` 直接使用 `engineInfo.getId()`就可以获得了，因为 Mybatis 已经把这个值设置到对象中了。

* Spring 中的 `@Scheduled` 注解可以用来配置定时任务，注解括号中的内容可以填入 `fixedRate` （距离上次执行 **开始** 多久后再次执行）`fixedDelay` (距离上次执行 **完成后** 多久再次执行)，还有 `initDelay` 可以设置第一次任务执行时的延时。同时也可以填入 cron 表达式，来更加灵活的设定定时任务的执行时间， cron 表达式由6或7个空格分隔的时间字段组成：秒 分钟 小时 日期 月份 星期 年（年可以不写）。

> 关于 cron 表达式可以看这片[文章](https://blog.csdn.net/jack_bob/article/details/78786740)。

* Kubernetes （又叫 K8S ）相关知识可以看[这里](https://www.kubernetes.org.cn/97.html)/[这里](http://www.dockone.io/article/932)和[这里](https://www.kubernetes.org.cn/tags/cicd)。

## 工作中遇到的问题：

* 使用 Mybatis 取表中的数据的时候，返回的 `List` 中记录的顺序不确定，需要手动调整；

* 使用 Mybatis 在插入的同时返回主键的值：

让 `Mapper` 中相应的 `insert` 方法使用注解 `@Options(useGeneratedKeys = true, keyProperty = "<param>.<id>", keyColumn = "<dbcolum>")`就可以了。

这个地方踩了个坑，因为 `keycolumn` 仅仅使用了默认的 `id` ，这会导致 Mybatis 无法识别对象中要设置的属性，导致无法从 `engineInfo` 中获得数据库生成的自增值。同时还要注意，`insert` 方法返回的 `int` 型变量代表的是这条语句影响的行数，如果要获得真实的 `id` 直接使用 `engineInfo.getId()`就可以获得了，因为 Mybatis 已经把这个值设置到对象中了。

* Spring 中的 `@Scheduled` 注解可以用来配置定时任务，注解括号中的内容可以填入 `fixedRate` （距离上次执行 **开始** 多久后再次执行）`fixedDelay` (距离上次执行 **完成后** 多久再次执行)，还有 `initDelay` 可以设置第一次任务执行时的延时。同时也可以填入 cron 表达式，来更加灵活的设定定时任务的执行时间， cron 表达式由6或7个空格分隔的时间字段组成：秒 分钟 小时 日期 月份 星期 年（年可以不写）。

> 关于 cron 表达式可以看这片[文章](https://blog.csdn.net/jack_bob/article/details/78786740)。

* Kubernetes （又叫 K8S ）相关知识可以看[这里](https://www.kubernetes.org.cn/97.html)/[这里](http://www.dockone.io/article/932)和[这里](https://www.kubernetes.org.cn/tags/cicd)。

* Kubernetes 相关知识可以看[这里](https://www.kubernetes.org.cn/97.html)/[这里](http://www.dockone.io/article/932)和[这里](https://www.kubernetes.org.cn/tags/cicd)。

* MyBatis 中查询结果不存在的时候，查询方法中返回 null 还是一个 size 为 0 的 List。

### Spring 笔记

* 依赖注入的例子：CD 机。 IOC 的目的是减少组建与组件之间的耦合，而 AOP 的目的是减少功能与功能之间的耦合。
* `@Autowired` 可以用在 **域**/**构造方法** 以及 **set方法** 上。但是 set 方法和其他方法其实都是一样的，`@Autowired` 也可以添加在其他方法上，Spring 会自动将与参数类型相同的 bean 配置到参数中去。可以通过 `required=false` 属性来防止没有对应 bean 情况下 Spring 抛出异常的情况，但是这个时候要谨慎对待。
* 在使用 Java 类进行配置的时候，我们可以在 `@Bean` 标注的方法中使用其他被 `@Bean` 标注的方法，来进行对象依赖的装配，但是这个时候的调用并不会真正的调用这个被 `@Bean` 标注的方法，而是会被拦截，直接从 bean 的对象池获得相应的对象，完成依赖的注入。同时我们也可以不使用这种方法，被 `@Bean` 标注的方法也可以自动将已经注册的 bean 注入到配置方法的参数中。
* `@RequestBody` 对象转换的机制。
* 在使用 Mybatis 的时候，`select` 内容转化为对象的时候默认是对对象的 **属性名** 和 **列名** 有要求的，两者名称相同或者驼峰对下划线都可以，否则对象的相关域中无法被注入，得到 `null` 值。但是可以通过 `@Results` 配合 `@Result` 两个注解来完成列域的映射。
* Spring Boot 打成 jar 包的一个比较简单的方法是直接使用命令行执行 maven 命令 `mvn clean package -Dmaven.test.skip=true` 。

### Jackson

* Jackson 在进行对象到 Json 的转化的时候，默认是只对公共域和 `public` 的 getter 进行转化的。这个时候它会对域的一些字母进行大小写的转化，如果想规定域转化到 Json 之后的名字，可以使用 `@JsonProperty("<name>")` 对其进行规定，这个注解可以加在 **域** 上也可以加在 **getter** 方法上。如果加在了 `private` 域上的话，可能会导致 Jackson 对域和 getter 方法分别进行了转换而导致转换出来的 Json 中有重复的键值对。这个时候可以通过 `@JsonAutoDetect` 来解决这个问题。

> Jackson 默认的字段属性发现规则如下：
所有被public修饰的字段->所有被public修饰的getter->所有被public修饰的setter

* Jackson 不能自动将 **非静态内部类** 转化成 Json 中的内容。
* Jackson 不能转化没有域的对象？

### Nginx

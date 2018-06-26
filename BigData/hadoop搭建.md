# Hadoop环境搭建

## 1. 搭建环境

操作系统：Ubuntu 16.04

Hadoop版本：2.8.4

## 2. 搭建过程

### 2.1 获取Hadoop文件

直接从Apache官网上下载，进入要放置Hadoop压缩包的目录执行：

`wget http://www-us.apache.org/dist/hadoop/common/hadoop-2.8.4/hadoop-2.8.4.tar.gz`

获得的是文件名为`hadoop-2.8.4.tar.gz`的压缩包，然后解压文件包：

`tar -zxvf hadoop-2.8.4.tar.gz`

获得的文件夹目录结构如下：

hadoop-2.8.4.tar.gz
|--- bin
|--- include
|--- libexec
|--- sbin
|--- etc
|--- lib
|--- share
|--- NOTICE.txt
|--- LICENSE.txt
|--- README.txt

### 2.2 配置修改

这部分主要根据[官网描述](http://hadoop.apache.org/docs/r2.8.4/hadoop-project-dist/hadoop-common/SingleCluster.html)进行配置。

首先要在`etc/hadoop/hadoop-env.sh`中添加上`export JAVA_HOME=/usr/java/latest`，用来在启动的时候告诉Hadoop本机的JVM在什么位置。

由于本次搭建我们要是用伪分布式的模式，所以需要在`etc/hadoop/core-site.xml`中添加：

    &lt;configuration>
        &lt;property>
            &lt;name>fs.defaultFS&lt;/name>
            &lt;value>hdfs://<服务器的IP地址>:8020&lt;/value>
        &lt;/property>
    &lt;/configuration>

> 注：官网上写的端口号是9000，但是我没用这个端口，因为看的视频里说那个是Hadoop 1.x中使用的端口，因此我就没有试验9000端口。地址千万别写成`localhost`！
刚开始我特么脑抽写错了(╯‵□′)╯︵┻━┻

    &lt;configuration>
        &lt;property>
            &lt;name>hadoop.tmp.dir&lt;/name>
            &lt;value>/opt/hadoop/app/tmp&lt;/value>
        &lt;/property>
    &lt;/configuration>

> 在没有设置hadoop.tmp.dir属性的时候，文件系统默认是存放在/temp下面的，这个时候如果系统重启，所有数据都会丢失，所以最好还是在这个文件中设置这个文件夹到一个硬盘上的位置比较稳妥。

并在`etc/hadoop/hdfs-site.xml`中添加：

    &lt;configuration>
        &lt;property>
            &lt;name>dfs.replication&lt;/name>
            &lt;value>1&lt;/value>
        &lt;/property>
    &lt;/configuration>

### 2.3 SSH免密码登陆

由于Hadoop在集群模式下需要通过SSH来进行管理，所以需要配置本机SSH免密码登陆，使用如下命令即可：

    $ ssh-keygen -t rsa -P '' -f ~/.ssh/id_rsa
    $ cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
    $ chmod 0600 ~/.ssh/authorized_keys

关于`authorized_keys`的相关内容，见这篇博客：[ssh-keygen的使用方法及配置authorized_keys两台linux机器相互认证](https://blog.csdn.net/xiaoyida11/article/details/51557174)。

### 2.4 HDFS的启动及使用

#### 2.4.1 HDFS的启动

首先格式化HDFS文件系统：

    $ bin/hdfs namenode -format

> 遇到一个坑，这里不要多次格式化hdfs，因为每回格式化都会为DataNode分配一个Cluster ID，这就会导致DataNode的Cluster ID和NameNode的Cluster ID不一致，导致启动HDFS的时候无法启动DataNode。这个时候删掉HDFS中data文件夹下的current在重新启动HDFS就可以了。看[这里](https://blog.csdn.net/gis_101/article/details/52679914)。

然后启动Hadoop的分布式文件系统：

     $ sbin/start-dfs.sh

如果上一步中存在问题的话，去logs文件夹下根据提示打开相应日志看问题出在哪里。如果正常启动的话一般只有两三行提示，告诉你namenode和datanode已经启动。这个时候就可以通过浏览器访问`http://hostname:50070`。

这个时候使用`jps`可以看到除了`Jps`程序正在运行之外还有`Datanode`和`Namenode`两个程序都在运行。

**下面使用一个MapReduce例程对我们部署好的伪分布式HDFS进行测试（以下只是测试，不是必须步骤）：**

然后创建两个文件夹用于进行MapReduce：

    $ bin/hdfs dfs -mkdir /user
    $ bin/hdfs dfs -mkdir /user/&lt;username>

`<username>`需要为当前的用户的用户名（比如root或者其他当前登录的账号）。

在通过以下命令在HDFS中导入输入的文件，将etc/hadoop下的配置文件作为输入，放到HDFS的input文件夹中：

    bin/hdfs dfs -put etc/hadoop input

运行Hadoop自带的测试程序：

    bin/hadoop jar share/hadoop/mapreduce/hadoop-mapreduce-examples-2.8.4.jar grep input output 'dfs[a-z.]+'

查看实验结果：

    bin/hdfs dfs -cat output/*

关闭HDFS：

    sbin/stop-dfs.sh

#### 2.4.2 HDFS的相关命令

想要操作HDFS中的文件，需要像操作Linux文件系统类似的命令。主要是使用`hdfs dfs -COMMAND`进行操作。

具体支持的指令可以通过官网或者输入`hdfs dfs`进行查询。

# HDFS相关概念及原理

HDFS是Hadoop Distributed File System的缩写，是Hadoop使用的文件系统。

和我们Windows和Linux的操作系统一样，HDFS也是我们用来存放文件和数据的系统，有着和Linux相似的目录结构，支持`ls`、`mkdir`、`get`、`put`、`delete`等命令。

HDFS与一般操作系统的文件系统的不同之处是其存储文件的方式，它并不将所有文件存放在同一节点之上，而是将文件分成固定大小的块（`Block`，默认为64M或128M），并复制多份（默认的副本系数是3），存放到不同的机器之上。

这样做的好处是如果一台机器宕机，我们还可以从其他的机器上获得相应文件的副本，保证了文件系统的高可用性，并且可以同时使用多台机器的存储空间，可以存放更多体积较大的文件。

下面我们要通过明确几个角色，并通过它们来了解HDFS运行的过程。

## 1. HDFS的三种节点

HDFS节点主要有三个角色：NameNode、SecondaryNameNode以及DataNode。其关系如下图所示：

![](images/hdfs/1.png)

### 1.1 NameNode介绍

NameNode中存放的内容包括了文件的命名空间（namespace）、文件Block的映射关系等，NameNode内存中存储的是`fsimage`和`fsedit`。

> `fsimage`是元数据的镜像文件，包括文件系统的目录树。
> 
> `fsedits`是针对文件系统操作的修改日志。

NameNode还负责文件副本策略的配置以及和客户端的交互，处理客户端的读写请求。

### 1.2 SecondaryNameNode介绍

SecondaryNameNode是NameNode的辅助，分担NameNode的工作，但不是一个热备份，也就是说，NameNode挂掉之后SecondaryNameNode不能马上替换NameNode提供服务。

每隔一定时间SecondaryNameNode会从NameNode那获取`fsimage`和`fsedits`，并进行合并，完成后推送给NameNode。可以辅助恢复NameNode。

### 1.3 DataNode介绍


# HDFS相关概念及原理

HDFS 是 Hadoop Distributed File System 的缩写，是 Hadoop 使用的文件系统。

和我们 Windows 和 Linux  的操作系统一样，HDFS 也是我们用来存放文件和数据的系统，有着和Linux相似的目录结构，支持 `ls`、`mkdir`、`get`、`put`、`delete` 等命令。

HDFS 与一般操作系统的文件系统的不同之处是其存储文件的方式，它并不将所有文件存放在同一节点之上，而是将文件分成固定大小的块（`Block`，默认为 64M 或 128M ），并复制多份（默认的副本系数是 3 ），存放到不同的机器之上。

这样做的好处是如果一台机器宕机，我们还可以从其他的机器上获得相应文件的副本，保证了文件系统的高可用性，并且可以同时使用多台机器的存储空间，可以存放更多体积较大的文件。

下面我们要通过明确几个角色，并通过它们来了解HDFS运行的过程。

## 1. HDFS的三种节点

HDFS节点主要有三个角色：NameNode、SecondaryNameNode 以及 DataNode 。其关系如下图所示：

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

DataNode根据NameNode的安排，存储客户端发送过来的Block。负责文件实际的读写。

### 1.4 Client客户端的工作

首先，文件分块的工作是由客户端完成的。

它与NameNode交互，得到文件的位置信息；与DataNode交互，从DataNode处读取或写入实际的数据。

## 2. HDFS文件读写

### 2.1 HDFS写流程

1. 客户端：用户首先将**分块大小**和**副本数量**告诉 Client ，Client 根据以上参数将文件进行相应的分割。（对应 1-4 图）
2. Client 首先发送第一个 Block 。Client 首先将分块大小和副本数量告诉 NameNode，NameNode 会从所有的 DataNode 中找出相应数量的 DataNode 组成一个有序的列表返回给 Client。（对应 5-7 图）
3. Client 将文件数据以及从 NameNode 处得到的列表发送给第一个 DataNode。 DataNode 会按照列表内容将文件继续传给下一个 DataNode。类似一个 PipeLine，每个 DataNode 接受到一点数据就把它存到自己的硬盘上，并将它传递给下一个 DataNode，直到最后一个 DataNode。
4. 当一个 Block 的数据已经完全写入到 DataNode 的硬盘上之后，就会给 NameNode 发送一个确认。当 NameNode 收到所有的 DataNode 的确认信号之后，向 Client 发送一个确认，Client 继续发送下一个 Block。
5. Client 发送完所有的 Block 之后，通知 NameNode，这时 Namenode 已经存储了文件的元数据，也就是文件被拆成了几块，复制了几份，每块分别存储在哪个 DataNode 上。

上述流程可以看下面的漫画：

![](images/hdfs/2.png)

![](images/hdfs/3.png)

### 2.2 HDFS读流程

1. 首先 Client 告诉 NameNode 希望读取的文件的文件名。
2. NameNode 会返回每个文件被拆成了多少个 Block，每个 Block 都在哪个 DataNode 上，并且这些 DataNode 也是根据距离排序好的。
3. Client 根据 NameNode 发回来的信息去相关的 DataNode 上去取相应的 Block，组成完整的文件。

![](images/hdfs/4.png)

### 2.3 错误处理


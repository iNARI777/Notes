# Kafka 基础知识笔记

## 1. Kafka 的核心概念

**Broker**： Kafka 是由多个 Kafka 服务器组成的一个集群，每个 Kafka 服务叫做 Kafka Broker。集群共同完成消息中间件的工作。

**Topic**：Kafka 作为一个消息中间件，提供了订阅/发布的服务，一个 Topic 就可以看作是传统消息队列的一个 queue，不过 Topic 中的消息是可以共多个订阅者消费的。

**Partion**：Kafka 的发布者常常是日志的生产系统这样的生产消息非常快的系统，而其订阅者往往是一些比较慢速的系统，比如 HDFS/HBASE 等。这个时候如果订阅者直接从 Kafka 中接受消息的话，消息消费的速度往往会赶不上消息生产的速度，由此会导致消息的堆积。为了应对这种情况，就产生了分区（ Partion ）的需求。一个 Topic 在建立的时候就可以选择分区的数量，分区具体的使用可以参见后面的 Consumer。

**Producer**：Kafka 的客户端有两种，其中的一种就是 Producer，是发布者，它负责向 Kafka 集群中的 Topic 中 push 消息，push 消息的时候还会进行消息的序列化以及负载均衡（消息分区）。

**Consumer**：Kafka 客户端的另外一种，是订阅者，它他会定时轮询地从 Topic 中 pull 最新的消息。多个订阅了同一个 Topic 的消费者可以指定同一个 Consumer Group （消费者群组），同一个 Group 中的 Consumer 共同消费同一个 Topic 下的消息，所以一个消费者群组也是一个 **消费者集群**。但是要注意，同一个 Consumer Group 中的 Consumer 不会共同消费同一个 Partion 下的消息，所以需要保证 Consumer Group 中的 Consumer 的数量不大于 Partion 的数量。具体如下图所示：

![](images/kafka/1.png)

> 一个 Consumer Group 可以看作为一个消费一个 Topic 下消息的集群，如果你有多个不同的集群（服务）想要对同一个 Topic 下的消息做不同的处理，那么你就需要分别为每个集群（服务）指定不同的 Group。比如，你有一个生产日志的 Producer，将生产出来的日志放到了 Kafka 的 *Log* 主题下，你有两个服务： HDFS 和 Strom 都订阅了这个 Topic，他们分别要把日志存储到 HDFS 和使用 Strom 集群进行实时分析，那么你就需要分别为两个集群分配两个 Group。
hdfs://nameservice1/user/prophet/data/work/pws/PicoTraining/automl_experiment_5_gbdt_train_tuning/4022/Node_PicoTraining_GBDT_1/0/pass-final
**Offset**：
hdfs://nameservice1/user/prophet/data/test/pws/PicoTraining/af767d42d488490484b8045a695ad71e/3813/767f0e23-00ad-4d34-8ee3-99cea346185c/0/pass-final
## 2. Kafka 的工作过程

### 2.1 Topic 的建立



### 2.2 Producer 消息生产



### 2.3 Consumer 消息消费



## 3. Zookeeper 的职责

* 负责 Broker Controller 的选举；
*

## 4. 豆知识

### 4.1 Kafka 为什么快

**生产者方面**：

Kafka 生产者写入是顺序写入，减少了随机寻址，对于机械硬盘的机器来说，顺序 IO 是可以大幅加快读写速度的。

另外，Kafka 写入数据的时候是写入到内存映射文件中的，内存映射文件的读写效率都要高于普通文件的读写。

> 内存映射文件（ mmap 函数调用）：
> 其原理是将 *逻辑内存* 中的一部分与磁盘上的文件进行映射，读的时候通过缺页中断直接从磁盘上将文件读入内存。
> 一般文件的写需要经过先读写用户态空间，之后进入内核态空间再进行一次复制，而内存映射文件就进行一次写就可以了。
> 因此内存映射文件的读写是很快的。

**消费者方面**：

Kafka 向消费者发送数据的时候使用了 Zero Copy 的技术，也是不经用户态，直接通过内核态将 socket 数据发送出去，获得了很快的速度。

同时，通过设置 Kafka 的最小文件发送块可以等待待发送的文件积攒到一定大小之后再一并发送出去，减小了多次发送的网络开销。

## 参考博客

[1] [Kafka史上最详细原理总结](https://blog.csdn.net/ychenfeng/article/details/74980531)（不过感觉这个帖子错误还挺多的...选择性参考）

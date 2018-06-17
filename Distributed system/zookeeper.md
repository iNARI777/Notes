# Zookeeper的算法以及运行流程

## 1. Paxos算法

这部分可以看这篇[帖子](https://www.jianshu.com/p/591c9ecc9827)，以后有时间了再整理一下这部分。

## 2. Zookeeper的实现

### 2.1 Zookeeper中的角色

Zookeeper的Server中有Leader和Learner，而Learner又包括Follower和Observer两个角色。还有就是Client。

**查询请求的处理：**

Client连接Server发送查询和修改数据的请求，Server中的Follower/Observer接到查询请求可以直接返回本地的结果，这个结果不一定是最新的结果，当然Client也可以使用sync请求最新的结果。

**修改请求的处理：**

如果Client发送的是修改请求，过程如下所示：
1. Follower/Observer需要向Leader提交请求；
2. 由Leader发起Proposal，交由Follower投票（Observer不会参与投票）；
3. Follower进行将自己的投票结果发送给Leader；
4. Leader根据投票结果发出commit指令，命令各个Follower和Observer提交/回滚。

可已看出引入Observer的目的是提高系统的伸缩性，提高系统的**读**性能，同时不会让Zookeeper的投票过程变得过于复杂。

### 2.2 Zookeeper使用的协议——ZAB协议

Zookeeper使用的ZAB协议并不是完全基于Paxos算法的一种实现，ZAB协议的目的是让集群在有节点崩溃的时候可以优雅的回复。ZAB协议主要由两个部分组成——**恢复模式**以及**广播模式**。

#### 2.2.1 恢复模式——Leader选举及数据同步

当前Leader挂掉或者集群刚刚开启的时候，集群会进入恢复模式。Leader选举同样依照过半原则进行投票，票数最多的Server便成为Leader。

（发现阶段）

1. 每个Server在投票之前都先向其他节点进行询问，询问他们投票给谁；
2. 对于其他Server的询问，Server每次都根据自己的状态回复推荐Server的ID以及上一次处理事务的zxid（刚启动的时候Server会推荐自己）；
3. 收到所有Server的回复之后，Server就会根据回复中的最大的zxid选出自己投票的Server；
4. 这个过程中得票数过半的Server会成为Leader，然后开始进行数据的同步；

（数据同步阶段）

5. Leader等待其他Server连接，Follower连接Leader，并将自己处理的最大的zxid发送给Leader；
6. Leader根据zxid确定同步点，为每一个Follower生成其相应的Proposal放入一个队列，并将相应Proposal和Commit发送给各个Follower；
7. Follower根据Leader的同步数据进行本地的数据同步，同步完成之后，通知Leader；
8. Leader收到Follower的通知之后，通知Follower进入Uptodate状态，Follower开始向Client提供服务。

#### 2.2.2 广播模式

在Zookeeper集群过半的Server可以正常工作且完成恢复模式的时候集群就可以进入广播的模式。

1. Leader收到Follower或者Client的事务请求，会生成相应的Proposal，并发送给所有的Follower；
2. Follower收到Leader发来的Proposal之后，向Leader返回ACK；
3. 当Leader收到过半Follower发送的ACK之后就会发送Commit给所有的Follower，提交事务；
4. Follower收到Commit之后，提交相应的Proposal，完成节点数据的修改。

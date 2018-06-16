# Zookeeper算法以及运行流程

## Paxos算法



### Zookeeper中的角色

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

### 恢复模式——Leader选举及数据同步

当前Leader挂掉或者集群刚刚开启的时候，集群会进入恢复模式。Leader选举同样依照过半原则进行投票，票数最多的Server便成为Leader。

1. 每个Server在投票之前都先向其他节点进行询问，询问他们投票给谁；
2. 对于其他Server的询问，Server每次都根据自己的状态回复推荐Server的ID以及上一次处理事务的zxid（刚启动的时候Server会推荐自己）；
3. 收到所有Server的回复之后，Server就会根据回复中的最大的zxid选出自己投票的Server；
4. 这个过程中得票数过半的Server会成为Leader，然后开始进行数据的同步；
5. Leader等待其他Server连接，Follower连接Leader，并将自己处理的最大啊zxid发送给Leader；
6. Leader根据zxid确定同步点，并将相应数据发送给Follower；
7. Follower根据Leader的同步数据进行本地的数据同步，同步完成之后，通知Leader；
8. Leader收到Follower的通知之后，通知Follower进入Uptodate状态，Follower开始向Client提供服务。

### 广播模式

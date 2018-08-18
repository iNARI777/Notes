# Thread Pool 源码阅读笔记

之前总结过 Executor 框架的笔记中，涉及到了 Executor 中包含的线程池的知识，当时这个部分只大致介绍了线程池的实现——使用工具类 `Executors` 通过 `ThreadPoolExecutor` 创建 `ExecutorService` 的那几项参数的涵义，但是并没有深度去探究线程池中的线程是如何去运作的。所以今天就读了下 `ThreadPoolExecutor` 的源码，把线程池运行的步骤大概总结一下。

在这篇笔记中，我不打算大篇幅的贴代码，因为代码其实并不复杂，思路很清晰，而且贴上大量的代码可能会混淆我们的视听，把我们的注意力引到一些细枝末节之上，而过于纠结代码的细枝末节并不利于我们掌握线程池的全貌。

所以希望通过逻辑上的描述并配合上少量的代码，将一个线程池从创建到任务提交的过程展示出来，而细节的部分可以在了解完线程池的大体流程后再进行分析。先确定大方向，而后扣细节，这是我阅读源码的时候比较喜欢的战术，也是我认为效率更高的方法。

## 1. ThreadPoolExecutor 中的核心成员变量

这部分要首先从 `ThreadPoolExecutor` 的构造器说起， `ThreadPoolExecutor` 的构造器我已经在之前 Executor 的笔记中稍有提及了，其最基本的构造器的声明是这样的：

    public ThreadPoolExecutor(int corePoolSize,
                          int maximumPoolSize,
                          long keepAliveTime,
                          TimeUnit unit,
                          BlockingQueue<Runnable> workQueue,
                          ThreadFactory threadFactory,
                          RejectedExecutionHandler handler)

这其中就涉及到了很多重要的变量：

* `corePoolSize` ：核心线程池中线程的数量；
* `maximumPoolSize` ：线程池中最大的线程数量，也就是 *核心线程池+费核心线程池* 的数量；
* `keepAliveTime` 以及 `unit` ：非核心线程池的最大空闲时间，其用法也会在稍后提及；
* `workQueue` ：线程池使用的任务队列，内部存放的任务是 `Runnable` 对象，是阻塞队列，常用的有 `LinkedBlockingQueue`、不存放任务的 `SynchronousQueue`等；
* `threadFactory` ：线程池中新建线程的时候都是通过这个 `threadFactory` 的 `newThread(Runnable r)` 方法获得的。
* `handler` ：在线程池中没有空闲线程并且任务队列中无法再放入任务的时候，就会通过 `handler` 所指定的拒绝策略进行拒绝服务。
>Executor 中使用的默认线程工厂就是简单的构造一个拥有 `r` 的新线程，并将其设置为非守护线程并将优先级设置为一般优先级。

## 2. ThreadPoolExecutor 内部的重要参数以及内部类

上面的几个变量都是我们需要控制的，而 `ThreadPoolExecutor` 中还有一些用于控制行为的变量和与线程池实现相关的内部类同样需要我们注意，因为它们对于线程池的运行至关重要。

下面先来看看内部类—— `Worker`，它是一个线程池的核心，新的线程都是通过新建一个 `Worker` 对象的时候使用 `threadFactory` 获得的。

    Worker(Runnable firstTask) {
        setState(-1); // inhibit interrupts until runWorker
        this.firstTask = firstTask;
        this.thread = getThreadFactory().newThread(this);
    }

看到 `newThread` 方法将 `Worker` 对象本身传了进去，这是因为 `Worker` 本身实现了 `Runnable` 接口。我们看看 `Worker` 的声明：

    private final class Worker extends AbstractQueuedSynchronizer implements Runnable

### 2.1 runWorker 方法

那么 `Worker` 的 `run` 方法做了什么事情呢？非常简单，就是让自己跑起来：

    public void run() {
        runWorker(this);
    }

所以我们就可以知道，在通过调用 `Worker` 对象中的 `thread` 的 `start()` 方法之后，就可以让我们的 `Worker` 对应的任务运行起来了。所以显而易见， `runWorker()` 就是我们的线程池 **获取** 并 **执行** 任务的核心方法了。

`runWorker` 方法中进行任务的获取和执行，获取任务的方式有两种，一种是在 `Worker` 对象的 `firstTask` 不为空的时候，直接获取这个 Task ，另一种是通过 `getTask` 方法从阻塞队列中获取任务，这个方法也关系到为什么核心线程可以一直运行，而非核心线程可以被销毁，这一部分后面讲到 `execute()` 方法的时候再具体说明。

而执行方法方面，其实很简单粗暴，直接调用了 Task 的 `run()` 方法。所以，事实上执行任务的时候并不是通过调用 `Thread` 的 `start()` 方法，所以你的任务大可不必是 `Runnable` 的对象，但是可能是设计者考虑到逻辑的一贯性，所以才把任务设计为 `Runnable` 对象。

获取任务和执行任务是在一个循环中的：

    while (task != null || (task = getTask()) != null)

所以只要条件满足，线程池中的线程就可以一直存在，而如果线程不满足了，就会执行到最后的 `processWorkerExit()` 方法，将当前的 `Worker` 从 `workers` 集合中删除（这些被删除的 `Worker` 其实就是非核心线程）。

那么 `Worker` 对象是如何被创建、其所在的线程是如何被运行起来的呢？

### 2.2 addWorker 方法

`addWorker` 方法的方法签名如下：

    private boolean addWorker(Runnable firstTask, boolean core)

在这个方法中会新建一个 `Worker` 的对象，并将其加入到 `workers` 集合中，这个过程会为其加上一个全局锁保证线程的安全性。`Worker` 对象成功添加之后就会直接拿这个 `Worker` 的 `thread` 成员并启动它，也就是开始执行 `Worker` 的 `run()` 方法，启动 `runWorker()` 方法的过程，也就连上了前面的内容。

使用 `addWorker()` 方法的方式有两种，当添加核心线程的 `Worker` 的时候，会传入 `firstTask` 并且将 `core` 设置为 `true` 。这样加入的 `Worker` 自己就带了一个 Task 进来，所以后面 `runWorker()` 的时候也是直接就可以获得 Task进行执行，并将这个 `Worker` 中的 Task 置空，然后执行 `runWorker()` 中的循环，所以第二次循环的时候核心线程才回去任务队列里面拿任务：

    while (task != null || (task = getTask()) != null)

而另一种方法是在创建非核心线程的 `Worker` 的时候，就不会传入 Task ，并且 `core` 的值也是 `false` 。这样在执行 `runWorker()` 的时候就必须要通过 `getTask()` 方法去获得阻塞队列中的任务。

并且，核心线程与非核心线程在执行 `getTask()` 的方式选择上也是不同的，主要是：

    Runnable r = timed ?
                workQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS) :
                workQueue.take();

看到了吧，因为核心线程如果在任务队列中取不到任务的话会一直阻塞进行 `take()` ，而非核心线程会使用 `poll()` 在等待最大生存时间 `keepAliveTime` 之后如果还获取不到任务的话就返回。返回后就会退出 `runWorker()` 中的循环，被销毁掉。

### 2.3 execute 方法

有了上面的解释，我们就可以很容易的理解线程池的 `execute()` 都做了些什么了（只看注释就可以了）：

    int c = ctl.get();
    // 如果线程数量不到 coreSize，创建的是核心线程
    if (workerCountOf(c) < corePoolSize) {
        if (addWorker(command, true))
            return;
        c = ctl.get();
    }
    // 线程池仍在运行且任务队列未满，就新建一个非核心线程
    if (isRunning(c) && workQueue.offer(command)) {
        int recheck = ctl.get();
        if (! isRunning(recheck) && remove(command))
            reject(command);
        else if (workerCountOf(recheck) == 0)
            addWorker(null, false);
    }
    // 任务队列满了且无法添加 Worker 对象了（如：已经达到最大线程数），就执行拒绝策略
    else if (!addWorker(command, false))
        reject(command);

至此，`ThreadPoolExecutor` 与任务执行相关的大流程就已经展现在我们的眼前了。剩下的还有一些维护线程池的变量（如 `ctl` 、 `mainLock` 还有一些线程池的状态变量）等的解释以及其在线程池运行过程中起到的维护作用在这篇笔记中是被我省略过去的，之后有时间会继续更加深入的进行分析。

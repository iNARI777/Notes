# 反射

## 1. Class对象

### 1.1 Class对象简介

**Class类中的信息**

Class是java.lang包中基本的一个类，其作用是保存Java类中的信息，是接口或者类的一个抽象。怎么理解呢，假如说我们有一个汽车，它有轮子、有发动机有方向盘等等，我们就可以把这些东西抽象出来成为一个Car类，有wheel，engine等变量，而不同的车我们可以创建不同的Car的对象，他们有着不同的wheel，engine等变量的值。而对于我们的Java类来说，Class类就是Java类的一个抽象，它记录了Java类的信息，比如：类型是class还是interface还是Enum还是annotation、名字、域及其信息、方法及其信息等等。

**Class类的创建**

每个Java类（注意不是Java文件）编译完成后都会生成一个class文件，在JVM进行类加载的时候，加载阶段会将class文件加载进内存的方法区并转化为运行时数据结构，并构造一个`Class`对象来作为这些数据的入口。且对于每个类的对象来说`Class`对象只会生成一个（由于双亲委派）。而每个类在被加载后，相应的`Class`对象就会被JVM赋予给相应的类。

`Class`没有`public`的构造方法，因此其构造方法只能有JVM来调用。而`Class`对象的作用是在运行时获得相应类的类型信息。

### 1.2 Class对象的获取

使用**Class.forName()**：

`forName()`方法是`Class`类的静态方法，调用的时候可以将一个类的完整名称传入，来获取这个类的`Class`对象。这个方法会抛出`ClassNotFoundException`。

使用字面量获取：

可以通过`Class clazz = Test.class;`这种方式来获得`Class`对象。同时，不光类可使用这种方式，**接口**、**数组**、**基本数据类型**。

> 基本数据类型的包装类中有一个`TYPE`字段，这个字段保存的就是相应基本数据类型的class对象。

使用Object的getClass()方法：

通过`Class clazz = Test.getClass();`来获取`Test`类的`Class`对象引用。

### 1.3 Class的泛化

在JDK5之后引入了泛型，在引用Class的时候同样可以使用泛型，如`Class<Integer> clazz = Integer.class;`。这样表示我们想要一个`Integer`的`Class`对象，而不是其他类型的对象，这样如果我们传入了一个非`Integer`的`Class`对象，就会在编译期报错，而不会把错误留到运行期。

如果我们直接使用`Class clazz；`的话会在编译期得到一个泛型的警告，这个时候如果我们不想规定这个`clazz`对象的类型的话，可以使用通配符`?`来解决这个问题：`Class<?> clazz;`。当然我们还可以用`<? extends ClassA>`这种方式来规定这个`clazz`对象只接受`ClassA`的子类的`class`对象。

## 2. 反射

反射机制是在运行状态中，对于任意一个类，都能够知道这个类的所有属性和方法；对于任意一个对象，都能够调用它的任意一个方法和属性，这种动态获取的信息以及动态调用对象的方法的功能称为java语言的反射机制。一直以来反射技术都是Java中的闪亮点，这也是目前大部分框架(如Spring/Mybatis等)得以实现的支柱。Class类与java.lang.reflect类库一起对反射技术进行了全力的支持。

在反射包中描述一个类的成员的类有`Constructor`、

## 参考资料

[1] [理解java.lang.Class类](https://blog.csdn.net/bingduanlbd/article/details/8424243/)

[2] [浅谈Java中的Class类](https://blog.csdn.net/my_truelove/article/details/51289217)

[3] [深入理解Java类型信息与反射机制](https://blog.csdn.net/javazejian/article/details/70768369)
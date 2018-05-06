# Java基础知识

## 1. 泛型中的\<T>和<?>

假如说我们有一个容器List，`List<T>`就是泛型方法，而`List<?>`是限制通配符。

`<T>`一般在定义泛型类或泛型方法的时候使用，**代表一个具体的Java类型**，保持每个T为同一个类或者限制方法的参数之间或参数和返回结果之间的关系，也就是泛型化参数，我们在使用泛型方法或类的时候要给它赋一个具体的值。

`<?>`是在使用的时候对泛型参数起一个限制的作用，**代表一个未知的Java类型**，而泛型声明的时候是不可以使用`<?>`的。`<?>`更多的是使用在`<? extends T>`(上界通配符)和`<? super T>`(下界通配符)中。

关于上下界通配符看[这里](https://www.zhihu.com/question/20400700/answer/117464182)和[这里](https://www.zhihu.com/question/31429113)。

这里有一个坑要注意：

	Plate<Fruit> p=new Plate<Apple>(new Apple());

是不能通过编译的，虽然`Apple`是`Fruit`的子类，但是`Plate<Fruit>`并不是`Plate<Apple>`的子类。

## 2. JDK8的几个新特性

1. 引入了流API(Stream API)；
2. 引入了Lambda表达式；
3. 接口中可以添加静态方法以及默认方法；
4. 引入了新的Time和Date API。
# 注解

## 1. 注解基础

### 1.1 注解的定义

注解的定义类似于接口的定义。假如说我们想定义一个名为Person的接口，用来描述一个人的基本特征，我们可以这么写：
	
	public @interface Person {
		String name();
		String address();
		String sex() default "male";
		int age() default 18;
		boolean isAlive() default true;
	}

可以看到，只要在想要定义的注解的名字前面加上`@interface`就可以了。同时在注解的内部可以向接口一样声明方法，这些方法被称为**注解元素**，同时，有的注解元素后面有default，后面加的就是其默认的值。

### 1.2 注解的使用

只需要在需要注解的方法使用@+注解名的方法就可以使用：
	
	public class Company {
		@Person(name="Shikura", address="Chioda, Tokyo", age=39)
		private Employee manager;

		@Person(name="Yaginuma Satoshi", address="Yokohama")
		private Employee composer;
	}

如果有的注解元素没有默认值的话，在使用注解的时候必须要在后面的小括号中加入这个注解元素的值。有默认值的注解元素可以不在小括号中出现，这个时候使用的就是该注解元素在定义时的默认值。

当然，注解中也可以没有注解元素，这个时候在使用注解的时候也就不用加小括号了。如果注解中只有一个注解元素，那么在使用注解的时候直接在小括号中写出这个注解元素的值就可以，而不用再写注解元素的名字了。

> 注意：注解对程序的运行没有影响！

### 1.3 注解到底是什么

如果我们将上面的Person注解使用`javap -c Person.class`反编译回来的话是这样：
	
	public interface Person extends java.lang.annotation.Annotation {
		public abstract java.lang.String name();
		public abstract java.lang.String address();
		public abstract java.lang.String sex();
		public abstract int age();
		public abstract boolean isAlive();
	}

可见，一个注解最终被编译成了继承了`java.lang.annotation.Annotation`一个普通的接口。因此，定义注解元素的时候那些方法自然会被改写成`public abstract`的。

注解元素的返回值可以为以下几种类型：

1. 所有基本数据类型（int、float、boolean、byte、double、char、long、short)
2. String类型
3. Class类型
4. enum类型
5. Annotation类型，它是所有注解的祖宗接口
6. 以上所有类型的数组

所以注解元素方法的返回类型是不能使用自定义的类的！

### 1.4 注解的注解——元注解

Java中自带了四个元注解，他们可以用来在**定义注解的时候**对自定义的注解进行注解。这四个注解分别是：

1. @Target：用于定义注解可以使用的位置，比如可以用在类、字段、方法等上面。
2. @Retention：用于定义注解保留的时期——源文件、class文件或运行时保留，如果在运行时保留的话，可以在运行时使用反射取得注解的信息。
3. @Document：与Java文档的生成有关，如果在一段代码上加上了这个注解，那么在使用javadoc的时候会把注解也收录到文档中。
4. @Inherited：如果在某个注解上加了这个元注解，则当该注解加在某个类上时，该类的子类也会继承到这个注解。

## 2. 注解解析器

上面我们说到了，注解本身对程序的运行没有任何影响，只是起到一个注释的作用。但是诸如Spring框架中的那些注解都起着很重要的作用。这是因为有自定义的注释解析器，这些注解的功能都是经过注解解析器的分析后才起的作用。

### 2.1 注解反射方法

在`java.lang.reflect`包中有一个`AnnotationElement`类：
	
	public interface AnnotatedElement {
	    <T extends Annotation> T getAnnotation(Class<T> annotationClass);
	    Annotation[] getAnnotations();
	    Annotation[] getDeclaredAnnotations();
	    boolean isAnnotationPresent(Class<? extends Annotation> annotationClass)
	}

所以`Class`、`Constructor`、`Field`、`Method`、`Package`都实现了AnnotatedElement接口。
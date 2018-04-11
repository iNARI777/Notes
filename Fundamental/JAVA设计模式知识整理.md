# 常见设计模式整理
作者：[whlmpower](http://note.youdao.com/noteshare?id=55f74db548a53b2de6446de4c1304bde&sub=708D3D846A4340CA95454C1E3F7B846D)

## 1. 适配器模式、装饰模式、代理模式异同   
[++ref++](https://blog.csdn.net/lulei9876/article/details/39994825)
### 1.1 概念
适配器模式：一个适配器允许因为接口不兼容而不能在一起工作的类在一起，将类自己的接口包裹在已存在的类中。   
装饰器模式：原有的不能满足需求，对原有进行增强   
代理模式：同一个类调用另一个类的方法，不对这个类直接进行操作   
在使用适配器模式的时候，我们必须同时持有原对象，适配对象，目标对象。  
装饰器模式特点在于**增强**，他的特点是被装饰类和所有的装饰类必须实现**同一个接口**，而且必须**持有被装饰**的对象，可以无限装饰。   
代理模式的特点在于**隔离**，隔离调用类和被调用类的关系，通过一个代理类去调用。     
### 1.2 总结
1. 适配器模式是将一个类a通过某种方式转换成另一个类b   
2. 装饰器模式是在一个原有类a的基础上增加某些新的功能转变成另一个类b   
3. 代理模式是将一个类a转换成具体的操作类b  

### 1.3 案例介绍
公司有一个ORDER系统，专门用于提供订单管理的接口，提供给O2O商城，WAP商城，手机类APP，微信等客户端调用。   
ORDER系统在上线之时，已经包含了非常完整的数据操作。   
(1)现手机类APP需要升级，老的接口可能不能满足需求，如原O2O订单提货延长有效期接口需要提供截至时间和订单号，新APP可能不提供截至时间，只需要 提供订单号即可延长时间。   
而老的接口又不能修改，因为老的order接口是针对所有平台的，那么必须将新接口要求与原接口进行适配。**重点在于：新老接口能进行兼容，且新接口要对老接口适配**  
**解决办法**：新增AppAdepter类，用来适配老的接口，同时开发给新的接口   
新的实现类持有了老接口的对象，就是把这个对象new出来，然后在新的方法里，进行适配操作。而这里所谓的适配就是兼容老接口和兼容新接口。
***只需要将原接口转化为客户希望的另一个接口，就是适配器模式！***

(2)公司的系统又要进行升级，我们原先的接口没有加入安全机制，导致了任何人都可以随意调用这个接口，现在公司需要对这个接口进行改造，只其只能被admin这个客户端调用，其他用户一律要输入账号密码才能调用,那么上面原接口和类不需要改动，我们只需要新增代理器即可。  
***代理模式与适配器模式最大的不同在于，代理模式是与原对象实现同一个接口，而适配器类则是匹配新的接口***  
这里的代理类必须要**实现原接口**并且持有**原接口的对象**，如何理解这句话呢？看代码中implement是实现接口，持有接口对象是在构造方法中，new一个原接口的实现对象(sourceOrderApi = new ...)   
```java
public class ProxySourceOrderApiImpl implements SourceOrderApi {  
    SourceOrderApi sourceOrderApiImpl;  
    public ProxySourceOrderApiImpl(){  
        sourceOrderApiImpl = new SourceOrderApiImpl();  
    }  
  
    @Override  
    public void updateDate(String orderId, String date, String client) {  
        //进行判断，如果是admin则更新否则让其输入账号密码  
        if("admin".equals(client)){  
            sourceOrderApiImpl.updateDate(orderId, date, client);  
        }else{  
            System.out.println("账号不是admin，没有查询权限，请输入以admin操作");  
        }  
    }  
}  
```
这样我们不需要修改原有的类，用一个代理的类来进行过滤。   


(3)现在这个延长订单，不光可以延长订单提货有效期，可以延长订单的退货有效期。我们需要做的是**丰富原有接口的功能**，不改动原有接口   
```java
public class NewSourceOrderApiImpl implements SourceOrderApi {  
  
    SourceOrderApi sourceOrderApi;  
    public NewSourceOrderApiImpl(SourceOrderApi sourceOrderApi){  
        this.sourceOrderApi = sourceOrderApi;  
    }  
    @Override  
    public void updateDate(String orderId, String date, String client) {  
        sourceOrderApi.updateDate(orderId, date, client);  
        System.out.println(client+"已将订单"+orderId+"的退款期延长至"+date);         
          
    }  
  
}  
```
在装饰器模式中，必须要有被装饰的类和装饰的类。在这套代码中，原先SourceOrderApi的对象就是被装饰的类，而新建NewSourceOrderApiImpl 就是装饰类，装饰类必须把被装饰的对象当作**参数传入**。   
***代理模式一定是自身持有这个对象，不需要外部传入，而装饰者模式一定是从外部传入，并且可以是没有顺序的，代理模式重点关注隔离机制，让外部不能访问你实际调用的对象；装饰者模式注重的是功能上的扩展，同一个方法下实现更多的功能***  

来自WHL的总结：
1. 适配器要求是**同一个业务**有新的需求，我们要做新的实现(实现可以看成接口)，更改旧的实现来匹配新的需求(加了新的接口)，两种需求是兼容的都存在。
2. 代理模式要求是代理对象和被代理对象实现的是同一个接口(只有一个接口),对代理对象是**持有状态**(new 的形式)，**屏蔽**对被代理对象的访问。
3. 装饰者模式要求的是对同一个接口(还是只有一个接口)增加**新的功能**，重点是功能上的扩展，对被装饰类是通过**参数传入**方式(和代理类的new完全不同)。


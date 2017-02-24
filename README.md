# PSF框架 Java版

## 一、工程介绍
### 1. psf-server
 * 服务端
### 2. psf-client-simple
 * 客户端,采用IO模型实现,适合低并发场景
### 3. psf-client
 * 客户端,采用NIO模型实现,适合高并发场景（实验版，不建议在线上环境使用）
### 4. psf-dome程序
 * weather-service   weather服务，打包后执行weather-service/bin/start.sh 启动服务
 * weather-app 客户端，调用各种服务
 * weather-common 通用jar包，可用于通用的Bean实体，实现序列化和反序列化

## 二、Quick Start

### 1. Add maven dependency

```xml
<dependency>
    <groupId>com.yongche.psf</groupId>
    <artifactId>psf-server</artifactId>
    <version>1.0</version>
</dependency>
```

### 2. Use spring
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd
       http://www.springframework.org/schema/context
       http://www.springframework.org/schema/context/spring-context.xsd">

    <context:component-scan base-package="com.yongche.psf.test.spring" use-default-filters="false">
        <context:include-filter type="annotation" expression="com.yongche.psf.service.ServiceController" />
    </context:component-scan>
    <!-- mmapfile线上线下机器配置 /home/y/var/ycconfig/mmap_cache.conf -->
    <bean id="serviceCenterManager" class="com.yongche.psf.server.ServiceCenterManager">
         <!--<constructor-arg name="serviceCentimes" value="10.0.11.71:5201,10.0.11.72:5201"/>-->
                <property name="serviceCenter" >
                    <array>
                        <value>10.0.11.71:5201</value>
                        <value>10.0.11.72:5201</value>
                    </array>
                </property>
    </bean>
    <bean id="serverManager" class="com.yongche.psf.server.ServerManager" init-method="init">
        <constructor-arg name="port" value="28080"/>
        <constructor-arg name="serviceType" value="weather"/>
        <constructor-arg name="version" value="1.0.0" />
        <constructor-arg name="serviceCenterManager" ref="serviceCenterManager"/>
    </bean>

</beans>
```


### 3. Use spring namespace
#### 3.1 注解标签说明建议使用配置，不会受框架底层变化影响业务代码

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:psf="http://www.yongche.com/schema/psf"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd
       http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd
       http://www.yongche.com/schema/psf http://www.yongche.com/schema/psf.xsd">

    <psf:service-scan base-package="com.yongche.psf.test.spring" />
    <!-- mmapfile线上线下机器配置 /home/y/var/ycconfig/mmap_cache.conf -->
    <psf:service-center-manager name="serviceCenterManager" service-center="10.0.11.71:5201,10.0.11.72:5201"/>

    <psf:server-manager port="28080" service-type="weather" version="1.0.0" service-center-manager="serviceCenterManager" />

</beans>
```
#### 3.2 标签 `<psf:server-manager />` 参数说明


| 属性      |  类型    | 是否必填 | 缺省值 | 作用 | 描述 |
|:----:| :----: | :----: | :----: | :----: | :-------: |
| name |         String  | 可选 |   | 标识    | 服务名称 |
| port |            int  | 必填 |   | 服务发现 | 服务注册端口 |
| service-type |  String | 必填 |   | 服务发现 | 服务注册类型 | 
| version         | int  | 必填 |   | 服务治理 | 服务注册版本1.0.0 |
| service-center-manager| String | 必填 || 配置关联| 注册中心管理器名称 | 
| weight        |   int  | 可选 | 1     | 服务治理 | 服务权重  |
| io-threads    |   int  | 可选 | cpu核数+1 | 性能调优 | IO线程池，接收网络读写中断 |
| service-threads |  int | 可选 | 50     | 性能调优 | 服务线程池大小  |

### 4. Java service 开发
#### 4.1 注解标签说明
 * @ServiceController 主要用于扫描控制层Class，等同于Java的Servlet，struts1的Action，spring mvc 中的@Controller
 * @ServiceMapping 用于uri 路径扫描，装配 装配规则 service_type ＋ class.value + method.value ，
   * 例如：CityInfoController的service_type 为 weather， getInfoByCityId 方法装配后的uri 为 /weather/getInfoByCityId；
   * CatController的getInfo方法 装配后的 uri 为 /weather/cat/info
 * @ServiceMapping 方法注解， 方法参数只有1个必须是ServiceRequest类，
   * 例如：`public String getInfoByCityId(ServiceRequest request){...}`
 * ServiceRequest 类用于service 方法入参封装类：
   * getParameters()  方法返回GET请求参数集合Map类型： /weather/getInfoByCityId?name=Sky&id=10000
   * getMessage() 方法返回POST请求的消息字符串
   * getBody() 方法返回 message字符串序列化后的JSONObject对象
   * get() 方法 首先查找body中的key，未找到后查找parameters
   * getHeaders() 方法返回headers信息的集合Map类型

#### 4.2 代码示例

```
@ServiceController
public class CityInfoController {

    @ServiceMapping(value = "/getInfoByCityId")
    public String getInfoByCityId(ServiceRequest request){
        System.out.println("header --> " + request.getHeaders());
        System.out.println("parameters --> " + request.getParameters());
        System.out.println("message --> " + request.getMessage());
        return "{\"city_id\":\"3011\",\"full\":true,\"weather_type\":\"CN\", \"res\": \"中文字母\"}";
    }
}

@ServiceController
@ServiceMapping(value = "/cat")
public class CatController {

    @ServiceMapping(value = "/info")
    public String getInfo(ServiceRequest request){
        System.out.println("header --> " + request.getHeaders());
        System.out.println("parameters --> " + request.getParameters());
        System.out.println("message --> " + request.getMessage());
        System.out.println("body --> " + request.getBody());
        System.out.println("get --> " + request.get("user_id"));
        System.out.println("get --> " + request.get("name"));
        return "{'name' : 'BlueSky','id' : 10000,'zh' : '中国'}";
    }

    @ServiceMapping(value = "/info/all")
    public Map getInfoAll(ServiceRequest request){
        System.out.println("header --> " + request.getHeaders());
        System.out.println("parameters --> " + request.getParameters());
        System.out.println("message --> " + request.getMessage());
        System.out.println("body --> " + request.getBody());
        System.out.println("get --> " + request.get("user_id"));
        System.out.println("get --> " + request.get("name"));

        Map result = new HashMap<>();
        result.put("Blue", "{'name' : 'BlueSky','id' : 10001,'zh' : '蓝色天空'}");
        result.put("Red", "{'name' : 'RedStorm','id' : 10002,'zh' : '红色风暴'}");
        result.put("Black", "{'name' : 'BlackFlash','id' : 10003,'zh' : '黑色闪电'}");
        return result;
    }

    @ServiceMapping(value = "/info/update")
    public void updateInfo(ServiceRequest request){
        System.out.println("header --> " + request.getHeaders());
        System.out.println("parameters --> " + request.getParameters());
        System.out.println("message --> " + request.getMessage());
        System.out.println("body --> " + request.getBody());
        System.out.println("get --> " + request.get("user_id"));
        System.out.println("get --> " + request.get("name"));
        // do something
    }
}

```

#### 4.3 启动服务
```
public static void main(String[] args){
    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring-context.xml");
    context.start();
}
```


#### 4.4 服务调用
```
@Test
public void testCatInfoAll() throws Exception {
   PSFClient psf = null;
   int count = 0;

   String[] serviceCenter = {"10.0.11.71:5201", "10.0.11.72:5201"};

   psf = new PSFClient(serviceCenter, 2000, 30000, 64 * 1024, 3);

   Hashtable headers = new Hashtable();
   headers.put("name","Bubu");
   headers.put("id", 1004423);

   String response = new String();
   PSFClient.PSFRPCRequestData request = new PSFClient.PSFRPCRequestData();
   request.data = "{\"user_id\":\"5555\",\"full\":true,\"user_type\":\"PA\", \"zh\": \"哈哈哦哦\"}";
   request.service_uri = "/weather/cat/info/all?ch=中国&uid=1001&time=8454534343";
   request.headers = headers;

   response = psf.call("weather", request);
   System.out.println(response);

   psf.close();
   System.out.println("|||----- Done.");
}
```
#### 4.4 服务调用打印
```
{"Red":"{'name' : 'RedStorm','id' : 10002,'zh' : '红色风暴'}","Blue":"{'name' : 'BlueSky','id' : 10001,'zh' : '蓝色天空'}","Black":"{'name' : 'BlackFlash','id' : 10003,'zh' : '黑色闪电'}"}
|||----- Done.
```

##### weather/cat/info/all service print
```
 header --> {name=可乐, id=1003223}
 parameters --> {uid=1001, ch=中国, time=8454534343}
 message --> {"user_id":"5555","full":true,"user_type":"PA", "zh": "啦啦啦啦啦"}
 body --> {"user_type":"PA","user_id":"5555","zh":"啦啦啦啦啦","full":true}
 get --> 5555
 get --> 可乐
```

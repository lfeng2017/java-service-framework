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


=====================================================================================================================================



Hi，All：
	1.消费者工程分支 https://git.yongche.org/ycpcs/consumer/tree/RELEASE，版本2.1  配置和Class、机器IP解耦，见配置一：
        2.包管理工程分支 https://git.yongche.org/ycpcs/yongche-parent/tree/RELEASE 版本 1.1
        3.配置公共类分支 https://git.yongche.org/ycpcs/config-commons/tree/RELEASE 版本1.2， 见配置二
		3.1 将Mango和数据源注册到spring容器，数据源在容器内可以复用，
		3.2 添加配置中心properties，将值以${keyname}的形式注入Bean




配置一：
     
1.开始
        <dependency>
            <groupId>com.yongche</groupId>
            <artifactId>consumer</artifactId>
            <version>2.1</version>
        </dependency>
2.配置中心 consumer.xml 属性enabled默认"true"开启，关闭可以配置false
<consumer>
  <worker name="appointWorker" vhostIp="10.0.11.209" vhostName="/dispatch" exchange="chelv_appoint" queue="chelv_appoint" count="2"/>
  <worker name="appointWorker1" vhostIp="10.0.11.215" vhostName="/dispatch" exchange="chelv_appoint" queue="chelv_appoint" count="1"/>
</consumer>
3.spring 配置 和java代码 需要添加ConsumerService 注解,默认开启模糊匹配，* 号可以省略
@ConsumerService("appointWorker*")
public class CommentWorker extends AbstractWorker {
        @Override
        protected boolean doWork(String routeKey, byte[] message)throws UnsupportedEncodingException {
            //do something
            return true;
        }
}
 <yongche:consumer-driven base-package="com.yongche.operation"/>



配置二：


1.开始
        <dependency>
             <groupId>config-commons</groupId>
             <artifactId>config-commons</artifactId>
             <version>1.2</version>
        </dependency>
2.配置中心数据源Mango，
所有数据源注册到spring容器，database注册为一个DataSourceFactory对象，master和slave注册为DataSource对象
default注册为一个DataSourceFactory，m_default 注册为一个DataSource，
如果多个slaves注册为 s_default_0，s_default_1 DataSource
database.xml 配置中心示例：
<databases>
  <database name="default">
    <master host="10.0.11.176" port="3311" db="yongche" connectionTimeout="300" maximumPoolSize="5"/>
    <slave host="10.0.11.175" port="3311" db="yongche" connectionTimeout="300" maximumPoolSize="5"/>
    <slave host="10.0.11.174" port="3311" db="yongche" connectionTimeout="300" maximumPoolSize="5"/>
  </database>
<databases>
   <!-- spring 默认配置 -->
   <bean id="centerDataSourceMango" class="com.yongche.config.database.ConfigCenterDataSourceMango" />
   <!—- 默认使用DatabaseConfig，可以自定义扩展
    <bean id="centerDataSourceMango" class="com.yongche.config.database.ConfigCenterDataSourceMango" >
        <property name="databaseConfigFaces" >
            <list>
                <value type="java.lang.Class">com.yongche.config.database.DatabaseConfig</value>
                <value type="java.lang.Class">com.yongche.operation.CheckDatabaseConfig</value>
            </list>
        </property>
    </bean>
    -->
    <context:component-scan base-package="com.yongche.operation" />
    <bean class="org.jfaster.mango.plugin.spring.MangoDaoScanner">
        <property name="packages">
            <list>
                <value>com.yongche.operation</value>
            </list>
        </property>
    </bean>
2.配置中心Properties
 properties.xml 示例：
<properties>
  <property key="namespace">test_dev</property>
  <property key="serverLists">10.0.11.175:2181,10.0.11.176:2181,10.0.11.177:2181</property>
  <property key="tracker_server_0">10.0.11.225:22122</property>
  <property key="tracker_server_1">10.0.11.217:22122</property>
  <name>ConfigCenterProperties</name>
</properties>
    <!—- 加载配置中心properties属性 -->
    <bean id="configProperties" class="com.yongche.config.prop.ConfigCenterPropertiesFactoryBean"/>
    <!--<bean id="propertyConfigurer" class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer">-->
           <!--<property name="properties" ref="configProperties" />-->
       <!--</bean>-->
    <context:property-placeholder properties-ref="configProperties" />
    <bean id="zkServer" class="com.yongche.operation.zk.ZkServer" >
            <property name="namespace" value="${namespace}"/>
            <property name="serverLists" value="${serverLists}"/>
    </bean>

# JAVA 从零到企业级电商（服务端)_v1.0
## 1. 架构

### 1. 淘宝项目架构

![image-20200127195249809](doc/image-20200127195249809.png)

### 2. 服务器版本演进

- 单一服务器

  ![image-20200127195427965](doc/image-20200127195427965.png)

- 数据，应用服务器分离

  ![image-20200127195517974](doc/image-20200127195517974.png)

- 添加缓存

  - 分布式缓存
  - 本地缓存

  ![image-20200127214027002](doc/image-20200127214027002.png)

- 服务器集群

  使用多个服务器，使用 负载均衡

  ![image-20200127214116686](doc/image-20200127214116686.png)

- 数据库读写分离

  1. 读在从库，写在主库
  2. 实现 data access module 模块，感知不到底层的读写分离（对业务没有侵入)

  ![image-20200127214549572](doc/image-20200127214549572.png)

- 使用 CDN 和反向代理服务器

  ![image-20200127214811375](doc/image-20200127214811375.png)

-  使用分布式文件服务器

  ![image-20200127214943502](doc/image-20200127214943502.png)

  
  
- 使用专库专用

![image-20200127215018750](doc/image-20200127215018750.png)

- 数据库水平拆分

  ![image-20200127215040303](doc/image-20200127215040303.png)

- 搜索功能抽取一个搜索引擎

   ![image-20200127215208858](doc/image-20200127215208858.png)

![image-20200127215258290](doc/image-20200127215258290.png)

==> 高大上的技术架构都不是一蹴而就的

## 2. 环境安装

### 1. vsftpd

![image-20200127215658704](doc/image-20200127215658704.png)

- 安装

  Docker 方式进行安装

  注意: 使用 /Users/ios/15_nginx 目录，如果用户为 file, 则 图片保存路径为 
  
   /Users/ios/15_nginx/file/xxx.jpg
  
  ==》只要 nginx 中 静态文件夹也使用 /Users/ios/15_nginx/file/ 就可以实现  ftp上传，拼接 url后，使用 nginx 进行显示
  
  ```
  docker pull fauria/vsftpd
  
  docker run -d 
  -v /Users/ios/15_nginx:/home/vsftpd 
  -p 20:20 -p 21:21 
  -p  21100-21110:21100-21110 
  -e FTP_USER=file 
  -e FTP_PASS=test 
  -e PASV_ADDRESS=127.0.0.1 
  -e PASV_MIN_PORT=21100 
  -e PASV_MAX_PORT=21110 
  --name vsftpd 
  --restart=always fauria/vsftpd
  ```

### 2. Nginx



![image-20200127230350413](doc/image-20200127230350413.png)

![image-20200127230412198](doc/image-20200127230412198.png)

- docker安装nginx

```
docker pull nginx:latest
```

- 启动临时 nginx（用来拷贝配置文件)

```
docker run --name tmp-nginx -d nginx
```

- 从docker拷贝出nginx的配置文件

```
docker cp tmp-nginx:/etc/nginx/nginx.conf /Users/ios/15_nginx/nginx.conf

docker cp tmp-nginx:/etc/nginx/conf.d/default.conf /Users/ios/15_nginx/conf.d/default.conf
```

- 删除 nginx 容器

```
docker rm -f tmp-nginx
```

- 创建 本地 nginx 目录结构

```
- 15_nginx
	- conf.d    // nginx 的自定义配置文件
		- default.conf
	- file      // 图片文件目录（用作图片服务器)
	- log       // 挂载 nginx 的日志
	- nginx.conf // nginx 的 配置文件
	- www        // 用来放置前端工程
```

- default.conf 文件内容

  其中 www.day.com 为 在 /etc/hosts 中配置的 127.0.0.1 地址

  location 中 root 指定 的是 docker 中 nginx 的目录（挂载到本地即可)

```conf
server {
    listen       80;
    server_name  www.day.com;

    #charset koi8-r;
    #access_log  /var/log/nginx/host.access.log  main;

    location / {
	#proxy_pass http://127.0.0.1:8080;
	#add_header Access-Control-Allow-Origin *;
        root   /home/file;
        index  index.html index.htm;
    }
    ...
}
```

- 启动 nginx 容器

  其中 -v /Users/ios/15_nginx/file:/home/file 是用来挂载 本地目录到 docker中的 /home/file 目录

```
docker run --name nginx -p 80:80 
-v /Users/ios/15_nginx/nginx.conf:/etc/nginx/nginx.conf 
-v /Users/ios/15_nginx/conf.d:/etc/nginx/conf.d 
-v /Users/ios/15_nginx/www:/www  
-v /Users/ios/15_nginx/log/:/var/log/nginx  
-v /Users/ios/15_nginx/file:/home/file 
-d nginx
```

- 测试

  在 /Users/ios/15_nginx/file 目录下放置一个文件 1.jpg，从浏览器输入 www.day.com/1.jpg 访问即可

## 3. 表结构

### 1. 用户表

```sql
DROP TABLE IF EXISTS `mmall_user`;
CREATE TABLE `mmall_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '用户表id',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(50) NOT NULL COMMENT '用户密码，MD5加密',
  `email` varchar(50) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `question` varchar(100) DEFAULT NULL COMMENT '找回密码问题',
  `answer` varchar(100) DEFAULT NULL COMMENT '找回密码答案',
  `role` int(4) NOT NULL COMMENT '角色0-管理员,1-普通用户',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '最后一次更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_name_unique` (`username`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8;
```

### 2. 分类表

```sql
DROP TABLE IF EXISTS `mmall_category`;
CREATE TABLE `mmall_category` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '类别Id',
  `parent_id` int(11) DEFAULT NULL COMMENT '父类别id当id=0时说明是根节点,一级类别',
  `name` varchar(50) DEFAULT NULL COMMENT '类别名称',
  `status` tinyint(1) DEFAULT '1' COMMENT '类别状态1-正常,2-已废弃',
  `sort_order` int(4) DEFAULT NULL COMMENT '排序编号,同类展示顺序,数值相等则自然排序',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100032 DEFAULT CHARSET=utf8;
```

### 3. 产品表

```sql
DROP TABLE IF EXISTS `mmall_product`;
CREATE TABLE `mmall_product` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '商品id',
  `category_id` int(11) NOT NULL COMMENT '分类id,对应mmall_category表的主键',
  `name` varchar(100) NOT NULL COMMENT '商品名称',
  `subtitle` varchar(200) DEFAULT NULL COMMENT '商品副标题',
  `main_image` varchar(500) DEFAULT NULL COMMENT '产品主图,url相对地址',
  `sub_images` text COMMENT '图片地址,json格式,扩展用',
  `detail` text COMMENT '商品详情',
  `price` decimal(20,2) NOT NULL COMMENT '价格,单位-元保留两位小数',
  `stock` int(11) NOT NULL COMMENT '库存数量',
  `status` int(6) DEFAULT '1' COMMENT '商品状态.1-在售 2-下架 3-删除',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8;
```

### 4. 购物车表

```sql
DROP TABLE IF EXISTS `mmall_cart`;
CREATE TABLE `mmall_cart` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `product_id` int(11) DEFAULT NULL COMMENT '商品id',
  `quantity` int(11) DEFAULT NULL COMMENT '数量',
  `checked` int(11) DEFAULT NULL COMMENT '是否选择,1=已勾选,0=未勾选',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `user_id_index` (`user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=146 DEFAULT CHARSET=utf8;
```

### 5. 支付信息表

```sql
DROP TABLE IF EXISTS `mmall_pay_info`;
CREATE TABLE `mmall_pay_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) DEFAULT NULL COMMENT '用户id',
  `order_no` bigint(20) DEFAULT NULL COMMENT '订单号',
  `pay_platform` int(10) DEFAULT NULL COMMENT '支付平台:1-支付宝,2-微信',
  `platform_number` varchar(200) DEFAULT NULL COMMENT '支付宝支付流水号',
  `platform_status` varchar(20) DEFAULT NULL COMMENT '支付宝支付状态',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=61 DEFAULT CHARSET=utf8;
```

### 6. 订单表

```sql
DROP TABLE IF EXISTS `mmall_order`;
CREATE TABLE `mmall_order` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '订单id',
  `order_no` bigint(20) DEFAULT NULL COMMENT '订单号',
  `user_id` int(11) DEFAULT NULL COMMENT '用户id',
  `shipping_id` int(11) DEFAULT NULL,
  `payment` decimal(20,2) DEFAULT NULL COMMENT '实际付款金额,单位是元,保留两位小数',
  `payment_type` int(4) DEFAULT NULL COMMENT '支付类型,1-在线支付',
  `postage` int(10) DEFAULT NULL COMMENT '运费,单位是元',
  ` status` int(10) DEFAULT NULL COMMENT '订单状态:0-已取消-10-未付款，20-已付款，40-已发货，50-交易成功，60-交易关闭',
  `payment_time` datetime DEFAULT NULL COMMENT '支付时间',
  `send_time` datetime DEFAULT NULL COMMENT '发货时间',
  `end_time` datetime DEFAULT NULL COMMENT '交易完成时间',
  `close_time` datetime DEFAULT NULL COMMENT '交易关闭时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_no_index` (`order_no`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=118 DEFAULT CHARSET=utf8;
```

### 7. 订单明细表

```sql
DROP TABLE IF EXISTS `mmall_order_item`;
CREATE TABLE `mmall_order_item` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '订单子表id',
  `user_id` int(11) DEFAULT NULL,
  `order_no` bigint(20) DEFAULT NULL,
  `product_id` int(11) DEFAULT NULL COMMENT '商品id',
  `product_name` varchar(100) DEFAULT NULL COMMENT '商品名称',
  `product_image` varchar(500) DEFAULT NULL COMMENT '商品图片地址',
  `current_unit_price` decimal(20,2) DEFAULT NULL COMMENT '生成订单时的商品单价，单位是元,保留两位小数',
  `quantity` int(10) DEFAULT NULL COMMENT '商品数量',
  `total_price` decimal(20,2) DEFAULT NULL COMMENT '商品总价,单位是元,保留两位小数',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `order_no_index` (`order_no`) USING BTREE,
  KEY `order_no_user_id_index` (`user_id`,`order_no`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=135 DEFAULT CHARSET=utf8;
```

### 8. 收货地址表

```sql
DROP TABLE IF EXISTS `mmall_shipping`;
CREATE TABLE `mmall_shipping` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) DEFAULT NULL COMMENT '用户id',
  `receiver_name` varchar(20) DEFAULT NULL COMMENT '收货姓名',
  `receiver_phone` varchar(20) DEFAULT NULL COMMENT '收货固定电话',
  `receiver_mobile` varchar(20) DEFAULT NULL COMMENT '收货移动电话',
  `receiver_province` varchar(20) DEFAULT NULL COMMENT '省份',
  `receiver_city` varchar(20) DEFAULT NULL COMMENT '城市',
  `receiver_district` varchar(20) DEFAULT NULL COMMENT '区/县',
  `receiver_address` varchar(200) DEFAULT NULL COMMENT '详细地址',
  `receiver_zip` varchar(6) DEFAULT NULL COMMENT '邮编',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8;
```

## 4. 项目搭建

### 1. 创建基本 maven 项目

1. 使用 maven  artchtype web-app进行创建

2. 项目创建完毕之后，补充完整文件夹

### 2. pom 添加依赖

### 3. 集成Spring

#### 1. 配置web.xml

##### 配置请求转码 utf-8 (使用 filter)

```xml
<!-- 将所有请求都转为 UTF_8 -->
    <filter>
        <filter-name>characterEncodingFilter</filter-name>
        <filter-class>org.springframework.web.filter.CharacterEncodingFilter</filter-class>
        <init-param>
            <param-name>encoding</param-name>
            <param-value>UTF-8</param-value>
        </init-param>
        <init-param>
            <param-name>forceEncoding</param-name>
            <param-value>true</param-value>
        </init-param>
    </filter>
    <filter-mapping>
        <filter-name>characterEncodingFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
```

##### 配置 Web容器 监听器

```xml
<listener>
  <listener-class>org.springframework.web.context.request.RequestContextListener</listener-class>
    </listener>
```

##### 配置 Web容器与 Spring容器整合的监听器

```
<listener>
        <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
    </listener>
```

##### 指定 Spring 的配置文件

```xml
 <context-param>
        <param-name>contextConfigLocation</param-name>
        <param-value>
            classpath:applicationContext.xml
        </param-value>
    </context-param>
```

##### 配置 SpringMVC 拦截的请求

```
 <!-- *.do 的请求，都会被 SpringMVC 进行拦截 -->
    <servlet>
        <servlet-name>dispatcher</servlet-name>
        <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
        <!-- 默认的 servlet 对应的 xml 文件名称为 servlet名称-servlet.xml
            在此处即 dispatcher-servlet.xml (在 WEB-INFO 下)
            如果需要更改名称，可以使用
            <init-param>
                <param-name>contextConfigLocation</param-name>
                <param-value>/WEB-INF/想要的.xml</param-value>
            </init-param>
        -->
        <load-on-startup>1</load-on-startup>
    </servlet>
    <servlet-mapping>
        <servlet-name>dispatcher</servlet-name>
        <url-pattern>*.do</url-pattern>
    </servlet-mapping>
```

#### 2. 配置 Spring 配置文件

- 扫描包
- 开启aop
- 加载另外的配置文件

```
		<!-- 扫描 com.mmall 包下的注解 -->
    <context:component-scan base-package="com.mmall" annotation-config="true"/>

    <!--<context:annotation-config/>-->
    <!-- aop 的配置 -->
    <aop:aspectj-autoproxy/>

    <!-- 加载另外的配置文件 (将配置放在多个 xml 配置文件中 )-->
    <import resource="applicationContext-datasource.xml"/>
```

- 创建property bean, 读取 properties中配置项

```xml
<bean id="propertyConfigurer"
          class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer">
        <property name="order" value="2"/>
        <property name="ignoreUnresolvablePlaceholders" value="true"/>
        <property name="locations">
            <list>
                <value>classpath:datasource.properties</value>
            </list>
        </property>
        <property name="fileEncoding" value="utf-8"/>
    </bean>
```

- 配置连接池

```xml
<!-- 连接池的配置 -->
    <bean id="dataSource" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="${db.driverClassName}"/>
        <property name="url" value="${db.url}"/>
        <property name="username" value="${db.username}"/>
        <property name="password" value="${db.password}"/>
        <!-- 连接池启动时的初始值 -->
        <property name="initialSize" value="${db.initialSize}"/>
        <!-- 连接池的最大值 -->
        <property name="maxActive" value="${db.maxActive}"/>
        <!-- 最大空闲值.当经过一个高峰时间后，连接池可以慢慢将已经用不到的连接慢慢释放一部分，一直减少到maxIdle为止 -->
        <property name="maxIdle" value="${db.maxIdle}"/>
        <!-- 最小空闲值.当空闲的连接数少于阀值时，连接池就会预申请去一些连接，以免洪峰来时来不及申请 -->
        <property name="minIdle" value="${db.minIdle}"/>
        <!-- 最大建立连接等待时间。如果超过此时间将接到异常。设为－1表示无限制 -->
        <property name="maxWait" value="${db.maxWait}"/>
        <!--#给出一条简单的sql语句进行验证 -->
         <!--<property name="validationQuery" value="select getdate()" />-->
        <property name="defaultAutoCommit" value="${db.defaultAutoCommit}"/>
        <!-- 回收被遗弃的（一般是忘了释放的）数据库连接到连接池中 -->
         <!--<property name="removeAbandoned" value="true" />-->
        <!-- 数据库连接过多长时间不用将被视为被遗弃而收回连接池中 -->
         <!--<property name="removeAbandonedTimeout" value="120" />-->
        <!-- #连接的超时时间，默认为半小时。 -->
        <property name="minEvictableIdleTimeMillis" value="${db.minEvictableIdleTimeMillis}"/>

        <!--# 失效检查线程运行时间间隔，要小于MySQL默认-->
        <property name="timeBetweenEvictionRunsMillis" value="40000"/>
        <!--# 检查连接是否有效-->
        <property name="testWhileIdle" value="true"/>
        <!--# 检查连接有效性的SQL语句-->
        <property name="validationQuery" value="SELECT 1 FROM dual"/>
    </bean>
```

- 配置 mybatis  (指定分页插件)

```xml
<!-- mybatis 的 sqlSessionFactory 的配置 -->
    <bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
        <!-- 指定数据源 -->
        <property name="dataSource" ref="dataSource"/>
        <!-- 指定 mapper xml 文件 -->
        <property name="mapperLocations" value="classpath*:mappers/*Mapper.xml"></property>

        <!-- 指定 分页插件 -->
        <property name="plugins">
            <array>
                <bean class="com.github.pagehelper.PageHelper">
                    <property name="properties">
                        <value>
                            dialect=mysql
                        </value>
                    </property>
                </bean>
            </array>
        </property>

    </bean>


<!-- 配置mybatis dao 层扫描 -->
    <bean name="mapperScannerConfigurer" class="org.mybatis.spring.mapper.MapperScannerConfigurer">
        <property name="basePackage" value="com.mmall.dao"/>
    </bean>
```

- 事务配置

```xml
<!-- 使用@Transactional进行声明式事务管理需要声明下面这行 -->
    <tx:annotation-driven transaction-manager="transactionManager" proxy-target-class="true" />
    <!-- 事务管理 -->
    <bean id="transactionManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
        <property name="dataSource" ref="dataSource"/>
        <property name="rollbackOnCommitFailure" value="true"/>
    </bean>
```

#### 3. 配置 servlet.xml

- 包扫描
- springmvc json 转换
- 文件上传

```
<context:component-scan base-package="com.mmall" annotation-config="true"/>

    <mvc:annotation-driven>
        <mvc:message-converters>
            <bean class="org.springframework.http.converter.StringHttpMessageConverter">
                <property name="supportedMediaTypes">
                    <list>
                        <value>text/plain;charset=UTF-8</value>
                        <value>text/html;charset=UTF-8</value>
                    </list>
                </property>
            </bean>
            <bean class="org.springframework.http.converter.json.MappingJacksonHttpMessageConverter">
                <property name="supportedMediaTypes">
                    <list>
                        <value>application/json;charset=UTF-8</value>
                    </list>
                </property>
            </bean>
        </mvc:message-converters>
    </mvc:annotation-driven>



    <!-- 文件上传 -->
    <bean id="multipartResolver" class="org.springframework.web.multipart.commons.CommonsMultipartResolver">
        <property name="maxUploadSize" value="10485760"/> <!-- 10m -->
        <property name="maxInMemorySize" value="4096" />
        <property name="defaultEncoding" value="UTF-8"></property>
    </bean>
```



### 4. Mybatis-genator 生成 dao, pojo, mapper 等

## 5. Mybatis-genator

使用 mybatis-genator 插件，根据数据表，生成对应的 dao, pojo, mapper

### 1. 添加 mybatis-genator 依赖

在 pom 中，添加 plugin 插件

```
<build>
    <finalName>mmall</finalName>
    <plugins>

      <!-- 配置 mybatis generator 依赖 -->
      <plugin>
        <groupId>org.mybatis.generator</groupId>
        <artifactId>mybatis-generator-maven-plugin</artifactId>
        <version>1.3.2</version>
        <configuration>
          <verbose>true</verbose>
          <overwrite>true</overwrite>
        </configuration>
      </plugin>

```

### 2.在 resources 下，定义 generatorConfig.xml 文件

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE generatorConfiguration
        PUBLIC "-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN"
        "http://mybatis.org/dtd/mybatis-generator-config_1_0.dtd">

<generatorConfiguration>
    <!--导入属性配置-->
    <properties resource="datasource.properties"></properties>

    <!--指定特定数据库的jdbc驱动jar包的位置-->
    <classPathEntry location="${db.driverLocation}"/>

    <context id="default" targetRuntime="MyBatis3">

        <!-- optional，旨在创建class时，对注释进行控制 -->
        <commentGenerator>
            <property name="suppressDate" value="true"/>
            <property name="suppressAllComments" value="true"/>
        </commentGenerator>

        <!--jdbc的数据库连接 -->
        <jdbcConnection
                driverClass="${db.driverClassName}"
                connectionURL="${db.url}"
                userId="${db.username}"
                password="${db.password}">
        </jdbcConnection>


        <!-- 非必需，类型处理器，在数据库类型和java类型之间的转换控制-->
        <javaTypeResolver>
            <property name="forceBigDecimals" value="false"/>
        </javaTypeResolver>


        <!-- Model模型生成器,用来生成含有主键key的类，记录类 以及查询Example类
            targetPackage     指定生成的model生成所在的包名
            targetProject     指定在该项目下所在的路径
        -->
        <!--<javaModelGenerator targetPackage="com.mmall.pojo" targetProject=".\src\main\java">-->
        <javaModelGenerator targetPackage="com.mmall.pojo" targetProject="./src/main/java">
            <!-- 是否允许子包，即targetPackage.schemaName.tableName -->
            <property name="enableSubPackages" value="false"/>
            <!-- 是否对model添加 构造函数 -->
            <property name="constructorBased" value="true"/>
            <!-- 是否对类CHAR类型的列的数据进行trim操作 -->
            <property name="trimStrings" value="true"/>
            <!-- 建立的Model对象是否 不可改变  即生成的Model对象不会有 setter方法，只有构造方法 -->
            <property name="immutable" value="false"/>
        </javaModelGenerator>

        <!--mapper映射文件生成所在的目录 为每一个数据库的表生成对应的SqlMap文件 -->
        <!--<sqlMapGenerator targetPackage="mappers" targetProject=".\src\main\resources">-->
        <sqlMapGenerator targetPackage="mappers" targetProject="./src/main/resources">
            <property name="enableSubPackages" value="false"/>
        </sqlMapGenerator>

        <!-- 客户端代码，生成易于使用的针对Model对象和XML配置文件 的代码
                type="ANNOTATEDMAPPER",生成Java Model 和基于注解的Mapper对象
                type="MIXEDMAPPER",生成基于注解的Java Model 和相应的Mapper对象
                type="XMLMAPPER",生成SQLMap XML文件和独立的Mapper接口
        -->

        <!-- targetPackage：mapper接口dao生成的位置 -->
        <!--<javaClientGenerator type="XMLMAPPER" targetPackage="com.mmall.dao" targetProject=".\src\main\java">-->
        <javaClientGenerator type="XMLMAPPER" targetPackage="com.mmall.dao" targetProject="./src/main/java">
            <!-- enableSubPackages:是否让schema作为包的后缀 -->
            <property name="enableSubPackages" value="false" />
        </javaClientGenerator>


        <table tableName="mmall_shipping" domainObjectName="Shipping" enableCountByExample="false" enableUpdateByExample="false" enableDeleteByExample="false" enableSelectByExample="false" selectByExampleQueryId="false"></table>
        <table tableName="mmall_cart" domainObjectName="Cart" enableCountByExample="false" enableUpdateByExample="false" enableDeleteByExample="false" enableSelectByExample="false" selectByExampleQueryId="false"></table>
        <table tableName="mmall_cart_item" domainObjectName="CartItem" enableCountByExample="false" enableUpdateByExample="false" enableDeleteByExample="false" enableSelectByExample="false" selectByExampleQueryId="false"></table>
        <table tableName="mmall_category" domainObjectName="Category" enableCountByExample="false" enableUpdateByExample="false" enableDeleteByExample="false" enableSelectByExample="false" selectByExampleQueryId="false"></table>
        <table tableName="mmall_order" domainObjectName="Order" enableCountByExample="false" enableUpdateByExample="false" enableDeleteByExample="false" enableSelectByExample="false" selectByExampleQueryId="false"></table>
        <table tableName="mmall_order_item" domainObjectName="OrderItem" enableCountByExample="false" enableUpdateByExample="false" enableDeleteByExample="false" enableSelectByExample="false" selectByExampleQueryId="false"></table>
        <table tableName="mmall_pay_info" domainObjectName="PayInfo" enableCountByExample="false" enableUpdateByExample="false" enableDeleteByExample="false" enableSelectByExample="false" selectByExampleQueryId="false"></table>
        <table tableName="mmall_product" domainObjectName="Product" enableCountByExample="false" enableUpdateByExample="false" enableDeleteByExample="false" enableSelectByExample="false" selectByExampleQueryId="false">
            <columnOverride column="detail" jdbcType="VARCHAR" />
            <columnOverride column="sub_images" jdbcType="VARCHAR" />
        </table>
        <table tableName="mmall_user" domainObjectName="User" enableCountByExample="false" enableUpdateByExample="false" enableDeleteByExample="false" enableSelectByExample="false" selectByExampleQueryId="false"></table>


        <!-- geelynote mybatis插件的搭建 -->
    </context>
</generatorConfiguration>
```

### 3. 在 resources 下，添加 datasource.properties

```properties
db.driverLocation=/Users/ios/mysql-connector-java-5.1.6-bin.jar
db.driverClassName=com.mysql.jdbc.Driver

db.url=jdbc:mysql://127.0.0.1:3306/mmall?characterEncoding=utf-8
db.username=root
db.password=123456


db.initialSize = 20
db.maxActive = 50
db.maxIdle = 20
db.minIdle = 10
db.maxWait = 10
db.defaultAutoCommit = true
db.minEvictableIdleTimeMillis = 3600000

```

### 4. 根据xml配置，生成 dao, pojo, mapper

![image-20200128223420483](doc/image-20200128223420483.png)

- 效果

  ![image-20200128223507380](doc/image-20200128223507380.png)

## 6. POJO BO VO

1. POJO 用来与数据库做交互
2. 请求数据，响应数据 用 VO做封装 （要什么，返回什么)
3. 如果 业务麻烦，可以在 service 层 新增一个 BO对象，对 POJO 做进一层的封装；如果简单，Service 层与 Controller 都使用 VO 即可

![image-20200130164210776](doc/image-20200130164210776.png)

## 7. 开发技巧

### 1. 统一返回对象

​	由于返回一般为 msg, code, data 其中 msg 和 code是通用的

因此:

	1. data 使用 泛型，可以保持任意需要返回的对象
 	2. 使用 static 方法，提供便捷方式（不提供 构造方法)

```java
package com.mmall.common;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;

/**
 * 通用响应对象
 * @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL) 用来忽略值为 null的字段 （json序列化后不返回)
 * @param <T>
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ServerResponse<T> implements Serializable {
    private int status;
    private String msg;
    private T data;

    /* 将构造方法都设置为 private，提供 static 方法供使用 */
    private ServerResponse(int status) {
        this.status = status;
    }

    /* 注意 这个和下面一个方法 的区别
    *
    * ServerResponse(1, new Object()) 会调用该方法
    * ServerResponse(1, "aa") 会调用下面的重载 (精确优先)
    * ==> 如果 data 为 string 类型，则会造成歧义(本来赋值给 data， 结果赋值给了 msg)
    *
    * */
    private ServerResponse(int status, T data) {
        this.status = status;
        this.data = data;
    }

    private ServerResponse(int status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    private ServerResponse(int status, String msg, T data) {
        this.status = status;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 使用 @JsonIgnore, 不序列化 isSuccess 字段
     * @return
     */
    @JsonIgnore
    public boolean isSuccess() {
        return this.status == ResponseCode.SUCCESS.getCode();
    }

    public static <T> ServerResponse<T> createBySuccess() {
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode());
    }

    public static <T> ServerResponse<T> createBySuccessMessage(String msg) {
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(), msg);
    }

    public static <T> ServerResponse<T> createBySuccess(T data) {
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(), data);
    }

    public static <T> ServerResponse<T> createBySuccess(String msg, T data) {
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(), msg, data);
    }

    public static <T> ServerResponse<T> createByError() {
        return new ServerResponse<T>(ResponseCode.ERROR.getCode(),
                ResponseCode.ERROR.getDesc());
    }

    public static <T> ServerResponse<T> createByErrorMessage(String msg) {
        return new ServerResponse<T>(ResponseCode.ERROR.getCode(),
                msg);
    }

    public static <T> ServerResponse<T> createByErrorCodeMessage(int code, String msg) {
        return new ServerResponse<T>(code, msg);
    }


    public int getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }
}

```

### 2. 接口做枚举

接口类型 用来定义枚举，相对于使用枚举类型，更加的轻量

```java
public class Const {
    /* 使用 内部接口方式，比枚举更加轻量 */
    public interface Role {
        int ROLE_CUSTOMER = 0; /* 用户 */
        int ROLE_ADMIN = 1; /* 管理员 */
    }
}
```

### 3. StringUtils 工具

用来做 字符串是否为空的判断

```
org.apache.commons.lang3.StringUtils的isBlank isNotBlank
```

### 4. TokenCache

使用 ```com.google.common.cache.LoadingCache``` 来做简单的 token 保存

### 5. MyBatis 分页

1. PageHelper.startPage
2. 创建 PageInfo 对象

```java
@Override
    public ServerResponse<PageInfo> list(Integer userId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        List<Shipping> shippingList = shippingMapper.selectByUserId(userId);
        PageInfo pageInfo = new PageInfo(shippingList);
        return ServerResponse.createBySuccess(pageInfo);
    }
```

### 6. 图片上传 & FTP

### 7. 无限分类 & 查找所有分类

### 8. Mybatis foreach

- 方法

  ```
      int deleteByUserIdProductIds(@Param("userId") Integer userId,@Param("productIdList")List<String> productIdList);
  
  ```

- 查询语句

```
<delete id="deleteByUserIdProductIds" parameterType="map">
    delete from mmall_cart
    where user_id = #{userId}
    <if test="productIdList != null">
      and product_id in
      <foreach collection="productIdList" item="item" index="index" open="(" separator="," close=")">
        #{item}
      </foreach>
    </if>
  </delete>
```

### 9. BigDecimal 做价格计算

BigDecimal 在做价格计算的时候，只有将 Double 转为 String，才能准确

因此，可以提供一个 Utils 类，方便使用

```
public static BigDecimal add(double v1,double v2){
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.add(b2);
    }
```



## 9. 支付

![image-20200131111315540](doc/image-20200131111315540.png)

### 10. natapp 进行内网穿透

- 地址

```
https://natapp.cn/
```

#### 1. 购买隧道

![image-20200201144606670](doc/image-20200201144606670.png)

#### 2. 命令行启动

- 下载客户端(mac版)
- 执行

```
chmod +x natapp

// 其中 authtoken 为 隧道对应点 authtoken
./natapp -authtoken=54c6d92f8b594b61
```

- 效果

  其中 红框部分是域名，会将请求转发到 127.0.0.1 的 8080 端口

![image-20200201144829594](doc/image-20200201144829594.png)

### 12. 云服务器部署

# V2.0

## 1. lombok

通过简单地注解，来消除冗长的代码

1. 可以避免 更改字段名之后，忘记修改 getter setter 方法
2. 支持 @slfj 或 @Log4j方便 logger 使用

### 原理:

Javac 从 Java6开始，就支持了 JSR 269 API 规范

​	==> 只要实现了该 API，就可以在 javac 运行期间得到调用

总结:

​	Lombok 实现了 该API，因此，在 javac 编译源码时，根据使用的注解，自动生成 setter 和 getter 方法

javac 编译流程

![image-20200202115034446](../笔记/image-20200202115034446.png)

### 使用

- maven 引入依赖

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.10</version>
    <scope>provided</scope>
</dependency>
```

- IDEA 安装 lombok 插件

### 注意点:

​	@Data 注解 包含了

```
 Getter 
 Setter
 RequiredArgsConstructor
 ToString
 EqualsAndHashCode
```

等注解，因此，通常使用 @Getter @Setter 即可
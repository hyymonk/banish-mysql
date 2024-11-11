# banish-mysql

一个易于使用的游戏ORM框架，可适用于JDK17以上版本

## 概览
+ 适用于Mysql5.x、Mysql8.x
+ 通过类级别、字段级别的注解，可自动构建Mysql数据表格，也可通过修改类、字段的类型定义、注解定义使数据库表格发生相应变化
+ 基本的CURD语句无需开发者拼写，并提供相应的SQL拼写工具来实现复杂的查询操作
+ 提供普通实体与分表实体的实现，分表实体大多应用于日志数据按时间分表
+ 开发者几乎不必关注SQL语句，取而代之的是一些get、insert、update的函数调用
+ 提供异步入库的特性，可在类级别的注解上根据应用场景设置异步入库的级别、时间间隔、批量操作数量等
+ 此框架也可应用于非游戏项目的开发，但事务的处理需要开发者自行实现

## 使用简述
+ 初始化  
DaosBooter daosBooter = new DaosBooter(服务器分区号);  
daosBooter.addDataSource(实现了IDataSource接口的数据源);  
daosBooter.addEntityClass(继承了AbstractEntity基类的类型);  
daosBooter.addEntityClasses(继承了AbstractEntity基类的类型集合);  
daosBooter.addValueFormater(实现了ValueFormatter的接口);  
daosBooter.setup();  

+ 调用  
Daos.get(数据库分区号, 实体类类型).insert(实体对象);  
Daos.get(数据库分区号, 实体类类型).update(实体对象);  
...
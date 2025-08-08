# banish-mysql

一个易于使用的游戏ORM框架，可适用于JDK17以上版本

## 概览
+ 适用于Mysql5.x、Mysql8.x、PostgreSql14+
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
daosBooter.addValueFormater(实现了ValueFormatter接口的值格式化器，可参考test目录中的JsonFormatter);  
daosBooter.setup();  

+ 调用  
Daos.get(数据库分区号, 实体类类型).insert(实体对象);  
Daos.get(数据库分区号, 实体类类型).update(实体对象);  
...

+ 实体类中的使用案例  

```code
//一个简单的实体类 
@Table(comment = "例子表", dbAlias = "data",  
    indexs = {  
        @Index(fields = { "provinceCode", "cityCode" }),  
        @Index(fields = { "cardId" }, type = IndexType.UNIQUE) })  
public class ExampleEntity extends AbstractEntity {  
    @Id(strategy = Strategy.AUTO)  
    @Column(comment = "唯一ID")  
    private long id;  
    @Column(comment = "身份ID", readonly = true)  
    private String cardId;  
    @Column(comment = "名字")  
    private String name;  
    @Column(comment = "性别")  
    private byte sex;  
    @Column(comment = "年龄")  
    private short age;  
    @Column(comment = "省份编码")  
    private int provinceCode;  
    @Column(comment = "城市编码")  
    private int cityCode;  
    @Column(comment = "金额")  
    private long money;  
    @Column(comment = "地址")  
    private String address;  
    @Column(comment = "出生时间", extra = "time")  
    private long bornTime;  
    @Column(comment = "描述", extra = "text")  
    private String describe;  
    @Column(comment = "爱好", length = 5000)  
    private List<String> hobbise = new ArrayList<>();  
    @Column(comment = "教育经历", extra = "text")  
    private List<EducationInfo> educationInfos = new ArrayList<>();  
    @Column(comment = "银行卡信息", extra = "text")  
    private Map<String, BankCard> bankCards = new HashMap<>();  
    @Column(comment = "最后一笔支付")  
    private float lastPay;  
    @Column(comment = "今日支付")  
    private double dailyPay;  
    @Column(comment = "历史支付", extra = {"30", "2"})  
    private BigDecimal hisPay = BigDecimal.ZERO;  
    @Column(comment = "是否在生")  
    private boolean alive;  
    @Column(comment = "国籍")  
    private Country country = Country.CHINA;  
    //getter setter  
}  
```

```code
//结合父类的使用，以游戏中玩家与名下的英雄为例的数据组织方式
@MappedSuperclass(sort = Priority._2)
@SuperIndex(indexs = {
	@Index(fields = { "playerId" }) })
public abstract class PlayerOneToMany extends AbstractEntity {  
	@Id(strategy = Strategy.AUTO)
	@Column(comment = "唯一ID（序号）")
	protected long idx;
	@Column(comment = "玩家ID", readonly = true)
	protected long playerId;
}

@Table(comment = "英雄表", dbAlias = "data")
public class Hero extends PlayerOneToMany {
	@Column(comment = "英雄标识", readonly = true)
	private int identity;
	@Column(comment = "等级")
	private int level;
	//other fields
}
```





/**
 * 
 */
package org.banish;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.banish.sql.core.annotation.Column;
import org.banish.sql.core.annotation.Id;
import org.banish.sql.core.annotation.Id.Strategy;
import org.banish.sql.core.annotation.Index;
import org.banish.sql.core.annotation.Table;
import org.banish.sql.core.annotation.enuma.IndexType;
import org.banish.sql.core.entity.AbstractEntity;

/**
 * @author YY
 */
@Table(comment = "例子表", dbAlias = "data", indexs = {
		@Index(fields = { "provinceCode", "cityCode" }),
		@Index(fields = { "cardId" }, type = IndexType.UNIQUE) })
public class ExampleEntity extends AbstractEntity {
	@Id(strategy = Strategy.AUTO)
	@Column(comment = "唯一ID")
	private int id;
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
	@Column(comment = "国际")
	private Country country = Country.CHINA;
	
	
	public static ExampleEntity createExample() {
		ExampleEntity exampleEntity = new ExampleEntity();
		exampleEntity.setCardId(UUID.randomUUID().toString());
		exampleEntity.setName("Tom");
		exampleEntity.setSex((byte)1);
		exampleEntity.setAge((short)35);
		exampleEntity.setProvinceCode(100001);
		exampleEntity.setCityCode(100001);
		exampleEntity.setMoney(98765432123456789L);
		exampleEntity.setAddress("some where");
		exampleEntity.setBornTime(System.currentTimeMillis());
		exampleEntity.setDescribe("secret");
		exampleEntity.getHobbise().add("game");
		exampleEntity.getHobbise().add("swim");
		
		EducationInfo edu1 = new EducationInfo();
		edu1.setSchoolName("school1");
		edu1.setYear(2000);
		exampleEntity.getEducationInfos().add(edu1);
		EducationInfo edu2 = new EducationInfo();
		edu2.setSchoolName("school2");
		edu2.setYear(2010);
		exampleEntity.getEducationInfos().add(edu2);
		
		BankCard bankCard1 = new BankCard();
		bankCard1.setCardCode("A1");
		bankCard1.setBankName("Bank 1");
		bankCard1.setExpireTime(System.currentTimeMillis());
		exampleEntity.getBankCards().put(bankCard1.getCardCode(), bankCard1);
		BankCard bankCard2 = new BankCard();
		bankCard2.setCardCode("A2");
		bankCard2.setBankName("Bank 2");
		bankCard2.setExpireTime(System.currentTimeMillis());
		exampleEntity.getBankCards().put(bankCard2.getCardCode(), bankCard2);
		
		exampleEntity.setLastPay(12.12f);
		exampleEntity.setDailyPay(100.100d);
		exampleEntity.setHisPay(new BigDecimal("78945.4785125"));
		exampleEntity.setAlive(true);
		exampleEntity.setCountry(Country.CHINA);
		return exampleEntity;
	}
	
	public static class EducationInfo {
		private String schoolName;
		private int year;
		public String getSchoolName() {
			return schoolName;
		}
		public void setSchoolName(String schoolName) {
			this.schoolName = schoolName;
		}
		public int getYear() {
			return year;
		}
		public void setYear(int year) {
			this.year = year;
		}
	}
	
	public static class BankCard {
		private String cardCode;
		private long expireTime;
		private String bankName;
		public String getCardCode() {
			return cardCode;
		}
		public void setCardCode(String cardCode) {
			this.cardCode = cardCode;
		}
		public long getExpireTime() {
			return expireTime;
		}
		public void setExpireTime(long expireTime) {
			this.expireTime = expireTime;
		}
		public String getBankName() {
			return bankName;
		}
		public void setBankName(String bankName) {
			this.bankName = bankName;
		}
	}
	
	public static enum Country {
		CHINA,
		USA,
		ENGLAND,
	}

//	public long getId() {
//		return id;
//	}
//
//	public void setId(long id) {
//		this.id = id;
//	}

	public String getCardId() {
		return cardId;
	}

	public void setCardId(String cardId) {
		this.cardId = cardId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public byte getSex() {
		return sex;
	}

	public void setSex(byte sex) {
		this.sex = sex;
	}

	public short getAge() {
		return age;
	}

	public void setAge(short age) {
		this.age = age;
	}

	public int getProvinceCode() {
		return provinceCode;
	}

	public void setProvinceCode(int provinceCode) {
		this.provinceCode = provinceCode;
	}

	public int getCityCode() {
		return cityCode;
	}

	public void setCityCode(int cityCode) {
		this.cityCode = cityCode;
	}

	public long getMoney() {
		return money;
	}

	public void setMoney(long money) {
		this.money = money;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public long getBornTime() {
		return bornTime;
	}

	public void setBornTime(long bornTime) {
		this.bornTime = bornTime;
	}

	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

	public List<String> getHobbise() {
		return hobbise;
	}

	public void setHobbise(List<String> hobbise) {
		this.hobbise = hobbise;
	}

	public List<EducationInfo> getEducationInfos() {
		return educationInfos;
	}

	public void setEducationInfos(List<EducationInfo> educationInfos) {
		this.educationInfos = educationInfos;
	}

	public Map<String, BankCard> getBankCards() {
		return bankCards;
	}

	public void setBankCards(Map<String, BankCard> bankCards) {
		this.bankCards = bankCards;
	}

	public float getLastPay() {
		return lastPay;
	}

	public void setLastPay(float lastPay) {
		this.lastPay = lastPay;
	}

	public double getDailyPay() {
		return dailyPay;
	}

	public void setDailyPay(double dailyPay) {
		this.dailyPay = dailyPay;
	}

	public BigDecimal getHisPay() {
		return hisPay;
	}

	public void setHisPay(BigDecimal hisPay) {
		this.hisPay = hisPay;
	}

	public boolean isAlive() {
		return alive;
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
	}

	public Country getCountry() {
		return country;
	}

	public void setCountry(Country country) {
		this.country = country;
	}

//	@Override
//	public long idGenerator() {
//		return 779621000000001L;
//	}
}

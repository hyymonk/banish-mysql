/**
 * 
 */
package org.banish.mysql.example;

import java.math.BigDecimal;
import java.util.UUID;

import org.banish.mysql.Daos;
import org.banish.mysql.Daos.DaosBooter;
import org.banish.mysql.example.ExampleEntity.BankCard;
import org.banish.mysql.example.ExampleEntity.Country;
import org.banish.mysql.example.ExampleEntity.EducationInfo;

/**
 * @author YY
 */
public class ExampleTesting {
	
	public static void main(String[] args) {
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
		
		
		DBConfig dbConfig = new DBConfig();
		dbConfig.setDbName("data_test");
		dbConfig.setAlias("data");
		dbConfig.setId(1);
		
		DataSource dataSource = new DataSource(dbConfig);
		
		DaosBooter daosBuilder = new DaosBooter(1);
		daosBuilder.addDataSource(dataSource);
		daosBuilder.addEntityClass(ExampleEntity.class);
		daosBuilder.addValueFormater(new JsonFormatter());
		daosBuilder.setup();
		
		Daos.get(1, ExampleEntity.class).insert(exampleEntity);
		
		ExampleEntity queryResult = Daos.get(1, ExampleEntity.class).queryByPrimaryKey(1000000001);
		System.out.println(JsonFormatter.gson.toJson(queryResult));
	}
}

/**
 * 
 */
package org.banish.postgresql.example;

import java.util.ArrayList;
import java.util.List;

import org.banish.DBConfigTesting;
import org.banish.ExampleEntity;
import org.banish.JsonFormatterTesting;
import org.banish.sql.DaosBooter;
import org.banish.sql.DaosBooter.Daos;

/**
 * @author YY
 */
public class PostgreSqlExampleTesting {
	
	public static void main(String[] args) {
		ExampleEntity exampleEntity = ExampleEntity.createExample();
		
		DBConfigTesting dbConfig = new DBConfigTesting();
		dbConfig.setDbName("data_test");
		dbConfig.setAlias("data");
		dbConfig.setId(1);
		dbConfig.setUser("postgres");
		dbConfig.setPassword("123456");
		dbConfig.setIpPort("127.0.0.1:5432");
		
		PostgreSqlDataSourceTesting dataSource = new PostgreSqlDataSourceTesting(dbConfig);
		
		DaosBooter daosBuilder = new DaosBooter(1);
		daosBuilder.addDataSource(dataSource);
		daosBuilder.addEntityClass(ExampleEntity.class);
		daosBuilder.addValueFormater(new JsonFormatterTesting());
		daosBuilder.setup();
		
		Daos.get(1, ExampleEntity.class).insert(exampleEntity);
		System.out.println("ExampleEntity1 " + JsonFormatterTesting.gson.toJson(exampleEntity));
		exampleEntity.setName("Peter1");
		
		ExampleEntity queryResult = Daos.get(1, ExampleEntity.class).queryByPrimaryKey(1000000002);
		queryResult.setName("Tome1");
		
		List<ExampleEntity> insertUpdateList = new ArrayList<>();
		insertUpdateList.add(exampleEntity);
		insertUpdateList.add(queryResult);
		Daos.get(1, ExampleEntity.class).insertUpdate(insertUpdateList);
		
		System.out.println("ExampleEntity2 " + JsonFormatterTesting.gson.toJson(queryResult));
	}
}

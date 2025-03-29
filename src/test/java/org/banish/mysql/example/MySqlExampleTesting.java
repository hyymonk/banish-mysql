/**
 * 
 */
package org.banish.mysql.example;

import org.banish.DBConfigTesting;
import org.banish.ExampleEntity;
import org.banish.JsonFormatterTesting;
import org.banish.sql.DaosBooter;
import org.banish.sql.DaosBooter.Daos;

/**
 * @author YY
 */
public class MySqlExampleTesting {
	
	public static void main(String[] args) {
		ExampleEntity exampleEntity = ExampleEntity.createExample();
		
		DBConfigTesting dbConfig = new DBConfigTesting();
		dbConfig.setDbName("data_test");
		dbConfig.setAlias("data");
		dbConfig.setId(1);
		
		MySqlDataSourceTesting dataSource = new MySqlDataSourceTesting(dbConfig);
		
		DaosBooter daosBuilder = new DaosBooter(1);
		daosBuilder.addDataSource(dataSource);
		daosBuilder.addEntityClass(ExampleEntity.class);
		daosBuilder.addValueFormater(new JsonFormatterTesting());
		daosBuilder.setup();
		
		Daos.get(1, ExampleEntity.class).insert(exampleEntity);
		
		ExampleEntity queryResult = Daos.get(1, ExampleEntity.class).queryByPrimaryKey(1000000001);
		System.out.println(JsonFormatterTesting.gson.toJson(queryResult));
	}
}

/**
 * 
 */
package org.banish.mysql.example;

import org.banish.DBConfig;
import org.banish.ExampleEntity;
import org.banish.JsonFormatter;
import org.banish.sql.DaosBooter;
import org.banish.sql.DaosBooter.Daos;

/**
 * @author YY
 */
public class MySqlExampleTesting {
	
	public static void main(String[] args) {
		ExampleEntity exampleEntity = ExampleEntity.createExample();
		
		DBConfig dbConfig = new DBConfig();
		dbConfig.setDbName("data_test");
		dbConfig.setAlias("data");
		dbConfig.setId(1);
		
		MySqlDataSource dataSource = new MySqlDataSource(dbConfig);
		
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

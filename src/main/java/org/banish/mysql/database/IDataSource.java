/**
 * 
 */
package org.banish.mysql.database;

import java.sql.Connection;

/**
 * 
 */
public interface IDataSource {

	int getZoneId();
	
	Connection getConnection();
	
	String getDbName();
	
	String getAlias();
}

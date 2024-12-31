/**
 * 
 */
package org.banish.mysql.database;

import java.sql.Connection;
import java.sql.SQLException;

import org.banish.base.IMetaFactory;

/**
 * 
 */
public interface IDataSource {
	/**
	 * 数据源的分区号
	 * @return
	 */
	int getZoneId();
	/**
	 * 通过数据源获取连接
	 * @return
	 */
	Connection getConnection() throws SQLException;
	/**
	 * 数据库的名字
	 * @return
	 */
	String getDbName();
	/**
	 * 数据库的别名
	 * @return
	 */
	String getAlias();
	
	IMetaFactory getMetaFactory();
}

/**
 * 
 */
package org.banish.sql.core.datasource;

import java.sql.Connection;
import java.sql.SQLException;

import org.banish.sql.core.orm.IOrmFactory;

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
	/**
	 * 元数据工厂
	 * @return
	 */
	IOrmFactory getMetaFactory();
	/**
	 * 关闭数据源
	 */
	void close();
	/**
	 * 数据表是否存在
	 * @param tableName
	 * @return
	 */
	boolean isTableExist(String tableName);
}

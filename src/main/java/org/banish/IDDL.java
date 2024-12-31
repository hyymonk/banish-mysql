/**
 * 
 */
package org.banish;

import java.util.List;

import org.banish.mysql.database.IDataSource;
import org.banish.mysql.orm.IndexMeta;

/**
 * @author YY
 */
public interface IDDL {

	boolean isTableExist(IDataSource dataSource, String tableName);

	List<? extends IIndexStruct> getKeys(IDataSource dataSource, String tableName);
	
	interface IIndexStruct {
		String getName();
		String getUnique();
		String getPrimary();
		String getWay();
		String getColumnName();
	}
	
	List<? extends ITableDes> getTableColumns(IDataSource dataSource, String tableName);

	interface ITableDes {
		String getField();
		String getType();
		String getExtra();
	}
	
	String getTableDropColumn(String tableName, String columnName);
	
	String getTableAddColumn(String tableName, String columnDefine);
	
	String getTableChangeColumn(String tableName, String columnName, String columnDefine);
	/**
	 * 添加索引
	 * @param tableName
	 * @param indexMeta
	 * @return
	 */
	String getTableAddIndex(String tableName, IndexMeta indexMeta);
	/**
	 * 修改索引
	 * @param tableName
	 * @param indexMeta
	 * @return
	 */
	String getTableChangeIndex(String tableName, IndexMeta indexMeta);
	/**
	 * 获取表中的最大ID
	 * @param dataSource
	 * @param tableName
	 * @param primaryKeyName
	 * @return
	 */
	long getTableMaxId(IDataSource dataSource, String tableName, String primaryKeyName);
	/**
	 * 获取表的自增序列当前值
	 * @param dataSource
	 * @param dbName
	 * @param tableName
	 * @return
	 */
	long getTableAutoinc(IDataSource dataSource, String dbName, String tableName);
	/**
	 * 设置表的自增序列值
	 * @param tableName
	 * @param autoinc
	 * @return
	 */
	String setAutoIncrement(String tableName, long autoinc);
}

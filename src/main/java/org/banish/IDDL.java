/**
 * 
 */
package org.banish;

import java.util.List;

import org.banish.mysql.database.IDataSource;
import org.banish.mysql.orm.EntityMeta;
import org.banish.mysql.orm.IndexMeta;
import org.banish.mysql.orm.column.ColumnMeta;

/**
 * @author YY
 */
public interface IDDL {

	List<String> getDDLs();
	void addDDL(String ddl, String remark);
	void addDDLs(List<String> ddls, String remark);
	
	class DDLSql {
		public String sql;
		public String remark;
	}
	
	String createTableSql(String tableName, EntityMeta<?> entityMeta);
	
	String getColumnDefine(ColumnMeta columnMeta);
	
	boolean isTableExist(String tableName);

	List<? extends IIndexStruct> getKeys(String tableName);
	
	interface IIndexStruct {
		String getName();
		boolean isUnique();
		boolean isPrimary();
		String getWay();
		String getColumnName();
	}
	
	List<? extends ITableDes> getTableColumns(String tableName);

	interface ITableDes {
		String getField();
		String getType();
		String getExtra();
	}
	/**
	 * 删除表中的指定列
	 * @param tableName
	 * @param columnName
	 * @return
	 */
	String getTableDropColumn(String tableName, String columnName);
	/**
	 * 添加新列到表中
	 * @param tableName
	 * @param columnDefine
	 * @return
	 */
	String getTableAddColumn(String tableName, String columnDefine);
	/**
	 * 修改表中原有列的定义
	 * @param tableName
	 * @param columnName
	 * @param columnDefine
	 * @return
	 */
	String getTableModifyColumn(String tableName, String columnName, String columnDefine);
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
	String getTableModifyIndex(String tableName, IndexMeta indexMeta);
	/**
	 * 获取表中的最大ID
	 * @param dataSource
	 * @param tableName
	 * @param primaryKeyName
	 * @return
	 */
	long getTableMaxId(String tableName, String primaryKeyName);
	/**
	 * 获取表的自增序列当前值
	 * @param dataSource
	 * @param dbName
	 * @param tableName
	 * @return
	 */
	long getTableAutoinc(String tableName);
	/**
	 * 设置表的自增序列值
	 * @param tableName
	 * @param autoinc
	 * @return
	 */
	String setAutoIncrement(String tableName, long autoinc);
}

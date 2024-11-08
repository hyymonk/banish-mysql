/**
 * 
 */
package org.banish.mysql.table.ddl;

import java.util.List;

import org.banish.mysql.annotation.Column;
import org.banish.mysql.dao.Dao;
import org.banish.mysql.database.IDataSource;

/**
 * @author YY
 *
 */
public class DDL {
	
	
	/**
	 * 查询是否存在表
	 */
	private final static String SELECT_TABLE_NAME = "SELECT `TABLE_NAME` FROM `INFORMATION_SCHEMA`.`TABLES` WHERE `TABLE_SCHEMA`=? AND `TABLE_NAME`=?;";
	
	public static boolean isTableExist(IDataSource dataSource, String dbName, String tableName) {
		TableExist tableExist = Dao.queryAliasObject(dataSource, TableExist.class, SELECT_TABLE_NAME, dbName, tableName);
		return tableExist != null;
	} 
	
	private static class TableExist {
		@Column(name = "TABLE_NAME", comment = "表名")
		private String tableName;
	}
	
	/**
	 * 查询按时间的分表
	 */
	public final static String SELECT_SUBMETER_TABLE_NAME = "SELECT `TABLE_NAME` FROM `INFORMATION_SCHEMA`.`TABLES` WHERE `TABLE_SCHEMA`=? AND `TABLE_NAME` LIKE ?;";

	/**
	 * 查看表索引
	 */
	private final static String SHOW_KEYS = "SHOW KEYS FROM `%s`";
	
	public static List<IndexStruct> getKeys(IDataSource dataSource, String tableName) {
		String sql = String.format(SHOW_KEYS, tableName);
		return Dao.queryAliasObjects(dataSource, IndexStruct.class, sql);
	}
	
	public static class IndexStruct {
		@Column(name = "Key_name", comment = "索引名")
		private String name;
		@Column(name = "Non_unique", comment = "是否唯一索引")
		private int unique;
		@Column(name = "Index_type", comment = "索引类型")
		private String way;
		@Column(name = "Column_name", comment = "索引使用的列名")
		private String columnName;
		public String getName() {
			return name;
		}
		public int getUnique() {
			return unique;
		}
		public String getWay() {
			return way;
		}
		public String getColumnName() {
			return columnName;
		}
	}

	/**
	 * 查看表结构
	 */
	private final static String TABLE_DES = "DESCRIBE `%s`";
	
	public static List<TableDes> getTableColumns(IDataSource dataSource, String tableName) {
		String sql = String.format(TABLE_DES, tableName);
		return Dao.queryAliasObjects(dataSource, TableDes.class, sql);
	}
	
	public static class TableDes {
		@Column(name = "Field", comment = "字段名")
		private String field;
		@Column(name = "Type", comment = "字段类型")
		private String type;
		@Column(name = "Extra", comment = "扩展信息")
		private String extra;
		public String getField() {
			return field;
		}
		public String getType() {
			return type;
		}
		public String getExtra() {
			return extra;
		}
	}

	/**
	 * 删除列
	 */
	public final static String TABLE_DROP_COLUMN = "ALTER TABLE `#tableName#` DROP COLUMN `#columnName#`;";

	/**
	 * 增加列
	 */
	public final static String TABLE_ADD_COLUMN = "ALTER TABLE `#tableName#` ADD COLUMN #columnDefine#;";

	/**
	 * 修改列属性
	 */
	public final static String TABLE_CHANGE_COLUMN = "ALTER TABLE `#tableName#` CHANGE COLUMN `#columnName#` #columnDefine#;";
	
	/**
	 * 增加索引
	 */
	public final static String TABLE_ADD_INDEX = "ALTER TABLE `#tableName#` ADD #indexType# INDEX `#indexName#` (#columnName#) USING #indexWay#;";
	
	/**
	 * 修改索引
	 */
	public final static String TABLE_MODIFY_INDEX = "ALTER TABLE `#tableName#` DROP INDEX `#oriIndex#`, ADD #indexType# INDEX `#indexName#` (#columnName#) USING #indexWay#;";

	/**
	 * 查询最大ID
	 */
	private final static String SELECT_MAX_ID = "SELECT max(`%s`) as maxid FROM `%s` LIMIT 1;";
	
	public static long getTableMaxId(IDataSource dataSource, String tableName, String primaryKeyName) {
		String sql = String.format(SELECT_MAX_ID, primaryKeyName, tableName);
		TableMaxId tableMaxId = Dao.queryAliasObject(dataSource, TableMaxId.class, sql);
		return tableMaxId.maxId;
	} 
	
	private static class TableMaxId {
		@Column(name = "maxid", comment = "表的最大ID")
		private long maxId;
	}
	
	
	/**
	 * 设置自增ID
	 */
	public final static String SET_AUTO_INCREMENT = "ALTER TABLE `#tableName#` AUTO_INCREMENT=?;";

	/**
	 * mysql5.x用这种方式来查自增主键
	 */
	private final static String TABLE_AUTOINC_5 = "SHOW TABLE STATUS WHERE NAME=?;";
	
	public static long getTableAutoinc5(IDataSource dataSource, String tableName) {
		TableStatus tableStatus = Dao.queryAliasObject(dataSource, TableStatus.class, TABLE_AUTOINC_5, tableName);
		return tableStatus.autoIncrement;
	}
	
	private static class TableStatus {
		@Column(name = "Auto_increment", comment = "自动增长ID")
		private long autoIncrement;
	}
	/**
	 * mysql8.x用这种方式来查自增主键
	 */
	private final static String TABLE_AUTOINC_8 = "SELECT `AUTOINC` AS Auto_increment FROM `INFORMATION_SCHEMA`.`INNODB_TABLESTATS` WHERE `NAME`=?;";
	
	public static long getTableAutoinc8(IDataSource dataSource, String tableName) {
		TableStatus tableStatus = Dao.queryAliasObject(dataSource, TableStatus.class, TABLE_AUTOINC_8, tableName);
		return tableStatus.autoIncrement;
	}
	
	
	/**
	 * 查mysql版本号
	 */
	private final static String MYSQL_VERSION = "SELECT VERSION() AS version;";
	
	public static String getMysqlVersion(IDataSource dataSource) {
		MysqlVersion mysqlVersion = Dao.queryAliasObject(dataSource, MysqlVersion.class, MYSQL_VERSION);
		return mysqlVersion.version;
	}
	
	private static class MysqlVersion {
		@Column(comment = "版本号")
		private String version;
	}
}

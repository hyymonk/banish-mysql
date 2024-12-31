/**
 * 
 */
package org.banish.sql.mysql.table.ddl;

import java.util.ArrayList;
import java.util.List;

import org.banish.sql.core.annotation.Column;
import org.banish.sql.core.annotation.Id.Strategy;
import org.banish.sql.core.datasource.IDataSource;
import org.banish.sql.core.orm.ColumnMeta;
import org.banish.sql.core.orm.EntityMeta;
import org.banish.sql.core.orm.IPrimaryKeyColumnMeta;
import org.banish.sql.core.orm.IndexMeta;
import org.banish.sql.core.sql.IDDL;
import org.banish.sql.mysql.dao.Dao;
import org.banish.sql.mysql.orm.column.MPrimaryKeyColumnMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author YY
 *
 */
public class MySqlDDL implements IDDL {
	
	private static Logger logger = LoggerFactory.getLogger(MySqlDDL.class);
	
	private IDataSource dataSource;
	private boolean autoBuild;
	private List<String> ddlSqls = new ArrayList<>();
	
	public MySqlDDL(IDataSource dataSource, boolean autoBuild) {
		this.dataSource = dataSource;
		this.autoBuild = autoBuild;
	}
	
	public void addDDL(String ddl, String remark) {
		ddlSqls.add(ddl);
		if(autoBuild) {
			Dao.executeSql(dataSource, ddl);
		}
		logger.info(remark + ":" + ddl);
	}
	public void addDDLs(List<String> ddls, String remark) {
		for(String ddl : ddls) {
			addDDL(ddl, remark);
		}
	}
	public List<String> getDDLs() {
		return ddlSqls;
	}
	
	/**
	 * 查询是否存在表
	 */
	private final String SELECT_TABLE_NAME = "SELECT `TABLE_NAME` FROM `INFORMATION_SCHEMA`.`TABLES` WHERE `TABLE_SCHEMA`=? AND `TABLE_NAME`=?;";
	
	@Override
	public boolean isTableExist(String tableName) {
		TableExist tableExist = Dao.queryAliasObject(dataSource, TableExist.class, SELECT_TABLE_NAME, dataSource.getDbName(), tableName);
		return tableExist != null;
	} 
	
	public static class TableExist {
		@Column(name = "TABLE_NAME", comment = "表名")
		private String tableName;
	}
	
	/**
	 * 查询按时间的分表，历史的表就不用去动了
	 */
	public final String SELECT_SUBMETER_TABLE_NAME = "SELECT `TABLE_NAME` FROM `INFORMATION_SCHEMA`.`TABLES` WHERE `TABLE_SCHEMA`=? AND `TABLE_NAME` LIKE ?;";

	/**
	 * 查看表索引
	 */
	private final String SHOW_KEYS = "SHOW KEYS FROM `%s`";
	
	@Override
	public List<IndexStruct> getKeys(String tableName) {
		String sql = String.format(SHOW_KEYS, tableName);
		return Dao.queryAliasObjects(dataSource, IndexStruct.class, sql);
	}
	
	public static class IndexStruct implements IIndexStruct {
		@Column(name = "Key_name", comment = "索引名")
		private String name;
		@Column(name = "Non_unique", comment = "是否唯一索引")
		private int nonUnique;
		@Column(name = "Index_type", comment = "索引类型")
		private String way;
		@Column(name = "Column_name", comment = "索引使用的列名")
		private String columnName;
		public String getName() {
			return name;
		}
		public String getWay() {
			return way;
		}
		public String getColumnName() {
			return columnName;
		}
		public int getNonUnique() {
			return nonUnique;
		}
		@Override
		public boolean isUnique() {
			return nonUnique == 0;
		}
		@Override
		public boolean isPrimary() {
			return name.equals("PRIMARY");
		}
	}

	/**
	 * 查看表结构
	 */
	private final String TABLE_DES = "DESCRIBE `%s`";
	
	@Override
	public List<TableDes> getTableColumns(String tableName) {
		String sql = String.format(TABLE_DES, tableName);
		return Dao.queryAliasObjects(dataSource, TableDes.class, sql);
	}
	
	public static class TableDes implements ITableDes {
		@Column(name = "Field", comment = "字段名")
		private String field;
		@Column(name = "Type", comment = "字段类型")
		private String type;
		//主键是否自动递增会在这里体现
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
	private final String TABLE_DROP_COLUMN = "ALTER TABLE `%s` DROP COLUMN `%s`;";

	@Override
	public String getTableDropColumn(String tableName, String columnName) {
		return String.format(TABLE_DROP_COLUMN, tableName, columnName);
	}
	
	/**
	 * 增加列
	 */
	private final String TABLE_ADD_COLUMN = "ALTER TABLE `%s` ADD COLUMN %s;";

	@Override
	public String getTableAddColumn(String tableName, String columnDefine) {
		return String.format(TABLE_ADD_COLUMN, tableName, columnDefine);
	}
	
	/**
	 * 修改列属性
	 */
	private final String TABLE_MODIFY_COLUMN = "ALTER TABLE `%s` CHANGE COLUMN `%s` %s;";
	
	@Override
	public String getTableModifyColumn(String tableName, String columnName, String columnDefine) {
		return String.format(TABLE_MODIFY_COLUMN, tableName, columnName, columnDefine);
	}
	
	/**
	 * 增加索引
	 */
	private final String TABLE_ADD_INDEX = "ALTER TABLE `%s` ADD %s INDEX `%s` (%s) USING %s;";
	
	@Override
	public String getTableAddIndex(String tableName, IndexMeta indexMeta) {
		return String.format(TABLE_ADD_INDEX, tableName, indexMeta.getType().value(), indexMeta.getName(),
				indexMeta.getColumnsString(), indexMeta.getWay().value());
	}
	
	/**
	 * 修改索引
	 */
	private final String TABLE_MODIFY_INDEX = "ALTER TABLE `%s` DROP INDEX `%s`, ADD %s INDEX `%s` (%s) USING %s;";

	@Override
	public String getTableModifyIndex(String tableName, IndexMeta indexMeta) {
		return String.format(TABLE_MODIFY_INDEX, tableName, indexMeta.getName(), indexMeta.getType().value(), indexMeta.getName(),
				indexMeta.getColumnsString(), indexMeta.getWay().value());
	}
	
	/**
	 * 查询最大ID
	 */
	private final String SELECT_MAX_ID = "SELECT max(`%s`) as max_id FROM `%s` LIMIT 1;";
	
	@Override
	public long getTableMaxId(String tableName, String primaryKeyName) {
		String sql = String.format(SELECT_MAX_ID, primaryKeyName, tableName);
		TableMaxId tableMaxId = Dao.queryAliasObject(dataSource, TableMaxId.class, sql);
		return tableMaxId.maxId;
	} 
	
	public static class TableMaxId {
		@Column(comment = "表的最大ID")
		private long maxId;
	}
	
	
	/**
	 * 设置自增ID
	 */
	private final String SET_AUTO_INCREMENT = "ALTER TABLE `%s` AUTO_INCREMENT=%s;";

	@Override
	public String setAutoIncrement(String tableName, long autoinc) {
		return String.format(SET_AUTO_INCREMENT, tableName, autoinc);
	}
	
	/**
	 * mysql5.x用这种方式来查自增主键
	 */
	private final String TABLE_AUTOINC_5 = "SELECT `AUTO_INCREMENT` AS auto_increment FROM `INFORMATION_SCHEMA`.`TABLES` WHERE TABLE_SCHEMA=? AND TABLE_NAME=?;";
	
	private long getTableAutoinc5(String dbName, String tableName) {
		TableStatus tableStatus = Dao.queryAliasObject(dataSource, TableStatus.class, TABLE_AUTOINC_5, dbName, tableName);
		return tableStatus.autoIncrement;
	}
	
	public static class TableStatus {
		@Column(comment = "自动增长ID")
		private long autoIncrement;
	}
	/**
	 * mysql8.x用这种方式来查自增主键
	 */
	private final String TABLE_AUTOINC_8 = "SELECT `AUTOINC` AS auto_increment FROM `INFORMATION_SCHEMA`.`INNODB_TABLESTATS` WHERE `NAME`=?;";
	
	private long getTableAutoinc8(String dbName, String tableName) {
		String tableFullName = dbName + "/" + tableName;
		TableStatus tableStatus = Dao.queryAliasObject(dataSource, TableStatus.class, TABLE_AUTOINC_8, tableFullName);
		return tableStatus.autoIncrement;
	}
	
	
	/**
	 * 查mysql版本号
	 */
	private final String MYSQL_VERSION = "SELECT VERSION() AS version;";
	
	private String getMysqlVersion() {
		MysqlVersion mysqlVersion = Dao.queryAliasObject(dataSource, MysqlVersion.class, MYSQL_VERSION);
		return mysqlVersion.version;
	}
	
	public static class MysqlVersion {
		@Column(comment = "版本号")
		private String version;
	}

	@Override
	public long getTableAutoinc(String tableName) {
		String mysqlVersion = getMysqlVersion();
		long currAutoId = 0;
		if(mysqlVersion.startsWith("8")) {
			currAutoId = getTableAutoinc8(this.dataSource.getDbName(), tableName);
		} else {
			currAutoId = getTableAutoinc5(this.dataSource.getDbName(), tableName);
		}
		return currAutoId;
	}
	
	@Override
	public String createTableSql(String tableName, EntityMeta<?> entityMeta) {
		StringBuilder result = new StringBuilder();
		result.append(String.format("CREATE TABLE `%s` (", tableName));
		
		for(ColumnMeta columnMeta : entityMeta.getColumnList()) {
			result.append(getColumnDefine(columnMeta)).append(",");
		}
		
		IPrimaryKeyColumnMeta primaryKeyMeta = entityMeta.getPrimaryKeyMeta();
		//创建表的时候只进行了主键的定义，索引的设置会在表构建好之后进行处理
		result.append(String.format("PRIMARY KEY (`%s`)", primaryKeyMeta.getColumnName()));
		//这里并没有对自增ID进行初始处理，自增ID的设置会在表构建好之后进行处理
		result.append(") ENGINE=InnoDB DEFAULT CHARSET=").append(entityMeta.getTableCharset().value());
		result.append(" COMMENT='").append(entityMeta.getTableComment()).append("';");
		return result.toString();
	}
	
	@Override
	public String getColumnDefine(ColumnMeta columnMeta) {
		StringBuilder result = new StringBuilder();
		result.append(String.format("`%s` %s", columnMeta.getColumnName(), columnMeta.dbColumnType()));
		String autoIncrement = "";
		if(columnMeta instanceof MPrimaryKeyColumnMeta) {
			MPrimaryKeyColumnMeta keyMeta = (MPrimaryKeyColumnMeta)columnMeta;
			if(keyMeta.getStrategy() == Strategy.AUTO) {
				autoIncrement = "AUTO_INCREMENT";
			}
		}
		result.append(" ").append(columnMeta.defaultValue()).append(" ").append(autoIncrement);
		// 字段备注
		result.append(" COMMENT '").append(columnMeta.getComment()).append("'");
		return result.toString();
	}
}

/**
 * 
 */
package org.banish.postgresql.table;

import java.util.List;

import org.banish.IDDL;
import org.banish.mysql.annotation.Column;
import org.banish.mysql.dao.Dao;
import org.banish.mysql.database.IDataSource;
import org.banish.mysql.orm.IndexMeta;

/**
 * @author YY
 */
public class PostgreSqlDDL implements IDDL {
	
	private final String SELECT_TABLE_NAME = "SELECT \"table_name\" FROM \"information_schema\".\"tables\" WHERE \"table_schema\"=? AND \"table_name\"=?;";

	@Override
	public boolean isTableExist(IDataSource dataSource, String tableName) {
		TableExist tableExist = Dao.queryAliasObject(dataSource, TableExist.class, SELECT_TABLE_NAME, dataSource.getDbName(), tableName);
		return tableExist != null;
	}

	public class TableExist {
		@Column(name = "table_name", comment = "表名")
		private String tableName;
	}
	
	/**
	 * 查看表索引
	 */
	private final String SHOW_KEYS = ""
			+ "SELECT"
			+ "	idx.indexrelid,"
			+ "	idx.indrelid,"
			+ "	idx.indisunique,"
			+ "	idx.indisprimary,"
			+ "	clz.oid,"
			+ "	clz.relname,"
			+ "	atr.attrelid,"
			+ "	atr.attname,"
			+ "	am.amname "
			+ "FROM"
			+ "	pg_index idx"
			+ "	JOIN pg_class clz ON idx.indexrelid = clz.oid"
			+ "	JOIN pg_attribute atr ON atr.attrelid = idx.indexrelid"
			+ "	JOIN pg_am am ON clz.relam = am.oid "
			+ "WHERE"
			+ "	indrelid = ( SELECT oid FROM pg_class WHERE relname = ? )";
	
	@Override
	public List<? extends IIndexStruct> getKeys(IDataSource dataSource, String tableName) {
		String sql = String.format(SHOW_KEYS, tableName);
		return Dao.queryAliasObjects(dataSource, IndexStruct.class, sql);
	}
	
	public class IndexStruct implements IIndexStruct {
		@Column(name = "relname", comment = "索引名")
		private String name;
		@Column(name = "indisunique", comment = "是否唯一索引")
		private String unique;
		@Column(name = "indisprimary", comment = "是否主键")
		private String primary;
		@Column(name = "amname", comment = "索引类型")
		private String way;
		@Column(name = "attname", comment = "索引使用的列名")
		private String columnName;
		public String getName() {
			return name;
		}
		public String getUnique() {
			return unique;
		}
		public String getPrimary() {
			return primary;
		}
		public String getWay() {
			return way;
		}
		public String getColumnName() {
			return columnName;
		}
	}
	
	private final String TABLE_DES = "SELECT \"column_name\",\"data_type\",\"column_default\" FROM \"information_schema\".\"columns\" WHERE \"table_name\" = '%s';";
	
	@Override
	public List<? extends ITableDes> getTableColumns(IDataSource dataSource, String tableName) {
		String sql = String.format(TABLE_DES, tableName);
		return Dao.queryAliasObjects(dataSource, TableDes.class, sql);
	}
	
	public class TableDes implements ITableDes {
		@Column(name = "column_name", comment = "字段名")
		private String field;
		@Column(name = "data_type", comment = "字段类型")
		private String type;
		@Column(name = "column_default", comment = "扩展信息")
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
	private final String TABLE_DROP_COLUMN = "ALTER TABLE \"%s\" DROP COLUMN \"%s\";";

	@Override
	public String getTableDropColumn(String tableName, String columnName) {
		return String.format(TABLE_DROP_COLUMN, tableName, columnName);
	}
	
	/**
	 * 增加列
	 */
	private final String TABLE_ADD_COLUMN = "ALTER TABLE \"%s\" ADD COLUMN %s;";

	@Override
	public String getTableAddColumn(String tableName, String columnDefine) {
		return String.format(TABLE_ADD_COLUMN, tableName, columnDefine);
	}
	
//	ALTER TABLE "public"."student" 
//	  ALTER COLUMN "habby" TYPE varchar(500) COLLATE "pg_catalog"."default" USING "habby"::varchar(500);
	
	
	/**
	 * 修改列属性
	 */
	private final String TABLE_CHANGE_COLUMN = "ALTER TABLE \"%s\" ALTER COLUMN \"%s\" TYPE %s;";
	
	public String getTableChangeColumn(String tableName, String columnName, String columnDefine) {
		return String.format(TABLE_CHANGE_COLUMN, tableName, columnName, columnDefine);
	}
	
//	CREATE UNIQUE INDEX "idx_grade_clazz" ON "public"."student" USING btree (
//			  "grade",
//			  "clazz"
//			);
	
	/**
	 * 增加索引
	 */
	private final String TABLE_ADD_INDEX = "CREATE \"%s\" INDEX \"%s\" ON \"%s\" USING %s (%s);";
	
	public String getTableAddIndex(String tableName, IndexMeta indexMeta) {
		return String.format(TABLE_ADD_INDEX, indexMeta.getType().value(), indexMeta.getName(), tableName,
				indexMeta.getWay().value(), indexMeta.getColumnsString());
	}
	
	/**
	 * 删除索引
	 */
	private final String TABLE_DROP_INDEX = "DROP INDEX \"%s\";";
	
	public String getTableDropIndex(String tableName, IndexMeta indexMeta) {
		return String.format(TABLE_DROP_INDEX, indexMeta.getName());
	}
	
	/**
	 * 查询最大ID
	 */
	private final String SELECT_MAX_ID = "SELECT max(`%s`) as max_id FROM `%s` LIMIT 1;";
	
	public long getTableMaxId(IDataSource dataSource, String tableName, String primaryKeyName) {
		String sql = String.format(SELECT_MAX_ID, primaryKeyName, tableName);
		TableMaxId tableMaxId = Dao.queryAliasObject(dataSource, TableMaxId.class, sql);
		return tableMaxId.maxId;
	} 
	
	public class TableMaxId {
		@Column(comment = "表的最大ID")
		private long maxId;
	}
	
	private final String TABLE_AUTOINC = "SELECT currval(\"%s\"::regclass);";
	
	public long getTableAutoinc(IDataSource dataSource, String dbName, String tableName) {
		TableStatus tableStatus = Dao.queryAliasObject(dataSource, TableStatus.class, TABLE_AUTOINC, dbName, tableName);
		return tableStatus.autoIncrement;
	}
	
	public class TableStatus {
		@Column(name = "currval", comment = "自动增长ID")
		private long autoIncrement;
	}
	
	/**
	 * 设置自增ID
	 */
	private final String SET_AUTO_INCREMENT = "SELECT setval(\"%s\"::regclass, %s);";

	public String setAutoIncrement(String tableName, long autoinc) {
		return String.format(SET_AUTO_INCREMENT, tableName, autoinc);
	}

	@Override
	public String getTableChangeIndex(String tableName, IndexMeta indexMeta) {
		// TODO Auto-generated method stub
		return null;
	}
}

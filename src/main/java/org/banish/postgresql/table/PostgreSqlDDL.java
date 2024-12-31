/**
 * 
 */
package org.banish.postgresql.table;

import java.util.ArrayList;
import java.util.List;

import org.banish.IDDL;
import org.banish.mysql.annotation.Column;
import org.banish.mysql.annotation.Id.Strategy;
import org.banish.mysql.dao.Dao;
import org.banish.mysql.database.IDataSource;
import org.banish.mysql.orm.EntityMeta;
import org.banish.mysql.orm.IndexMeta;
import org.banish.mysql.orm.column.ColumnMeta;
import org.banish.mysql.orm.column.PrimaryKeyColumnMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author YY
 */
public class PostgreSqlDDL implements IDDL {
	
	private static Logger logger = LoggerFactory.getLogger(PostgreSqlDDL.class);
	
	private IDataSource dataSource;
	private boolean autoBuild;
	private List<String> ddlSqls = new ArrayList<>();
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
	
	
	private final String SELECT_TABLE_NAME = "SELECT \"table_name\" FROM \"information_schema\".\"tables\" WHERE \"table_schema\"=? AND \"table_name\"=?;";

	@Override
	public boolean isTableExist(String tableName) {
		TableExist tableExist = Dao.queryAliasObject(dataSource, TableExist.class, SELECT_TABLE_NAME, dataSource.getDbName(), tableName);
		return tableExist != null;
	}

	public static class TableExist {
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
	public List<? extends IIndexStruct> getKeys(String tableName) {
		String sql = String.format(SHOW_KEYS, tableName);
		return Dao.queryAliasObjects(dataSource, IndexStruct.class, sql);
	}
	
	public static class IndexStruct implements IIndexStruct {
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
		@Override
		public boolean isUnique() {
			return unique.equals("t");
		}
		@Override
		public boolean isPrimary() {
			return primary.equals("t");
		}
		public String getWay() {
			return way;
		}
		public String getColumnName() {
			return columnName;
		}
	}
	
	private final String TABLE_DES = ""
			+ "SELECT"
			+ " column_name,"
			+ " udt_name,"
			+ " character_maximum_length,"
			+ " numeric_precision,"
			+ " numeric_scale,"
			+ " column_default "
			+ "FROM"
			+ " information_schema.columns "
			+ "WHERE"
			+ " table_name = '%s';";
	
	@Override
	public List<? extends ITableDes> getTableColumns(String tableName) {
		String sql = String.format(TABLE_DES, tableName);
		return Dao.queryAliasObjects(dataSource, TableDes.class, sql);
	}
	
	public static class TableDes implements ITableDes {
		@Column(name = "column_name", comment = "字段名")
		private String field;
		@Column(name = "udt_name", comment = "字段类型")
		private String type;
		@Column(name = "character_maximum_length", comment = "字段名")
		private int characterMaximumLength;
		@Column(name = "numeric_precision", comment = "字段名")
		private int numericPrecision;
		@Column(name = "numeric_scale", comment = "字段名")
		private int numericScale;
		@Column(name = "column_default", comment = "扩展信息")
		private String extra;
		
		public String getField() {
			return field;
		}
		public String getType() {
			if(type.equals("numeric")) {
				return type + "(" + numericPrecision + "," + numericScale + ")";
			} else if(type.equals("varchar")) {
				return type + "(" + characterMaximumLength + ")";
			} else if(type.equals("bpchar")) {
				return "char(" + characterMaximumLength + ")";
			}
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
	private final String TABLE_MODIFY_COLUMN = "ALTER TABLE \"%s\" ALTER COLUMN \"%s\" TYPE %s;";
	
	@Override
	public String getTableModifyColumn(String tableName, String columnName, String columnDefine) {
		return String.format(TABLE_MODIFY_COLUMN, tableName, columnName, columnDefine);
	}
	
//	CREATE UNIQUE INDEX "idx_grade_clazz" ON "public"."student" USING btree (
//			  "grade",
//			  "clazz"
//			);
	
	/**
	 * 增加索引
	 */
	private final String TABLE_ADD_INDEX = "CREATE \"%s\" INDEX \"%s\" ON \"%s\" USING %s (%s);";
	
	@Override
	public String getTableAddIndex(String tableName, IndexMeta indexMeta) {
		return String.format(TABLE_ADD_INDEX, indexMeta.getType().value(), indexMeta.getName(), tableName,
				indexMeta.getWay().value(), indexMeta.getColumnsString());
	}
	
	/**
	 * 删除索引
	 */
	private final String TABLE_DROP_INDEX = "DROP INDEX \"%s\";";
	
	@Override
	public String getTableModifyIndex(String tableName, IndexMeta indexMeta) {
		String dropIndex = String.format(TABLE_DROP_INDEX, indexMeta.getName());
		return dropIndex + getTableAddIndex(tableName, indexMeta);
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
	
	public class TableMaxId {
		@Column(comment = "表的最大ID")
		private long maxId;
	}
	
	private final String TABLE_AUTOINC = "SELECT currval(\"%s\"::regclass);";
	
	@Override
	public long getTableAutoinc(String tableName) {
		TableStatus tableStatus = Dao.queryAliasObject(dataSource, TableStatus.class, TABLE_AUTOINC, dataSource.getDbName(), tableName);
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

	@Override
	public String setAutoIncrement(String tableName, long autoinc) {
		return String.format(SET_AUTO_INCREMENT, tableName, autoinc);
	}

	@Override
	public String createTableSql(String tableName, EntityMeta<?> entityMeta) {
		StringBuilder result = new StringBuilder();
		result.append(String.format("CREATE TABLE \"%s\" (", tableName));
		
		for(ColumnMeta columnMeta : entityMeta.getColumnList()) {
			result.append(getColumnDefine(columnMeta)).append(",");
		}
		
		ColumnMeta primaryKeyMeta = entityMeta.getPrimaryKeyMeta();
		//创建表的时候只进行了主键的定义，索引的设置会在表构建好之后进行处理
		result.append(String.format("PRIMARY KEY (`%s`)", primaryKeyMeta.getColumnName()));
		//这里并没有对自增ID进行初始处理，自增ID的设置会在表构建好之后进行处理
		result.append(") ENGINE=InnoDB DEFAULT CHARSET=").append(entityMeta.getTableCharset().value());
		result.append(" COMMENT='").append(entityMeta.getTableComment()).append("';");
		return result.toString();
	}
	
	@Override
	public String getColumnDefine(ColumnMeta columnMeta) {
		
//		COMMENT ON COLUMN "public"."Untitled"."name" IS 'mingzi';
		
		StringBuilder result = new StringBuilder();
		result.append(String.format("\"%s\" %s", columnMeta.getColumnName(), columnMeta.dbColumnType()));
		String autoIncrement = "";
		if(columnMeta instanceof PrimaryKeyColumnMeta) {
			PrimaryKeyColumnMeta keyMeta = (PrimaryKeyColumnMeta)columnMeta;
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

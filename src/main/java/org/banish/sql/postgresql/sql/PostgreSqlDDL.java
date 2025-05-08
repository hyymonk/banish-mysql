/**
 * 
 */
package org.banish.sql.postgresql.sql;

import java.util.ArrayList;
import java.util.List;

import org.banish.sql.core.annotation.Column;
import org.banish.sql.core.annotation.enuma.IndexType;
import org.banish.sql.core.dao.Dao;
import org.banish.sql.core.datasource.IDataSource;
import org.banish.sql.core.orm.ColumnMeta;
import org.banish.sql.core.orm.EntityMeta;
import org.banish.sql.core.orm.IPrimaryKeyColumnMeta;
import org.banish.sql.core.orm.IndexMeta;
import org.banish.sql.core.sql.IDDL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author YY
 */
public class PostgreSqlDDL implements IDDL {
	
	private static Logger logger = LoggerFactory.getLogger(PostgreSqlDDL.class);
	
	private final IDataSource dataSource;
	private final boolean autoBuild;
	private final List<String> ddlSqls = new ArrayList<>();
	
	public PostgreSqlDDL(IDataSource dataSource, boolean autoBuild) {
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
	
	
	private final String SELECT_TABLE_NAME = "SELECT \"table_name\" FROM \"information_schema\".\"tables\" WHERE \"table_catalog\"=? AND \"table_name\"=?;";

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
			+ "	atr.attnum,"
			+ "	am.amname "
			+ "FROM"
			+ "	pg_index idx"
			+ "	JOIN pg_class clz ON idx.indexrelid = clz.oid"
			+ "	JOIN pg_attribute atr ON atr.attrelid = idx.indexrelid"
			+ "	JOIN pg_am am ON clz.relam = am.oid "
			+ "WHERE"
			+ "	indrelid = ( SELECT oid FROM pg_class WHERE relname = ? ) "
			+ "ORDER BY"
			+ " atr.attnum";
	
	@Override
	public List<? extends IIndexStruct> getKeys(String tableName) {
		List<IndexStruct> result = Dao.queryAliasObjects(dataSource, IndexStruct.class, SHOW_KEYS, tableName);
		return result;
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
		@Override
		public String toString() {
			return "IndexStruct [name=" + name + ", unique=" + unique + ", primary=" + primary + ", way=" + way
					+ ", columnName=" + columnName + "]";
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
	private final String TABLE_MODIFY_COLUMN = "ALTER TABLE \"%s\" ALTER COLUMN \"%s\" TYPE %s USING \"%s\";";
	
	@Override
	public String getTableModifyColumn(String tableName, String columnName, ColumnMeta columnMeta) {
		return String.format(TABLE_MODIFY_COLUMN, tableName, columnName, columnMeta.dbColumnType(), columnMeta.getColumnName());
	}
	
//	CREATE UNIQUE INDEX "idx_grade_clazz" ON "public"."student" USING btree (
//			  "grade",
//			  "clazz"
//			);
	
	/**
	 * 增加索引
	 */
	private final String TABLE_ADD_INDEX = "CREATE INDEX \"%s\" ON \"%s\" USING %s (%s);";
	private final String TABLE_ADD_UNIQUE_INDEX = "CREATE UNIQUE INDEX \"%s\" ON \"%s\" USING %s (%s);";
	
	@Override
	public String getTableAddIndex(String tableName, IndexMeta indexMeta) {
		if(indexMeta.getType() == IndexType.UNIQUE) {
			return String.format(TABLE_ADD_UNIQUE_INDEX, indexMeta.getRealName(), tableName,
					indexMeta.getWay().value(), indexMeta.getColumnsString("\""));
		} else {
			return String.format(TABLE_ADD_INDEX, indexMeta.getRealName(), tableName,
					indexMeta.getWay().value(), indexMeta.getColumnsString("\""));
		}
	}
	
	/**
	 * 删除索引
	 */
	private final String TABLE_DROP_INDEX = "DROP INDEX \"%s\";";
	
	@Override
	public String getTableModifyIndex(String tableName, IndexMeta indexMeta) {
		String dropIndex = String.format(TABLE_DROP_INDEX, indexMeta.getRealName());
		return dropIndex + getTableAddIndex(tableName, indexMeta);
	}
	
	/**
	 * 查询最大ID
	 */
	private final String SELECT_MAX_ID = "SELECT max(\"%s\") as max_id FROM \"%s\" LIMIT 1;";
	
	@Override
	public long getTableMaxId(String tableName, String primaryKeyName) {
		String sql = String.format(SELECT_MAX_ID, primaryKeyName, tableName);
		TableMaxId tableMaxId = Dao.queryAliasObject(dataSource, TableMaxId.class, sql);
		return tableMaxId.maxId;
	} 
	
	public static class TableMaxId {
		@Column(name = "max_id", comment = "表的最大ID")
		private long maxId;
	}
	
//	 SELECT pg_get_serial_sequence('example', 'id');//只能查询出在创建表时就指定使用serial或者bigserial的序列
	
	/**
	 * 查询自增序列的情况
	 */
	private final String TABLE_QUERY_SEQ = "SELECT sequencename,data_type,last_value FROM pg_sequences WHERE sequencename = ?;";
	/**
	 * 创建自增序列
	 */
	private final String CREATE_TABLE_SEQ = "CREATE SEQUENCE %s AS %s INCREMENT BY 1;";
	/**
	 * 将自增序列设置到自增主键上
	 */
	private final String SET_SEQ_TO_PRIMARYKEY = "ALTER TABLE \"%s\" ALTER COLUMN \"%s\" SET DEFAULT nextval('%s'::regclass);";
	/**
	 * 设置自增序列的下一个值
	 */
	private final String SET_AUTO_INCREMENT = "SELECT SETVAL('%s'::regclass, %s, false);";
	/**
	 * 丢弃旧序列
	 */
	private final String DROP_OLD_SEQ = "DROP SEQUENCE IF EXISTS %s;";
	/**
	 * 丢弃旧序列前要先将自增主键的默认值移除
	 */
	private final String DROP_PRIMARY_KEY_DEFAULT = "ALTER TABLE %s ALTER COLUMN %s DROP DEFAULT;";
	
	@Override
	public long getTableAutoinc(String tableName, String primaryKeyName) {
		String seqName = String.format("%s_%s_seq", tableName, primaryKeyName);
		TableSeq tableSeq = Dao.queryAliasObject(dataSource, TableSeq.class, TABLE_QUERY_SEQ, seqName);
		return tableSeq.lastValue + 1;
	}
	
	@Override
	public boolean hasAutoIncrement(String tableName, String primaryKeyName) {
		String seqName = String.format("%s_%s_seq", tableName, primaryKeyName);
		TableSeq tableSeq = Dao.queryAliasObject(dataSource, TableSeq.class, TABLE_QUERY_SEQ, seqName);
		return tableSeq != null && tableSeq.name != null;
	}
	
	public static class TableSeq {
		@Column(name = "sequencename", comment = "序列名字")
		private String name;
		@Column(name = "data_type", comment = "序列的数据类型")
		private String dataType;
		@Column(name = "last_value", comment = "最后的记录值")
		private long lastValue;
		
	}
	
	@Override
	public List<String> createAutoIncrement(String tableName, IPrimaryKeyColumnMeta primaryKey, long startWith) {
		String seqName = String.format("%s_%s_seq", tableName, primaryKey.getColumnName());
		String sql = null;
		if(primaryKey.getField().getType() == int.class || primaryKey.getField().getType() == Integer.class) {
			sql = String.format(CREATE_TABLE_SEQ, seqName, "integer", startWith);
		} else {
			sql = String.format(CREATE_TABLE_SEQ, seqName, "bigint", startWith);
		}
		List<String> results = new ArrayList<>();
		results.add(String.format(DROP_PRIMARY_KEY_DEFAULT, tableName, primaryKey.getColumnName()));
		results.add(String.format(DROP_OLD_SEQ, seqName));
		results.add(sql);
		String setSeq = String.format(SET_SEQ_TO_PRIMARYKEY, tableName, primaryKey.getColumnName(), seqName);
		results.add(setSeq);
		results.add(String.format(SET_AUTO_INCREMENT, seqName, startWith));
		return results;
	}

	@Override
	public String setAutoIncrement(String tableName, String primaryKeyName, long autoinc) {
		String seqName = String.format("%s_%s_seq", tableName, primaryKeyName);
		return String.format(SET_AUTO_INCREMENT, seqName, autoinc);
	}
	
	/**
	 * 自增序列是否发生了变化
	 * @param tableName
	 * @param primaryKey
	 * @return
	 */
	public boolean checkAutoIncrement(String tableName, IPrimaryKeyColumnMeta primaryKey) {
		String seqName = String.format("%s_%s_seq", tableName, primaryKey.getColumnName());
		TableSeq tableSeq = Dao.queryAliasObject(dataSource, TableSeq.class, TABLE_QUERY_SEQ, seqName);
		
		if(tableSeq.dataType.equals("integer") && (primaryKey.getField().getType() == int.class || primaryKey.getField().getType() == Integer.class)) {
			return false;
		} else if(tableSeq.dataType.equals("bigint") && (primaryKey.getField().getType() == long.class || primaryKey.getField().getType() == Long.class)) {
			return false;
		} else {
			return true;
		}
	}
	

	@Override
	public List<String> createTableSql(String tableName, EntityMeta<?> entityMeta) {
		StringBuilder result = new StringBuilder();
		result.append(String.format("CREATE TABLE \"%s\" (", tableName));
		
		for(ColumnMeta columnMeta : entityMeta.getColumnList()) {
			result.append(getColumnDefine(columnMeta)).append(",");
		}
		
		IPrimaryKeyColumnMeta primaryKeyMeta = entityMeta.getPrimaryKeyMeta();
		//创建表的时候只进行了主键的定义，索引的设置会在表构建好之后进行处理
		result.append(String.format("CONSTRAINT \"%s_pkey\" PRIMARY KEY (\"%s\")", tableName, primaryKeyMeta.getColumnName()));
		//这里并没有对自增ID进行初始处理，自增ID的设置会在表构建好之后进行处理
		result.append(");");
		
		List<String> results = new ArrayList<>();
		results.add(result.toString());
		String tableComment = String.format("COMMENT ON TABLE \"%s\" IS '%s';", tableName, entityMeta.getTableComment());
		results.add(tableComment);
		for(ColumnMeta columnMeta : entityMeta.getColumnList()) {
			String columnComment = String.format("COMMENT ON COLUMN \"%s\".\"%s\" IS '%s';", tableName, columnMeta.getColumnName(), columnMeta.getComment());
			results.add(columnComment);
		}
		return results;
	}
	
	@Override
	public String getColumnDefine(ColumnMeta columnMeta) {
//		COMMENT ON COLUMN "public"."Untitled"."name" IS 'mingzi';
		return String.format("\"%s\" %s %s", columnMeta.getColumnName(), columnMeta.dbColumnType(), columnMeta.defaultValue());
	}
}

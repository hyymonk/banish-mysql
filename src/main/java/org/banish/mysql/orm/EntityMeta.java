/**
 * 
 */
package org.banish.mysql.orm;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.banish.mysql.AbstractEntity;
import org.banish.mysql.annotation.enuma.Charset;
import org.banish.mysql.orm.column.ColumnMeta;
import org.banish.mysql.orm.column.PrimaryKeyColumnMeta;
import org.banish.mysql.orm.table.ITable;
import org.banish.mysql.util.ReflectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author YY
 * 数据库字段与实体对象的映射信息
 */
public abstract class EntityMeta<T extends AbstractEntity> implements IEntityMeta<T> {
	
	private static Logger logger = LoggerFactory.getLogger(EntityMeta.class);
	
	/**
	 * 实体类
	 */
	private final Class<T> clazz;
	/**
	 * 原始的表名
	 */
	private final String tableName;
	/**
	 * 表的备注
	 */
	private final String tableComment;
	/**
	 * 表的字符集
	 */
	private final Charset tableCharset;
	/**
	 * 表所隶属的数据库别名
	 */
	private final String dbAlias;
	/**
	 * 是否自动建表
	 */
	private final boolean autoBuild;
	/**
	 * 主键元数据
	 */
	private final PrimaryKeyColumnMeta primaryKeyMeta;
	/**
	 * 所有字段列表，包含主键，在合服的时候主键会被保留原来的值
	 */
	private final List<ColumnMeta> columnList;
	/**
	 * 字段（及数据库字段并列）索引
	 */
	private final Map<String, ColumnMeta> columnMap;
	/**
	 * 索引列表
	 */
	private final Map<String, IndexMeta> indexMap;
	/**
	 * 实体类属性名与数据列名的对应关系
	 */
	private final Map<String, String> fieldToColumn;
	
	protected EntityMeta(Class<T> clazz, ITable table) {
		this.clazz = clazz;
		if("".equals(table.name())) {
			this.tableName = IEntityMeta.makeSnakeCase(clazz.getSimpleName());
		} else {
			this.tableName = table.name().toLowerCase();
		}
		this.tableComment = table.comment();
		this.tableCharset = table.charset();
		this.dbAlias = table.dbAlias();
		this.autoBuild = table.autoBuild();
		
		List<Field> allFields = ReflectUtil.getAllFields(clazz);
		//构建列元数据
		List<ColumnMeta> columnList = new ArrayList<>(allFields.size());
		Map<String, ColumnMeta> columnNameMap = new HashMap<>(allFields.size());
		Map<String, String> fieldToColumnMap = new HashMap<>(allFields.size());
		
		this.primaryKeyMeta = ColumnMeta.buildAndReturnKey(allFields, columnList, columnNameMap, fieldToColumnMap);
		if(this.primaryKeyMeta == null) {
			throw new RuntimeException(clazz.getSimpleName() + " @Id field not found");
		}
		this.columnList = Collections.unmodifiableList(columnList);
		this.columnMap = Collections.unmodifiableMap(columnNameMap);
		this.fieldToColumn = Collections.unmodifiableMap(fieldToColumnMap);
		//构建索引元数据
		this.indexMap = Collections.unmodifiableMap(IndexMeta.build(table, this.fieldToColumn));
	}

	public Class<T> getClazz() {
		return clazz;
	}

	public String getTableName() {
		return tableName;
	}

	public PrimaryKeyColumnMeta getPrimaryKeyMeta() {
		return primaryKeyMeta;
	}

	public List<ColumnMeta> getColumnList() {
		return columnList;
	}

	public Map<String, ColumnMeta> getColumnMap() {
		return columnMap;
	}
	
	public Map<String, IndexMeta> getIndexMap() {
		return indexMap;
	}
	
	public String getColumnName(String fieldName) {
		return this.fieldToColumn.get(fieldName);
	}
	
	public <A extends Annotation> A getClzAnnotation(Class<A> annotationClz) {
		return clazz.getAnnotation(annotationClz);
	}

	public String getTableComment() {
		return tableComment;
	}

	public Charset getTableCharset() {
		return tableCharset;
	}

	public String getDbAlias() {
		return dbAlias;
	}

	@Override
	public ColumnMeta getColumnMeta(String columnName) {
		return columnMap.get(columnName);
	}

	@Override
	public T newInstance() throws Exception {
		return clazz.getConstructor().newInstance();
	}
	
	public Object getPrimaryKeyValue(T t) {
		try {
			Field field = this.primaryKeyMeta.getField();
			field.setAccessible(true);
			return field.get(t);
		} catch (IllegalArgumentException e) {
			logger.error(String.format("%s的对象在获取主键值的时候发生异常", this.clazz.getSimpleName()), e);
		} catch (IllegalAccessException e) {
			logger.error(String.format("%s的对象在获取主键值的时候发生异常", this.clazz.getSimpleName()), e);
		}
		return null;
	}

	public boolean isAutoBuild() {
		return autoBuild;
	}
	
	public long getCustomInitId() {
		Class<T> clazz = this.getClazz();
		long customInitId = 0;
		try {
			T entity = clazz.getConstructor().newInstance();
			customInitId = entity.idGenerator();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return customInitId;
	}
}

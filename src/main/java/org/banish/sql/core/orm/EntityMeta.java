/**
 * 
 */
package org.banish.sql.core.orm;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.banish.sql.core.annotation.Column;
import org.banish.sql.core.annotation.Id;
import org.banish.sql.core.annotation.enuma.Charset;
import org.banish.sql.core.entity.AbstractEntity;
import org.banish.sql.core.orm.tableinfo.ITable;
import org.banish.sql.core.util.ReflectUtil;
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
	private final IPrimaryKeyColumnMeta primaryKeyMeta;
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
	
	private final IMetaFactory metaFactory;
	
	protected EntityMeta(Class<T> clazz, ITable table, IMetaFactory metaFactory) {
		this.clazz = clazz;
		this.metaFactory = metaFactory;
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
		
		this.primaryKeyMeta = buildAndReturnKey(allFields, columnList, columnNameMap, fieldToColumnMap);
		if(this.primaryKeyMeta == null) {
			throw new RuntimeException(clazz.getSimpleName() + " @Id field not found");
		}
		this.columnList = Collections.unmodifiableList(columnList);
		this.columnMap = Collections.unmodifiableMap(columnNameMap);
		this.fieldToColumn = Collections.unmodifiableMap(fieldToColumnMap);
		//构建索引元数据
		this.indexMap = Collections.unmodifiableMap(IndexMeta.build(clazz, table, this.tableName, this.fieldToColumn));
	}
	
	
	/**
	 * 创建字段的元数据，并返回主键原数据
	 * @param allFields
	 * @param columnList
	 * @param columnMap
	 * @return
	 */
	private IPrimaryKeyColumnMeta buildAndReturnKey(List<Field> allFields, List<ColumnMeta> columnList,
			Map<String, ColumnMeta> columnMap, Map<String, String> fieldMap) {
        //列信息
    	IPrimaryKeyColumnMeta idMeta = null;
        for (Field field : allFields) {
            Column column = field.getAnnotation(Column.class);
            if(column == null) {
                continue;
            }
            Id id = field.getAnnotation(Id.class);
            ColumnMeta columnMeta = null;
            if(id != null) {
            	if(idMeta == null) {
    				idMeta = metaFactory.newPrimaryKeyColumnMeta(field);
    			} else {
    				throw new RuntimeException(field.getDeclaringClass().getSimpleName() + " @Id field more than one");
    			}
            	columnMeta = (ColumnMeta)idMeta;
            } else {
            	columnMeta = metaFactory.newColumnMeta(field);
            }
            //构建数据库字段与对象属性关系
            columnList.add(columnMeta);
            columnMap.put(columnMeta.getColumnName(), columnMeta);
            fieldMap.put(columnMeta.getFieldName(), columnMeta.getColumnName());
        }
        return idMeta;
    }
	

	public Class<T> getClazz() {
		return clazz;
	}

	public String getTableName() {
		return tableName;
	}

	public IPrimaryKeyColumnMeta getPrimaryKeyMeta() {
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

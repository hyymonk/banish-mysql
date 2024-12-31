/**
 * 
 */
package org.banish.mysql.orm;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.banish.mysql.orm.column.MColumnMetaFactory;
import org.banish.mysql.util.ReflectUtil;

/**
 * @author YY
 *
 */
public class AliasEntityMeta<T> implements IEntityMeta<T> {
	/**
	 * 实体类
	 */
	private final Class<T> clazz;
	/**
	 * 所有字段列表
	 */
	private final List<ColumnMeta> columnList;
	/**
	 * 字段（及数据库字段并列）索引
	 */
	private final Map<String, ColumnMeta> columnMap;
	
	public AliasEntityMeta(Class<T> clazz) {
		this.clazz = clazz;
		
		List<ColumnMeta> columnList = new ArrayList<>();
		Map<String, ColumnMeta> columnMap = new HashMap<>();
		
		List<Field> allFields = ReflectUtil.getAllFields(clazz);
		buildAlias(allFields, columnList, columnMap);
		
		this.columnList = Collections.unmodifiableList(columnList);
		this.columnMap = Collections.unmodifiableMap(columnMap);
	}
	
	private void buildAlias(List<Field> allFields, List<ColumnMeta> columnList, Map<String, ColumnMeta> columnMap) {
        //列信息
        for (Field field : allFields) {
            ColumnMeta columnMeta = MColumnMetaFactory.INS.build(field);        
            //构建数据库字段与对象属性关系
            columnList.add(columnMeta);
            columnMap.put(columnMeta.getColumnName(), columnMeta);
        }
    }

	public List<ColumnMeta> getColumnList() {
		return columnList;
	}

	public Map<String, ColumnMeta> getColumnMap() {
		return columnMap;
	}

	@Override
	public ColumnMeta getColumnMeta(String columnName) {
		return columnMap.get(columnName);
	}

	@Override
	public T newInstance() throws Exception {
		return clazz.getConstructor().newInstance();
	}
}

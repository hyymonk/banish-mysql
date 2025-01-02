/**
 * 
 */
package org.banish.sql.core.orm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.banish.sql.core.annotation.Index;
import org.banish.sql.core.annotation.SuperIndex;
import org.banish.sql.core.annotation.enuma.IndexType;
import org.banish.sql.core.annotation.enuma.IndexWay;
import org.banish.sql.core.orm.tableinfo.ITable;

/**
 * @author YY
 *
 */
public class IndexMeta {
	private String name;
	private List<String> columns = new ArrayList<>();
	private IndexType type;
	private IndexWay way;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public IndexType getType() {
		return type;
	}

	public void setType(IndexType type) {
		this.type = type;
	}

	public IndexWay getWay() {
		return way;
	}

	public void setWay(IndexWay way) {
		this.way = way;
	}

	public List<String> getColumns() {
		return columns;
	}

	public void setColumns(List<String> columns) {
		this.columns = columns;
	}

	public String getColumnsString(String dot) {
		StringBuffer columnBuff = new StringBuffer();
		boolean isFirst = true;
		for (String column : columns) {
			if (!isFirst) {
				columnBuff.append(",");
			}
			columnBuff.append(String.format(dot + "%s" + dot, column));
			isFirst = false;
		}
		return columnBuff.toString();
	}

	public static Map<String, IndexMeta> build(Class<?> clazz, ITable table, String tableName,
			Map<String, String> fieldToColumn) {
		List<Index> allIndexes = new ArrayList<>();

		Class<?> currClazz = clazz;
		while (currClazz != null) {
			SuperIndex superIndex = currClazz.getAnnotation(SuperIndex.class);
			if (superIndex != null) {
				for (Index index : superIndex.indexs()) {
					allIndexes.add(index);
				}
			}
			currClazz = currClazz.getSuperclass();
		}
		for (Index index : table.indexs()) {
			allIndexes.add(index);
		}

		Map<String, IndexMeta> indexMap = new HashMap<>();
		Map<String, String> fieldsMap = new HashMap<>();
		// 表注解上定义的索引
		for (Index index : allIndexes) {
			String indexName = "idx";
			for (String fieldName : index.fields()) {
				indexName += "_" + IEntityMeta.makeSnakeCase(fieldName);
			}
			if (indexMap.containsKey(indexName)) {
				throw new RuntimeException("实体类[" + tableName + "]中名字为[" + indexName + "]的索引被重复定义");
			}
			String useFields = String.join("_", index.fields());
			if (fieldsMap.containsKey(useFields)) {
				throw new RuntimeException("实体类[" + tableName + "]中名字为[" + indexName + "]的索引被重复定义");
			}

			IndexMeta tableIndex = new IndexMeta();
			tableIndex.setName(indexName);
			for (String fieldName : index.fields()) {
				String columnName = fieldToColumn.get(fieldName);
				if (columnName == null) {
					throw new RuntimeException("实体类[" + tableName + "]中未找到字段名[" + fieldName + "]映射的列名");
				}
				tableIndex.getColumns().add(columnName);
			}
			tableIndex.setType(index.type());
			tableIndex.setWay(index.way());
			indexMap.put(tableIndex.getName(), tableIndex);
			fieldsMap.put(useFields, indexName);
		}
		return indexMap;
	}

}

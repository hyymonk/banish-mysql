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
	//在解释类注解时生成的名字（不带表名前缀）
	private String rawName;
	//结合表名的索引名字，即实际在数据库创建索引时使用的名字
	private String realName;
	private List<String> columns = new ArrayList<>();
	private IndexType type;
	private IndexWay way;

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
			String indexName = formatIndexName(index.alias(), index.fields());
			if (indexMap.containsKey(indexName)) {
				throw new RuntimeException("实体类[" + tableName + "]中名字为[" + indexName + "]的索引被重复定义");
			}
			String useFields = String.join("_", index.fields());
			if (fieldsMap.containsKey(useFields)) {
				throw new RuntimeException("实体类[" + tableName + "]中名字为[" + indexName + "]的索引被重复定义");
			}
			//此时的索引名字还不具有表名的前缀
			IndexMeta tableIndex = new IndexMeta();
			tableIndex.setRawName(indexName);
			for (String fieldName : index.fields()) {
				String columnName = fieldToColumn.get(fieldName);
				if (columnName == null) {
					throw new RuntimeException("实体类[" + tableName + "]中未找到字段名[" + fieldName + "]映射的列名");
				}
				tableIndex.getColumns().add(columnName);
			}
			tableIndex.setType(index.type());
			tableIndex.setWay(index.way());
			indexMap.put(tableIndex.getRawName(), tableIndex);
			fieldsMap.put(useFields, indexName);
		}
		return indexMap;
	}
	
	/**
	 * 格式化索引名字
	 * @param indexAlias
	 * @param fieldNames
	 * @return
	 */
	private static String formatIndexName(String indexAlias, String[] fieldNames) {
		String indexName = "idx";
		if(indexAlias.equals("")) {
			for (String fieldName : fieldNames) {
				indexName += "_" + IEntityMeta.makeSnakeCase(fieldName);
			}
		} else {
			indexName += "_" + indexAlias;
		}
		return indexName;
	}

	public String getRawName() {
		return rawName;
	}

	public void setRawName(String rawName) {
		this.rawName = rawName;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

}

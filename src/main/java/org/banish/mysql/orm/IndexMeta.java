/**
 * 
 */
package org.banish.mysql.orm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.banish.mysql.annotation.Index;
import org.banish.mysql.annotation.enuma.IndexType;
import org.banish.mysql.annotation.enuma.IndexWay;
import org.banish.mysql.orm.table.ITable;

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
	public String getColumnsString() {
		StringBuffer columnBuff = new StringBuffer();
		boolean isFirst = true;
		for(String column : columns) {
			if(!isFirst) {
				columnBuff.append(",");
			}
			columnBuff.append(String.format("`%s`", column));
			isFirst = false;
		}
		return columnBuff.toString();
	}
	
	public static Map<String, IndexMeta> build(ITable table, Map<String, String> fieldToColumn) {
		Map<String, IndexMeta> indexMap = new HashMap<>();
		Index[] indexes = table.indexs();
		//表注解上定义的索引
		for(Index index : indexes) {
			if(indexMap.containsKey(index.name())) {
				throw new RuntimeException("实体类[" + table.name() + "]中名字为[" + index.name() + "]的索引被重复定义");
			}
			IndexMeta tableIndex = new IndexMeta();
			tableIndex.setName(index.name());
			for(String fieldName : index.fields()) {
				String columnName = fieldToColumn.get(fieldName);
				if(columnName == null) {
					throw new RuntimeException("实体类[" + table.name() + "]中未找到字段名[" + fieldName + "]映射的列名");
				}
				tableIndex.getColumns().add(columnName);
			}
			tableIndex.setType(index.type());
			tableIndex.setWay(index.way());
			indexMap.put(tableIndex.getName(), tableIndex);
		}
		return indexMap;
	}
	
}

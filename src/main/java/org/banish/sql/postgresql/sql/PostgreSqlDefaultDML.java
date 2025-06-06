/**
 * 
 */
package org.banish.sql.postgresql.sql;


import org.banish.sql.core.annotation.enuma.IndexType;
import org.banish.sql.core.entity.AbstractEntity;
import org.banish.sql.core.orm.ColumnMeta;
import org.banish.sql.core.orm.EntityMeta;
import org.banish.sql.core.orm.IndexMeta;
import org.banish.sql.core.sql.DefaultDML;

/**
 * @author YY
 *
 */
public class PostgreSqlDefaultDML<T extends AbstractEntity> extends DefaultDML<T> {
	
	public PostgreSqlDefaultDML(EntityMeta<T> entityMeta) {
		super(entityMeta, "\"");
	}

	/**
	 * PostgreSql使用insertUpdate特性后会导致自增序列发生跳跃，需谨慎使用
	 * 并且在使用insertUpdate时需要确保数据列表中同一个唯一约束的数据只有一条，
	 * 不可更新成功的数据列表[example(id=【1】,name=A),example(id=【1】,name=B)]
	 * 可以更新成功的数据列表[example(id=【1】,name=A),example(id=【2】,name=B)]
	 */
	public String insertUpdate(EntityMeta<T> entityMeta, int dataCount) {
		IndexMeta uniqueIndex = null;
		for(IndexMeta indexMeta : entityMeta.getIndexMap().values()) {
			if(indexMeta.getType() == IndexType.UNIQUE) {
				uniqueIndex = indexMeta;
				break;
			}
		}
		StringBuilder sql = new StringBuilder();
		sql.append(String.format("INSERT INTO \"%s\" (", TABLE_NAME));
		boolean isFirst = true;
		for(ColumnMeta column : entityMeta.getInsertColumnList()) {
			if(!isFirst) {
				sql.append(",");
			}
			sql.append(String.format("\"%s\"", column.getColumnName()));
			isFirst = false;
		}
		sql.append(") VALUES ");
		
		isFirst = true;
		for(int c = 0; c < dataCount; c++) {
			if(!isFirst) {
				sql.append(",");
			}
			
			boolean firstColumn = true;
			sql.append("(");
			for(int i = 0; i < entityMeta.getInsertColumnList().size(); i++) {
				if(!firstColumn) {
					sql.append(",");
				}
				sql.append("?");
				firstColumn = false;
			}
			sql.append(")");
			
			isFirst = false;
		}
		
		if(uniqueIndex != null) {
			sql.append(String.format(" ON CONFLICT (%s) DO UPDATE SET ", uniqueIndex.getColumnsString(dot)));
		} else {
			sql.append(String.format(" ON CONFLICT (%s) DO UPDATE SET ", entityMeta.getPrimaryKeyMeta().getColumnName()));
		}
		
		isFirst = true;
		for (ColumnMeta columnMeta : entityMeta.getInsertColumnList()) {
			if(columnMeta.isReadonly()) {
				continue;
			}
			if (!isFirst) {
				sql.append(",");
			}
			isFirst = false;
			sql.append(String.format("\"%s\"= EXCLUDED.\"%s\"", columnMeta.getColumnName(), columnMeta.getColumnName()));
		}
		return sql.toString();
	}
}

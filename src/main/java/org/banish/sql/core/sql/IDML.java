/**
 * 
 */
package org.banish.sql.core.sql;

import org.banish.sql.core.annotation.Id.Strategy;
import org.banish.sql.core.orm.ColumnMeta;
import org.banish.sql.core.orm.EntityMeta;

/**
 * @author YY
 *
 */
public abstract class IDML<T> {
	
	public abstract String insert();
	
	public abstract String update();
	
	public abstract String delete();
	
	protected abstract String dot();
	
	/**
	 * 构建插入语句
	 * @param entityMeta
	 * @param tableName
	 * @return
	 */
	protected final String buildInsertSql(EntityMeta<?> entityMeta, String tableName) {
		StringBuilder sql = new StringBuilder();
		sql.append(String.format("INSERT INTO %s%s%s (", dot(), tableName, dot()));
		boolean isFirst = true;
		
		boolean isAutoIncrement = false;
		if(entityMeta.getPrimaryKeyMeta().getStrategy() == Strategy.AUTO) {
			isAutoIncrement = true;
		}
		for(ColumnMeta column : entityMeta.getColumnList()) {
			if(column == entityMeta.getPrimaryKeyMeta() && isAutoIncrement) {
				continue;
			}
			if(!isFirst) {
				sql.append(",");
			}
			sql.append(String.format("%s%s%s", dot(), column.getColumnName(), dot()));
			isFirst = false;
		}
		sql.append(") VALUES (");
		isFirst = true;
		for(ColumnMeta column : entityMeta.getColumnList()) {
			if(column == entityMeta.getPrimaryKeyMeta() && isAutoIncrement) {
				continue;
			}
			if(!isFirst) {
				sql.append(",");
			}
			sql.append("?");
			isFirst = false;
		}
		sql.append(")");
		return sql.toString();
	}
	
	/**
	 * 构建更新语句
	 * @param entityMeta
	 * @param tableName
	 * @return
	 */
	protected final String buildUpdateSql(EntityMeta<?> entityMeta, String tableName) {
		StringBuilder sql = new StringBuilder();
		sql.append(String.format("UPDATE %s%s%s SET ", dot(), tableName, dot()));
		boolean isFirst = true;
		for(ColumnMeta column : entityMeta.getColumnList()) {
			if(column.isReadonly()) {
				continue;
			}
			if(column == entityMeta.getPrimaryKeyMeta()) {
				continue;
			}
			if(!isFirst) {
				sql.append(",");
			}
			sql.append(String.format("%s%s%s=?", dot(), column.getColumnName(), dot()));
			isFirst = false;
		}
		sql.append(String.format(" WHERE %s%s%s=?", dot(), entityMeta.getPrimaryKeyMeta().getColumnName(), dot()));
		return sql.toString();
	}
	
	/**
	 * 构建删除语句
	 * @param entityMeta
	 * @param tableName
	 * @return
	 */
	protected final String buildDeleteSql(EntityMeta<?> entityMeta, String tableName) {
		return String.format("DELETE FROM %s%s%s WHERE %s%s%s=?", dot(), tableName, dot(), dot(), entityMeta.getPrimaryKeyMeta().getColumnName(), dot());
	}
	
	/**
	 * 查询整个表数据的SQL
	 * @param entityMeta
	 * @return
	 */
	protected final String buildSelectAll(String tableName) {
		return String.format("SELECT * FROM %s%s%s", dot(), tableName, dot());
	}
	
	/**
	 * 查询表中的数据条数
	 * @param entityMeta
	 * @return
	 */
	protected final String buildCountAll(String tableName) {
		return String.format("SELECT count(1) AS number FROM %s%s%s", dot(), tableName, dot());
	}
}

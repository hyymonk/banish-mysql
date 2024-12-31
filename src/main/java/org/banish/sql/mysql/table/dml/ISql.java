/**
 * 
 */
package org.banish.sql.mysql.table.dml;

import org.banish.sql.core.orm.ColumnMeta;
import org.banish.sql.core.orm.EntityMeta;

/**
 * @author YY
 *
 */
public interface ISql<T> {
	
	String insert();
	
	String update();
	
	String delete();
	
	/**
	 * 构建插入语句
	 * @param entityMeta
	 * @param tableName
	 * @return
	 */
	static String buildInsertSql(EntityMeta<?> entityMeta, String tableName) {
		StringBuilder sql = new StringBuilder();
		sql.append(String.format("INSERT INTO `%s` (", tableName));
		boolean isFirst = true;
		for(ColumnMeta column : entityMeta.getColumnList()) {
			if(!isFirst) {
				sql.append(",");
			}
			sql.append(String.format("`%s`", column.getColumnName()));
			isFirst = false;
		}
		sql.append(") VALUES (");
		isFirst = true;
		for(int i = 0; i < entityMeta.getColumnList().size(); i++) {
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
	static String buildUpdateSql(EntityMeta<?> entityMeta, String tableName) {
		StringBuilder sql = new StringBuilder();
		sql.append(String.format("UPDATE `%s` SET ", tableName));
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
			sql.append(String.format("`%s`=?", column.getColumnName()));
			isFirst = false;
		}
		sql.append(String.format(" WHERE `%s`=?", entityMeta.getPrimaryKeyMeta().getColumnName()));
		return sql.toString();
	}
	
	/**
	 * 构建删除语句
	 * @param entityMeta
	 * @param tableName
	 * @return
	 */
	static String buildDeleteSql(EntityMeta<?> entityMeta, String tableName) {
		return String.format("DELETE FROM `%s` WHERE `%s`=?", tableName, entityMeta.getPrimaryKeyMeta().getColumnName());
	}
	
	/**
	 * 查询整个表数据的SQL
	 * @param entityMeta
	 * @return
	 */
	static String buildSelectAll(String tableName) {
		return String.format("SELECT * FROM `%s`", tableName);
	}
	
	/**
	 * 查询表中的数据条数
	 * @param entityMeta
	 * @return
	 */
	static String buildCountAll(String tableName) {
		return String.format("SELECT count(1) AS number FROM `%s`", tableName);
	}
}

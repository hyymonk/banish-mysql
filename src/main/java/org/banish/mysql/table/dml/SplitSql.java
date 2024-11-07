/**
 * 
 */
package org.banish.mysql.table.dml;

import static org.banish.mysql.table.Symbol.DOT;

import org.banish.mysql.orm.EntityMeta;
import org.banish.mysql.orm.column.ColumnMeta;

/**
 * @author YY
 *
 */
public class SplitSql<T> implements ISql<T> {

	public final String TABLE_NAME;
	public final String INSERT;
	public final String UPDATE;
	public final String DELETE;
	
	public SplitSql(EntityMeta<T> entityMeta, String tableName) {
		this.TABLE_NAME = tableName;
		
//		SELECT = select(entityMeta);
//		SELECT_ALL = selectAll(entityMeta);
		INSERT = insert(entityMeta);
		UPDATE = update(entityMeta);
		DELETE = delete(entityMeta);
//		DELETE_IDS = deleteIds(entityMeta);
//		DELETE_ALL = deleteAll(entityMeta);
	}
	
	/**
	 * 插入数据的SQL
	 * @param entityMeta
	 * @return
	 */
	public String insert(EntityMeta<T> entityMeta) {
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO " + DOT + TABLE_NAME + DOT + " (");
		boolean isFirst = true;
		for(ColumnMeta column : entityMeta.getColumnList()) {
			if(!isFirst) {
				sql.append(",");
			}
			sql.append(DOT + column.getColumnName() + DOT);
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
	 * 根据ID更新数据的SQL
	 * @param entityMeta
	 * @return
	 */
	public String update(EntityMeta<T> entityMeta) {
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE " + DOT + TABLE_NAME + DOT + " SET ");
		boolean isFirst = true;
		for(ColumnMeta column : entityMeta.getColumnList()) {
			if(!isFirst) {
				sql.append(",");
			}
			sql.append(DOT + column.getColumnName() + DOT).append("=?");
			isFirst = false;
		}
		sql.append(" WHERE " + DOT + entityMeta.getPrimaryKeyMeta().getColumnName() + DOT + "=?");
		return sql.toString();
	}
	/**
	 * 根据ID删除数据的SQL
	 * @param entityMeta
	 * @return
	 */
	public String delete(EntityMeta<T> entityMeta) {
		return "DELETE FROM " + DOT + TABLE_NAME + DOT + " WHERE " + DOT + entityMeta.getPrimaryKeyMeta().getColumnName() + DOT + "=?";
	}

	@Override
	public String insert() {
		return INSERT;
	}

	@Override
	public String update() {
		return UPDATE;
	}

	@Override
	public String delete() {
		return DELETE;
	}

}

/**
 * 
 */
package org.banish.mysql.table.dml;


import org.banish.mysql.orm.EntityMeta;
import org.banish.mysql.orm.column.ColumnMeta;
import static org.banish.mysql.table.Symbol.DOT;

/**
 * @author YY
 *
 */
public class DefaultSql<T> implements ISql<T> {

	public final String TABLE_NAME;
	public final String SELECT;
	public final String SELECT_ALL;
	public final String INSERT;
	public final String UPDATE;
	public final String DELETE;
	public final String DELETE_ALL;
	public final String COUNT_ALL;
	
	public DefaultSql(EntityMeta<T> entityMeta) {
		TABLE_NAME = entityMeta.getTableName();
		
		SELECT = select(entityMeta);
		SELECT_ALL = selectAll(entityMeta);
		INSERT = insert(entityMeta);
		UPDATE = update(entityMeta);
		DELETE = delete(entityMeta);
		DELETE_ALL = deleteAll(entityMeta);
		COUNT_ALL = countAll(entityMeta);
		
//		System.out.println(SELECT);
//		System.out.println(SELECT_ALL);
//		System.out.println(INSERT);
//		System.out.println(UPDATE);
//		System.out.println(DELETE);
//		System.out.println(DELETE_IDS);
//		System.out.println(DELETE_ALL);
	}
	/**
	 * 根据ID查询数据的SQL
	 * @param entityMeta
	 * @return
	 */
	private String select(EntityMeta<T> entityMeta) {
		return "SELECT * FROM " + DOT + TABLE_NAME + DOT + " WHERE " + DOT + entityMeta.getPrimaryKeyMeta().getColumnName() + DOT + "=?";
	}
	/**
	 * 查询整个表数据的SQL
	 * @param entityMeta
	 * @return
	 */
	private String selectAll(EntityMeta<T> entityMeta) {
		return "SELECT * FROM " + DOT + TABLE_NAME + DOT;
	}
	/**
	 * 插入数据的SQL
	 * @param entityMeta
	 * @return
	 */
	private String insert(EntityMeta<T> entityMeta) {
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
	private String update(EntityMeta<T> entityMeta) {
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE " + DOT + TABLE_NAME + DOT + " SET ");
		boolean isFirst = true;
		for(ColumnMeta column : entityMeta.getColumnList()) {
			if(column.isReadonly()) {
				continue;
			}
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
	private String delete(EntityMeta<T> entityMeta) {
		return "DELETE FROM " + DOT + TABLE_NAME + DOT + " WHERE " + DOT + entityMeta.getPrimaryKeyMeta().getColumnName() + DOT + "=?";
	}
	/**
	 * 删除某个表所有数据的SQL
	 * @param entityMeta
	 * @return
	 */
	private String deleteAll(EntityMeta<T> entityMeta) {
		return "DELETE FROM " + DOT + TABLE_NAME + DOT;
	}
	/**
	 * 查询表中的数据条数
	 * @param entityMeta
	 * @return
	 */
	private String countAll(EntityMeta<T> entityMeta) {
		return "SELECT count(1) AS number FROM " + DOT + TABLE_NAME + DOT;
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
	
	public String insertUpdate(EntityMeta<T> entityMeta, int dataCount) {
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
		sql.append(") VALUES ");
		
		isFirst = true;
		for(int c = 0; c < dataCount; c++) {
			if(!isFirst) {
				sql.append(",");
			}
			
			boolean firstColumn = true;
			sql.append("(");
			for(int i = 0; i < entityMeta.getColumnList().size(); i++) {
				if(!firstColumn) {
					sql.append(",");
				}
				sql.append("?");
				firstColumn = false;
			}
			sql.append(")");
			
			isFirst = false;
		}
		sql.append(" ON DUPLICATE KEY UPDATE ");
		isFirst = true;
		for (ColumnMeta columnMeta : entityMeta.getColumnList()) {
			if (columnMeta.getColumnName().equals(entityMeta.getPrimaryKeyMeta().getColumnName())) {
				continue;
			}
			if(columnMeta.isReadonly()) {
				continue;
			}
			if (!isFirst) {
				sql.append(",");
			}
			isFirst = false;
			sql.append(DOT + columnMeta.getColumnName() + DOT + " = VALUES(" + columnMeta.getColumnName() + ")");
		}
		return sql.toString();
	}
	
}

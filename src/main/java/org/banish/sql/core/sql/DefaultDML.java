/**
 * 
 */
package org.banish.sql.core.sql;

import org.banish.sql.core.entity.AbstractEntity;
import org.banish.sql.core.orm.EntityMeta;

/**
 * @author YY
 */
public abstract class DefaultDML<T extends AbstractEntity> extends IDML<T> {
	public final String TABLE_NAME;
	public final String SELECT;
	public final String SELECT_ALL;
	public final String INSERT;
	public final String MERGE_INSERT;
	public final String UPDATE;
	public final String DELETE;
	public final String DELETE_ALL;
	public final String COUNT_ALL;
	
	public DefaultDML(EntityMeta<T> entityMeta) {
		TABLE_NAME = entityMeta.getTableName();
		
		SELECT = select(entityMeta);
		SELECT_ALL = buildSelectAll(TABLE_NAME);
		INSERT = buildInsertSql(entityMeta, TABLE_NAME);
		MERGE_INSERT = buildMergeInsertSql(entityMeta, TABLE_NAME);
		UPDATE = buildUpdateSql(entityMeta, TABLE_NAME);
		DELETE = buildDeleteSql(entityMeta, TABLE_NAME);
		DELETE_ALL = deleteAll(entityMeta);
		COUNT_ALL = buildCountAll(TABLE_NAME);
		
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
		return String.format("SELECT * FROM %s%s%s WHERE %s%s%s=?", dot(), TABLE_NAME, dot(), dot(), entityMeta.getPrimaryKeyMeta().getColumnName(), dot());
	}
	/**
	 * 删除某个表所有数据的SQL
	 * @param entityMeta
	 * @return
	 */
	private String deleteAll(EntityMeta<T> entityMeta) {
		return String.format("DELETE FROM %s%s%s", dot(), TABLE_NAME, dot());
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
	
	public abstract String insertUpdate(EntityMeta<T> entityMeta, int dataCount);
}

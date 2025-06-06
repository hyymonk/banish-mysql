/**
 * 
 */
package org.banish.sql.core.sql;

import org.banish.sql.core.entity.AbstractEntity;
import org.banish.sql.core.orm.EntityMeta;

/**
 * @author YY
 */
public abstract class SplitDML<T extends AbstractEntity> extends IDML<T> {

	public final String TABLE_NAME;
	public final String SELECT_ALL;
	public final String INSERT;
	public final String UPDATE;
	public final String DELETE;
	public final String COUNT_ALL;
	
	public SplitDML(EntityMeta<T> entityMeta, String tableName, String dot) {
		super(dot);
		this.TABLE_NAME = tableName;
		
//		SELECT = select(entityMeta);
		SELECT_ALL = buildSelectAll(TABLE_NAME);
		INSERT = buildInsertSql(entityMeta, TABLE_NAME);
		UPDATE = buildUpdateSql(entityMeta, TABLE_NAME);
		DELETE = buildDeleteSql(entityMeta, TABLE_NAME);
//		DELETE_IDS = deleteIds(entityMeta);
//		DELETE_ALL = deleteAll(entityMeta);
		COUNT_ALL = buildCountAll(TABLE_NAME);
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

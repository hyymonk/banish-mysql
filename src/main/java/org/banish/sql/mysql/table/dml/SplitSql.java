/**
 * 
 */
package org.banish.sql.mysql.table.dml;

import org.banish.sql.core.entity.AbstractEntity;
import org.banish.sql.core.orm.EntityMeta;

/**
 * @author YY
 *
 */
public class SplitSql<T extends AbstractEntity> implements ISql<T> {

	public final String TABLE_NAME;
	public final String SELECT_ALL;
	public final String INSERT;
	public final String UPDATE;
	public final String DELETE;
	public final String COUNT_ALL;
	
	public SplitSql(EntityMeta<T> entityMeta, String tableName) {
		this.TABLE_NAME = tableName;
		
//		SELECT = select(entityMeta);
		SELECT_ALL = ISql.buildSelectAll(TABLE_NAME);
		INSERT = ISql.buildInsertSql(entityMeta, TABLE_NAME);
		UPDATE = ISql.buildUpdateSql(entityMeta, TABLE_NAME);
		DELETE = ISql.buildDeleteSql(entityMeta, TABLE_NAME);
//		DELETE_IDS = deleteIds(entityMeta);
//		DELETE_ALL = deleteAll(entityMeta);
		COUNT_ALL = ISql.buildCountAll(TABLE_NAME);
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

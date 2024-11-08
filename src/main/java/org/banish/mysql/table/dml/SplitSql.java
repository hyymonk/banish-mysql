/**
 * 
 */
package org.banish.mysql.table.dml;

import org.banish.mysql.AbstractEntity;
import org.banish.mysql.orm.EntityMeta;

/**
 * @author YY
 *
 */
public class SplitSql<T extends AbstractEntity> implements ISql<T> {

	public final String TABLE_NAME;
	public final String INSERT;
	public final String UPDATE;
	public final String DELETE;
	
	public SplitSql(EntityMeta<T> entityMeta, String tableName) {
		this.TABLE_NAME = tableName;
		
//		SELECT = select(entityMeta);
//		SELECT_ALL = selectAll(entityMeta);
		INSERT = ISql.buildInsertSql(entityMeta, TABLE_NAME);
		UPDATE = ISql.buildUpdateSql(entityMeta, TABLE_NAME);
		DELETE = ISql.buildDeleteSql(entityMeta, TABLE_NAME);
//		DELETE_IDS = deleteIds(entityMeta);
//		DELETE_ALL = deleteAll(entityMeta);
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

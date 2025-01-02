/**
 * 
 */
package org.banish.sql.postgresql.sql;

import org.banish.sql.core.entity.AbstractEntity;
import org.banish.sql.core.orm.EntityMeta;
import org.banish.sql.core.sql.SplitDML;

/**
 * @author YY
 *
 */
public class PostgreSqlSplitDML<T extends AbstractEntity> extends SplitDML<T> {

	
	public PostgreSqlSplitDML(EntityMeta<T> entityMeta, String tableName) {
		super(entityMeta, tableName);
	}

	@Override
	protected String dot() {
		return "\"";
	}
}

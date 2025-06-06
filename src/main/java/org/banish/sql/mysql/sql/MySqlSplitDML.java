/**
 * 
 */
package org.banish.sql.mysql.sql;

import org.banish.sql.core.entity.AbstractEntity;
import org.banish.sql.core.orm.EntityMeta;
import org.banish.sql.core.sql.SplitDML;

/**
 * @author YY
 *
 */
public class MySqlSplitDML<T extends AbstractEntity> extends SplitDML<T> {

	public MySqlSplitDML(EntityMeta<T> entityMeta, String tableName) {
		super(entityMeta, tableName, "`");
	}
}

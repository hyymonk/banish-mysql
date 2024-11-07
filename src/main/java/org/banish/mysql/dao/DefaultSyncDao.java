/**
 * 
 */
package org.banish.mysql.dao;

import org.banish.mysql.database.IDataSource;
import org.banish.mysql.orm.AbstractEntity;
import org.banish.mysql.orm.EntityMeta;

/**
 * @author YY
 *
 */
public class DefaultSyncDao<T extends AbstractEntity> extends DefaultBaseDao<T> {

	public DefaultSyncDao(IDataSource dataSource, EntityMeta<T> entityMeta) {
		super(dataSource, entityMeta);
	}
}

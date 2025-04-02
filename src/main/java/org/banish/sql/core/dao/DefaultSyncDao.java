/**
 * 
 */
package org.banish.sql.core.dao;

import org.banish.sql.core.IIDIniter;
import org.banish.sql.core.datasource.IDataSource;
import org.banish.sql.core.entity.AbstractEntity;
import org.banish.sql.core.orm.EntityMeta;

/**
 * @author YY
 *
 */
public class DefaultSyncDao<T extends AbstractEntity> extends DefaultBaseDao<T> {

	public DefaultSyncDao(IDataSource dataSource, EntityMeta<T> entityMeta, IIDIniter idIniter) {
		super(dataSource, entityMeta, idIniter);
	}
}

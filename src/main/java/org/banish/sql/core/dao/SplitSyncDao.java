/**
 * 
 */
package org.banish.sql.core.dao;

import org.banish.sql.core.datasource.IDataSource;
import org.banish.sql.core.entity.AbstractEntity;
import org.banish.sql.core.orm.SplitEntityMeta;

/**
 * @author YY
 *
 */
public class SplitSyncDao<T extends AbstractEntity> extends SplitBaseDao<T> {
	
	public SplitSyncDao(IDataSource dataSource, SplitEntityMeta<T> entityMeta) {
		super(dataSource, entityMeta);
	}
}

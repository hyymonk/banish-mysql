/**
 * 
 */
package org.banish.mysql.dao;

import org.banish.mysql.AbstractEntity;
import org.banish.mysql.database.IDataSource;
import org.banish.mysql.orm.SplitEntityMeta;

/**
 * @author YY
 *
 */
public class SplitSyncDao<T extends AbstractEntity> extends SplitBaseDao<T> {
	
	public SplitSyncDao(IDataSource dataSource, SplitEntityMeta<T> entityMeta) {
		super(dataSource, entityMeta);
	}
}

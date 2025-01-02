/**
 * 
 */
package org.banish.sql.core.orm;

import org.banish.sql.core.annotation.Table;
import org.banish.sql.core.entity.AbstractEntity;
import org.banish.sql.core.orm.tableinfo.DefaultTableInfo;

/**
 * @author YY
 *
 */
public class DefaultEntityMeta<T extends AbstractEntity> extends EntityMeta<T> {

	public DefaultEntityMeta(Class<T> clazz, IOrmFactory metaFactory) {
		super(clazz, new DefaultTableInfo(clazz.getAnnotation(Table.class)), metaFactory);
	}
}

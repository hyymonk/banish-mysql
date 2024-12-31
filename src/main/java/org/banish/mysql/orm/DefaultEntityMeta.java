/**
 * 
 */
package org.banish.mysql.orm;

import org.banish.base.IMetaFactory;
import org.banish.mysql.AbstractEntity;
import org.banish.mysql.annotation.Table;
import org.banish.mysql.orm.table.DefaultTableInfo;

/**
 * @author YY
 *
 */
public class DefaultEntityMeta<T extends AbstractEntity> extends EntityMeta<T> {

	public DefaultEntityMeta(Class<T> clazz, IMetaFactory metaFactory) {
		super(clazz, new DefaultTableInfo(clazz.getAnnotation(Table.class)), metaFactory);
	}
}

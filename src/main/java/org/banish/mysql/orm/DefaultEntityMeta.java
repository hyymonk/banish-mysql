/**
 * 
 */
package org.banish.mysql.orm;

import org.banish.mysql.annotation.Table;
import org.banish.mysql.orm.table.TableInfo;

/**
 * @author YY
 *
 */
public class DefaultEntityMeta<T> extends EntityMeta<T> {

	public DefaultEntityMeta(Class<T> clazz) {
		super(clazz, new TableInfo(clazz.getAnnotation(Table.class)));
	}
}

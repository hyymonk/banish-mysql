/**
 * 
 */
package org.banish.base;

import java.lang.reflect.Field;

import org.banish.mysql.orm.ColumnMeta;

/**
 * @author YY
 */
public interface IMetaFactory {

	ColumnMeta newColumnMeta(Field field);
	
	IPrimaryKeyColumnMeta newPrimaryKeyColumnMeta(Field field);
}

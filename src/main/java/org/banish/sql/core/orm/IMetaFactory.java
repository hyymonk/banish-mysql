/**
 * 
 */
package org.banish.sql.core.orm;

import java.lang.reflect.Field;

/**
 * @author YY
 */
public interface IMetaFactory {

	ColumnMeta newColumnMeta(Field field);
	
	IPrimaryKeyColumnMeta newPrimaryKeyColumnMeta(Field field);
}

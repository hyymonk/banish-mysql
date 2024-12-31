/**
 * 
 */
package org.banish.sql.core.orm;

import java.lang.reflect.Field;
import java.sql.ResultSet;

import org.banish.sql.core.annotation.Id.Strategy;

/**
 * @author YY
 */
public interface IPrimaryKeyColumnMeta {
	Strategy getStrategy();
	long getBase();
	void fillValue(Object t, int columnIndex, ResultSet rs) throws Exception;
	String getColumnName();
	String getFieldName();
	Field getField();
}

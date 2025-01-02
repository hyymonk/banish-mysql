/**
 * 
 */
package org.banish.sql.postgresql.ormcolumn;

import java.lang.reflect.Field;
import java.sql.ResultSet;

import org.banish.sql.core.annotation.Column;
import org.banish.sql.core.orm.ColumnMeta;

/**
 * @author YY
 *
 */
public class PLongColumnMeta extends ColumnMeta {
	
	public PLongColumnMeta(Field field) {
		super(field);
	}

	@Override
	public Object takeValue(Object t) throws Exception {
		return field.getLong(t);
	}

	@Override
	public void fillValue(Object t, int columnIndex, ResultSet rs) throws Exception {
		field.set(t, rs.getLong(columnIndex));
	}

	@Override
	public String dbColumnType() {
		return "int8";
	}
	
	@Override
	public String defaultValue() {
		return "DEFAULT 0";
	}
	
	@Override
	public boolean isChange(String dbColumnType, String dbColumnExtra) {
		return !dbColumnType.startsWith("int8");
	}
	
	public static ColumnMeta newLongColumnMeta(Field field) {
		Column column = field.getAnnotation(Column.class);
		boolean isTime = false;
		if(column != null && column.extra().length >= 1) {
			isTime = "time".equals(column.extra()[0]);
		}
		if(isTime) {
			return new PLongTimeColumnMeta(field);
		} else {
			return new PLongColumnMeta(field);
		}
	}
}

/**
 * 
 */
package org.banish.mysql.orm.column;

import java.lang.reflect.Field;
import java.sql.ResultSet;

import org.banish.mysql.annotation.Column;
import org.banish.mysql.orm.ColumnMeta;

/**
 * @author YY
 *
 */
public class MLongColumnMeta extends ColumnMeta {
	
	protected MLongColumnMeta(Field field) {
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
		return "bigint";
	}
	
	@Override
	public String defaultValue() {
		return "DEFAULT 0";
	}
	
	@Override
	public boolean isChange(String dbColumnType, String dbColumnExtra) {
		return !dbColumnType.startsWith("bigint");
	}
	
	public static ColumnMeta newLongColumnMeta(Field field) {
		Column column = field.getAnnotation(Column.class);
		boolean isTime = false;
		if(column != null && column.extra().length >= 1) {
			isTime = "time".equals(column.extra()[0]);
		}
		if(isTime) {
			return new MLongTimeColumnMeta(field);
		} else {
			return new MLongColumnMeta(field);
		}
	}
}

/**
 * 
 */
package org.banish.postgresql.orm.column;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.ResultSet;

import org.banish.mysql.annotation.Column;
import org.banish.mysql.orm.ColumnMeta;

/**
 * @author YY
 *
 */
public class PEnumColumnMeta extends ColumnMeta {

	private final int length;
	private Method method;
	
	protected PEnumColumnMeta(Field field) {
		super(field);
		Column column = field.getAnnotation(Column.class);
		if(column != null) {
			this.length = column.length();
		} else {
			this.length = 255;
		}
		Class<?> clazz = field.getType();
		try {
			method = clazz.getMethod("valueOf", String.class);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public Object takeValue(Object t) throws Exception {
		return field.get(t).toString();
	}

	@Override
	public void fillValue(Object t, int columnIndex, ResultSet rs) throws Exception {
		String value = rs.getString(columnIndex);
		if(value != null) {
			field.set(t, method.invoke(null, value));
		}
	}

	@Override
	public String dbColumnType() {
		return "varchar(" + length + ")";
	}

	@Override
	public String defaultValue() {
		return "DEFAULT ''";
	}

	@Override
	public boolean isChange(String dbColumnType, String dbColumnExtra) {
		String typeStr = "varchar(" + length + ")";
		return !dbColumnType.contains(typeStr);
	}

}

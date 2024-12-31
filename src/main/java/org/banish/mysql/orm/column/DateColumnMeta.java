/**
 * 
 */
package org.banish.mysql.orm.column;

import java.lang.reflect.Field;
import java.sql.ResultSet;

/**
 * @author YY
 *
 */
public class DateColumnMeta extends ColumnMeta {
	
	protected DateColumnMeta(Field field) {
		super(field);
	}

	@Override
	public Object takeValue(Object t) throws Exception {
		return field.get(t);
	}

	@Override
	public void fillValue(Object t, int columnIndex, ResultSet rs) throws Exception {
		field.set(t, rs.getTimestamp(columnIndex));
	}
	
	@Override
	public String dbColumnType() {
		return "datetime";
	}
	
	@Override
	public String defaultValue() {
		return "DEFAULT NULL";
	}
	
	@Override
	public boolean isChange(String dbColumnType, String dbColumnExtra) {
		return !dbColumnType.startsWith("datetime");
	}
}

/**
 * 
 */
package org.banish.mysql.orm.column;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.Timestamp;

/**
 * @author YY
 *
 */
public class LocalDateTimeColumnMeta extends ColumnMeta {

	protected LocalDateTimeColumnMeta(Field field) {
		super(field);
	}

	@Override
	public Object takeValue(Object t) throws Exception {
		return field.get(t);
	}

	@Override
	public void fillValue(Object t, int columnIndex, ResultSet rs) throws Exception {
		Timestamp timestamp = rs.getTimestamp(columnIndex);
		if(timestamp != null) {
			field.set(t, timestamp.toLocalDateTime());
		}
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
		if(!dbColumnType.startsWith("datetime")) {
			return true;
		}
		return false;
	}
}

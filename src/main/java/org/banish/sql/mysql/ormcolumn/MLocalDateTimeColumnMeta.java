/**
 * 
 */
package org.banish.sql.mysql.ormcolumn;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.Timestamp;

import org.banish.sql.core.orm.ColumnMeta;

/**
 * @author YY
 *
 */
public class MLocalDateTimeColumnMeta extends ColumnMeta {

	public MLocalDateTimeColumnMeta(Field field) {
		super(field, true);
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
		return !dbColumnType.startsWith("datetime");
	}
}

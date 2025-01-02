/**
 * 
 */
package org.banish.sql.postgresql.ormcolumn;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.Timestamp;

import org.banish.sql.core.orm.ColumnMeta;

/**
 * @author YY
 *
 */
public class PLocalDateTimeColumnMeta extends ColumnMeta {

	public PLocalDateTimeColumnMeta(Field field) {
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
		return "timestamp";
	}
	
	@Override
	public String defaultValue() {
		return "";
	}
	
	@Override
	public boolean isChange(String dbColumnType, String dbColumnExtra) {
		return !dbColumnType.startsWith("timestamp");
	}
}

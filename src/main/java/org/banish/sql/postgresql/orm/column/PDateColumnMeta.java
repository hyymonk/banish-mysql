/**
 * 
 */
package org.banish.sql.postgresql.orm.column;

import java.lang.reflect.Field;
import java.sql.ResultSet;

import org.banish.sql.core.orm.ColumnMeta;

/**
 * @author YY
 *
 */
public class PDateColumnMeta extends ColumnMeta {
	
	protected PDateColumnMeta(Field field) {
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

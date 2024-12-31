/**
 * 
 */
package org.banish.sql.mysql.orm.column;

import java.lang.reflect.Field;
import java.sql.ResultSet;

import org.banish.sql.core.orm.ColumnMeta;

/**
 * @author YY
 *
 */
public class MDateColumnMeta extends ColumnMeta {
	
	protected MDateColumnMeta(Field field) {
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

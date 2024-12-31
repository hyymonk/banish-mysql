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
public class PIntegerColumnMeta extends ColumnMeta {
	
	protected PIntegerColumnMeta(Field field) {
		super(field);
	}

	@Override
	public Object takeValue(Object t) throws Exception {
		return field.get(t);
	}

	@Override
	public void fillValue(Object t, int columnIndex, ResultSet rs) throws Exception {
		field.set(t, rs.getInt(columnIndex));
	}
	
	@Override
	public String dbColumnType() {
		return "int4";
	}
	
	@Override
	public String defaultValue() {
		return "DEFAULT 0";
	}
	
	@Override
	public boolean isChange(String dbColumnType, String dbColumnExtra) {
		return !dbColumnType.startsWith("int4");
	}
}

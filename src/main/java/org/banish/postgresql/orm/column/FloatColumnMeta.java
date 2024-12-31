/**
 * 
 */
package org.banish.postgresql.orm.column;

import java.lang.reflect.Field;
import java.sql.ResultSet;

import org.banish.mysql.orm.column.ColumnMeta;

/**
 * @author YY
 *
 */
public class FloatColumnMeta extends ColumnMeta {
	
	protected FloatColumnMeta(Field field) {
		super(field);
	}

	@Override
	public Object takeValue(Object t) throws Exception {
		return field.getFloat(t);
	}

	@Override
	public void fillValue(Object t, int columnIndex, ResultSet rs) throws Exception {
		field.set(t, rs.getFloat(columnIndex));
	}
	
	@Override
	public String dbColumnType() {
		return "float4";
	}
	
	@Override
	public String defaultValue() {
		return "DEFAULT 0";
	}
	
	@Override
	public boolean isChange(String dbColumnType, String dbColumnExtra) {
		return !dbColumnType.startsWith("float4");
	}
}

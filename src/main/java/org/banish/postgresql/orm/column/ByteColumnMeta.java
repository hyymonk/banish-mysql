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
public class ByteColumnMeta extends ColumnMeta {
	
	protected ByteColumnMeta(Field field) {
		super(field);
	}

	@Override
	public Object takeValue(Object t) throws Exception {
		return field.getByte(t);
	}

	@Override
	public void fillValue(Object t, int columnIndex, ResultSet rs) throws Exception {
		field.set(t, rs.getByte(columnIndex));
	}
	
	@Override
	public String dbColumnType() {
		return "int2";
	}
	
	@Override
	public String defaultValue() {
		return "DEFAULT 0";
	}
	
	@Override
	public boolean isChange(String dbColumnType, String dbColumnExtra) {
		return !dbColumnType.startsWith("int2");
	}
}

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
public class IntegerColumnMeta extends ColumnMeta {
	
	protected IntegerColumnMeta(Field field) {
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
		return "int";
	}
	
	@Override
	public String defaultValue() {
		return "DEFAULT 0";
	}
	
	@Override
	public boolean isChange(String dbColumnType, String dbColumnExtra) {
		if(!dbColumnType.startsWith("int")) {
			return true;
		}
		return false;
	}
}

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
public class BooleanColumnMeta extends ColumnMeta {

	protected BooleanColumnMeta(Field field) {
		super(field);
	}

	@Override
	public Object takeValue(Object t) throws Exception {
		boolean value = field.getBoolean(t);
		if(value) {
			return "true";
		} else {
			return "false";
		}
	}

	@Override
	public void fillValue(Object t, int columnIndex, ResultSet rs) throws Exception {
		String value = rs.getString(columnIndex);
		if("true".equals(value) || "True".equals(value) || "TRUE".equals(value) || "1".equals(value)) {
			field.setBoolean(t, true);
		} else {
			field.setBoolean(t, false);
		}
	}
	
	@Override
	public String dbColumnType() {
		return "char(5)";
	}
	
	@Override
	public String defaultValue() {
		return "DEFAULT 'false'";
	}
	
	@Override
	public boolean isChange(String dbColumnType, String dbColumnExtra) {
		if(dbColumnType.startsWith("char(5)")) {
			return false;
		} else {
			return true;
		}
	}
}

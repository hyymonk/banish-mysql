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
public class ShortColumnMeta extends ColumnMeta {
	
	protected ShortColumnMeta(Field field) {
		super(field);
	}

	@Override
	public Object takeValue(Object t) throws Exception {
		return field.getShort(t);
	}

	@Override
	public void fillValue(Object t, int columnIndex, ResultSet rs) throws Exception {
		field.set(t, rs.getShort(columnIndex));
	}
	
	@Override
	public String dbColumnType() {
		return "smallint";
	}
	
	@Override
	public String defaultValue() {
		return "DEFAULT 0";
	}
	
	@Override
	public boolean isChange(String dbColumnType, String dbColumnExtra) {
		if(!dbColumnType.startsWith("smallint")) {
			return true;
		}
		return false;
	}
}

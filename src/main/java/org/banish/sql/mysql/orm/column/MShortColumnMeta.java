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
public class MShortColumnMeta extends ColumnMeta {
	
	public MShortColumnMeta(Field field) {
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
		return !dbColumnType.startsWith("smallint");
	}
}

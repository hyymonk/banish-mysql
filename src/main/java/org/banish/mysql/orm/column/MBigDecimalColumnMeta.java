/**
 * 
 */
package org.banish.mysql.orm.column;

import java.lang.reflect.Field;
import java.sql.ResultSet;

import org.banish.mysql.annotation.Column;
import org.banish.mysql.orm.ColumnMeta;

/**
 * @author YY
 *
 */
public class MBigDecimalColumnMeta extends ColumnMeta {
	
	private final String[] extra;
	
	protected MBigDecimalColumnMeta(Field field) {
		super(field);
		Column column = field.getAnnotation(Column.class);
		if(column != null) {
			this.extra = column.extra();
		} else {
			this.extra = new String[0];
		}
	}

	@Override
	public Object takeValue(Object t) throws Exception {
		return field.get(t);
	}

	@Override
	public void fillValue(Object t, int columnIndex, ResultSet rs) throws Exception {
		field.set(t, rs.getBigDecimal(columnIndex));
	}
	
	@Override
	public String dbColumnType() {
		if(extra.length != 2) {
			return "decimal(20, 2)";
		} else {
			return "decimal(" + extra[0] + ", " + extra[1] + ")";
		}
	}
	
	@Override
	public String defaultValue() {
		return "DEFAULT 0";
	}
	
	@Override
	public boolean isChange(String dbColumnType, String dbColumnExtra) {
		if(!dbColumnType.startsWith("decimal")) {
			return true;
		}
		String[] precision = dbColumnType.substring(dbColumnType.indexOf("(") + 1, dbColumnType.indexOf(")")).split(",");
		if(extra.length == 2) {
			if(extra[0].equals(precision[0].trim()) && extra[1].equals(precision[1].trim())) {
				return false;
			} else {
				return true;
			}
		} else {
			if("20".equals(precision[0].trim()) && "2".equals(precision[1].trim())) {
				return false;
			} else {
				return true;
			}
		}
	}
}

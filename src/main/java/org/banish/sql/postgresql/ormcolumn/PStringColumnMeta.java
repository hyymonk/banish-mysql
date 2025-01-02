/**
 * 
 */
package org.banish.sql.postgresql.ormcolumn;

import java.lang.reflect.Field;
import java.sql.ResultSet;

import org.banish.sql.core.annotation.Column;
import org.banish.sql.core.orm.ColumnMeta;

/**
 * @author YY
 *
 */
public class PStringColumnMeta extends ColumnMeta {
	
	private final String[] extra;
	private final int length;
	
	public PStringColumnMeta(Field field) {
		super(field);
		Column column = field.getAnnotation(Column.class);
		int length = 0;
		if(column != null) {
			this.extra = column.extra();
			length = column.length();
		} else {
			this.extra = new String[0];
		}
		if(length <= 0) {
			length = 255;
		}
		this.length = length;
	}

	@Override
	public Object takeValue(Object t) throws Exception {
		return field.get(t);
	}

	@Override
	public void fillValue(Object t, int columnIndex, ResultSet rs) throws Exception {
		field.set(t, rs.getString(columnIndex));
	}
	
	@Override
	public String dbColumnType() {
		if(extra.length == 1) {
			if("text".equals(extra[0])) {
				return "text";
			} else {
				return "varchar(" + length + ")";
			}
		} else {
			return "varchar(" + length + ")";
		}
	}
	
	@Override
	public String defaultValue() {
		if(extra.length == 1) {
			if("text".equals(extra[0])) {
				return "";
			} else {
				return "DEFAULT ''";
			}
		} else {
			return "DEFAULT ''";
		}
	}
	
	@Override
	public boolean isChange(String dbColumnType, String dbColumnExtra) {
		if(extra.length == 1) {
			if("text".equals(extra[0]) && dbColumnType.startsWith("text")) {
				return false;
			} else {
				return true;
			}
		} else {
			String typeStr = "varchar(" + length + ")";
			if(!dbColumnType.contains(typeStr)) {
				return true;
			} else {
				return false;
			}
		}
	}
}

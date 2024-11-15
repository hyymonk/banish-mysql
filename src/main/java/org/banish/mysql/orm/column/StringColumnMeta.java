/**
 * 
 */
package org.banish.mysql.orm.column;

import java.lang.reflect.Field;
import java.sql.ResultSet;

import org.banish.mysql.annotation.Column;

/**
 * @author YY
 *
 */
public class StringColumnMeta extends ColumnMeta {
	
	private final String[] extra;
	private final int length;
	
	protected StringColumnMeta(Field field) {
		super(field);
		Column column = field.getAnnotation(Column.class);
		if(column != null) {
			this.extra = column.extra();
			this.length = column.length();
		} else {
			this.extra = new String[0];
			this.length = 255;
		}
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
				// text类型，64k容量
				return "text";
			} else if("mediumtext".equals(extra[0])) {
				// mediumtext类型，16m容量
				return "mediumtext";
			} else if("longtext".equals(extra[0])) {
				// longtext类型，4g容量，认真的吗存4g的字符
				throw new RuntimeException("you want to save 4g string content to db, are you serious?");
			} else {
				throw new RuntimeException("nothing is defined to the string field!");
			}
		} else {
			return "varchar(" + length + ")";
		}
	}
	
	@Override
	public String defaultValue() {
		if(extra.length == 1) {
			if("text".equals(extra[0])) {
				// text类型，64k容量
				return "";
			} else if("mediumtext".equals(extra[0])) {
				// mediumtext类型，16m容量
				return "";
			} else if("longtext".equals(extra[0])) {
				// longtext类型，4g容量，认真的吗存4g的字符
				throw new RuntimeException("you want to save 4g string content to db, are you serious?");
			} else {
				throw new RuntimeException("nothing is defined to the string field!");
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
			} else if("mediumtext".equals(extra[0]) && dbColumnType.startsWith("mediumtext")) {
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

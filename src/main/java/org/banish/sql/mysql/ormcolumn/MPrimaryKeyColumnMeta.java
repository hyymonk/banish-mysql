/**
 * 
 */
package org.banish.sql.mysql.ormcolumn;

import java.lang.reflect.Field;
import java.sql.ResultSet;

import org.banish.sql.core.annotation.Column;
import org.banish.sql.core.annotation.Id;
import org.banish.sql.core.annotation.Id.Strategy;
import org.banish.sql.core.orm.ColumnMeta;
import org.banish.sql.core.orm.IPrimaryKeyColumnMeta;

/**
 * @author YY
 *
 */
public class MPrimaryKeyColumnMeta extends ColumnMeta implements IPrimaryKeyColumnMeta {
	/**
	 * ID策略
	 */
	private final Strategy strategy;
	private final int length;
	private final long base;
	
	public MPrimaryKeyColumnMeta(Field field) {
		super(field, false);
		Id id = field.getAnnotation(Id.class);
		this.strategy = id.strategy();
		this.base = id.autoBase();
		Column column = field.getAnnotation(Column.class);
		this.length = column.length();
	}

	public Strategy getStrategy() {
		return strategy;
	}

	@Override
	public Object takeValue(Object t) throws Exception {
		return field.get(t);
	}

	@Override
	public void fillValue(Object t, int columnIndex, ResultSet rs) throws Exception {
		if(field.getType() == int.class || field.getType() == Integer.class) {
			field.set(t, rs.getInt(columnIndex));
		} else if(field.getType() == long.class || field.getType() == Long.class) {
			field.set(t, rs.getLong(columnIndex));
		} else if(field.getType() == String.class) {
			field.set(t, rs.getString(columnIndex));
		}
	}

	@Override
	public String dbColumnType() {
		if(field.getType() == int.class || field.getType() == Integer.class) {
			return "int";
		} else if(field.getType() == long.class || field.getType() == Long.class) {
			return "bigint";
		} else if(field.getType() == String.class) {
			return "varchar(" + length + ")";
		} else {
			throw new RuntimeException(String.format("不支持类型%s作为主键类型", field.getType().getName()));
		}
	}

	@Override
	public String defaultValue() {
		return "NOT NULL";
	}

	@Override
	public boolean isChange(String dbColumnType, String dbColumnExtra) {
		if(strategy == Strategy.AUTO && !"auto_increment".equals(dbColumnExtra)) {
			return true;
		}
		if(field.getType() == int.class || field.getType() == Integer.class) {
			if(!dbColumnType.startsWith("int")) {
				return true;
			} else {
				return false;
			}
		} else if(field.getType() == long.class || field.getType() == Long.class) {
			if(!dbColumnType.startsWith("bigint")) {
				return true;
			} else {
				return false;
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

	public long getBase() {
		return base;
	}
}

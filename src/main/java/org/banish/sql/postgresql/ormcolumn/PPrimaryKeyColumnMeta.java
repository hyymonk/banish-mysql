/**
 * 
 */
package org.banish.sql.postgresql.ormcolumn;

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
public class PPrimaryKeyColumnMeta extends ColumnMeta implements IPrimaryKeyColumnMeta {
	/**
	 * ID策略
	 */
	private final Strategy strategy;
	private final int length;
	private final long base;
	
	public PPrimaryKeyColumnMeta(Field field) {
		super(field);
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
		//PostgreSql在插入具有自增主键的数据后，其返回的自增主键ResultSet不像Mysql那样只具有下标1的数据，而是包含了完整数据
		if(field.getType() == int.class || field.getType() == Integer.class) {
			field.set(t, rs.getInt(this.getColumnName()));
		} else if(field.getType() == long.class || field.getType() == Long.class) {
			field.set(t, rs.getLong(this.getColumnName()));
		} else if(field.getType() == String.class) {
			field.set(t, rs.getString(this.getColumnName()));
		}
	}

	@Override
	public String dbColumnType() {
		if(field.getType() == int.class || field.getType() == Integer.class) {
			return "int4";
		} else if(field.getType() == long.class || field.getType() == Long.class) {
			return "int8";
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
//		if(strategy == Strategy.AUTO && !"auto_increment".equals(dbColumnExtra)) {
//			return true;
//		}
		if(field.getType() == int.class || field.getType() == Integer.class) {
			if(!dbColumnType.startsWith("int4")) {
				return true;
			} else {
				return false;
			}
		} else if(field.getType() == long.class || field.getType() == Long.class) {
			if(!dbColumnType.startsWith("int8")) {
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

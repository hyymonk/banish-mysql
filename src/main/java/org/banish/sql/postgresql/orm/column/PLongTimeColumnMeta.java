/**
 * 
 */
package org.banish.sql.postgresql.orm.column;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.Timestamp;

import org.banish.sql.core.orm.ColumnMeta;

/**
 * @author YY
 */
public class PLongTimeColumnMeta extends ColumnMeta {

	protected PLongTimeColumnMeta(Field field) {
		super(field);
	}

	@Override
	public Object takeValue(Object t) throws Exception {
		return new Timestamp(field.getLong(t));
	}

	@Override
	public void fillValue(Object t, int columnIndex, ResultSet rs) throws Exception {
		Timestamp timestamp = rs.getTimestamp(columnIndex);
		if(timestamp != null) {
			//如果某个表新加了一个用长整型表示时间的字段，那么当首次从数据库加载回来的时候，会有空指针的情况
			field.set(t, timestamp.getTime());
		}
	}

	@Override
	public String dbColumnType() {
		return "timestamp";
	}

	@Override
	public String defaultValue() {
		return "";
	}

	@Override
	public boolean isChange(String dbColumnType, String dbColumnExtra) {
		return !dbColumnType.startsWith("timestamp");
	}

}

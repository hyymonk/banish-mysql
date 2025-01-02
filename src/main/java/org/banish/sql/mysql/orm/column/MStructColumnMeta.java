/**
 * 
 */
package org.banish.sql.mysql.orm.column;

import java.lang.reflect.Field;
import java.sql.ResultSet;

import org.banish.sql.core.annotation.Column;
import org.banish.sql.core.valueformat.ValueFormatters;
import org.banish.sql.core.valueformat.ValueFormatters.ValueFormatter;


/**
 * @author YY
 *
 */
public class MStructColumnMeta extends MStringColumnMeta {

	private final String formatter;
	
	public MStructColumnMeta(Field field) {
		super(field);
		Column column = field.getAnnotation(Column.class);
		if("".equals(column.formatter())) {
			this.formatter = "json";
		} else {
			this.formatter = column.formatter();
		}
	}

	@Override
	public Object takeValue(Object t) throws Exception {
		ValueFormatter valueFormatter = ValueFormatters.getFormatter(formatter);
		return valueFormatter.encode(field.get(t));
	}

	@Override
	public void fillValue(Object t, int columnIndex, ResultSet rs) throws Exception {
		String value = rs.getString(columnIndex);
		ValueFormatter valueFormatter = ValueFormatters.getFormatter(formatter);
		Object v = valueFormatter.decode(field, value);
		field.set(t, v);
	}
}

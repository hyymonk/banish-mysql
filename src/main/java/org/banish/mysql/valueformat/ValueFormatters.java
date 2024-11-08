/**
 * 
 */
package org.banish.mysql.valueformat;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author YY
 *
 */
public class ValueFormatters {

	private static final Map<String, ValueFormatter> VALUE_FORMATERS = new HashMap<>();
	
	public static void addFormater(List<ValueFormatter> valueFormaters) {
		for(ValueFormatter formater : valueFormaters) {
			VALUE_FORMATERS.put(formater.formatter(), formater);
		}
	}
	
	public static interface ValueFormatter {
		String formatter();
		
		Object decode(Field field, String value);
		
		String encode(Object object);
	}
	
	public static ValueFormatter getFormatter(String formatter) {
		return VALUE_FORMATERS.get(formatter);
	}
}

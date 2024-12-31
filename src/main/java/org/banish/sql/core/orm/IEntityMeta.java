/**
 * 
 */
package org.banish.sql.core.orm;

import java.util.List;

/**
 * @author YY
 *
 */
public interface IEntityMeta<T> {

	T newInstance() throws Exception;

	ColumnMeta getColumnMeta(String columnName);

	List<ColumnMeta> getColumnList();
	
	public static String makeSnakeCase(String str) {
		String snake = "";
		for(int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if(Character.isUpperCase(c)) {
				if(snake.equals("")) {
					snake += c;
				} else {
					snake += "_" + c;
				}
			} else {
				snake += c;
			}
		}
		snake = snake.toLowerCase();
		return snake;
	}
	
	public static void main(String[] args) {
		System.out.println(makeSnakeCase("AAAA"));
		System.out.println(makeSnakeCase("aaaAaaAwwAssA"));
	}
}

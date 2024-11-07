/**
 * 
 */
package org.banish.mysql.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author YY
 * 父类注解
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MappedSuperclass {
	/**
	 * 从大到小排序
	 * @return
	 */
	Priority sort() default Priority.NORMAL;
	
	public static enum Priority {
		FIRST(1),
		
		_2(2),
		
		_3(3),
		
		_4(4),
		
		NORMAL(5),
		
		_6(6),
		
		_7(7),
		
		_8(8),
		
		_9(9),
		
		LAST(10),
		
		;
		private final int value;
		private Priority(int value) {
			this.value = value;
		}
		public int getValue() {
			return value;
		}
	}
}

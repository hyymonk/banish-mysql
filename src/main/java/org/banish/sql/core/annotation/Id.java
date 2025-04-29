/**
 * 
 */
package org.banish.sql.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author YY
 * 
 * 数据库ID属性
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Id {
	/**
	 * ID生成策略
	 * @return
	 */
	Strategy strategy();
	/**
	 * 当ID策略为auto时的自增基数
	 * @return
	 */
	long autoBase() default 1000000000L;
	
	public enum Strategy {
		/**
		 * 自定义ID
		 */
		IDENTITY,
		/**
		 * 自增长ID
		 */
		AUTO
	}
}

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
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {
	/**
	 * 字段名字
	 * @return
	 */
	String name() default "";
	/**
	 * 字段长度，仅对String生效
	 * @return
	 */
	int length() default 255;
	/**
	 * 字段注释
	 * @return
	 */
	String comment();
	/**
	 * 格式化方式，只对数据库中的字符字段可用
	 * @return
	 */
	String formatter() default "";
	/**
	 * 扩展数据
	 * long类型可扩展extra="time"
	 * float类型可扩展extra={"20","2"}
	 * double类型可扩展extra={"20","2"}
	 * BigDecimal类型可扩展extra={"20","2"}
	 * String类型可扩展extra="text"、extra="mediumtext"
	 * @return
	 */
	String[] extra() default {};
	/**
	 * 是否只读，在插入数据后将不再通过update语句修改该字段
	 * @return
	 */
	boolean readonly() default false;
}

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
	 * length=-1时将使用text类型
	 * 可拓展-3是longtext之类的
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
	 * @return
	 */
	String[] extra() default {};
	/**
	 * 是否只读，在插入数据后将不再能修改
	 * @return
	 */
	boolean readonly() default false;
	/**
	 * 默认值
	 * @return
	 */
	String defaultValue() default "";
}

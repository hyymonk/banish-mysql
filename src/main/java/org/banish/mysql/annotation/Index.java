/**
 * 
 */
package org.banish.mysql.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.banish.mysql.annotation.enuma.IndexType;
import org.banish.mysql.annotation.enuma.IndexWay;

/**
 * @author YY
 * 
 * 数据库索引属性
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Index {
	/**
	 * 索引名字
	 * 
	 * @return
	 */
	public abstract String name();
	/**
	 * 索引使用到的类字段名（不是数据库的列名）
	 * 
	 * @return
	 */
	public abstract String[] fields();
	/**
	 * 索引的类型
	 * 
	 * @return
	 */
	public abstract IndexType type() default IndexType.NORMAL;
	/**
	 * 索引的方式
	 * 
	 * @return
	 */
	public abstract IndexWay way() default IndexWay.BTREE;
}

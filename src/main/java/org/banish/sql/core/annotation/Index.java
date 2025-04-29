/**
 * 
 */
package org.banish.sql.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.banish.sql.core.annotation.enuma.IndexType;
import org.banish.sql.core.annotation.enuma.IndexWay;

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
	 * 索引别名
	 * 
	 * @return
	 */
	String alias() default "";
	/**
	 * 索引使用到的类字段名（不是数据库的列名）
	 * 
	 * @return
	 */
	String[] fields();
	/**
	 * 索引的类型
	 * 
	 * @return
	 */
	IndexType type() default IndexType.NORMAL;
	/**
	 * 索引的方式
	 * 
	 * @return
	 */
	IndexWay way() default IndexWay.BTREE;
}

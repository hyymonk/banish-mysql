/**
 * 
 */
package org.banish.sql.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.banish.sql.core.annotation.enuma.AsyncType;
import org.banish.sql.core.annotation.enuma.Charset;
import org.banish.sql.core.annotation.enuma.UpdateType;


/**
 * @author YY
 * 普通的数据表
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Table {
	/**
	 * 表名
	 * @return
	 */
	public String name() default "";
	/**
	 * 表注释
	 * @return
	 */
	public String comment();
	/**
	 * 数据库的别名
	 * @return
	 */
	public String dbAlias();
	/**
	 * 表索引
	 * @return
	 */
	public Index[] indexs() default {};
	/**
	 * 数据表的字符集
	 * @return
	 */
	public Charset charset() default Charset.UTF8MB4;
	/**
	 * 数据入库的方式，实时还是异步
	 * @return
	 */
	public AsyncType asyncType() default AsyncType.NONE;
	/**
	 * 进行异步批量处理的数量，根据对象的大小可以调整批量数，越小的对象，批量数可以越大
	 * @return
	 */
	public int asyncSize() default 500;
	/**
	 * 异步延迟入库的时间，单位秒
	 * @return
	 */
	public int asyncDelay() default 60;
	/**
	 * 是否自动建表
	 * @return
	 */
	public boolean autoBuild() default true;
	/**
	 * 更新方式
	 * @return
	 */
	public UpdateType updateType() default UpdateType.UPDATE;
}

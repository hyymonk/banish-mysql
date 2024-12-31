/**
 * 
 */
package org.banish.sql.core.orm.tableinfo;

import org.banish.sql.core.annotation.Index;
import org.banish.sql.core.annotation.enuma.AsyncType;
import org.banish.sql.core.annotation.enuma.Charset;

/**
 * @author YY
 *
 */
public interface ITable {
	/**
	 * 表名
	 * @return
	 */
	String name();
	/**
	 * 表注释
	 * @return
	 */
	String comment();
	/**
	 * 数据库的别名
	 * @return
	 */
	String dbAlias();
	/**
	 * 表索引
	 * @return
	 */
	Index[] indexs();
	/**
	 * 数据表的字符集
	 * @return
	 */
	Charset charset();
	/**
	 * 数据入库的方式，实时还是异步
	 * @return
	 */
	AsyncType asyncType();
	/**
	 * 进行异步批量处理的数量，根据对象的大小可以调整批量数，越小的对象，批量数可以越大
	 * @return
	 */
	int asyncSize();
	/**
	 * 异步延迟入库的时间，单位秒
	 * @return
	 */
	int asyncDelay();
	/**
	 * 是否自动建表
	 * @return
	 */
	boolean autoBuild();
}

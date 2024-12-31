/**
 * 
 */
package org.banish.sql.core.annotation.enuma;

/**
 * @author YY
 * 异步方式，
 */
public enum AsyncType {
	/**
	 * 不进行异步处理
	 */
	NONE,
	/**
	 * 对更新做异步
	 */
	UPDATE,
	/**
	 * 对更新和插入做异步，异步插入包含异步更新，原因是能够进行异步插入的数据，其数据更新的时效性也不会要求实时
	 */
	INSERT,
}

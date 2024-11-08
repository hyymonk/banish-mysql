/**
 * 
 */
package org.banish.mysql.annotation.enuma;

/**
 * @author YY
 * 分表方式
 */
public enum SplitWay {
	NULL,
	/**
	 * 每分钟进行分表，多用于测试
	 */
	MINUTE,
	/**
	 * 每小时进行分表
	 */
	HOUR,
	/**
	 * 每天时进行分表
	 */
	DAY,
	/**
	 * 每周时进行分表
	 */
	WEEK,
	/**
	 * 每月时进行分表
	 */
	MONTH,
	/**
	 * 每年时进行分表
	 */
	YEAR,
	/**
	 * 根据该字段的值进行分表
	 */
	VALUE,
}

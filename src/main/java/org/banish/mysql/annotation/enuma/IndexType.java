/**
 * 
 */
package org.banish.mysql.annotation.enuma;

/**
 * @author YY
 * 索引类型
 */
public enum IndexType {
	/**
	 * 普通索引
	 */
	NORMAL(""),
	/**
	 * 唯一索引
	 */
	UNIQUE("UNIQUE"),
	;
	
	private String v;
	
	private IndexType(String value) {
		this.v = value;
	}
	public String value() {
		return this.v;
	}
}

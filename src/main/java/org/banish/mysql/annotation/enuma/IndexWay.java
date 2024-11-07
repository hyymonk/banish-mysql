/**
 * 
 */
package org.banish.mysql.annotation.enuma;

/**
 * @author YY
 * 索引方式
 */
public enum IndexWay {
	BTREE("BTREE"),
	HASH("HASH");
	
	private final String value;
	
	private IndexWay(String value) {
		this.value = value;
	}
	public String value() {
		return this.value;
	}
}

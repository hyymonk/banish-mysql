/**
 * 
 */
package org.banish.mysql.annotation.enuma;

/**
 * @author YY
 *
 */
public enum Charset {
	UTF8("utf8"),
	UTF8MB4("utf8mb4"),
	;
	
	private final String value;
	
	private Charset(String value) {
		this.value = value;
	}
	public String value() {
		return value;
	}
}

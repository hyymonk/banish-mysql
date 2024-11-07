/**
 * 
 */
package org.banish.mysql.table.dml;

/**
 * @author YY
 *
 */
public interface ISql<T> {
	
	public String insert();
	
	public String update();
	
	public String delete();
}

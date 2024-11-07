/**
 * 
 */
package org.banish.mysql.orm;

import org.banish.mysql.annotation.enuma.AsyncType;

/**
 * @author YY
 *
 */
public interface IAsyncEntityMeta {
	
	public AsyncType getAsyncType();

	public int getAsyncSize();

	public int getAsyncDelay();
	
	public String getAsyncName();
}

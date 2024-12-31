/**
 * 
 */
package org.banish.sql.core.orm;

import org.banish.sql.core.annotation.enuma.AsyncType;

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

/**
 * 
 */
package org.banish.mysql.orm;

import org.banish.mysql.annotation.SplitTable;
import org.banish.mysql.annotation.enuma.AsyncType;
import org.banish.mysql.orm.table.SplitTableInfo;

/**
 * @author YY
 *
 */
public class SplitAsyncEntityMeta<T> extends SplitEntityMeta<T> implements IAsyncEntityMeta {

	private final AsyncType asyncType;
	private final int asyncSize;
	private final int asyncDelay;
	private final String asyncName;
	
	public SplitAsyncEntityMeta(Class<T> clazz) {
		super(clazz);
		SplitTableInfo table = new SplitTableInfo(clazz.getAnnotation(SplitTable.class));
		this.asyncType = table.asyncType();
		this.asyncSize = table.asyncSize();
		this.asyncDelay = table.asyncDelay();
		this.asyncName = table.name();
	}

	@Override
	public AsyncType getAsyncType() {
		return asyncType;
	}

	@Override
	public int getAsyncSize() {
		return asyncSize;
	}

	@Override
	public int getAsyncDelay() {
		return asyncDelay;
	}

	@Override
	public String getAsyncName() {
		return asyncName;
	}
}

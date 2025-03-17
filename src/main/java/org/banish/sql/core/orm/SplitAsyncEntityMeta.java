/**
 * 
 */
package org.banish.sql.core.orm;

import org.banish.sql.core.annotation.SplitTable;
import org.banish.sql.core.annotation.enuma.AsyncType;
import org.banish.sql.core.entity.AbstractEntity;
import org.banish.sql.core.orm.tableinfo.SplitTableInfo;

/**
 * @author YY
 *
 */
public class SplitAsyncEntityMeta<T extends AbstractEntity> extends SplitEntityMeta<T> implements IAsyncEntityMeta {

	private final AsyncType asyncType;
	private final int asyncSize;
	private final int asyncDelay;
	private final String asyncName;
	
	public SplitAsyncEntityMeta(Class<T> clazz, IOrmFactory metaFactory) {
		super(clazz, metaFactory);
		SplitTableInfo table = new SplitTableInfo(clazz.getAnnotation(SplitTable.class));
		this.asyncType = table.asyncType();
		this.asyncSize = table.asyncSize();
		this.asyncDelay = table.asyncDelay();
		this.asyncName = this.getTableName();
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

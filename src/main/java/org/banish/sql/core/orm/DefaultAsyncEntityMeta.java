/**
 * 
 */
package org.banish.sql.core.orm;

import org.banish.sql.core.annotation.Table;
import org.banish.sql.core.annotation.enuma.AsyncType;
import org.banish.sql.core.annotation.enuma.UpdateType;
import org.banish.sql.core.entity.AbstractEntity;
import org.banish.sql.core.orm.tableinfo.DefaultTableInfo;

/**
 * @author YY
 * 具有异步特性的元数据信息
 */
public class DefaultAsyncEntityMeta<T extends AbstractEntity> extends DefaultEntityMeta<T> implements IAsyncEntityMeta {
	
	private final AsyncType asyncType;
	private final int asyncSize;
	private final int asyncDelay;
	private final String asyncName;
	private final UpdateType updateType;
	
	public DefaultAsyncEntityMeta(Class<T> clazz, IMetaFactory metaFactory) {
		super(clazz, metaFactory);
		DefaultTableInfo table = new DefaultTableInfo(clazz.getAnnotation(Table.class));
		this.asyncType = table.asyncType();
		this.asyncSize = table.asyncSize();
		this.asyncDelay = table.asyncDelay();
		this.asyncName = table.name();
		this.updateType = table.updateType();
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

	public UpdateType getUpdateType() {
		return updateType;
	}
}

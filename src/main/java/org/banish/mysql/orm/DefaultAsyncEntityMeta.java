/**
 * 
 */
package org.banish.mysql.orm;

import org.banish.mysql.AbstractEntity;
import org.banish.mysql.annotation.Table;
import org.banish.mysql.annotation.enuma.AsyncType;
import org.banish.mysql.orm.table.DefaultTableInfo;

/**
 * @author YY
 * 具有异步特性的元数据信息
 */
public class DefaultAsyncEntityMeta<T extends AbstractEntity> extends DefaultEntityMeta<T> implements IAsyncEntityMeta {
	
	private final AsyncType asyncType;
	private final int asyncSize;
	private final int asyncDelay;
	private final String asyncName;
	
	public DefaultAsyncEntityMeta(Class<T> clazz) {
		super(clazz);
		DefaultTableInfo table = new DefaultTableInfo(clazz.getAnnotation(Table.class));
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

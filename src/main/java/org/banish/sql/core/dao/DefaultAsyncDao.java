/**
 * 
 */
package org.banish.sql.core.dao;

import java.util.List;

import org.banish.sql.core.IIDIniter;
import org.banish.sql.core.annotation.enuma.UpdateType;
import org.banish.sql.core.datasource.IDataSource;
import org.banish.sql.core.entity.AbstractEntity;
import org.banish.sql.core.orm.ColumnMeta;
import org.banish.sql.core.orm.DefaultAsyncEntityMeta;
import org.banish.sql.core.orm.IAsyncEntityMeta;

/**
 * @author YY
 *
 */
public class DefaultAsyncDao<T extends AbstractEntity> extends DefaultBaseDao<T> implements IAsyncDao<T> {
	
	private IAsyncEntityMeta asyncMeta;
	
	private AsyncDaoPlugin<T> asyncDaoPlugin;
	
	public DefaultAsyncDao(IDataSource dataSource, DefaultAsyncEntityMeta<T> entityMeta, IIDIniter idIniter) {
		super(dataSource, entityMeta, idIniter);
		this.asyncMeta = entityMeta;
		if(entityMeta.getUpdateType() == UpdateType.UPDATE) {
			this.asyncDaoPlugin = new AsyncDaoPlugin<T>(this);
		} else {
			this.asyncDaoPlugin = new AsyncDaoFastPlugin<T>(this);
		}
	}

	@Override
	public void insertNow(T t) {
		super.insert(t);
	}

	@Override
	public void insertAllNow(List<T> ts) {
		super.insertAll(ts);
	}
	
	@Override
	public void updateNow(T t) {
		super.update(t);
	}

	@Override
	public void updateAllNow(List<T> ts) {
		super.updateAll(ts);
	}

	@Override
	public IAsyncEntityMeta getAsyncMeta() {
		return asyncMeta;
	}

	@Override
	public AsyncDaoPlugin<T> getAsyncPlugin() {
		return asyncDaoPlugin;
	}
	
	@Override
	public void insert(T t) {
		getAsyncPlugin().insert(t);
	}
	
	@Override
	public void insertAll(List<T> ts) {
		getAsyncPlugin().insertAll(ts);
	}
	
	@Override
	public void update(T t) {
		getAsyncPlugin().update(t);
	}
	
	@Override
	public void updateAll(List<T> ts) {
		getAsyncPlugin().updateAll(ts);
	}

	@Override
	public Object[] getValues(T t) {
		Object[] values = new Object[this.getEntityMeta().getColumnList().size()];
		for(int i = 0; i < this.getEntityMeta().getColumnList().size(); i++) {
			ColumnMeta columnMeta = this.getEntityMeta().getColumnList().get(i);
			try {
				values[i] = columnMeta.takeValue(t);
			} catch (Exception e) {
			}
		}
		return values;
	}

	@Override
	public void fastUpdateAllNow(List<T> ts) {
		super.insertUpdate(ts);
	}
}

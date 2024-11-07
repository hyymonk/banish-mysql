/**
 * 
 */
package org.banish.mysql.dao;

import java.util.List;

import org.banish.mysql.database.IDataSource;
import org.banish.mysql.orm.AbstractEntity;
import org.banish.mysql.orm.IAsyncEntityMeta;
import org.banish.mysql.orm.SplitAsyncEntityMeta;
import org.banish.mysql.orm.column.ColumnMeta;

/**
 * @author YY
 *
 */
public class SplitAsyncDao<T extends AbstractEntity> extends SplitBaseDao<T> implements IAsyncDao<T> {

	private IAsyncEntityMeta asyncMeta;
	
	private AsyncDaoPlugin<T> asyncDaoPlugin;
	
	public SplitAsyncDao(IDataSource dataSource, SplitAsyncEntityMeta<T> entityMeta) {
		super(dataSource, entityMeta);
		this.asyncMeta = entityMeta;
		this.asyncDaoPlugin = new AsyncDaoPlugin<T>(this);
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
}

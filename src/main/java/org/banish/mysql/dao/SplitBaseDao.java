/**
 * 
 */
package org.banish.mysql.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.banish.mysql.annotation.SplitTable.SplitWay;
import org.banish.mysql.database.IDataSource;
import org.banish.mysql.orm.AbstractEntity;
import org.banish.mysql.orm.SplitEntityMeta;
import org.banish.mysql.table.TableBuilder;
import org.banish.mysql.table.dml.ISql;
import org.banish.mysql.table.dml.SplitSql;

/**
 * @author YY
 *
 */
public abstract class SplitBaseDao<T extends AbstractEntity> extends OriginDao<T> {
	/**
	 * <表名，Sql组合>
	 */
	private Map<String, SplitSql<T>> splitSqlMap = new ConcurrentHashMap<>();
	
	private SplitEntityMeta<T> logEntityMeta;
	
	public SplitBaseDao(IDataSource dataSource, SplitEntityMeta<T> entityMeta) {
		super(dataSource, entityMeta);
		this.logEntityMeta = entityMeta;
		//创建Dao对象的时候检查当前时间对应的表
		SplitWay splitWay = entityMeta.getSplitWay();
		if(splitWay != SplitWay.VALUE) {
			String tableName = entityMeta.getLogTableName(System.currentTimeMillis());
			SplitSql<T> sql = createSql(tableName);
			splitSqlMap.put(tableName, sql);
		}
	}
	
	@Override
	protected ISql<T> getSql(T t) {
		return ensureSqlInit(t);
	}
	
	private SplitSql<T> ensureSqlInit(T t) {
		String tableName = logEntityMeta.getLogTableName(t);
		SplitSql<T> sql = splitSqlMap.get(tableName);
		if(sql == null) {
			synchronized (this) {
				sql = splitSqlMap.get(tableName);
				if(sql == null) {
					sql = createSql(tableName);
					splitSqlMap.put(tableName, sql);
				}
			}
		}
		return sql;
	}
	
	private synchronized SplitSql<T> createSql(String tableName) {
		//自动建表
		TableBuilder.build(this, tableName);
		//构建Sql对象
		SplitSql<T> sql = new SplitSql<>(this.getEntityMeta(), tableName);
		return sql;
	} 
	

	@Override
	protected void insertAllWithAutoId(List<T> ts) {
		Map<SplitSql<T>, List<T>> splitMap = splitIntoMap(ts);
		for(Entry<SplitSql<T>, List<T>> entry : splitMap.entrySet()) {
			insertAllWithAutoId(entry.getValue(), entry.getKey());
		}
	}
	
	private void insertAllWithAutoId(List<T> ts, SplitSql<T> sqlTemplate) {
		String sql = sqlTemplate.insert();
		super.insertAllWithAutoId(ts, sql);
	}
	
	private Map<SplitSql<T>, List<T>> splitIntoMap(List<T> ts) {
		Map<SplitSql<T>, List<T>> splitMap = new HashMap<>();
		for(T t : ts) {
			SplitSql<T> sql = ensureSqlInit(t);
			List<T> list = splitMap.get(sql);
			if(list == null) {
				list = new ArrayList<>();
				splitMap.put(sql, list);
			}
			list.add(t);
		}
		return splitMap;
	}

	@Override
	protected void insertAllWithIdentityId(List<T> ts) {
		Map<SplitSql<T>, List<T>> splitMap = splitIntoMap(ts);
		for(Entry<SplitSql<T>, List<T>> entry : splitMap.entrySet()) {
			insertAllWithIdentityId(entry.getValue(), entry.getKey());
		}
	}
	
	/**
	 * 插入所有具有自定义ID属性的数据
	 * @param ts 必须要求是列表，以保证返回的ID数组顺序与列表对象顺序一致
	 * @return
	 */
	private void insertAllWithIdentityId(List<T> ts, SplitSql<T> sqlTemplate) {
		String sql = sqlTemplate.insert();
		super.insertAllWithIdentityId(ts, sql);
	}

	@Override
	public void updateAll(List<T> ts) {
		Map<SplitSql<T>, List<T>> splitMap = splitIntoMap(ts);
		for(Entry<SplitSql<T>, List<T>> entry : splitMap.entrySet()) {
			updateAll(entry.getValue(), entry.getKey());
		}
	}
	
	private void updateAll(List<T> ts, SplitSql<T> sqlTemplate) {
		String sql = sqlTemplate.update();
		super.updateAll(ts, sql);
	}
	

	@Override
	public void deleteAll(List<T> ts) {
		throw new UnsupportedOperationException("不该被调用的函数");
	}

	@Override
	public void deleteWhere(String where, Object... params) {
		throw new UnsupportedOperationException("不该被调用的函数");
	}

	@Override
	public T query(Object id) {
		throw new UnsupportedOperationException("不该被调用的函数");
	}

	@Override
	public List<T> queryAll() {
		throw new UnsupportedOperationException("不该被调用的函数");
	}

	@Override
	public T queryOneWhere(String where, Object... params) {
		throw new UnsupportedOperationException("不该被调用的函数");
	}

	@Override
	public List<T> queryListWhere(String where, Object... params) {
		throw new UnsupportedOperationException("不该被调用的函数");
	}

	@Override
	public void insertUpdate(List<T> ts) {
		throw new UnsupportedOperationException("不该被调用的函数");
	}

	@Override
	public long count(String where, Object... params) {
		throw new UnsupportedOperationException("不该被调用的函数");
	}
}

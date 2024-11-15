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
import java.util.concurrent.ConcurrentMap;

import org.banish.mysql.AbstractEntity;
import org.banish.mysql.annotation.enuma.SplitWay;
import org.banish.mysql.database.IDataSource;
import org.banish.mysql.orm.SplitEntityMeta;
import org.banish.mysql.table.TableBuilder;
import org.banish.mysql.table.ddl.DDL;
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
	private ConcurrentMap<String, SplitSql<T>> splitSqlMap = new ConcurrentHashMap<>();
	
	private SplitEntityMeta<T> logEntityMeta;
	
	public SplitBaseDao(IDataSource dataSource, SplitEntityMeta<T> entityMeta) {
		super(dataSource, entityMeta);
		this.logEntityMeta = entityMeta;
		//创建Dao对象的时候检查当前时间对应的表
		SplitWay splitWay = entityMeta.getSplitWay();
		if(splitWay == SplitWay.NULL) {
		} else if(splitWay == SplitWay.VALUE) {
		} else {
			String tableName = entityMeta.getSplitTableName(System.currentTimeMillis());
			SplitSql<T> sql = createSql(tableName);
			splitSqlMap.put(tableName, sql);
		}
	}
	
	@Override
	protected ISql<T> getSql(T t) {
		return ensureSqlInit(t);
	}
	
	private SplitSql<T> ensureSqlInit(T t) {
		String tableName = logEntityMeta.getSplitTableNameByEntity(t);
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
	
	private SplitSql<T> createSql(String tableName) {
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
	public T queryByPrimaryKey(Object primaryKey) {
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

	
	public List<T> queryListWhere(Object splitValue, String where, Object... params) {
		String tableName = this.logEntityMeta.getSplitTableName(splitValue);
		SplitSql<T> isql = splitSqlMap.get(tableName);
		if(isql == null) {
			if(DDL.isTableExist(getDataSource(), tableName)) {
				synchronized (this) {
					isql = splitSqlMap.get(tableName);
					if(isql == null) {
						isql = createSql(tableName);
						splitSqlMap.put(tableName, isql);
					}
				}
			} else {
				return new ArrayList<>();
			}
		}
		String sql = isql.SELECT_ALL + " " + where;
		return this.queryList(sql, params);
	}
	
	public long countWhere(Object splitValue, String where, Object... params) {
		String tableName = this.logEntityMeta.getSplitTableName(splitValue);
		SplitSql<T> isql = splitSqlMap.get(tableName);
		if(isql == null) {
			if(DDL.isTableExist(getDataSource(), tableName)) {
				synchronized (this) {
					isql = splitSqlMap.get(tableName);
					if(isql == null) {
						isql = createSql(tableName);
						splitSqlMap.put(tableName, isql);
					}
				}
			} else {
				return 0;
			}
		}
		String sql = isql.COUNT_ALL + " " + where;
		return this.count(sql, params);
	}

	@Override
	public long countWhere(String where, Object... params) {
		// TODO Auto-generated method stub
		return 0;
	}
}

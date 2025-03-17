/**
 * 
 */
package org.banish.sql.core.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.banish.sql.core.builder.TableBuilder;
import org.banish.sql.core.datasource.IDataSource;
import org.banish.sql.core.entity.AbstractEntity;
import org.banish.sql.core.orm.ColumnMeta;
import org.banish.sql.core.orm.EntityMeta;
import org.banish.sql.core.sql.DefaultDML;
import org.banish.sql.core.sql.IDML;

/**
 * @author YY
 *
 */
public abstract class DefaultBaseDao<T extends AbstractEntity> extends OriginDao<T> {
	
	private DefaultDML<T> sql;
	
	public DefaultBaseDao(IDataSource dataSource, EntityMeta<T> entityMeta) {
		super(dataSource, entityMeta);
		this.sql = dataSource.getMetaFactory().newDefaultDML(entityMeta);
		//自动建表
		TableBuilder.build(this, entityMeta.getTableName());
	}
	/**
	 * 插入所有具有自增ID属性的数据
	 * @param ts 必须要求是列表，以保证返回的ID数组顺序与列表对象顺序一致
	 * @return
	 */
	protected void insertAllWithAutoId(List<T> ts) {
		String sql = this.sql.INSERT;
		super.insertAllWithAutoId(ts, sql);
	}
	
	/**
	 * 插入所有具有自定义ID属性的数据
	 * @param ts 必须要求是列表，以保证返回的ID数组顺序与列表对象顺序一致
	 * @return
	 */
	protected void insertAllWithIdentityId(List<T> ts) {
		String sql = this.sql.INSERT;
		super.insertAllWithIdentityId(ts, sql);
	}
	
	/**
	 * 合并数据库时的插入
	 */
	@Override
	public void mergeInsertAll(List<T> ts) {
		String sql = this.sql.MERGE_INSERT;
		super.mergeInsertAll(ts, sql);
	}
	
	/**
	 * 根据数据的ID进行更新
	 * @param ts
	 */
	public void updateAll(List<T> ts) {
		String sql = this.sql.UPDATE;
		super.updateAll(ts, sql);
	}
	
//	/**
//	 * 根据整个表的数据，不建议进行物理删除
//	 */
//	public void deleteAll() {
//		Connection connection = null;
//		PreparedStatement statement = null;
//		try {
//			connection = this.getDataBase().getDataSource().getConnection();
//			String sql = this.sql.DELETE_ALL;
//			statement = connection.prepareStatement(sql);
//			statement.execute();
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		} finally {
//			close(null, statement, connection);
//		}
//	}
	
	/**
	 * 删除多个数据，不建议进行物理删除
	 * @param ts
	 */
	public void deleteAll(List<T> ts) {
		Connection connection = null;
		PreparedStatement statement = null;
		String sql = this.sql.DELETE;
		try {
			connection = this.getDataSource().getConnection();
			statement = connection.prepareStatement(sql);
			for(T t : ts) {
				statement.setObject(1, this.getEntityMeta().getPrimaryKeyValue(t));
				statement.addBatch();
			}
			statement.executeBatch();
		} catch (Exception e) {
			logger.error("{}.deleteAll error with sql {}", this.getEntityMeta().getTableName(), sql);
			throw new RuntimeException(e);
		} finally {
			close(null, statement, connection);
		}
	}
	
	/**
	 * 根据where条件删除数据，不建议进行物理删除
	 * @param where
	 * @param params
	 * @return
	 */
	public void deleteWhere(String where, Object... params) {
		Connection connection = null;
		PreparedStatement statement = null;
		String sql = this.sql.DELETE_ALL + " " + where;
		try {
			connection = this.getDataSource().getConnection();
			statement = connection.prepareStatement(sql);
			for(int i = 0; i < params.length; i++) {
				statement.setObject(i + 1, params[i]);
			}
			statement.execute();
		} catch (Exception e) {
			logger.error("{}.deleteWhere error with sql {}", this.getEntityMeta().getTableName(), sql);
			throw new RuntimeException(e);
		} finally {
			close(null, statement, connection);
		}
	}
	
	/**
	 * 根据主键查询数据
	 * @param id
	 * @return
	 */
	public T queryByPrimaryKey(Object primaryKey) {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		String sql = this.sql.SELECT;
		try {
			connection = this.getDataSource().getConnection();
			statement = connection.prepareStatement(sql);
			statement.setObject(1, primaryKey);
			rs = statement.executeQuery();
			if(rs.next()) {
				return formObject(getEntityMeta(), rs, false);
			} else {
				return null;
			}
		} catch (Exception e) {
			logger.error("{}.query error with sql {}", this.getEntityMeta().getTableName(), sql);
			throw new RuntimeException(e);
		} finally {
			close(rs, statement, connection);
		}
	}
	
	/**
	 * 查询整个表的数据
	 * @return
	 */
	public List<T> queryAll() {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		String sql = this.sql.SELECT_ALL;
		try {
			connection = this.getDataSource().getConnection();
			statement = connection.prepareStatement(sql);
			rs = statement.executeQuery();
			
			List<T> ts = new ArrayList<>();
			while(rs.next()) {
				ts.add(formObject(getEntityMeta(), rs, false));
			}
			return ts;
		} catch (Exception e) {
			logger.error("{}.queryAll error with sql {}", this.getEntityMeta().getTableName(), sql);
			throw new RuntimeException(e);
		} finally {
			close(rs, statement, connection);
		}
	}
	
	/**
	 * 根据where条件查询单条数据
	 * @param where
	 * @param params
	 * @return
	 */
	public T queryOneWhere(String where, Object... params) {
		List<T> result = queryListWhere(where, params);
		if(result.size() > 1) {
			throw new RuntimeException("selection result more than one");
		} else if(result.size() == 1) {
			return result.get(0);
		} else {
			return null;
		}
	}
	/**
	 * 根据where条件查询数据
	 * @param where
	 * @param params
	 * @return
	 */
	public List<T> queryListWhere(String where, Object... params) {
		String sql = this.sql.SELECT_ALL + " " + where;
		return this.queryList(sql, params);
	}
	
	@Override
	public long countWhere(String where, Object... params) {
		String sql = this.sql.COUNT_ALL + " " + where;
		return this.count(sql, params);
	}

	@Override
	protected IDML<T> getSql(T t) {
		return sql;
	}
	@Override
	public void insertUpdate(List<T> ts) {
		if(ts.isEmpty()) {
			return;
		}
		long startTime = System.currentTimeMillis();
		String sql = this.sql.insertUpdate(getEntityMeta(), ts.size());
		
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			connection = this.getDataSource().getConnection();
			statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			
			LocalDateTime now = LocalDateTime.now();
			
			for(int j = 0; j < ts.size(); j++) {
				T t = ts.get(j);
				if(t.getInsertTime() == null) {
					t.setInsertTime(now);
				}
				t.setUpdateTime(now);
				int offset = getEntityMeta().getInsertColumnList().size() * j;
				for(int i = 0; i < getEntityMeta().getInsertColumnList().size(); i++) {
					ColumnMeta columnMeta = getEntityMeta().getInsertColumnList().get(i);
					Object obj = columnMeta.takeValue(t);
					statement.setObject(offset + i + 1, obj);
				}
			}
			//批量插入
			statement.executeUpdate();
			rs = statement.getGeneratedKeys();
			for(int i = 0; i < ts.size() && rs.next(); i++) {
				T t = ts.get(i);
				getEntityMeta().getPrimaryKeyMeta().fillValue(t, 1, rs);
			}
		} catch (Exception e) {
			logger.error("{}.insertUpdate error with sql {}", this.getEntityMeta().getTableName(), sql);
			throw new RuntimeException(e);
		} finally {
			long useTime = System.currentTimeMillis() - startTime;
			if(useTime >= this.slowTime) {
				logger.warn("sql {} execute showly use {} millis with {} entities", sql, useTime, ts.size());
			}
			close(rs, statement, connection);
		}
	}
}

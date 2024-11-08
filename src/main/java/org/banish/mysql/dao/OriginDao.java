/**
 * 
 */
package org.banish.mysql.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

import org.banish.mysql.AbstractEntity;
import org.banish.mysql.annotation.Id.Strategy;
import org.banish.mysql.database.IDataSource;
import org.banish.mysql.orm.EntityMeta;
import org.banish.mysql.orm.column.ColumnMeta;
import org.banish.mysql.table.dml.ISql;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author YY
 *
 */
public abstract class OriginDao<T extends AbstractEntity> {

	protected static Logger logger = LoggerFactory.getLogger(OriginDao.class);
	
	private IDataSource dataSource;
	private final EntityMeta<T> entityMeta;
	protected int slowTime = 50;
	
	/**
	 * 
	 * @param dataBase
	 * @param entityMeta 约束子类进行entityMeta的输入
	 */
	public OriginDao(IDataSource dataSource, EntityMeta<T> entityMeta) {
		this.dataSource = dataSource;
		this.entityMeta = entityMeta;
	}
	
	protected abstract ISql<T> getSql(T t);
	
	public final EntityMeta<T> getEntityMeta() {
		return entityMeta;
	}
	
	/**
	 * 插入一条具有自增ID属性的数据
	 * @param t
	 * @return
	 */
	private void insertWithAutoId(T t) {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		String sql = "";
		try {
			t.setInsertTime(LocalDateTime.now());
			t.setUpdateTime(LocalDateTime.now());
			connection = this.dataSource.getConnection();
			sql = this.getSql(t).insert();
			statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			for(int i = 0; i < getEntityMeta().getColumnList().size(); i++) {
				ColumnMeta columnMeta = getEntityMeta().getColumnList().get(i);
				Object obj = columnMeta.takeValue(t);
				statement.setObject(i + 1, obj);
			}
			statement.executeUpdate();
			rs = statement.getGeneratedKeys();
			rs.next();
			getEntityMeta().getPrimaryKeyMeta().fillValue(t, 1, rs);
		} catch (Exception e) {
			logger.error("{}.insertWithAutoId error with sql {}", this.getEntityMeta().getTableName(), sql);
			throw new RuntimeException(e);
		} finally {
			Dao.close(rs, statement, connection);
		}
	}
	/**
	 * 插入一条具有自定义ID属性的数据
	 * @param t
	 * @return
	 */
	private void insertWithIdentityId(T t) {
		Connection connection = null;
		PreparedStatement statement = null;
		String sql = "";
		try {
			t.setInsertTime(LocalDateTime.now());
			t.setUpdateTime(LocalDateTime.now());
			connection = this.dataSource.getConnection();
			sql = this.getSql(t).insert();
			statement = connection.prepareStatement(sql);
			for(int i = 0; i < getEntityMeta().getColumnList().size(); i++) {
				ColumnMeta columnMeta = getEntityMeta().getColumnList().get(i);
				Object obj = columnMeta.takeValue(t);
				statement.setObject(i + 1, obj);
			}
			statement.executeUpdate();
		} catch (Exception e) {
			logger.error("{}.insertWithIdentityId error with sql {}", this.getEntityMeta().getTableName(), sql);
			throw new RuntimeException(e);
		} finally {
			Dao.close(null, statement, connection);
		}
	}
	/**
	 * 对外的insert函数
	 * @param t
	 * @return
	 */
	public void insert(T t) {
		if(getEntityMeta().getPrimaryKeyMeta().getStrategy() == Strategy.AUTO) {
			insertWithAutoId(t);
		} else {
			insertWithIdentityId(t);
		}
	}
	
	public void insertAll(List<T> ts) {
		if(ts.isEmpty()) {
			return;
		}
		if(getEntityMeta().getPrimaryKeyMeta().getStrategy() == Strategy.AUTO) {
			insertAllWithAutoId(ts);
		} else {
			insertAllWithIdentityId(ts);
		}
	}
	/**
	 * 插入所有具有自增ID属性的数据
	 * @param ts 必须要求是列表，以保证返回的ID数组顺序与列表对象顺序一致
	 * @return
	 */
	protected abstract void insertAllWithAutoId(List<T> ts);
	
	protected final void insertAllWithAutoId(List<T> ts, String sql) {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			connection = this.dataSource.getConnection();
			statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			
			LocalDateTime now = LocalDateTime.now();
			for(int j = 0; j < ts.size(); j++) {
				T t = ts.get(j);
				t.setInsertTime(now);
				t.setUpdateTime(now);
				for(int i = 0; i < getEntityMeta().getColumnList().size(); i++) {
					ColumnMeta columnMeta = getEntityMeta().getColumnList().get(i);
					Object obj = columnMeta.takeValue(t);
					statement.setObject(i + 1, obj);
				}
				statement.addBatch();
			}
			//批量插入
			statement.executeBatch();
			rs = statement.getGeneratedKeys();
			for(int i = 0; i < ts.size() && rs.next(); i++) {
				T t = ts.get(i);
				getEntityMeta().getPrimaryKeyMeta().fillValue(t, 1, rs);
			}
		} catch (Exception e) {
			logger.error("{}.insertAllWithAutoId error with sql {}", this.getEntityMeta().getTableName(), sql);
			throw new RuntimeException(e);
		} finally {
			Dao.close(rs, statement, connection);
		}
	}
	
	/**
	 * 插入所有具有自定义ID属性的数据
	 * @param ts 必须要求是列表，以保证返回的ID数组顺序与列表对象顺序一致
	 * @return
	 */
	protected abstract void insertAllWithIdentityId(List<T> ts);
	
	protected final void insertAllWithIdentityId(List<T> ts, String sql) {
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = this.dataSource.getConnection();
			statement = connection.prepareStatement(sql);
			
			LocalDateTime now = LocalDateTime.now();
			for(int j = 0; j < ts.size(); j++) {
				T t = ts.get(j);
				t.setInsertTime(now);
				t.setUpdateTime(now);
				for(int i = 0; i < getEntityMeta().getColumnList().size(); i++) {
					ColumnMeta columnMeta = getEntityMeta().getColumnList().get(i);
					Object obj = columnMeta.takeValue(t);
					statement.setObject(i + 1, obj);
				}
				statement.addBatch();
			}
			//批量插入
			statement.executeBatch();
		} catch (Exception e) {
			logger.error("{}.insertAllWithIdentityId error with sql {}", this.getEntityMeta().getTableName(), sql);
			throw new RuntimeException(e);
		} finally {
			Dao.close(null, statement, connection);
		}
	}
	
	public abstract void insertUpdate(List<T> ts);
	
	/**
	 * 根据数据的ID进行更新
	 * @param t
	 */
	public void update(T t) {
		Connection connection = null;
		PreparedStatement statement = null;
		String sql = "";
		try {
			connection = this.dataSource.getConnection();
			sql = this.getSql(t).update();
			statement = connection.prepareStatement(sql);
			
			t.setUpdateTime(LocalDateTime.now());
			setUpdateValues(statement, t);
			statement.executeUpdate();
		} catch (Exception e) {
			logger.error("{}.update error with sql {}", this.getEntityMeta().getTableName(), sql);
			throw new RuntimeException(e);
		} finally {
			Dao.close(null, statement, connection);
		}
	}
	
	/**
	 * 根据数据的ID进行更新
	 * @param ts
	 */
	public abstract void updateAll(List<T> ts);
	
	protected final void updateAll(List<T> ts, String sql) {
		if(ts.isEmpty()) {
			return;
		}
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = this.dataSource.getConnection();
			statement = connection.prepareStatement(sql);
			
			LocalDateTime now = LocalDateTime.now();
			for(int j = 0; j < ts.size(); j++) {
				T t = ts.get(j);
				t.setUpdateTime(now);
				setUpdateValues(statement, t);
				statement.addBatch();
			}
			statement.executeBatch();
		} catch (Exception e) {
			logger.error("{}.updateAll error with sql {}", this.getEntityMeta().getTableName(), sql);
			throw new RuntimeException(e);
		} finally {
			Dao.close(null, statement, connection);
		}
	}
	
	private void setUpdateValues(PreparedStatement statement, T t) throws Exception {
		int index = 0;
		for(int i = 0; i < getEntityMeta().getColumnList().size(); i++) {
			ColumnMeta column = getEntityMeta().getColumnList().get(i);
			if(column.isReadonly()) {
				continue;
			}
			if(column == getEntityMeta().getPrimaryKeyMeta()) {
				continue;
			}
			index += 1;
			Object obj = column.takeValue(t);
			statement.setObject(index, obj);
		}
		//设置ID参数值
		statement.setObject(index + 1, getEntityMeta().getPrimaryKeyValue(t));
	}

	/**
	 * 根据数据的ID进行删除，不建议进行物理删除
	 * @param t
	 */
	public void delete(T t) {
		Connection connection = null;
		PreparedStatement statement = null;
		String sql = "";
		try {
			connection = this.dataSource.getConnection();
			sql = this.getSql(t).delete();
			statement = connection.prepareStatement(sql);
			statement.setObject(1, getEntityMeta().getPrimaryKeyValue(t));
			statement.execute();
		} catch (Exception e) {
			logger.error("{}.delete error with sql {}", this.getEntityMeta().getTableName(), sql);
			throw new RuntimeException(e);
		} finally {
			Dao.close(null, statement, connection);
		}
	}
	
	/**
	 * 删除多个数据，不建议进行物理删除
	 */
	public abstract void deleteAll(List<T> ts);
	/**
	 * 根据where条件删除数据，不建议进行物理删除
	 * @param where
	 * @param params
	 * @return
	 */
	public abstract void deleteWhere(String where, Object... params);
	/**
	 * 根据ID查询数据
	 * @param id
	 * @return
	 */
	public abstract T query(Object primaryKey);
	/**
	 * 查询整个表的数据
	 * @return
	 */
	public abstract List<T> queryAll();
	/**
	 * 根据where条件查询单条数据
	 * @param where
	 * @param params
	 * @return
	 */
	public abstract T queryOneWhere(String where, Object... params);
	/**
	 * 根据where条件查询数据
	 * @param where
	 * @param params
	 * @return
	 */
	public abstract List<T> queryListWhere(String where, Object... params);
	/**
	 * 统计符合条件的数据数量
	 * @param where
	 * @param params
	 * @return
	 */
	public abstract long count(String where, Object... params);
	
	public IDataSource getDataSource() {
		return dataSource;
	}
}

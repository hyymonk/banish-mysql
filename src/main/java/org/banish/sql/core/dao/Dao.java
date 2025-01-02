/**
 * 
 */
package org.banish.sql.core.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.banish.sql.core.datasource.IDataSource;
import org.banish.sql.core.orm.AliasEntityMeta;
import org.banish.sql.core.orm.ColumnMeta;
import org.banish.sql.core.orm.IEntityMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author YY
 *
 */
public class Dao {

	private static Logger logger = LoggerFactory.getLogger(Dao.class);
	
	protected static void close(ResultSet resultSet, PreparedStatement statement, Connection connection) {
		try {
			if (resultSet != null) {
				resultSet.close();
			}
			if (statement != null) {
				statement.close();
			}
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException e) {
			logger.error("", e);
		}
	}
	
	protected static <E> E formObject(IEntityMeta<E> entityMeta, ResultSet rs, boolean useAlias) {
        try {
        	ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount();
			E obj = entityMeta.newInstance();
			
			for (int i = 1; i <= cols; i++) {
				String colomnName = null;
				if(useAlias) {
					//当查询语句中出现as关键字的时候，该别名字段将通过getColumnLabel的方式进行获取
					//当对于某列没有使用别名时，getColumnLabel=getColumnName
					colomnName = meta.getColumnLabel(i);
				} else {
					colomnName = meta.getColumnName(i);
				}
				ColumnMeta columnMeta = entityMeta.getColumnMeta(colomnName);
				if(columnMeta == null) {
					continue;
				}
				try {
					columnMeta.fillValue(obj, i, rs);
				} catch (Exception e) {
					if(rs.isFirst()) {
						//数据库中某一列的数据其格式是固定的，若然数据库的数据格式无法跟类属性进行对应，
						//那么在进行首行数据转化为对象的过程中就会出现异常
						//因此只在首行出现异常时进行打印，避免重复日志太多，不利于日志复查
						e.printStackTrace();
					}
				}
			}
			return obj;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
	}
	
	public static void executeSql(IDataSource dataSource, String sql, Object... params) {
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql);
			for(int i = 0; i < params.length; i++) {
				statement.setObject(i + 1, params[i]);
			}
			statement.execute();
		} catch (Exception e) {
			logger.error("executeSql error with sql {}, params {}", sql, Arrays.toString(params));
			throw new RuntimeException(e);
		} finally {
			Dao.close(null, statement, connection);
		}
	}
	
	public static <U> List<U> queryAliasObjects(IDataSource dataSource, Class<U> clazz, String sql, Object... params) {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql);
			for(int i = 0; i < params.length; i++) {
				statement.setObject(i + 1, params[i]);
			}
			rs = statement.executeQuery();
			
			//取消对别名实体元数据的缓存，如果要缓存，还需要根据元数据工厂的不同进行分组
			AliasEntityMeta<U> aliasEntityMeta = new AliasEntityMeta<>(clazz, dataSource.getMetaFactory());
			List<U> ts = new ArrayList<>();
			while(rs.next()) {
				ts.add(Dao.formObject(aliasEntityMeta, rs, true));
			}
			return ts;
		} catch (Exception e) {
			logger.error("queryAliasObjects error with sql {}, cause by {}", sql, e.getMessage());
			throw new RuntimeException(e);
		} finally {
			Dao.close(rs, statement, connection);
		}
	}
	
	public static <U> U queryAliasObject(IDataSource dataSource, Class<U> clazz, String sql, Object... params) {
		List<U> result = queryAliasObjects(dataSource, clazz, sql, params);
		if(result.size() > 1) {
			throw new RuntimeException("selection result more than one");
		} else if(result.size() == 1) {
			return result.get(0);
		} else {
			return null;
		}
	}
	
	public static long count(IDataSource dataSource, String tableName, String where, Object... params) {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		String sql = "SELECT count(1) AS number FROM " + tableName + " " + where;
		try {
			connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql);
			for(int i = 0; i < params.length; i++) {
				statement.setObject(i + 1, params[i]);
			}
			rs = statement.executeQuery();
			rs.next();
			return rs.getLong(1);
		}  catch (Exception e) {
			logger.error("{}.count error with sql {}, cause by {}", tableName, sql, e.getMessage());
			throw new RuntimeException(e);
		} finally {
			Dao.close(rs, statement, connection);
		}
	}
}

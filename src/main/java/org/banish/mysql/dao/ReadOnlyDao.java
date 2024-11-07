/**
 * 
 */
package org.banish.mysql.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.banish.mysql.database.IDataSource;
import org.banish.mysql.orm.AliasEntityMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author YY
 *
 */
public class ReadOnlyDao {

	private static Logger logger = LoggerFactory.getLogger(ReadOnlyDao.class);
	
	private static final ConcurrentMap<Class<?>, AliasEntityMeta<?>> aliasMetas = new ConcurrentHashMap<>();
	
	public static <U> List<U> queryObjects(IDataSource dataSource, Class<U> clazz, String tableName, String where, Object... params) {
		String sql = "SELECT * FROM " + tableName + " " + where;
		return queryAliasObjects(dataSource, clazz, sql, params);
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
			
			@SuppressWarnings("unchecked")
			AliasEntityMeta<U> aliasEntityMeta = (AliasEntityMeta<U>)aliasMetas.get(clazz);
			if(aliasEntityMeta == null) {
				aliasEntityMeta = new AliasEntityMeta<>(clazz);
				aliasMetas.put(clazz, aliasEntityMeta);
			}
			
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
	
	/**
	 * 
	 * @param dataBase
	 * @param tableName
	 * @return
	 */
	public static boolean isTableExist(IDataSource dataSource, String tableName) {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		String sql = "SELECT 1 FROM " + tableName + " LIMIT 1";
		try {
			connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql);
			rs = statement.executeQuery();
			return true;
		}  catch (Exception e) {
			return false;
		} finally {
			Dao.close(rs, statement, connection);
		}
	}
}

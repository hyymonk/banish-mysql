/**
 * 
 */
package org.banish.mysql.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.banish.mysql.orm.IEntityMeta;
import org.banish.mysql.orm.column.ColumnMeta;
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
}

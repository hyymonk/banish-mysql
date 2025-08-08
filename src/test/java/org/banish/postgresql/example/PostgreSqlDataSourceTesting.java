/**
 * 
 */
package org.banish.postgresql.example;

import java.sql.Connection;
import java.sql.SQLException;

import org.banish.DBConfigTesting;
import org.banish.sql.core.datasource.DbMode;
import org.banish.sql.core.datasource.IDataSource;
import org.banish.sql.core.orm.IOrmFactory;
import org.banish.sql.core.sql.IDDL;
import org.banish.sql.postgresql.PostgreSqlOrmFactory;

import com.alibaba.druid.pool.DruidDataSource;

/**
 * @author YY
 *
 */
public class PostgreSqlDataSourceTesting implements IDataSource {
	
	private DBConfigTesting dbConfig;
	private DruidDataSource dataSource;
	
	public PostgreSqlDataSourceTesting(DBConfigTesting dbConfig) {
		this.dbConfig = dbConfig;
		this.dataSource = createDataSource();
		try {
			this.dataSource.init();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 创建数据源，亦即数据库连接池
	 * @return
	 */
	private DruidDataSource createDataSource() {
		DruidDataSource dataSource = new DruidDataSource();
		String jdbcUrl = "jdbc:postgresql://" + dbConfig.getIpPort() + "/" + dbConfig.getDbName()
				+ "?useSSL=false&useUnicode=true&characterEncoding=UTF-8&serverTimezone=GMT%2B8&rewriteBatchedStatements=true";
		
		dataSource.setUrl(jdbcUrl);
		dataSource.setUsername(dbConfig.getUser());
		dataSource.setPassword(dbConfig.getPassword());

		dataSource.setDriverClassName("org.postgresql.Driver");
		// 初始化时建立物理连接的个数。初始化发生在显示调用init方法，或者第一次getConnection时
		dataSource.setInitialSize(dbConfig.getMinIdle());
		// 最小连接池数量
		dataSource.setMinIdle(dbConfig.getMinIdle());
		// 最大连接池数量
		dataSource.setMaxActive(dbConfig.getMaxActive());
		// 申请连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能。
		dataSource.setTestOnBorrow(false);
		// 建议配置为true，不影响性能，并且保证安全性。申请连接的时候检测，如果空闲时间大于timeBetweenEvictionRunsMillis，执行validationQuery检测连接是否有效。
		dataSource.setTestWhileIdle(true);
		// 归还连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能
		dataSource.setTestOnReturn(false);
		// 用来检测连接是否有效的sql，要求是一个查询语句。如果validationQuery为null，testOnBorrow、testOnReturn、testWhileIdle都不会其作用。
		dataSource.setValidationQuery("SELECT 1");
		dataSource.setMaxWait(dbConfig.getMaxWaitMillis());
		// druid会再无法连接数据库时进行无限重连，下面的三个参数可以调解是否无限重连，重连次数，重连间隔
//		dataSource.setBreakAfterAcquireFailure(true);
//		dataSource.setConnectionErrorRetryAttempts(10);
//		dataSource.setTimeBetweenConnectErrorMillis(1000);
		return dataSource;
	}

	public DBConfigTesting getDbConfig() {
		return dbConfig;
	}

	@Override
	public void close() {
		dataSource.close();
	}
	
	public String getAlias() {
		return dbConfig.getAlias();
	}

	@Override
	public int getZoneId() {
		return dbConfig.getId();
	}

	@Override
	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	@Override
	public String getDbName() {
		return dbConfig.getDbName();
	}

	@Override
	public IOrmFactory getMetaFactory() {
		return PostgreSqlOrmFactory.INS;
	}
	
	@Override
	public boolean isTableExist(String tableName) {
		IDDL iddl = this.getMetaFactory().newDDL(this, false);
		return iddl.isTableExist(tableName);
	}
	
	@Override
	public DbMode dbMode() {
		return DbMode.POSTGRESQL;
	}
}

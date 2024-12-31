/**
 * 
 */
package org.banish;

/**
 * @author YY
 */
public class DBConfig {
	/**
	 * 服务器ID
	 */
	private int id;
	/**
	 * 数据源的别名
	 */
	private String alias = "game";
	/**
	 * 数据库的连接地址
	 */
	private String ipPort = "127.0.0.1:3306";
	/**
	 * 数据库的名字
	 */
	private String dbName = "game1";
	/**
	 * 数据库用户
	 */
	private String user = "root";
	/**
	 * 数据库用户的密码
	 */
	private String password = "123456";
	/**
	 * 数据库最小空闲连接数
	 */
	private int minIdle = 1;
	/**
	 * 数据库最大连接数
	 */
	private int maxActive = 5;
	/**
	 * 数据库最大等待时间
	 */
	private long maxWaitMillis = 60000;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	public String getIpPort() {
		return ipPort;
	}
	public void setIpPort(String ipPort) {
		this.ipPort = ipPort;
	}
	public String getDbName() {
		return dbName;
	}
	public void setDbName(String dbName) {
		this.dbName = dbName;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public int getMinIdle() {
		return minIdle;
	}
	public void setMinIdle(int minIdle) {
		this.minIdle = minIdle;
	}
	public int getMaxActive() {
		return maxActive;
	}
	public void setMaxActive(int maxActive) {
		this.maxActive = maxActive;
	}
	public long getMaxWaitMillis() {
		return maxWaitMillis;
	}
	public void setMaxWaitMillis(long maxWaitMillis) {
		this.maxWaitMillis = maxWaitMillis;
	}
}

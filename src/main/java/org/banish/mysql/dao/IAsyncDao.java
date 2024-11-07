/**
 * 
 */
package org.banish.mysql.dao;

import java.util.List;

import org.banish.mysql.annotation.enuma.AsyncType;
import org.banish.mysql.orm.IAsyncEntityMeta;

/**
 * @author YY
 *
 */
public interface IAsyncDao<T> {
	
	public void insertNow(T t);
	public void insertAllNow(List<T> ts);
	public void updateNow(T t);
	public void updateAllNow(List<T> ts);
	
	public AsyncDaoPlugin<T> getAsyncPlugin();
	
	//insert、insertAll、update、updateAll四个函数不能使用默认的接口实现方式
	public void insert(T t);
	public void insertAll(List<T> ts);
	public void update(T t);
	public void updateAll(List<T> ts);
	
	/**
	 * 在异步中调用的insert函数
	 */
	default public void insert() {
		getAsyncPlugin().insert();
	}
	/**
	 * 在异步中调用的insertAll函数
	 */
	default public int insertAllNow() {
		return getAsyncPlugin().insertAllNow();
	}
	/**
	 * 在异步中调用的update函数
	 * 多线程环境下，同时从队列中poll对象和向队列中add对象时，会有如下特殊情况
	 * update(t)-------------------------------update()
	 * 								1、已经从updateQueue中poll对象
	 * 2、特征码判断对象还在队列中    
	 * 								3、list.add(t)先于特征码的移除，即对象已经被放入更新列表
	 * 通过上面分析，即使这种情况发生，也不会导致数据没有更新
	 */
	default public void update() {
		getAsyncPlugin().update();
	}
	/**
	 * 在异步中调用的updateAll函数
	 */
	default public int updateAllNow() {
		return getAsyncPlugin().updateAllNow();
	}
	
	public IAsyncEntityMeta getAsyncMeta();
	
	default public AsyncType getAsyncType() {
		return getAsyncMeta().getAsyncType();
	}
	default public int getAsyncSize() {
		return getAsyncMeta().getAsyncSize();
	}
	default public int getAsyncDelay() {
		return getAsyncMeta().getAsyncDelay();
	}
	default public String getAsyncName() {
		return getAsyncMeta().getAsyncName();
	}
	
	public Object[] getValues(T t);
}

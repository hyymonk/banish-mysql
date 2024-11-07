/**
 * 
 */
package org.banish.mysql.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import org.banish.mysql.annotation.enuma.AsyncType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author YY
 *
 */
public class AsyncDaoPlugin<T> {

	protected static Logger logger = LoggerFactory.getLogger(AsyncDaoPlugin.class);
	
	/**
	 * 异步更新的队列
	 */
	private Queue<T> updateQueue = new ConcurrentLinkedQueue<>();
	private ConcurrentMap<Integer, Object> updateMap = new ConcurrentHashMap<>();
	private final Object EMPTY = new Object();
	/**
	 * 异步插入的队列
	 */
	private Queue<T> insertQueue = new ConcurrentLinkedQueue<>();
	
	private IAsyncDao<T> asyncDao;
	
	public AsyncDaoPlugin(IAsyncDao<T> asyncDao) {
		this.asyncDao = asyncDao;
	}
	
	public void insert(T t) {
		if(asyncDao.getAsyncType() == AsyncType.INSERT) {
			insertQueue.add(t);
		} else {
			asyncDao.insertNow(t);
		}
	}

	public void insertAll(List<T> ts) {
		if(asyncDao.getAsyncType() == AsyncType.INSERT) {
			insertQueue.addAll(ts);
		} else {
			asyncDao.insertAllNow(ts);
		}
	}

	public void update(T t) {
		//数据库更新如果不是进行分布式部署，可以使用hashCode进行判断是否有重复对象在队列中
		//数据库更新如果采用分布式部署，hashCode的判断将失效
		if(!updateMap.containsKey(t.hashCode())) {
			updateMap.put(t.hashCode(), EMPTY);
			updateQueue.add(t);
		}
	}

	public void updateAll(List<T> ts) {
		for(T t : ts) {
			update(t);
		}
	}
	/**
	 * 在异步中调用的insert函数
	 */
	protected void insert() {
		T t = insertQueue.poll();
		if(t == null) {
			return;
		}
		List<T> list = new ArrayList<>();
		int batchSize = asyncDao.getAsyncSize();
		while(t != null) {
			list.add(t);
			if(list.size() >= batchSize) {
				break;
			}
			t = insertQueue.poll();
		}
		insert(list);
		logger.debug(String.format("after insert %s once left data %s", asyncDao.getAsyncName(), updateQueue.size()));
	}
	/**
	 * 立马插入队列中的所有数据
	 */
	protected int insertAllNow() {
		T t = insertQueue.poll();
		if(t == null) {
			return 0;
		}
		List<T> list = new ArrayList<>();
		int batchSize = asyncDao.getAsyncSize();
		int count = 0;
		while(t != null) {
			list.add(t);
			count += 1;

			if(list.size() >= batchSize) {
				insert(list);
				list.clear();
			}
			t = insertQueue.poll();
		}
		insert(list);
		return count;
	}
	
	private void insert(List<T> insertList) {
		try {
			asyncDao.insertAllNow(insertList);
			logger.debug(String.format("insertAll right now %s %s data to db", asyncDao.getAsyncName(), insertList.size()));
		} catch (Exception e1) {
			logger.error(String.format("insertAll right now %s %s data to db cause exception, retry to insert each", asyncDao.getAsyncName(), insertList.size()), e1);
			for(T t : insertList) {
				try {
					asyncDao.insertNow(t);
				} catch (Exception e2) {
					String objectValue = Arrays.toString(asyncDao.getValues(t));
					logger.error(String.format("retry insert %s data %s fail", asyncDao.getAsyncName(), objectValue), e2);
				}
			}
		}
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
	protected void update() {
		T t = updateQueue.poll();
		if(t == null) {
			return;
		}
		List<T> list = new ArrayList<>();
		int batchSize = asyncDao.getAsyncSize();
		while(t != null) {
			list.add(t);
			updateMap.remove(t.hashCode());
			if(list.size() >= batchSize) {
				break;
			}
			t = updateQueue.poll();
		}
		update(list);
		logger.debug(String.format("after update %s once left data %s", asyncDao.getAsyncName(), updateQueue.size()));
		if(updateQueue.size() >= batchSize) {
			update();
		}
	}
	/**
	 * 立马更新队列中的所有数据
	 */
	protected int updateAllNow() {
		T t = updateQueue.poll();
		if(t == null) {
			return 0;
		}
		List<T> list = new ArrayList<>();
		int batchSize = asyncDao.getAsyncSize();
		int count = 0;
		while(t != null) {
			list.add(t);
			count += 1;

			updateMap.remove(t.hashCode());
			if(list.size() >= batchSize) {
				update(list);
				list.clear();
			}
			t = updateQueue.poll();
		}
		update(list);
		return count;
	}
	
	private void update(List<T> updateList) {
		try {
			asyncDao.updateAllNow(updateList);
			logger.debug(String.format("updateAll right now %s %s data to db", asyncDao.getAsyncName(), updateList.size()));
		} catch (Exception e1) {
			logger.error(String.format("updateAll right now %s %s data to db cause exception, retry to insert each", asyncDao.getAsyncName(), updateList.size()), e1);
			for(T t : updateList) {
				try {
					asyncDao.updateNow(t);
				} catch (Exception e2) {
					String objectValue = Arrays.toString(asyncDao.getValues(t));
					logger.error(String.format("retry update %s data %s fail", asyncDao.getAsyncName(), objectValue), e2);
				}
			}
		}
	}
	
	
	public int delayTime() {
		return asyncDao.getAsyncDelay();
	}
}

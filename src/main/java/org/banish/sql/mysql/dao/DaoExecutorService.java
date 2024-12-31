/**
 * 
 */
package org.banish.sql.mysql.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author YY
 * 数据异步入库
 */
public class DaoExecutorService {
	
	private static Logger logger = LoggerFactory.getLogger(DaoExecutorService.class);
	
	private static ScheduledExecutorService[] executors;
	
	private static List<DelayDaoExecutor> executorList = new ArrayList<>();
	
	/**
	 * 使用步骤1，初始化线程池数量
	 * @param threadCount
	 */
	public static void start(int threadCount, Collection<IAsyncDao<?>> asyncDaos) {
		executors = new ScheduledExecutorService[threadCount];
		for (int i = 0; i < threadCount; i++) {
			executors[i] = Executors.newSingleThreadScheduledExecutor();
		}
		int poolIndex = 0;
		for(IAsyncDao<?> asyncDao : asyncDaos) {
			int index = poolIndex % executors.length;
			ScheduledExecutorService executor = executors[index];
			DelayDaoExecutor delayDaoExecutor = new DelayDaoExecutor(asyncDao, executor);
			executorList.add(delayDaoExecutor);
			poolIndex += 1;
		}
		startExecutor();
	}
	
	private static void startExecutor() {
		for(int i = 0; i < executorList.size(); i++) {
			DelayDaoExecutor delayDaoExecutor = executorList.get(i);
			delayDaoExecutor.executor.scheduleWithFixedDelay(new Runnable() {
				@Override
				public void run() {
					try {
						delayDaoExecutor.asyncDao.insert();
					} catch (Exception e) {
						e.printStackTrace();
					}
					try {
						delayDaoExecutor.asyncDao.update();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}, i % 30, delayDaoExecutor.asyncDao.getAsyncDelay(), TimeUnit.SECONDS);
			//调优异步DAO，使得各个异步DAO可以错峰执行
		}
	}
	
	public static void shutdownDaoExecutor() {
		if(executors != null) {
			for(ScheduledExecutorService executor : executors) {
				executor.shutdown();
			}
			logger.info("shutdown dao executors");
		}
		for(DelayDaoExecutor daoExecutor : executorList) {
			try {
				int count = daoExecutor.asyncDao.insertAllNow();
				if(count > 0) {
					logger.info("[{}] async insert data count {}", daoExecutor.getName(), count);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				int count = daoExecutor.asyncDao.updateAllNow();
				if(count > 0) {
					logger.info("[{}] async update data count {}", daoExecutor.getName(), count);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private static class DelayDaoExecutor {
		private IAsyncDao<?> asyncDao;
		private ScheduledExecutorService executor;
		public DelayDaoExecutor(IAsyncDao<?> asyncDao, ScheduledExecutorService executor) {
			this.asyncDao = asyncDao;
			this.executor = executor;
		}
		
		public String getName() {
			return asyncDao.getAsyncName();
		}
	}
}

/**
 * 
 */
package org.banish.sql.core.dao;

import java.util.Arrays;
import java.util.List;

/**
 * @author YY
 *
 */
public class AsyncDaoFastPlugin<T> extends AsyncDaoPlugin<T> {
	
	public AsyncDaoFastPlugin(IAsyncDao<T> asyncDao) {
		super(asyncDao);
	}
	
	@Override
	protected void update(List<T> updateList) {
		try {
			asyncDao.fastUpdateAllNow(updateList);
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
}

/**
 * 
 */
package org.banish.mysql.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.banish.mysql.database.IDataSource;
import org.banish.mysql.orm.AbstractEntity;
import org.banish.mysql.orm.DefaultAsyncEntityMeta;
import org.banish.mysql.orm.DefaultEntityMeta;
import org.banish.mysql.orm.EntityMeta;
import org.banish.mysql.orm.MetaManager;
import org.banish.mysql.orm.SplitAsyncEntityMeta;
import org.banish.mysql.orm.SplitEntityMeta;
import org.banish.mysql.valueformat.ValueFormatters;
import org.banish.mysql.valueformat.ValueFormatters.ValueFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author YY
 *
 */
public class DaoManager {
	
	private static Logger logger = LoggerFactory.getLogger(DaoManager.class);
	
	/**
	 * 程序运行期内实体类Dao集合
	 * <区号，<实体类类型，对应该实体类运行期Dao>>
	 */
	private Map<Integer, Map<Class<?>, OriginDao<?>>> RUNTIME_DAOS;
	/**
	 * 运行期内的异步实体类Dao列表
	 */
	private List<IAsyncDao<?>> RUNTIME_ASYNC_DAOS;
	
	
	public DaoManager(List<IDataSource> dbConfigs, Collection<Class<?>> entityClasses, ValueFormatter... valueFormater) {
		this(dbConfigs, entityClasses, 4, valueFormater);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public DaoManager(List<IDataSource> dbConfigs, Collection<Class<?>> entityClasses, int asyncPoolSize, ValueFormatter... valueFormater) {
		
		//加载数据库连接信息文件
		Map<String, List<IDataSource>> dbsByAlias = fillListMap(dbConfigs, IDataSource::getAlias);
		
		//构建所有实体类的运行期元数据信息
		MetaManager.buildMeta(entityClasses);
		
		ValueFormatters.addFormater(valueFormater);
		
		//用于保存所有运行期Dao的集合
		Map<Integer, Map<Class<?>, OriginDao<?>>> tempRuntimeDaos = new HashMap<>();
		
		
		Map<String, List<EntityMeta<?>>> metaByAlias = fillListMap(MetaManager.getMetas(), EntityMeta::getDbAlias);
		
		for(Entry<String, List<EntityMeta<?>>> entry : metaByAlias.entrySet()) {
			String dbAlias = entry.getKey();
			List<IDataSource> dbList = dbsByAlias.get(dbAlias);
			
			for(EntityMeta<?> entityMeta : entry.getValue()) {
				if(dbList == null) {
                    throw new RuntimeException("[" + entityMeta.getClazz().getSimpleName() + "]无法找到别名为[" + entityMeta.getDbAlias() + "]的数据源信息");
				}
				for(IDataSource dataSource : dbList) {
					//通过实体类对应的数据库与元数据信息，动态地创建出该实体类对应的Dao对象
					OriginDao<?> runtimeDao = null;
					
					if(entityMeta instanceof DefaultAsyncEntityMeta) {
						if(asyncPoolSize <= 0) {
							logger.warn("entity {} declare async type, expect async pool size positive, but is {}",
									entityMeta.getClazz().getSimpleName(), asyncPoolSize);
							runtimeDao = new DefaultSyncDao(dataSource, (DefaultEntityMeta)entityMeta);
						} else {
							runtimeDao = new DefaultAsyncDao(dataSource, (DefaultAsyncEntityMeta)entityMeta);
						}
					} else if(entityMeta instanceof DefaultEntityMeta) {
						runtimeDao = new DefaultSyncDao(dataSource, (DefaultEntityMeta)entityMeta);
						
					} else if(entityMeta instanceof SplitAsyncEntityMeta) {
						if(asyncPoolSize <= 0) {
							logger.warn("entity {} declare async type, expect async pool size positive, but is {}",
									entityMeta.getClazz().getSimpleName(), asyncPoolSize);
							runtimeDao = new SplitSyncDao(dataSource, (SplitEntityMeta)entityMeta);
						} else {
							runtimeDao = new SplitAsyncDao(dataSource, (SplitAsyncEntityMeta)entityMeta);
						}
					} else if(entityMeta instanceof SplitEntityMeta) {
						runtimeDao = new SplitSyncDao(dataSource, (SplitEntityMeta)entityMeta);
						
					} else {
						throw new RuntimeException("unknow type entity meta " + entityMeta.getClazz().getSimpleName());
					}
					
					int zoneId = dataSource.getZoneId();
					Map<Class<?>, OriginDao<?>> zoneDaos = tempRuntimeDaos.get(zoneId);
					if(zoneDaos == null) {
						zoneDaos = new HashMap<>();
						tempRuntimeDaos.put(zoneId, zoneDaos);
					}
					zoneDaos.put(entityMeta.getClazz(), runtimeDao);
					logger.info("初始化[{}]数据保存器，类型{}，所在数据区{}-{}", runtimeDao.getEntityMeta().getTableName(),
							runtimeDao.getClass().getSimpleName(), zoneId, dataSource.getAlias());
				}
			}
		}
		RUNTIME_DAOS = tempRuntimeDaos;
		
		initRuntimeAsyncDaos();
		startAsync(asyncPoolSize);
	}
	
	private void initRuntimeAsyncDaos() {
		//异步Dao列表
		List<IAsyncDao<?>> tempRuntimeAsyncDaos = new ArrayList<>();
		for(Map<Class<?>, OriginDao<?>> daoMap : RUNTIME_DAOS.values()) {
			for(OriginDao<?> dao : daoMap.values()) {
				if(dao instanceof IAsyncDao) {
					tempRuntimeAsyncDaos.add((IAsyncDao<?>)dao);
				}
			}
		}
		RUNTIME_ASYNC_DAOS = tempRuntimeAsyncDaos;
	}
	
	
	/**
	 * 通过区号、实体类类型获取对应的运行期Dao
	 * @param zoneId
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends AbstractEntity> OriginDao<T> getDao(int zoneId, Class<T> clazz) {
		Map<Class<?>, OriginDao<?>> daos = RUNTIME_DAOS.get(zoneId);
		if(daos == null) {
			throw new RuntimeException("没有区号["+zoneId+"]对应的运行期Dao");
		}
		OriginDao<?> baseDao = daos.get(clazz);
		if(baseDao == null) {
			throw new RuntimeException("没有实体类["+clazz.getSimpleName()+"]对应的运行期Dao");
		}
		return (OriginDao<T>) baseDao;
	}
	
	/**
	 * 获取所有的运行期Dao
	 * @return
	 */
	public List<OriginDao<?>> getAllDaos() {
		List<OriginDao<?>> allDaos = new ArrayList<>();
		for(Map<Class<?>, OriginDao<?>> daoMap : RUNTIME_DAOS.values()) {
			for(OriginDao<?> baseDao : daoMap.values()) {
				allDaos.add(baseDao);
			}
		}
		return allDaos;
	}
	
	/**
	 * 开启异步Dao的定时任务
	 */
	private void startAsync(int asyncPoolSize) {
		if(RUNTIME_ASYNC_DAOS.isEmpty() || asyncPoolSize <= 0) {
			return;
		}
		DaoExecutorService.start(asyncPoolSize, RUNTIME_ASYNC_DAOS);
	}
	
	public void stopAsync() {
		DaoExecutorService.shutdownDaoExecutor();
	}
	
	private static <T, K> Map<K, List<T>> fillListMap(List<T> tlist, Function<T, K> f) {
        Map<K, List<T>> map = new HashMap<>();
        for (T t : tlist) {
            K keyValue = f.apply(t);
            List<T> list = map.get(keyValue);
            if (list == null) {
                list = new ArrayList<>();
                map.put(keyValue, list);
            }
            list.add(t);
        }
        return map;
    }
}

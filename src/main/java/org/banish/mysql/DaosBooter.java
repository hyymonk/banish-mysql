/**
 * 
 */
package org.banish.mysql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.banish.mysql.annotation.SplitTable;
import org.banish.mysql.annotation.Table;
import org.banish.mysql.annotation.enuma.AsyncType;
import org.banish.mysql.dao.DaoExecutorService;
import org.banish.mysql.dao.DefaultAsyncDao;
import org.banish.mysql.dao.DefaultSyncDao;
import org.banish.mysql.dao.IAsyncDao;
import org.banish.mysql.dao.OriginDao;
import org.banish.mysql.dao.SplitAsyncDao;
import org.banish.mysql.dao.SplitSyncDao;
import org.banish.mysql.database.IDataSource;
import org.banish.mysql.orm.DefaultAsyncEntityMeta;
import org.banish.mysql.orm.DefaultEntityMeta;
import org.banish.mysql.orm.EntityMeta;
import org.banish.mysql.orm.SplitAsyncEntityMeta;
import org.banish.mysql.orm.SplitEntityMeta;
import org.banish.mysql.table.TableBuilder;
import org.banish.mysql.valueformat.ValueFormatters;
import org.banish.mysql.valueformat.ValueFormatters.ValueFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author YY
 *
 */
public class DaosBooter {
	
	private static Logger logger = LoggerFactory.getLogger(DaosBooter.class);
	
	private static Daos INSTANCE = new Daos();
	
	private final int tableBaseZone;
	private List<IDataSource> dataSources = new ArrayList<>();
	private List<Class<? extends AbstractEntity>> entityClasses = new ArrayList<>();
	private int asyncPoolSize = 4;
	private List<ValueFormatter> valueFormaters = new ArrayList<>();
	
	public DaosBooter(int tableBaseZone) {
		this.tableBaseZone = tableBaseZone;
	}
	
	public void addDataSource(IDataSource dataSource) {
		this.dataSources.add(dataSource);
	}
	public void addEntityClasses(Collection<Class<? extends AbstractEntity>> entityClasses) {
		this.entityClasses.addAll(entityClasses);
	}
	public void addEntityClass(Class<? extends AbstractEntity> entityClass) {
		this.entityClasses.add(entityClass);
	}
	public void setAsyncPoolSize(int asyncPoolSize) {
		this.asyncPoolSize = asyncPoolSize;
	}
	public void addValueFormater(ValueFormatter valueFormater) {
		this.valueFormaters.add(valueFormater);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void setup() {
		TableBuilder.SERVER_IDENTITY = tableBaseZone;
		
		//构建所有实体类的运行期元数据信息
		List<EntityMeta<?>> metas = buildMetas();
		
		//添加值格式化方式
		ValueFormatters.addFormater(valueFormaters);
		
		//用于保存所有运行期Dao的集合
		Map<Integer, Map<Class<?>, OriginDao<?>>> runtimeDaos = new HashMap<>();
		
		//加载数据库连接信息文件
		Map<String, List<IDataSource>> dbsByAlias = fillListMap(dataSources, IDataSource::getAlias);
		
		//将元数据按照使用的数据库别名来分组
		Map<String, List<EntityMeta<?>>> metaByAlias = fillListMap(metas, EntityMeta::getDbAlias);
		
		for(Entry<String, List<EntityMeta<?>>> entry : metaByAlias.entrySet()) {
			String dbAlias = entry.getKey();
			List<IDataSource> dbList = dbsByAlias.get(dbAlias);
			
			List<EntityMeta<?>> metaList = entry.getValue();
			Collections.sort(metaList, META_SORTER);
			
			for(EntityMeta<?> entityMeta : metaList) {
				if(dbList == null) {
					panic("Entity class [%s] can not find datasource using alias named %s", entityMeta.getClazz().getSimpleName(), entityMeta.getDbAlias());
				}
				for(IDataSource dataSource : dbList) {
					//通过实体类对应的数据库与元数据信息，动态地创建出该实体类对应的Dao对象
					OriginDao<?> runtimeDao = null;
					
					if(entityMeta instanceof DefaultAsyncEntityMeta) {
						runtimeDao = new DefaultAsyncDao(dataSource, (DefaultAsyncEntityMeta)entityMeta);
						
					} else if(entityMeta instanceof DefaultEntityMeta) {
						runtimeDao = new DefaultSyncDao(dataSource, (DefaultEntityMeta)entityMeta);
						
					} else if(entityMeta instanceof SplitAsyncEntityMeta) {
						runtimeDao = new SplitAsyncDao(dataSource, (SplitAsyncEntityMeta)entityMeta);
						
					} else if(entityMeta instanceof SplitEntityMeta) {
						runtimeDao = new SplitSyncDao(dataSource, (SplitEntityMeta)entityMeta);
						
					} else {
						panic("This situation will never happen");
					}
					
					int zoneId = dataSource.getZoneId();
					Map<Class<?>, OriginDao<?>> zoneDaos = runtimeDaos.get(zoneId);
					if(zoneDaos == null) {
						zoneDaos = new HashMap<>();
						runtimeDaos.put(zoneId, zoneDaos);
					}
					zoneDaos.put(entityMeta.getClazz(), runtimeDao);
					logger.debug(String.format(
							"Table [%-25.25s]'s dao is initialized, with type [%-20s], at zone [%4s] using alias named [%-5.5s]",
							runtimeDao.getEntityMeta().getTableName(), runtimeDao.getClass().getSimpleName(), zoneId,
							dataSource.getAlias()));
				}
			}
		}
		DaosBooter.INSTANCE.setupRuntimeDaos(runtimeDaos, asyncPoolSize);
	}
	
	public static void stopAsync() {
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
	
	private List<EntityMeta<? extends AbstractEntity>> buildMetas() {
		Map<Class<? extends AbstractEntity>, EntityMeta<?>> metas = new HashMap<>();
		for(Class<? extends AbstractEntity> clazz : entityClasses) {
			if(metas.containsKey(clazz)) {
				panic("Duplicate entity class %s is added to the dao builder", clazz.getSimpleName());
			}
			
			Table table = clazz.getAnnotation(Table.class);
			SplitTable splitTable = clazz.getAnnotation(SplitTable.class);
			if(table != null && splitTable != null) {
				panic("Entity class %s contains multiple @?Table annotation", clazz.getSimpleName());
			}
			if(table == null && splitTable == null) {
				panic("Entity class %s does not contains any @?Table annotation", clazz.getSimpleName());
			}
			
			EntityMeta<?> entityMeta = null;
			if(table != null) {
				//普通的数据表
				if(table.asyncType() == AsyncType.NONE) {
					entityMeta = new DefaultEntityMeta<>(clazz);
				} else {
					entityMeta = new DefaultAsyncEntityMeta<>(clazz);
				}
			} else if(splitTable != null) {
				//按时间分表的数据表
				if(splitTable.asyncType() == AsyncType.NONE) {
					entityMeta = new SplitEntityMeta<>(clazz);
				} else {
					entityMeta = new SplitAsyncEntityMeta<>(clazz);
				}
			}
			metas.put(entityMeta.getClazz(), entityMeta);
		}
		return new ArrayList<>(metas.values());
	}
	
	public static class Daos {
		/**
		 * 程序运行期内实体类Dao集合
		 * <区号，<实体类类型，对应该实体类运行期Dao>>
		 */
		private Map<Integer, Map<Class<?>, OriginDao<?>>> RUNTIME_DAOS;
		/**
		 * 运行期内的异步实体类Dao列表
		 */
		private List<IAsyncDao<?>> RUNTIME_ASYNC_DAOS;
		
		private Daos() {}
		
		private void setupRuntimeDaos(Map<Integer, Map<Class<?>, OriginDao<?>>> runtimeDaos, int asyncPoolSize) {
			this.RUNTIME_DAOS = runtimeDaos;
			
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
			
			if(RUNTIME_ASYNC_DAOS.isEmpty()) {
				return;
			}
			if(asyncPoolSize <= 0) {
				panic("Async daos require positive async pool size to run data save");
			}
			DaoExecutorService.start(asyncPoolSize, RUNTIME_ASYNC_DAOS);
		}
		
		/**
		 * 通过区号、实体类类型获取对应的运行期Dao
		 * @param zoneId
		 * @param clazz
		 * @return
		 */
		@SuppressWarnings("unchecked")
		public static <T extends AbstractEntity> OriginDao<T> get(int zoneId, Class<T> clazz) {
			Map<Class<?>, OriginDao<?>> daos = INSTANCE.RUNTIME_DAOS.get(zoneId);
			if(daos == null) {
				panic("Can not find any runtime dao in zone %s", +zoneId);
			}
			OriginDao<?> baseDao = daos.get(clazz);
			if(baseDao == null) {
				panic("Can not find entity class [%s]'s runtime dao", clazz.getSimpleName());
			}
			return (OriginDao<T>) baseDao;
		}
		
		/**
		 * 获取所有的运行期Dao
		 * @return
		 */
		public static List<OriginDao<?>> allDaos() {
			List<OriginDao<?>> allDaos = new ArrayList<>();
			for(Map<Class<?>, OriginDao<?>> daoMap : INSTANCE.RUNTIME_DAOS.values()) {
				for(OriginDao<?> baseDao : daoMap.values()) {
					allDaos.add(baseDao);
				}
			}
			return allDaos;
		}
	}
	
	public static void panic(String format, Object... args) {
		throw new RuntimeException(String.format(format, args));
	}
	
	private static Comparator<EntityMeta<?>> META_SORTER = new Comparator<EntityMeta<?>>() {
		@Override
		public int compare(EntityMeta<?> o1, EntityMeta<?> o2) {
			return o1.getTableName().compareTo(o2.getTableName());
		}
	};
}

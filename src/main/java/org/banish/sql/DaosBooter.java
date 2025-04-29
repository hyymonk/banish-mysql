/**
 * 
 */
package org.banish.sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.banish.sql.core.IIDIniter;
import org.banish.sql.core.annotation.SplitTable;
import org.banish.sql.core.annotation.Table;
import org.banish.sql.core.annotation.enuma.AsyncType;
import org.banish.sql.core.builder.TableBuilder;
import org.banish.sql.core.dao.DaoExecutorService;
import org.banish.sql.core.dao.DefaultAsyncDao;
import org.banish.sql.core.dao.DefaultSyncDao;
import org.banish.sql.core.dao.IAsyncDao;
import org.banish.sql.core.dao.OriginDao;
import org.banish.sql.core.dao.SplitAsyncDao;
import org.banish.sql.core.dao.SplitSyncDao;
import org.banish.sql.core.datasource.IDataSource;
import org.banish.sql.core.entity.AbstractEntity;
import org.banish.sql.core.orm.DefaultAsyncEntityMeta;
import org.banish.sql.core.orm.DefaultEntityMeta;
import org.banish.sql.core.orm.EntityMeta;
import org.banish.sql.core.orm.IOrmFactory;
import org.banish.sql.core.orm.SplitAsyncEntityMeta;
import org.banish.sql.core.orm.SplitEntityMeta;
import org.banish.sql.core.valueformat.ValueFormatters;
import org.banish.sql.core.valueformat.ValueFormatters.ValueFormatter;
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
	private Map<Class<? extends AbstractEntity>, IIDIniter> idIniters = new HashMap<>();
	
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
	public void addIdIniter(Class<? extends AbstractEntity> entityClass, IIDIniter idIniter) {
		this.idIniters.put(entityClass, idIniter);
	}
	
	
	public Daos setup() {
		TableBuilder.SERVER_IDENTITY = tableBaseZone;
		
		//添加值格式化方式
		ValueFormatters.addFormater(valueFormaters);
		
		Map<String, List<Class<? extends AbstractEntity>>> aliasClassMap = groupByAlias();
		
		//TODO 检查数据库是否都配置上
		Set<String> dbAlias = new HashSet<>();
		for(IDataSource dataSource : dataSources) {
			dbAlias.add(dataSource.getAlias());
		}
		for(String needAlias : aliasClassMap.keySet()) {
			if(!dbAlias.contains(needAlias)) {
                panic("Can not find datasource using alias named %s", needAlias);
			}
		}
		
		//<表名，类名>
		Map<String, String> checkDuplicateTables = new HashMap<>();
		//用于保存所有运行期Dao的集合
		Map<Integer, Map<Class<?>, OriginDao<?>>> runtimeDaos = new HashMap<>();
		for(IDataSource dataSource : dataSources) {
			List<Class<? extends AbstractEntity>> clazzList = aliasClassMap.get(dataSource.getAlias());
			if(clazzList == null) {
				continue;
			}
			Collections.sort(clazzList, CLAZZ_SORTER);
			for(Class<? extends AbstractEntity> clazz : clazzList) {
				EntityMeta<?> entityMeta = buildMeta(clazz, dataSource.getMetaFactory());
				
				String clazzName = checkDuplicateTables.get(entityMeta.getTableName());
				if(clazzName == null) {
					checkDuplicateTables.put(entityMeta.getTableName(), clazz.getSimpleName());
				} else if(!clazzName.equals(clazz.getSimpleName())) {
					 panic("Class %s has duplicate table name with class %s", clazz.getSimpleName(), clazzName);
				}
				
				IIDIniter idIniter = idIniters.get(clazz);
				OriginDao<?> runtimeDao = buildRuntimeDao(dataSource, entityMeta, idIniter);
				
				int zoneId = dataSource.getZoneId();
				Map<Class<?>, OriginDao<?>> zoneDaos = runtimeDaos.get(zoneId);
				if(zoneDaos == null) {
					zoneDaos = new HashMap<>();
					runtimeDaos.put(zoneId, zoneDaos);
				}
				zoneDaos.put(entityMeta.getClazz(), runtimeDao);
				logger.debug(String.format(
						"Table [%-25.25s]'s dao is initialized, with type [%-20s], at zone [%4s] using alias named [%-10.10s]",
						runtimeDao.getEntityMeta().getTableName(), runtimeDao.getClass().getSimpleName(), zoneId,
						dataSource.getAlias()));
			}
		}
		DaosBooter.INSTANCE.setupRuntimeDaos(runtimeDaos, asyncPoolSize);
		return DaosBooter.INSTANCE;
	}
	
	public static void stopAsync() {
		DaoExecutorService.shutdownDaoExecutor();
	}
	
	private Map<String, List<Class<? extends AbstractEntity>>> groupByAlias() {
		Map<String, List<Class<? extends AbstractEntity>>> aliasClassMap = new HashMap<>();
		for(Class<? extends AbstractEntity> clazz : entityClasses) {
			Table table = clazz.getAnnotation(Table.class);
			SplitTable splitTable = clazz.getAnnotation(SplitTable.class);
			if(table != null && splitTable != null) {
				panic("Entity class %s contains multiple @?Table annotation", clazz.getSimpleName());
			}
			if(table == null && splitTable == null) {
				panic("Entity class %s does not contains any @?Table annotation", clazz.getSimpleName());
			}
			String alias = null;
			if(table != null) {
				alias = table.dbAlias();
			} else if(splitTable != null) {
				alias = splitTable.dbAlias();
			} else {
				panic("will never happen");
			}
			List<Class<? extends AbstractEntity>> classList = aliasClassMap.get(alias);
			if(classList == null) {
				classList = new ArrayList<>();
				aliasClassMap.put(alias, classList);
			}
			classList.add(clazz);
		}
		return aliasClassMap;
	}
	
	
	private EntityMeta<?> buildMeta(Class<? extends AbstractEntity> clazz, IOrmFactory metaFactory) {
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
				entityMeta = new DefaultEntityMeta<>(clazz, metaFactory);
			} else {
				entityMeta = new DefaultAsyncEntityMeta<>(clazz, metaFactory);
			}
		} else if(splitTable != null) {
			//按时间分表的数据表
			if(splitTable.asyncType() == AsyncType.NONE) {
				entityMeta = new SplitEntityMeta<>(clazz, metaFactory);
			} else {
				entityMeta = new SplitAsyncEntityMeta<>(clazz, metaFactory);
			}
		}
		return entityMeta;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private OriginDao<?> buildRuntimeDao(IDataSource dataSource, EntityMeta<?> entityMeta, IIDIniter idIniter) {
		//通过实体类对应的数据库与元数据信息，动态地创建出该实体类对应的Dao对象
		OriginDao<?> runtimeDao = null;
		
		if(entityMeta instanceof DefaultAsyncEntityMeta) {
			runtimeDao = new DefaultAsyncDao(dataSource, (DefaultAsyncEntityMeta)entityMeta, idIniter);
			
		} else if(entityMeta instanceof DefaultEntityMeta) {
			runtimeDao = new DefaultSyncDao(dataSource, (DefaultEntityMeta)entityMeta, idIniter);
			
		} else if(entityMeta instanceof SplitAsyncEntityMeta) {
			runtimeDao = new SplitAsyncDao(dataSource, (SplitAsyncEntityMeta)entityMeta, idIniter);
			
		} else if(entityMeta instanceof SplitEntityMeta) {
			runtimeDao = new SplitSyncDao(dataSource, (SplitEntityMeta)entityMeta, idIniter);
			
		} else {
			panic("This situation will never happen");
		}
		return runtimeDao;
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
		public List<OriginDao<?>> allDaos() {
			List<OriginDao<?>> allDaos = new ArrayList<>();
			for(Map<Class<?>, OriginDao<?>> daoMap : this.RUNTIME_DAOS.values()) {
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
	
	private static Comparator<Class<?>> CLAZZ_SORTER = new Comparator<Class<?>>() {
		@Override
		public int compare(Class<?> o1, Class<?> o2) {
			return o1.getName().compareTo(o2.getName());
		}
	};
}

/**
 * 
 */
package org.banish.sql.core.builder;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.banish.sql.core.annotation.Id.Strategy;
import org.banish.sql.core.annotation.enuma.IndexType;
import org.banish.sql.core.annotation.enuma.IndexWay;
import org.banish.sql.core.dao.OriginDao;
import org.banish.sql.core.orm.ColumnMeta;
import org.banish.sql.core.orm.EntityMeta;
import org.banish.sql.core.orm.IPrimaryKeyColumnMeta;
import org.banish.sql.core.orm.IndexMeta;
import org.banish.sql.core.sql.IDDL;
import org.banish.sql.core.sql.IDDL.IIndexStruct;
import org.banish.sql.core.sql.IDDL.ITableDes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author YY
 *
 */
public class TableBuilder {
	
	private static Logger logger = LoggerFactory.getLogger(TableBuilder.class);
	
	public static int SERVER_IDENTITY;
	
	/**
	 * 自动构建数据表
	 * @param dao
	 * @param tableName
	 */
	public static IDDL build(OriginDao<?> dao, String tableName)  {
		if(SERVER_IDENTITY <= 0) {
			throw new RuntimeException("数据库构建工具还没有指定服务器标识");
		}
		EntityMeta<?> entityMeta = dao.getEntityMeta();
		IDDL iddl = dao.getDataSource().getMetaFactory().newDDL(dao.getDataSource(), entityMeta.isAutoBuild());
		
		boolean tablExist = iddl.isTableExist(tableName);
		if (!tablExist) {
			// 创建数据表
			createTable(iddl, tableName, entityMeta);
		} else {
			// 更新数据表结构
			updateTable(iddl, tableName, entityMeta);
		}
		//更新索引
		updateIndex(iddl, tableName, tablExist, entityMeta.getIndexMap());
		//更新自增主键
		updateAutoIncrement(iddl, tableName, tablExist, entityMeta, dao.getIdStartWith());
		
		if(!entityMeta.isAutoBuild() && !iddl.getDDLs().isEmpty()) {
			for(String ddlSql : iddl.getDDLs()) {
				String errorMsg = String.format("在别名为%s的数据库中需要对名字为%s的表格作如下修正，请前往处理，参考语句如下：%s",
						dao.getDataSource().getAlias(), tableName, ddlSql);
				logger.warn(errorMsg);
			}
			String errorMsg = String.format("在别名为%s数据库中需要对名字为%s的表格进行修正，请参考上述日志",
					dao.getDataSource().getAlias(), tableName);
			logger.warn(errorMsg);
//			throw new RuntimeException(errorMsg);
		}
		return iddl;
	}
	
	/**
	 * 创建数据表
	 * @param baseDao
	 */
	private static void createTable(IDDL iddl, String tableName, EntityMeta<?> entityMeta) {
		List<String> ddlSqls = iddl.createTableSql(tableName, entityMeta);
		iddl.addDDLs(ddlSqls, "数据库DDL创建表");
	}
	
	/**
	 * 更新数据表结构
	 * @param baseDao
	 */
	private static void updateTable(IDDL iddl, String tableName, EntityMeta<?> entityMeta) {
		//查询数据库中的字段定义
		List<? extends ITableDes> tableDesList = iddl.getTableColumns(tableName);
		
		// 数据表列的名字与数据类型集合
		Map<String, ITableDes> dbColumns = new HashMap<>();
		
		Map<String, ColumnMeta> columnMetas = entityMeta.getColumnMap();
		//先以查询结果来检测数据表中是否存在实体类中没有定义的列，有则删除
		
		for (ITableDes tableDes : tableDesList) {
			String columnName = tableDes.getField();
			if (!columnMetas.containsKey(columnName)) {
				// 删除列
				String ddlSql = iddl.getTableDropColumn(tableName, columnName);
				iddl.addDDL(ddlSql, "数据库DDL删除列");
			} else {
				dbColumns.put(columnName, tableDes);
			}
		}
		//再以实体类元数据来检测数据表中是否缺失了某些列，缺失则添加，
		//没有缺失则对比数据类型，不一致则以实体类数据类型为基础修改数据表的数据类型
		for (Entry<String, ColumnMeta> entry : entityMeta.getColumnMap().entrySet()) {
			ColumnMeta columnMeta = entry.getValue();
			
			if (!dbColumns.containsKey(entry.getKey())) {
				// 新增列
				String ddlSql = iddl.getTableAddColumn(tableName, iddl.getColumnDefine(columnMeta));
				iddl.addDDL(ddlSql, "数据库DDL新增列");
			} else {
				// 数据表中字段的类型
				ITableDes columnDes = dbColumns.get(entry.getKey());
				// 判断字段的类型、长度是否发生了改变 TODO 不同数据库的判定不一样
				boolean isChange = columnMeta.isChange(columnDes.getType(), columnDes.getExtra());
				if (!isChange) {
					continue;
				}
				// 修改列
				String ddlSql = iddl.getTableModifyColumn(tableName, entry.getKey(), columnMeta);
				iddl.addDDL(ddlSql, "数据库DDL修改列");
			}
		}
	}
	
	/**
	 * 更新索引
	 * @param iddl
	 * @param tableName
	 * @param entityIndexes 实体类中定义的索引<索引名字，索引对象>
	 */
	private static void updateIndex(IDDL iddl, String tableName, boolean tablExist, Map<String, IndexMeta> entityIndexes) {
		for(IndexMeta entityIndex : entityIndexes.values()) {
			String realName = tableName + "_" + entityIndex.getRawName();
			entityIndex.setRealName(realName);
			//Mysql和PostgreSql都对索引的名字长度有限制，太长会被截断
			if(entityIndex.getRealName().length() > 60) {
				String errorMsg = String.format("数据表%s的索引%s名字过长，无法添加到数据库", tableName, realName);
				throw new RuntimeException(errorMsg);
			}
		}
		
		if(tablExist) {
			// 数据表中已有的索引<索引名字，索引对象>
			Map<String, IndexMeta> indexMap = getDbIndex(iddl, tableName);
			
			// 以实体类为基准比对数据表中的索引状态
			for(IndexMeta entityIndex : entityIndexes.values()) {
				IndexMeta dbIndex = indexMap.get(entityIndex.getRealName());
				if(dbIndex == null) {
					//添加索引
					String ddlSql = iddl.getTableAddIndex(tableName, entityIndex);
					iddl.addDDL(ddlSql, "数据库DDL添加索引");
				} else {
					if (entityIndex.getType() == dbIndex.getType() && entityIndex.getWay() == dbIndex.getWay()
							&& entityIndex.getColumns().equals(dbIndex.getColumns())) {
						continue;
					}
					//更新索引
					String ddlSql = iddl.getTableModifyIndex(tableName, entityIndex);
					iddl.addDDL(ddlSql, "数据库DDL修改索引");
				}
			}
		} else {
			for(IndexMeta entityIndex : entityIndexes.values()) {
				//添加索引
				String ddlSql = iddl.getTableAddIndex(tableName, entityIndex);
				iddl.addDDL(ddlSql, "数据库DDL添加索引");
			}
		}
	}
	
	/**
	 * 获取某个实体类对应的数据表中所有定义的索引
	 * @param baseDao
	 * @return
	 */
	private static Map<String, IndexMeta> getDbIndex(IDDL iddl, String tableName) {
		// 查询当前表格索引
		List<? extends IIndexStruct> keys = iddl.getKeys(tableName);
		
		// 表中已有的索引<索引名字，索引对象>
		Map<String, IndexMeta> indexMap = new HashMap<>();
		for (IIndexStruct indexStruct : keys) {
			String indexName = indexStruct.getName();
			IndexMeta index = indexMap.get(indexName);
			if(index == null) {
				index = new IndexMeta();
				index.setRealName(indexName);
				//索引的类型
				index.setType(indexStruct.isUnique() ? IndexType.UNIQUE : IndexType.NORMAL);
				//索引的方式
				String way = indexStruct.getWay().toLowerCase();
				index.setWay("btree".equals(way) ? IndexWay.BTREE : IndexWay.HASH);
				
				indexMap.put(indexName, index);
			}
			index.getColumns().add(indexStruct.getColumnName());
		}
		return indexMap;
	}
	
	/**
	 * 更新自增主键
	 * @param baseDao
	 */
	private static void updateAutoIncrement(IDDL iddl, String tableName, boolean tableExist, EntityMeta<?> entityMeta,
			long idStartWith) {
		IPrimaryKeyColumnMeta keyMeta = entityMeta.getPrimaryKeyMeta();
		if(keyMeta.getStrategy() != Strategy.AUTO) {
			return;
		}
		if(tableExist) {
			if(!iddl.hasAutoIncrement(tableName, keyMeta.getColumnName())) {
				//业务开发定义的自增主键
				long customInitId = idStartWith;
				if(customInitId > 0) {
					customInitId = customInitId + 1;
				} else {
					customInitId = keyMeta.getBase() * SERVER_IDENTITY + 1;
				}
				List<String> ddlSqls = iddl.createAutoIncrement(tableName, keyMeta, customInitId);
				logger.info("currAutoId:{}, currMaxId:{}, customInitId:{}, finalMaxId:{}", 0, 0, customInitId, customInitId);
				iddl.addDDLs(ddlSqls, "数据库DDL新增自增ID");
			} else {
				//TODO 判断自增序列的类型是否不变
				
				//当前表中定义的自增主键
				long currAutoId = iddl.getTableAutoinc(tableName, keyMeta.getColumnName());
				long finalMaxId = currAutoId;
				//查询出当前表中最大的自增主键
				long currMaxId = iddl.getTableMaxId(tableName, keyMeta.getColumnName());
				if(currMaxId + 1 > finalMaxId) {
					finalMaxId = currMaxId + 1;
				}
				//业务开发定义的自增主键
				long customInitId = idStartWith;
				if(customInitId > 0) {
					customInitId = customInitId + 1;
				} else {
					customInitId = keyMeta.getBase() * SERVER_IDENTITY + 1;
				}
				if(customInitId > finalMaxId) {
					finalMaxId = customInitId;
				}
				
				if(iddl.checkAutoIncrement(tableName, keyMeta)) {
					List<String> ddlSqls = iddl.createAutoIncrement(tableName, keyMeta, finalMaxId);
					logger.info("currAutoId:{}, currMaxId:{}, customInitId:{}, finalMaxId:{}", currAutoId, currMaxId, customInitId, finalMaxId);
					iddl.addDDLs(ddlSqls, "数据库DDL变更自增ID");
				} else {
					if(finalMaxId > currAutoId) {
						String ddlSql = iddl.setAutoIncrement(tableName, keyMeta.getColumnName(), finalMaxId);
						logger.info("currAutoId:{}, currMaxId:{}, customInitId:{}, finalMaxId:{}", currAutoId, currMaxId, customInitId, finalMaxId);
						iddl.addDDL(ddlSql, "数据库DDL修改自增ID");
					}
				}
			}
		} else {
			//业务开发定义的自增主键
			long customInitId = idStartWith;
			if(customInitId > 0) {
				customInitId = customInitId + 1;
			} else {
				customInitId = keyMeta.getBase() * SERVER_IDENTITY + 1;
			}
			List<String> ddlSqls = iddl.createAutoIncrement(tableName, keyMeta, customInitId);
			logger.info("currAutoId:{}, currMaxId:{}, customInitId:{}, finalMaxId:{}", 0, 0, customInitId, customInitId);
			iddl.addDDLs(ddlSqls, "数据库DDL新增自增ID");
		}
	}
}

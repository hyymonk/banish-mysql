/**
 * 
 */
package org.banish.mysql.table;


import static org.banish.mysql.table.Symbol.DOT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.banish.mysql.AbstractEntity;
import org.banish.mysql.annotation.Id.Strategy;
import org.banish.mysql.annotation.enuma.IndexType;
import org.banish.mysql.annotation.enuma.IndexWay;
import org.banish.mysql.dao.Dao;
import org.banish.mysql.dao.OriginDao;
import org.banish.mysql.orm.EntityMeta;
import org.banish.mysql.orm.IndexMeta;
import org.banish.mysql.orm.column.ColumnMeta;
import org.banish.mysql.orm.column.PrimaryKeyColumnMeta;
import org.banish.mysql.table.ddl.DDL;
import org.banish.mysql.table.ddl.DDL.IndexStruct;
import org.banish.mysql.table.ddl.DDL.TableDes;
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
	public static void build(OriginDao<?> dao, String tableName)  {
		if(SERVER_IDENTITY <= 0) {
			throw new RuntimeException("数据库构建工具还没有指定服务器标识");
		}
		//数据库的名字
		String dbName = dao.getDataSource().getDbName();
		
		List<String> ddlSqls = new ArrayList<>();
		
		if (!DDL.isTableExist(dao.getDataSource(), dbName, tableName)) {
			// 创建数据表
			String ddlSql = createTable(dao, tableName);
			ddlSqls.add(ddlSql);
		} else {
			// 更新数据表结构
			List<String> tableDdlSqls = updateTable(dao, tableName);
			ddlSqls.addAll(tableDdlSqls);
		}
		//更新索引
		List<String> indexDdlSqls = updateIndex(dao, tableName);
		ddlSqls.addAll(indexDdlSqls);
		//更新自增主键
		List<String> autoIncDdlSqls = updateAutoIncrement(dao, tableName);
		ddlSqls.addAll(autoIncDdlSqls);
		
		EntityMeta<?> entityMeta = dao.getEntityMeta();
		if(!entityMeta.isAutoBuild() && !ddlSqls.isEmpty()) {
			for(String ddlSql : ddlSqls) {
				String errorMsg = String.format("在别名为%s的数据库中需要对名字为%s的表格作如下修正，请前往处理，参考语句如下：%s",
						dao.getDataSource().getAlias(), tableName, ddlSql);
				logger.warn(errorMsg);
			}
			String errorMsg = String.format("在别名为%s数据库中需要对名字为%s的表格进行修正，请参考上述日志",
					dao.getDataSource().getAlias(), tableName);
			logger.warn(errorMsg);
//			throw new RuntimeException(errorMsg);
		}
	}
	
	/**
	 * 创建数据表
	 * @param baseDao
	 */
	private static String createTable(OriginDao<?> baseDao, String tableName) {
		EntityMeta<?> entityMeta = baseDao.getEntityMeta();
		String ddlSql = createTableSql(baseDao, tableName);
		if(entityMeta.isAutoBuild()) {
			Dao.executeSql(baseDao.getDataSource(), ddlSql);
		}
		logger.info("数据库DDL新建表：{}", ddlSql);
		return ddlSql;
	}
	
	/**
	 * 构建创建表的语句
	 * @param baseDao
	 * @return
	 */
	private static String createTableSql(OriginDao<?> baseDao, String tableName) {
		EntityMeta<?> entityMeta = baseDao.getEntityMeta();
		StringBuilder result = new StringBuilder();
		result.append("CREATE TABLE ").append(DOT).append(tableName).append(DOT).append(" (");
		
		for(ColumnMeta columnMeta : entityMeta.getColumnList()) {
			result.append(getColumnDefine(columnMeta)).append(",");
		}
		
		ColumnMeta primaryKeyMeta = entityMeta.getPrimaryKeyMeta();
		//创建表的时候只进行了主键的定义，索引的设置会在表构建好之后进行处理
		result.append("PRIMARY KEY (").append(DOT).append(primaryKeyMeta.getColumnName()).append(DOT).append(")").append("\n");
		//这里并没有对自增ID进行初始处理，自增ID的设置会在表构建好之后进行处理
		result.append(") ENGINE=InnoDB DEFAULT CHARSET=").append(entityMeta.getTableCharset().value());
		result.append(" COMMENT='").append(entityMeta.getTableComment()).append("';");
		return result.toString();
	}
	
	/**
	 * 更新数据表结构
	 * @param baseDao
	 */
	private static List<String> updateTable(OriginDao<?> baseDao, String tableName) {
		//查询数据库中的字段定义
		List<TableDes> tableDesList = DDL.getTableColumns(baseDao.getDataSource(), tableName);
		
		// 数据表列的名字与数据类型集合
		Map<String, TableDes> dbColumns = new HashMap<>();
		
		boolean autoBuild = baseDao.getEntityMeta().isAutoBuild();
		
		Map<String, ColumnMeta> columnMetas = baseDao.getEntityMeta().getColumnMap();
		//先以查询结果来检测数据表中是否存在实体类中没有定义的列，有则删除
		
		List<String> updateDdlSqls = new ArrayList<>();
		for (TableDes tableDes : tableDesList) {
			String columnName = tableDes.getField();
			if (!columnMetas.containsKey(columnName)) {
				// 删除列
				String ddlSql = DDL.TABLE_DROP_COLUMN.replaceAll("#tableName#", tableName).replaceAll("#columnName#", columnName);
				if(autoBuild) {
					Dao.executeSql(baseDao.getDataSource(), ddlSql);
				}
				logger.info("数据库DDL删除列：{}", ddlSql);
				updateDdlSqls.add(ddlSql);
			} else {
				dbColumns.put(columnName, tableDes);
			}
		}
		//再以实体类元数据来检测数据表中是否缺失了某些列，缺失则添加，
		//没有缺失则对比数据类型，不一致则以实体类数据类型为基础修改数据表的数据类型
		for (Entry<String, ColumnMeta> entry : baseDao.getEntityMeta().getColumnMap().entrySet()) {
			ColumnMeta columnMeta = entry.getValue();
			
			if (!dbColumns.containsKey(entry.getKey())) {
				// 新增列
				String ddlSql = DDL.TABLE_ADD_COLUMN.replaceAll("#tableName#", tableName).replaceAll(
						"#columnDefine#", getColumnDefine(columnMeta));
				if(autoBuild) {
					Dao.executeSql(baseDao.getDataSource(), ddlSql);
				}
				logger.info("数据库DDL新增列：{}", ddlSql);
				updateDdlSqls.add(ddlSql);
			} else {
				// 数据表中字段的类型
				TableDes columnDes = dbColumns.get(entry.getKey());
				// 判断字段的类型、长度是否发生了改变
				boolean isChange = columnMeta.isChange(columnDes.getType(), columnDes.getExtra());
				if (!isChange) {
					continue;
				}
				// 修改列
				String ddlSql = DDL.TABLE_CHANGE_COLUMN.replaceAll("#tableName#", tableName).replaceAll("#columnName#", entry.getKey())
						.replaceAll("#columnDefine#", getColumnDefine(columnMeta));
				if(autoBuild) {
					Dao.executeSql(baseDao.getDataSource(), ddlSql);
				}
				logger.info("数据库DDL修改列：{}", ddlSql);
				updateDdlSqls.add(ddlSql);
			}
		}
		return updateDdlSqls;
	}
	
	/**
	 * 获取列定义
	 * @param columnMeta
	 * @return
	 */
	private static String getColumnDefine(ColumnMeta columnMeta) {
		StringBuilder result = new StringBuilder();
		
		result.append(DOT).append(columnMeta.getColumnName()).append(DOT).append(" ");
		
		result.append(columnMeta.dbColumnType());
		String defaultValue = columnMeta.defaultValue();
		
		String autoIncrement = "";
		if(columnMeta instanceof PrimaryKeyColumnMeta) {
			PrimaryKeyColumnMeta keyMeta = (PrimaryKeyColumnMeta)columnMeta;
			if(keyMeta.getStrategy() == Strategy.AUTO) {
				autoIncrement = "AUTO_INCREMENT";
			}
		}
		
		result.append(" ").append(defaultValue).append(" ").append(autoIncrement);
		// 字段备注
		result.append(" COMMENT '").append(columnMeta.getComment()).append("'");
		return result.toString();
	}
	
	
	/**
	 * 更新索引
	 * @param baseDao
	 */
	private static List<String> updateIndex(OriginDao<?> baseDao, String tableName) {
		List<String> updateDdlSqls = new ArrayList<>();
		
		boolean autoBuild = baseDao.getEntityMeta().isAutoBuild();
		
		// 数据表中已有的索引<索引名字，索引对象>
		Map<String, IndexMeta> indexMap = getDbIndex(baseDao, tableName);
		// 实体类中定义的索引<索引名字，索引对象>
		Map<String, IndexMeta> entityIndexes = baseDao.getEntityMeta().getIndexMap();
		// 以实体类为基准比对数据表中的索引状态
		for(IndexMeta entityIndex : entityIndexes.values()) {
			IndexMeta dbIndex = indexMap.get(entityIndex.getName().toUpperCase());
			if(dbIndex == null) {
				//添加索引
				String ddlSql = DDL.TABLE_ADD_INDEX.replaceAll("#tableName#", tableName)
						.replaceAll("#indexType#", entityIndex.getType().value())
						.replaceAll("#indexName#", entityIndex.getName())
						.replaceAll("#columnName#", entityIndex.getColumnsString())
						.replaceAll("#indexWay#", entityIndex.getWay().value());
				if(autoBuild) {
					Dao.executeSql(baseDao.getDataSource(), ddlSql);
				}
				logger.info("数据库DDL添加索引：{}", ddlSql);
				updateDdlSqls.add(ddlSql);
			} else {
				if (entityIndex.getType() != dbIndex.getType() || entityIndex.getWay() != dbIndex.getWay()
						|| !entityIndex.getColumns().equals(dbIndex.getColumns())) {
					//更新索引
					String ddlSql = DDL.TABLE_MODIFY_INDEX.replaceAll("#tableName#", tableName)
							.replaceAll("#oriIndex#", dbIndex.getName())
							.replaceAll("#indexType#", entityIndex.getType().value())
							.replaceAll("#indexName#", entityIndex.getName())
							.replaceAll("#columnName#", entityIndex.getColumnsString())
							.replaceAll("#indexWay#", entityIndex.getWay().value());
					if(autoBuild) {
						Dao.executeSql(baseDao.getDataSource(), ddlSql);
					}
					logger.info("数据库DDL修改索引：{}", ddlSql);
					updateDdlSqls.add(ddlSql);
				}
			}
		}
		return updateDdlSqls;
	}
	
	/**
	 * 获取某个实体类对应的数据表中所有定义的索引
	 * @param baseDao
	 * @return
	 */
	private static Map<String, IndexMeta> getDbIndex(OriginDao<?> dao, String tableName) {
		// 查询当前表格索引
		List<IndexStruct> keys = DDL.getKeys(dao.getDataSource(), tableName);
		
		
		// 表中已有的索引<索引名字，索引对象>
		Map<String, IndexMeta> indexMap = new HashMap<>();
		for (IndexStruct indexStruct : keys) {
			String indexName = indexStruct.getName().toUpperCase();
			IndexMeta index = indexMap.get(indexName);
			if(index == null) {
				index = new IndexMeta();
				index.setName(indexName);
				//索引的类型
				int isUnique = indexStruct.getUnique();
				index.setType(isUnique == 0 ? IndexType.UNIQUE : IndexType.NORMAL);
				//索引的方式
				String way = indexStruct.getWay();
				index.setWay(!"BTREE".equals(way) ? IndexWay.HASH : IndexWay.BTREE);
				
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
	private static List<String> updateAutoIncrement(OriginDao<?> baseDao, String tableName) {
		PrimaryKeyColumnMeta keyMeta = baseDao.getEntityMeta().getPrimaryKeyMeta();
		if(keyMeta.getStrategy() != Strategy.AUTO) {
			return Collections.emptyList();
		}
		
		Class<? extends AbstractEntity> clazz = baseDao.getEntityMeta().getClazz();
		long customInitId = 0;
		try {
			AbstractEntity entity = clazz.getConstructor().newInstance();
			customInitId = entity.idGenerator();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		boolean autoBuild = baseDao.getEntityMeta().isAutoBuild();
		List<String> ddlSqls = new ArrayList<>();
		
		//查询出当前表中最大的自增主键
		long currMaxId = DDL.getTableMaxId(baseDao.getDataSource(), tableName, keyMeta.getColumnName());
		
		String mysqlVersion = DDL.getMysqlVersion(baseDao.getDataSource());
		
		String ddlSql = DDL.SET_AUTO_INCREMENT.replaceAll("#tableName#", tableName);
		
		//自增主键的检查
		long currAutoId = 0;
		if(mysqlVersion.startsWith("8")) {
			String tableFullName = baseDao.getDataSource().getDbName() + "/" + tableName;
			currAutoId = DDL.getTableAutoinc8(baseDao.getDataSource(), tableFullName);
		} else {
			currAutoId = DDL.getTableAutoinc5(baseDao.getDataSource(), tableName);
		}
		if(currMaxId >= currAutoId) {
			if(autoBuild) {
				Dao.executeSql(baseDao.getDataSource(), ddlSql, currMaxId + 1);
			}
			logger.info("数据库DDL修改自增ID：{}，参数：{}", ddlSql, currMaxId + 1);
			ddlSqls.add(ddlSql);
		}
		long baseValue = 0;
		if(customInitId > 0) {
			baseValue = customInitId + 1;
		} else {
			baseValue = keyMeta.getBase() * SERVER_IDENTITY + 1;
		}
		if(baseValue > currMaxId && baseValue > currAutoId) {
			if(autoBuild) {
				Dao.executeSql(baseDao.getDataSource(), ddlSql, baseValue);
			}
			logger.info("数据库DDL修改自增ID：{}，参数：{}", ddlSql, baseValue);
			ddlSqls.add(ddlSql);
		}
		return ddlSqls;
	}
}

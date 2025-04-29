/**
 * 
 */
package org.banish.sql.core.orm;

import java.lang.reflect.Field;

import org.banish.sql.core.datasource.IDataSource;
import org.banish.sql.core.entity.AbstractEntity;
import org.banish.sql.core.sql.DefaultDML;
import org.banish.sql.core.sql.IDDL;
import org.banish.sql.core.sql.SplitDML;

/**
 * @author YY
 */
public interface IOrmFactory {
	/**
	 * 创建DDL语句的对象
	 * @param dataSource
	 * @param autoBuild
	 * @return
	 */
	IDDL newDDL(IDataSource dataSource, boolean autoBuild);
	/**
	 * 创建默认的数据库操纵语言的对象
	 * @param <T>
	 * @param entityMeta
	 * @return
	 */
	<T extends AbstractEntity> DefaultDML<T> newDefaultDML(EntityMeta<T> entityMeta);
	/**
	 * 创建分表的数据库操纵语言的对象
	 * @param <T>
	 * @param entityMeta
	 * @param tableName
	 * @return
	 */
	<T extends AbstractEntity> SplitDML<T> newSplitDML(EntityMeta<T> entityMeta, String tableName);
	/**
	 * 构建列的元数据
	 * @param field
	 * @return
	 */
	ColumnMeta newColumnMeta(Field field);
	/**
	 * 构建主键列的元数据
	 * @param field
	 * @return
	 */
	IPrimaryKeyColumnMeta newPrimaryKeyColumnMeta(Field field);
	/**
	 * 格式化索引名字
	 * @param tableName
	 * @param indexAlias
	 * @param fieldNames
	 * @return
	 */
	String formatIndexName(String tableName, String indexAlias, String[] fieldNames);
}

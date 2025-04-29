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

	IDDL newDDL(IDataSource dataSource, boolean autoBuild);
	
	<T extends AbstractEntity> DefaultDML<T> newDefaultDML(EntityMeta<T> entityMeta);
	
	<T extends AbstractEntity> SplitDML<T> newSplitDML(EntityMeta<T> entityMeta, String tableName);
	
	ColumnMeta newColumnMeta(Field field);
	
	IPrimaryKeyColumnMeta newPrimaryKeyColumnMeta(Field field);
	
	String formatIndexName(String tableName, String indexAlias, String[] fieldNames);
}

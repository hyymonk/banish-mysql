/**
 * 
 */
package org.banish.sql.mysql;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

import org.banish.sql.core.datasource.IDataSource;
import org.banish.sql.core.entity.AbstractEntity;
import org.banish.sql.core.orm.ColumnMeta;
import org.banish.sql.core.orm.EntityMeta;
import org.banish.sql.core.orm.IEntityMeta;
import org.banish.sql.core.orm.IOrmFactory;
import org.banish.sql.core.sql.DefaultDML;
import org.banish.sql.core.sql.IDDL;
import org.banish.sql.core.sql.SplitDML;
import org.banish.sql.mysql.ormcolumn.MBigDecimalColumnMeta;
import org.banish.sql.mysql.ormcolumn.MBooleanColumnMeta;
import org.banish.sql.mysql.ormcolumn.MByteColumnMeta;
import org.banish.sql.mysql.ormcolumn.MDateColumnMeta;
import org.banish.sql.mysql.ormcolumn.MDoubleColumnMeta;
import org.banish.sql.mysql.ormcolumn.MEnumColumnMeta;
import org.banish.sql.mysql.ormcolumn.MFloatColumnMeta;
import org.banish.sql.mysql.ormcolumn.MIntegerColumnMeta;
import org.banish.sql.mysql.ormcolumn.MLocalDateTimeColumnMeta;
import org.banish.sql.mysql.ormcolumn.MLongColumnMeta;
import org.banish.sql.mysql.ormcolumn.MPrimaryKeyColumnMeta;
import org.banish.sql.mysql.ormcolumn.MShortColumnMeta;
import org.banish.sql.mysql.ormcolumn.MStringColumnMeta;
import org.banish.sql.mysql.ormcolumn.MStructColumnMeta;
import org.banish.sql.mysql.sql.MySqlDDL;
import org.banish.sql.mysql.sql.MySqlDefaultDML;
import org.banish.sql.mysql.sql.MySqlSplitDML;

/**
 * @author YY
 */
public class MySqlOrmFactory implements IOrmFactory {

	public static final MySqlOrmFactory INS = new MySqlOrmFactory();
	
	private MySqlOrmFactory() {}
	
	@Override
	public IDDL newDDL(IDataSource dataSource, boolean autoBuild) {
		return new MySqlDDL(dataSource, autoBuild);
	}
	
	@Override
	public <T extends AbstractEntity> DefaultDML<T> newDefaultDML(EntityMeta<T> entityMeta) {
		return new MySqlDefaultDML<T>(entityMeta);
	}

	@Override
	public <T extends AbstractEntity> SplitDML<T> newSplitDML(EntityMeta<T> entityMeta, String tableName) {
		return new MySqlSplitDML<T>(entityMeta, tableName);
	}
	
	@Override
	public MPrimaryKeyColumnMeta newPrimaryKeyColumnMeta(Field field) {
		return new MPrimaryKeyColumnMeta(field);
	}
	
	@Override
	public ColumnMeta newColumnMeta(Field field) {
        if(field.getType() == byte.class || field.getType() == Byte.class) {
            return new MByteColumnMeta(field);
            
        } else if(field.getType() == short.class || field.getType() == Short.class) {
        	return new MShortColumnMeta(field);
        	
        } else if(field.getType() == int.class || field.getType() == Integer.class) {
        	return new MIntegerColumnMeta(field);
        	
        } else if(field.getType() == long.class || field.getType() == Long.class) {
        	return MLongColumnMeta.newLongColumnMeta(field);
        	
        } else if(field.getType() == float.class || field.getType() == Float.class) {
        	return new MFloatColumnMeta(field);
        	
        } else if(field.getType() == double.class || field.getType() == Double.class) {
        	return new MDoubleColumnMeta(field);
        	
        } else if(field.getType() == BigDecimal.class) {
        	return new MBigDecimalColumnMeta(field);
        	
        } else if(field.getType() == Date.class) {
        	return new MDateColumnMeta(field);
        	
        } else if(field.getType() == LocalDateTime.class) {
        	return new MLocalDateTimeColumnMeta(field);
        	
        } else if(field.getType() == boolean.class || field.getType() == Boolean.class) {
        	return new MBooleanColumnMeta(field);
        	
        } else if(field.getType() == String.class) {
        	return new MStringColumnMeta(field);
        	
        } else if (field.getType().isEnum()) {
        	return new MEnumColumnMeta(field);
        	
        } else {
        	return new MStructColumnMeta(field);
        }
    }
	
	@Override
	public String formatIndexName(String indexAlias, String[] fieldNames) {
		String indexName = "idx";
		if(indexAlias.equals("")) {
			for (String fieldName : fieldNames) {
				indexName += "_" + IEntityMeta.makeSnakeCase(fieldName);
			}
		} else {
			indexName += "_" + indexAlias;
		}
		return indexName;
	}
}

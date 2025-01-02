/**
 * 
 */
package org.banish.sql.postgresql.orm.column;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

import org.banish.sql.core.datasource.IDataSource;
import org.banish.sql.core.entity.AbstractEntity;
import org.banish.sql.core.orm.ColumnMeta;
import org.banish.sql.core.orm.EntityMeta;
import org.banish.sql.core.orm.IMetaFactory;
import org.banish.sql.core.orm.IPrimaryKeyColumnMeta;
import org.banish.sql.core.sql.DefaultDML;
import org.banish.sql.core.sql.IDDL;
import org.banish.sql.core.sql.SplitDML;
import org.banish.sql.postgresql.sql.PostgreSqlDDL;
import org.banish.sql.postgresql.sql.PostgreSqlDefaultDML;
import org.banish.sql.postgresql.sql.PostgreSqlSplitDML;

/**
 * @author YY
 */
public class PostgreSqlMetaFactory implements IMetaFactory {

	public static final PostgreSqlMetaFactory INS = new PostgreSqlMetaFactory();
	
	private PostgreSqlMetaFactory() {}
	
	@Override
	public IDDL newDDL(IDataSource dataSource, boolean autoBuild) {
		return new PostgreSqlDDL(dataSource, autoBuild);
	}
	
	public <T extends AbstractEntity> DefaultDML<T> newDefaultDML(EntityMeta<T> entityMeta) {
		return new PostgreSqlDefaultDML<T>(entityMeta);
	}
	
	public <T extends AbstractEntity> SplitDML<T> newSplitDML(EntityMeta<T> entityMeta, String tableName) {
		return new PostgreSqlSplitDML<T>(entityMeta, tableName);
	}
	
	@Override
	public ColumnMeta newColumnMeta(Field field) {
        if(field.getType() == byte.class || field.getType() == Byte.class) {
            return new PByteColumnMeta(field);
            
        } else if(field.getType() == short.class || field.getType() == Short.class) {
        	return new PShortColumnMeta(field);
        	
        } else if(field.getType() == int.class || field.getType() == Integer.class) {
        	return new PIntegerColumnMeta(field);
        	
        } else if(field.getType() == long.class || field.getType() == Long.class) {
        	return PLongColumnMeta.newLongColumnMeta(field);
        	
        } else if(field.getType() == float.class || field.getType() == Float.class) {
        	return new PFloatColumnMeta(field);
        	
        } else if(field.getType() == double.class || field.getType() == Double.class) {
        	return new PDoubleColumnMeta(field);
        	
        } else if(field.getType() == BigDecimal.class) {
        	return new PBigDecimalColumnMeta(field);
        	
        } else if(field.getType() == Date.class) {
        	return new PDateColumnMeta(field);
        	
        } else if(field.getType() == LocalDateTime.class) {
        	return new PLocalDateTimeColumnMeta(field);
        	
        } else if(field.getType() == boolean.class || field.getType() == Boolean.class) {
        	return new PBooleanColumnMeta(field);
        	
        } else if(field.getType() == String.class) {
        	return new PStringColumnMeta(field);
        	
        } else if (field.getType().isEnum()) {
        	return new PEnumColumnMeta(field);
        	
        } else {
        	return new PStructColumnMeta(field);
        }
    }

	@Override
	public IPrimaryKeyColumnMeta newPrimaryKeyColumnMeta(Field field) {
		return new PPrimaryKeyColumnMeta(field);
	}
}

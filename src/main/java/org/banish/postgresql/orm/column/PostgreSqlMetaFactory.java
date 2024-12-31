/**
 * 
 */
package org.banish.postgresql.orm.column;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

import org.banish.base.IMetaFactory;
import org.banish.base.IPrimaryKeyColumnMeta;
import org.banish.mysql.orm.ColumnMeta;

/**
 * @author YY
 */
public class PostgreSqlMetaFactory implements IMetaFactory {

	public static final PostgreSqlMetaFactory INS = new PostgreSqlMetaFactory();
	
	private PostgreSqlMetaFactory() {}
	
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

/**
 * 
 */
package org.banish.mysql.orm.column;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

import org.banish.base.IMetaFactory;
import org.banish.mysql.orm.ColumnMeta;

/**
 * @author YY
 */
public class MySqlMetaFactory implements IMetaFactory {

	public static final MySqlMetaFactory INS = new MySqlMetaFactory();
	
	private MySqlMetaFactory() {}
	
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
	public MPrimaryKeyColumnMeta newPrimaryKeyColumnMeta(Field field) {
		return new MPrimaryKeyColumnMeta(field);
	}
}

/**
 * 
 */
package org.banish.mysql.orm.column;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.banish.mysql.annotation.Column;
import org.banish.mysql.annotation.Id;

/**
 * @author YY
 * 表列的信息
 */
public abstract class ColumnMeta {
	/**
	 * 字段类型
	 */
	protected final Class<?> clazz;
	/**
	 * 字段的反射
	 */
	protected final Field field;
	/**
	 * 类的字段名
	 */
	protected final String fieldName;
	/**
	 * 表的列名
	 */
	protected final String columnName;
	/**
	 * 是否只读
	 */
	protected final boolean readonly;
	/**
	 * 备注
	 */
	protected final String comment;
	/**
	 * 列的默认值
	 */
	protected final String defaultValue;
	
	protected ColumnMeta(Field field) {
		this.field = field;
		//用于通过反射来对对象中的属性进行赋值
		this.field.setAccessible(true);
		this.clazz = field.getType();
		
		Column column = field.getAnnotation(Column.class);
		if(column == null) {
			//非实体表类的结构进行查询
			this.fieldName = this.field.getName();
			this.columnName = this.field.getName();
			this.readonly = false;
			this.comment = "";
			this.defaultValue = "";
		} else {
			this.fieldName = this.field.getName();
			if("".equals(column.name())) {
				this.columnName = this.field.getName();
			} else {
				this.columnName = column.name();
			}
			this.readonly = column.readonly();
			this.comment = column.comment();
			this.defaultValue = column.defaultValue();
		}
	}
	
	public abstract Object takeValue(Object t) throws Exception;
	public abstract void fillValue(Object t, int columnIndex, ResultSet rs) throws Exception;
	/**
	 * 数据库列的类型
	 * @param length
	 * @param extra
	 * @return
	 */
	public abstract String dbColumnType();
	/**
	 * 数据库中的默认值
	 * @param defaultValue
	 * @param extra
	 * @return
	 */
	public abstract String defaultValue();
	/**
	 * 数据库中的列定义是否发生了改变
	 * @param javaFieldType
	 * @param dbColumnType
	 * @param length
	 * @param extra
	 * @return
	 */
	public abstract boolean isChange(String dbColumnType, String dbColumnExtra);

	public Class<?> getClazz() {
		return clazz;
	}
	public Field getField() {
		return field;
	}
	public boolean isReadonly() {
		return readonly;
	}
	public String getFieldName() {
		return fieldName;
	}
	public String getColumnName() {
		return columnName;
	}
	public String getComment() {
		return comment;
	}
	
	/**
	 * 创建字段的元数据，并返回主键原数据
	 * @param allFields
	 * @param columnList
	 * @param columnMap
	 * @return
	 */
    public static PrimaryKeyColumnMeta buildAndReturnKey(List<Field> allFields, List<ColumnMeta> columnList, Map<String, ColumnMeta> columnMap) {
        //列信息
    	PrimaryKeyColumnMeta idMeta = null;
        for (Field field : allFields) {
            Column column = field.getAnnotation(Column.class);
            if(column == null) {
                continue;
            }
            Id id = field.getAnnotation(Id.class);
            ColumnMeta columnMeta = null;
            if(id != null) {
            	if(idMeta == null) {
    				idMeta = new PrimaryKeyColumnMeta(field);
    			} else {
    				throw new RuntimeException(field.getDeclaringClass().getSimpleName() + " @Id field more than one");
    			}
            	columnMeta = idMeta;
            } else {
            	columnMeta = build(field);
            }
            //构建数据库字段与对象属性关系
            columnList.add(columnMeta);
            columnMap.put(columnMeta.getColumnName(), columnMeta);
        }
        return idMeta;
    }
    
    public static void buildAlias(List<Field> allFields, List<ColumnMeta> columnList, Map<String, ColumnMeta> columnMap) {
        //列信息
        for (Field field : allFields) {
            ColumnMeta columnMeta = build(field);        
            //构建数据库字段与对象属性关系
            columnList.add(columnMeta);
            columnMap.put(columnMeta.getColumnName(), columnMeta);
        }
    }
    
    private static ColumnMeta build(Field field) {
        if(field.getType() == byte.class || field.getType() == Byte.class) {
            return new ByteColumnMeta(field);
            
        } else if(field.getType() == short.class || field.getType() == Short.class) {
        	return new ShortColumnMeta(field);
        	
        } else if(field.getType() == int.class || field.getType() == Integer.class) {
        	return new IntegerColumnMeta(field);
        	
        } else if(field.getType() == long.class || field.getType() == Long.class) {
        	return LongColumnMeta.newLongColumnMeta(field);
        	
        } else if(field.getType() == float.class || field.getType() == Float.class) {
        	return new FloatColumnMeta(field);
        	
        } else if(field.getType() == double.class || field.getType() == Double.class) {
        	return new DoubleColumnMeta(field);
        	
        } else if(field.getType() == BigDecimal.class) {
        	return new BigDecimalColumnMeta(field);
        	
        } else if(field.getType() == Date.class) {
        	return new DateColumnMeta(field);
        	
        } else if(field.getType() == LocalDateTime.class) {
        	return new LocalDateTimeColumnMeta(field);
        	
        } else if(field.getType() == boolean.class || field.getType() == Boolean.class) {
        	return new BooleanColumnMeta(field);
        	
        } else if(field.getType() == String.class) {
        	return new StringColumnMeta(field);
        	
        } else if (field.getType().isEnum()) {
        	return new EnumColumnMeta(field);
        	
        } else {
        	return new StructColumnMeta(field);
        }
    }
}
/**
 * 
 */
package org.banish.sql.core.orm;

import java.lang.reflect.Field;
import java.sql.ResultSet;

import org.banish.sql.core.annotation.Column;

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
	//取消了默认值的设定，
	//	第一程序字段在创建实体时对于原生类型有默认值，如boolean类型，程序的默认值是false，但声明默认值为true就显得矛盾
	//	第二像结构体、容器类、时间类字段也应该在创建实体时设置对应的值
	
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
		} else {
			this.fieldName = this.field.getName();
			if("".equals(column.name())) {
				this.columnName = IEntityMeta.makeSnakeCase(this.field.getName());
			} else {
				this.columnName = column.name();
			}
			this.readonly = column.readonly();
			this.comment = column.comment();
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
}
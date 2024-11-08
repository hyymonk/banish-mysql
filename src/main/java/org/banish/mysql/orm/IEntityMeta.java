/**
 * 
 */
package org.banish.mysql.orm;

import java.util.List;

import org.banish.mysql.orm.column.ColumnMeta;

/**
 * @author YY
 *
 */
public interface IEntityMeta<T> {

	T newInstance() throws Exception;

	ColumnMeta getColumnMeta(String columnName);

	List<ColumnMeta> getColumnList();
}

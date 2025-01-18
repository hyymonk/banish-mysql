/**
 * 
 */
package org.banish.sql.mysql.sql;


import org.banish.sql.core.entity.AbstractEntity;
import org.banish.sql.core.orm.ColumnMeta;
import org.banish.sql.core.orm.EntityMeta;
import org.banish.sql.core.sql.DefaultDML;

/**
 * @author YY
 *
 */
public class MySqlDefaultDML<T extends AbstractEntity> extends DefaultDML<T> {
	
	public MySqlDefaultDML(EntityMeta<T> entityMeta) {
		super(entityMeta);
	}
	
	@Override
	protected String dot() {
		return "`";
	}

	public String insertUpdate(EntityMeta<T> entityMeta, int dataCount) {
		StringBuilder sql = new StringBuilder();
		sql.append(String.format("INSERT INTO `%s` (", TABLE_NAME));
		boolean isFirst = true;
		for(ColumnMeta column : entityMeta.getInsertColumnList()) {
			if(!isFirst) {
				sql.append(",");
			}
			sql.append(String.format("`%s`", column.getColumnName()));
			isFirst = false;
		}
		sql.append(") VALUES ");
		
		isFirst = true;
		for(int c = 0; c < dataCount; c++) {
			if(!isFirst) {
				sql.append(",");
			}
			
			boolean firstColumn = true;
			sql.append("(");
			for(int i = 0; i < entityMeta.getInsertColumnList().size(); i++) {
				if(!firstColumn) {
					sql.append(",");
				}
				sql.append("?");
				firstColumn = false;
			}
			sql.append(")");
			
			isFirst = false;
		}
		sql.append(" ON DUPLICATE KEY UPDATE ");
		isFirst = true;
		for (ColumnMeta columnMeta : entityMeta.getInsertColumnList()) {
			if (columnMeta == entityMeta.getPrimaryKeyMeta()) {
				continue;
			}
			if(columnMeta.isReadonly()) {
				continue;
			}
			if (!isFirst) {
				sql.append(",");
			}
			isFirst = false;
			sql.append(String.format("`%s`= VALUES(%s)", columnMeta.getColumnName(), columnMeta.getColumnName()));
		}
		return sql.toString();
	}
}

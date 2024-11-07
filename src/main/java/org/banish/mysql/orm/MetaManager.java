/**
 * 
 */
package org.banish.mysql.orm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.banish.mysql.annotation.SplitTable;
import org.banish.mysql.annotation.Table;
import org.banish.mysql.annotation.enuma.AsyncType;

/**
 * @author YY
 *
 */
public class MetaManager {
	/**
	 * 实体类元数据 
	 */
	private static Map<Class<?>, EntityMeta<?>> METAS;
	
	public static List<EntityMeta<?>> getMetas() {
		return new ArrayList<>(METAS.values());
	}
	
	public static void buildMeta(Collection<Class<?>> entityClasses) {
		Map<Class<?>, EntityMeta<?>> tempMetas = new HashMap<>();
		for(Class<?> clazz : entityClasses) {
			EntityMeta<?> entityMeta = null;
			
			Table table = clazz.getAnnotation(Table.class);
			SplitTable splitTable = clazz.getAnnotation(SplitTable.class);
			if(table != null && splitTable != null) {
				throw new RuntimeException("entity contains multiple @?Table annotation");
			}
			if(table == null && splitTable == null) {
				throw new RuntimeException(clazz.getSimpleName() + " entity class not contains any @?Table annotation");
			}
			if(table != null) {
				//普通的数据表
				if(table.asyncType() == AsyncType.NONE) {
					entityMeta = new DefaultEntityMeta<>(clazz);
				} else {
					entityMeta = new DefaultAsyncEntityMeta<>(clazz);
				}
			} else if(splitTable != null) {
				//按时间分表的数据表
				if(splitTable.asyncType() == AsyncType.NONE) {
					entityMeta = new SplitEntityMeta<>(clazz);
				} else {
					entityMeta = new SplitAsyncEntityMeta<>(clazz);
				}
			}
			tempMetas.put(entityMeta.getClazz(), entityMeta);
		}
		METAS = tempMetas;
	}
}

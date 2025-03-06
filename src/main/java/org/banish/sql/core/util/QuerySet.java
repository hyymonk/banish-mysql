/**
 * 
 */
package org.banish.sql.core.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author YY
 *
 */
public class QuerySet {

	public static class QueryCondition {
		private final String filter;
		private final List<Object> values = new ArrayList<>();

		public QueryCondition(String filter, Object... values) {
			this.filter = filter;
			this.values.addAll(Arrays.asList(values));
		}
	}

	private final List<QueryCondition> conditions = new ArrayList<>();
	private int page; // 从1开始
	private int pageSize;
	private String orderBy;
	private String groupBy;

	private String where;
	private Object[] params;

	private String countWhere;
	private Object[] countParams;

	public void addCondition(QueryCondition condition) {
		conditions.add(condition);
	}

	/**
	 * @param filter 数据库的字段名，如果是like的匹配，填写格式如some like ?, "%" + some + "%"
	 * @param values
	 */
	public void addCondition(String filter, Object... values) {
		conditions.add(new QueryCondition(filter, values));
	}
	
	public void like(String columnName, String value) {
		conditions.add(new QueryCondition(columnName + " like ?", "%" + value + "%"));
	}

	public void findInSet(String columnName, Collection<?> values) {
		conditions.add(new QueryCondition("FIND_IN_SET(" + columnName + ", ?)", QuerySet.join(values, ",")));
	}
	
	public void in(String columnName, Collection<?> values) {
		String inClause = values.stream().map(id -> "?").reduce((a, b) -> a + "," + b).orElse("");
		conditions.add(new QueryCondition(columnName + " in (" + inClause + ")", values.toArray()));
	}

	public void orderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	public void groupBy(String groupBy) {
		this.groupBy = groupBy;
	}

	public void limit(int page, int pageSize) {
		if (page <= 0) {
			page = 1;
		}
		if (pageSize <= 0) {
			pageSize = 50;
		}
		this.page = page;
		this.pageSize = pageSize;
	}

	public void formWhere() {
		StringBuilder where = new StringBuilder();
		List<Object> params = new ArrayList<>();
		if (!conditions.isEmpty()) {
			where.append("where ");
			boolean isFirst = true;
			for (QueryCondition condition : conditions) {
				if (!isFirst) {
					where.append(" and ");
				}
				where.append(condition.filter);
				params.addAll(condition.values);
				isFirst = false;
			}
		}
		this.countWhere = where.toString();
		this.countParams = params.toArray();
		if (this.groupBy != null) {
			where.append(" ").append(this.groupBy);
		}
		if (this.orderBy != null) {
			where.append(" ").append(this.orderBy);
		}
		if (pageSize > 0) {
			where.append(" limit ? offset ?");
			params.add(pageSize);
			int startIndex = (page - 1) * pageSize;
			params.add(startIndex);
		}
		this.where = where.toString();
		this.params = params.toArray();
	}

	public String getWhere() {
		return where;
	}

	public Object[] getParams() {
		return params;
	}

	public String getCountWhere() {
		return countWhere;
	}

	public Object[] getCountParams() {
		return countParams;
	}

	public void print() {
		System.out.println(where);
		System.out.println(Arrays.toString(params));
		System.out.println(countWhere);
		System.out.println(Arrays.toString(countParams));
	}

	public static String join(Collection<?> objs, String sperator) {
		StringBuilder builder = new StringBuilder();
		boolean isFirst = true;
		for (Object obj : objs) {
			if (!isFirst) {
				builder.append(sperator);
			}
			builder.append(obj.toString());
			isFirst = false;
		}
		return builder.toString();
	}
}

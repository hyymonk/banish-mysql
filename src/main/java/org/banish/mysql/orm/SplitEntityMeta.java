/**
 * 
 */
package org.banish.mysql.orm;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

import org.banish.mysql.annotation.SplitTable;
import org.banish.mysql.annotation.SplitTable.SplitWay;
import org.banish.mysql.orm.column.ColumnMeta;
import org.banish.mysql.orm.column.DateColumnMeta;
import org.banish.mysql.orm.column.IntegerColumnMeta;
import org.banish.mysql.orm.column.LocalDateTimeColumnMeta;
import org.banish.mysql.orm.column.LongColumnMeta;
import org.banish.mysql.orm.column.LongTimeColumnMeta;
import org.banish.mysql.orm.table.LogTableInfo;

/**
 * @author YY
 *
 */
public class SplitEntityMeta<T> extends EntityMeta<T> {
	
	private static ThreadLocal<DateFormat> yyyy = new ThreadLocal<>();
	private static ThreadLocal<DateFormat> yyyyMM = new ThreadLocal<>();
	private static ThreadLocal<DateFormat> yyyyww = new ThreadLocal<>();
	private static ThreadLocal<DateFormat> yyyyMMdd = new ThreadLocal<>();
	private static ThreadLocal<DateFormat> yyyyMMddHH = new ThreadLocal<>();
	private static ThreadLocal<DateFormat> yyyyMMddHHmm = new ThreadLocal<>();

	/**
	 * 分表的字段元数据
	 */
	private final ColumnMeta splitMeta;
	
	private final SplitWay splitWay;
	
	public SplitEntityMeta(Class<T> clazz) {
		super(clazz, new LogTableInfo(clazz.getAnnotation(SplitTable.class)));
		
		//分表的元数据信息
		SplitTable splitTable = clazz.getAnnotation(SplitTable.class);
		
		if(splitTable != null) {
			this.splitWay = splitTable.way();
			ColumnMeta splitingMeta = this.getColumnMap().get(splitTable.byColumn());
			if(splitingMeta == null) {
				throw new RuntimeException(
						clazz.getSimpleName() + " can not find @TimeTable column named " + splitTable.byColumn());
			}
			if(splitingMeta instanceof DateColumnMeta) {
				this.splitMeta = splitingMeta;
			} else if(splitingMeta instanceof LocalDateTimeColumnMeta) {
				this.splitMeta = splitingMeta;
			} else if(splitingMeta instanceof LongColumnMeta) {
				this.splitMeta = splitingMeta;
			} else if(splitingMeta instanceof LongTimeColumnMeta) {
				this.splitMeta = splitingMeta;
			} else if(splitingMeta instanceof IntegerColumnMeta) {
				this.splitMeta = splitingMeta;
			} else {
				throw new RuntimeException(String.format("实体类%s的字段%s无法作为时间分表的元数据", clazz.getSimpleName(), splitingMeta.getFieldName()));
			}
		} else {
			this.splitWay = SplitWay.NULL;
			this.splitMeta = null;
		}
	}
	
	public String getLogTableName(long splitValue) {
		String formatName = getFormat(splitValue);
		return this.getTableName() + "_" + formatName;
	}
	
	public String getLogTableName(Object t) {
		long splitValue = splitValue(t);
		String formatName = getFormat(splitValue);
		return this.getTableName() + "_" + formatName;
	}
	
	private long splitValue(Object t) {
		try {
			Object timeObj = splitMeta.takeValue(t);
			if(timeObj != null) {
				if(this.splitWay == SplitWay.VALUE) {
					if(timeObj instanceof Long) {
						return ((Long) timeObj).longValue();
					} else if(timeObj instanceof Integer) {
						return ((Integer) timeObj).intValue() * 1000L;
					}
				} else {
					if(timeObj instanceof Date) {
						Date date = (Date)timeObj;
						ZonedDateTime zonedDateTime = date.toInstant().atZone(ZoneOffset.systemDefault());
						return zonedDateTime.toInstant().toEpochMilli();
					} else if(timeObj instanceof LocalDateTime) {
						LocalDateTime localDateTime = (LocalDateTime)timeObj;
						ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneOffset.systemDefault());
						return zonedDateTime.toInstant().toEpochMilli();
					} else if(timeObj instanceof Long) {
						return ((Long) timeObj).longValue();
					} else if(timeObj instanceof Integer) {
						return ((Integer) timeObj).intValue() * 1000L;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return System.currentTimeMillis();
	}

	public SplitWay getSplitWay() {
		return splitWay;
	}
	
	private String getFormat(long splitValue) {
		DateFormat dateFormat  = null;
		if(splitWay == SplitWay.YEAR) {
			dateFormat = yyyy.get();
			if(dateFormat == null) {
				dateFormat = new SimpleDateFormat("yyyy");
				yyyy.set(dateFormat);
			}
			return dateFormat.format(new Date(splitValue));
		} else if(splitWay == SplitWay.MONTH) {
			dateFormat = yyyyMM.get();
			if(dateFormat == null) {
				dateFormat = new SimpleDateFormat("yyyyMM");
				yyyyMM.set(dateFormat);
			}
			return dateFormat.format(new Date(splitValue));
		} else if(splitWay == SplitWay.WEEK) {
			dateFormat = yyyyww.get();
			if(dateFormat == null) {
				dateFormat = new SimpleDateFormat("yyyyww");
				yyyyww.set(dateFormat);
			}
			return dateFormat.format(new Date(splitValue));
		} else if(splitWay == SplitWay.DAY) {
			dateFormat = yyyyMMdd.get();
			if(dateFormat == null) {
				dateFormat = new SimpleDateFormat("yyyyMMdd");
				yyyyMMdd.set(dateFormat);
			}
			return dateFormat.format(new Date(splitValue));
		} else if(splitWay == SplitWay.HOUR) {
			dateFormat = yyyyMMddHH.get();
			if(dateFormat == null) {
				dateFormat = new SimpleDateFormat("yyyyMMddHH");
				yyyyMMddHH.set(dateFormat);
			}
			return dateFormat.format(new Date(splitValue));
		} else if(splitWay == SplitWay.MINUTE) {
			dateFormat = yyyyMMddHHmm.get();
			if(dateFormat == null) {
				dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
				yyyyMMddHHmm.set(dateFormat);
			}
			return dateFormat.format(new Date(splitValue));
		} else if(splitWay == SplitWay.VALUE) {
			return splitValue + "";
		} else {
			throw new RuntimeException("no supporting split way " + splitWay);
		}
	}
	
}

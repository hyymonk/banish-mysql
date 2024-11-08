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
import org.banish.mysql.annotation.enuma.SplitWay;
import org.banish.mysql.orm.column.ByteColumnMeta;
import org.banish.mysql.orm.column.ColumnMeta;
import org.banish.mysql.orm.column.DateColumnMeta;
import org.banish.mysql.orm.column.IntegerColumnMeta;
import org.banish.mysql.orm.column.LocalDateTimeColumnMeta;
import org.banish.mysql.orm.column.LongColumnMeta;
import org.banish.mysql.orm.column.LongTimeColumnMeta;
import org.banish.mysql.orm.column.ShortColumnMeta;
import org.banish.mysql.orm.column.StringColumnMeta;
import org.banish.mysql.orm.table.SplitTableInfo;

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
		super(clazz, new SplitTableInfo(clazz.getAnnotation(SplitTable.class)));
		
		//分表的元数据信息
		SplitTable splitTable = clazz.getAnnotation(SplitTable.class);
		
		if(splitTable != null) {
			this.splitWay = splitTable.way();
			ColumnMeta splitingMeta = this.getColumnMap().get(splitTable.byColumn());
			if(splitingMeta == null) {
				throw new RuntimeException(
						clazz.getSimpleName() + " can not find @TimeTable column named " + splitTable.byColumn());
			}
			if(this.splitWay == SplitWay.VALUE) {
				if(splitingMeta instanceof LongColumnMeta) {
					this.splitMeta = splitingMeta;
				} else if(splitingMeta instanceof IntegerColumnMeta) {
					this.splitMeta = splitingMeta;
				} else if(splitingMeta instanceof ShortColumnMeta) {
					this.splitMeta = splitingMeta;
				} else if(splitingMeta instanceof ByteColumnMeta) {
					this.splitMeta = splitingMeta;
				} else if(splitingMeta instanceof StringColumnMeta) {
					this.splitMeta = splitingMeta;
				} else {
					throw new RuntimeException(String.format("实体类%s的字段%s无法作为分表的元数据", clazz.getSimpleName(), splitingMeta.getFieldName()));
				}
			} else if (this.splitWay == SplitWay.MINUTE || this.splitWay == SplitWay.HOUR
					|| this.splitWay == SplitWay.DAY || this.splitWay == SplitWay.WEEK
					|| this.splitWay == SplitWay.MONTH || this.splitWay == SplitWay.YEAR) {
				if(splitingMeta instanceof DateColumnMeta) {
					this.splitMeta = splitingMeta;
				} else if(splitingMeta instanceof LocalDateTimeColumnMeta) {
					this.splitMeta = splitingMeta;
				} else if(splitingMeta instanceof LongTimeColumnMeta) {
					this.splitMeta = splitingMeta;
				} else if(splitingMeta instanceof LongColumnMeta) {
					this.splitMeta = splitingMeta;
				} else {
					throw new RuntimeException(String.format("实体类%s的字段%s无法作为分表的元数据", clazz.getSimpleName(), splitingMeta.getFieldName()));
				}
			} else {
				this.splitMeta = null;
			}
		} else {
			this.splitWay = SplitWay.NULL;
			this.splitMeta = null;
		}
	}
	
	public String getSplitTableName(Object splitValue) {
		String formatName = getSplitPartition(splitValue);
		return this.getTableName() + "_" + formatName;
	}
	
	public String getSplitTableNameByEntity(T t) {
		try {
			Object splitValue = splitMeta.takeValue(t);
			String splitPartition = getSplitPartition(splitValue);
			return this.getTableName() + "_" + splitPartition;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this.getTableName();
	}
	
	private String getSplitPartition(Object splitValue) {
		try {
			if(this.splitWay == SplitWay.NULL) {
				return "";
			} else if(this.splitWay == SplitWay.VALUE) {
				if(splitValue instanceof Long) {
					return splitValue.toString();
				} else if(splitValue instanceof Integer) {
					return splitValue.toString();
				} else if(splitValue instanceof Short) {
					return splitValue.toString();
				} else if(splitValue instanceof Byte) {
					return splitValue.toString();
				} else if(splitValue instanceof String) {
					return splitValue.toString();
				}
			} else {
				long millisTime = 0;
				if(splitValue instanceof Date) {
					Date date = (Date)splitValue;
					ZonedDateTime zonedDateTime = date.toInstant().atZone(ZoneOffset.systemDefault());
					millisTime = zonedDateTime.toInstant().toEpochMilli();
				} else if(splitValue instanceof LocalDateTime) {
					LocalDateTime localDateTime = (LocalDateTime)splitValue;
					ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneOffset.systemDefault());
					millisTime = zonedDateTime.toInstant().toEpochMilli();
				} else if(splitValue instanceof Long) {
					millisTime = ((Long) splitValue).longValue();
				}
				return formatTime(millisTime);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public SplitWay getSplitWay() {
		return splitWay;
	}
	
	private String formatTime(long millisTime) {
		DateFormat dateFormat  = null;
		if(splitWay == SplitWay.YEAR) {
			dateFormat = yyyy.get();
			if(dateFormat == null) {
				dateFormat = new SimpleDateFormat("yyyy");
				yyyy.set(dateFormat);
			}
			return dateFormat.format(new Date(millisTime));
		} else if(splitWay == SplitWay.MONTH) {
			dateFormat = yyyyMM.get();
			if(dateFormat == null) {
				dateFormat = new SimpleDateFormat("yyyyMM");
				yyyyMM.set(dateFormat);
			}
			return dateFormat.format(new Date(millisTime));
		} else if(splitWay == SplitWay.WEEK) {
			dateFormat = yyyyww.get();
			if(dateFormat == null) {
				dateFormat = new SimpleDateFormat("yyyyww");
				yyyyww.set(dateFormat);
			}
			return dateFormat.format(new Date(millisTime));
		} else if(splitWay == SplitWay.DAY) {
			dateFormat = yyyyMMdd.get();
			if(dateFormat == null) {
				dateFormat = new SimpleDateFormat("yyyyMMdd");
				yyyyMMdd.set(dateFormat);
			}
			return dateFormat.format(new Date(millisTime));
		} else if(splitWay == SplitWay.HOUR) {
			dateFormat = yyyyMMddHH.get();
			if(dateFormat == null) {
				dateFormat = new SimpleDateFormat("yyyyMMddHH");
				yyyyMMddHH.set(dateFormat);
			}
			return dateFormat.format(new Date(millisTime));
		} else if(splitWay == SplitWay.MINUTE) {
			dateFormat = yyyyMMddHHmm.get();
			if(dateFormat == null) {
				dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
				yyyyMMddHHmm.set(dateFormat);
			}
			return dateFormat.format(new Date(millisTime));
		} else {
			throw new RuntimeException("no supporting split way " + splitWay);
		}
	}
	
}

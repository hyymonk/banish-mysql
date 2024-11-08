/**
 * 
 */
package org.banish.mysql;

import java.time.LocalDateTime;

import org.banish.mysql.annotation.Column;
import org.banish.mysql.annotation.MappedSuperclass;
import org.banish.mysql.annotation.MappedSuperclass.Priority;

/**
 * @author YY
 *
 */
@MappedSuperclass(sort = Priority.FIRST)
public abstract class AbstractEntity {
	@Column(comment = "插入时间", readonly = true)
	private LocalDateTime insertTime;
	@Column(comment = "更新时间")
	private LocalDateTime updateTime;
	
	public final LocalDateTime getInsertTime() {
		return insertTime;
	}
	public final void setInsertTime(LocalDateTime insertTime) {
		this.insertTime = insertTime;
	}
	public final LocalDateTime getUpdateTime() {
		return updateTime;
	}
	public final void setUpdateTime(LocalDateTime updateTime) {
		this.updateTime = updateTime;
	}
	//ID生成器
	public long idGenerator() {
		return 0;
	}
}

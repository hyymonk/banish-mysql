/**
 * 
 */
package org.banish.sql.core.orm.tableinfo;

import org.banish.sql.core.annotation.Index;
import org.banish.sql.core.annotation.Table;
import org.banish.sql.core.annotation.enuma.AsyncType;
import org.banish.sql.core.annotation.enuma.Charset;
import org.banish.sql.core.annotation.enuma.UpdateType;

/**
 * @author YY
 *
 */
public class DefaultTableInfo implements ITable {

	private final String name;
	private final String comment;
	private final String dbAlias;
	private final Index[] indexs;
	private final Charset charset;
	private final AsyncType asyncType;
	private final int asyncSize;
	private final int asyncDelay;
	private final boolean autoBuild;
	private final UpdateType updateType;
	
	public DefaultTableInfo(Table table) {
		this.name = table.name();
		this.comment = table.comment();
		this.dbAlias = table.dbAlias();
		this.indexs = table.indexs();
		this.charset = table.charset();
		this.asyncType = table.asyncType();
		this.asyncSize = table.asyncSize();
		this.asyncDelay = table.asyncDelay();
		this.autoBuild = table.autoBuild();
		this.updateType = table.updateType();
	}
	
	@Override
	public String name() {
		return name;
	}

	@Override
	public String comment() {
		return comment;
	}

	@Override
	public String dbAlias() {
		return dbAlias;
	}

	@Override
	public Index[] indexs() {
		return indexs;
	}

	@Override
	public Charset charset() {
		return charset;
	}

	@Override
	public AsyncType asyncType() {
		return asyncType;
	}

	@Override
	public int asyncSize() {
		return asyncSize;
	}

	@Override
	public int asyncDelay() {
		return asyncDelay;
	}

	@Override
	public boolean autoBuild() {
		return autoBuild;
	}
	
	public UpdateType updateType() {
		return updateType;
	}
}

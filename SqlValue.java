package com.gsteam.common.util.dao;

import java.util.List;

public class SqlValue {
	private String sql;
	private List<Object> values;
	
	public SqlValue(String sql, List<Object> values) {
		this.sql = sql;
		this.values = values;
	}
	public String getSql() {
		return sql;
	}
	public void setSql(String sql) {
		this.sql = sql;
	}
	public List<Object> getValues() {
		return values;
	}
	public void setValues(List<Object> values) {
		this.values = values;
	}
}

package org.leeyaf.dborm;

import java.util.ArrayList;
import java.util.List;

/**
 * SqlQuery contains sql and parameters
 * 
 * @author leeyaf
 *
 */
public class SqlQuery {
	private StringBuilder sql=new StringBuilder();
	private List<Object> params=new ArrayList<Object>();
	
	public SqlQuery(){
	}
	
	public SqlQuery(String sql){
		this.sql.append(sql);
	}
	
	public SqlQuery(String sql,Object[] params){
		this.sql.append(sql);
		for (Object param : params) {
			this.params.add(param);
		}
	}
	
	public SqlQuery appendSql(String sql){
		this.sql.append(sql);
		return this;
	}
	
	public SqlQuery addParam(Object param){
		this.params.add(param);
		return this;
	}
	
	public SqlQuery paramAddAll(Object[] params){
		for (Object object : params) {
			this.params.add(object);
		}
		return this;
	}
	
	public String getSql(){
		return this.sql.toString();
	}
	
	public StringBuilder getSqlData(){
		return this.sql;
	}
	
	public Object[] getParams(){
		return this.params.toArray();
	}
	
	public List<Object> getParamsData(){
		return this.params;
	}
}

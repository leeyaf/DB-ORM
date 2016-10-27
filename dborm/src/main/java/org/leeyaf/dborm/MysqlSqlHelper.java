package org.leeyaf.dborm;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * some sql operations and entity operation support
 * 
 * @author leeyaf
 *
 */
public class MysqlSqlHelper extends AbstractSqlHelper{
	
	@Override
	public SqlQuery getCountSql(SqlQuery query){
		String beforSql=query.getSql();
		beforSql="select count(*) "+beforSql.substring(beforSql.indexOf("from"));
		
		int orderIndex,limitIndex;
		if((orderIndex=beforSql.indexOf("order"))>-1)
			beforSql=beforSql.substring(0, orderIndex);
		if((limitIndex=beforSql.indexOf("limit"))>-1)
			beforSql=beforSql.substring(0,limitIndex);
		
		SqlQuery countQuery=new SqlQuery(beforSql);
		Object[] params;
		if(beforSql.contains("?")){
			byte[] ba=beforSql.getBytes();
			int count=0;
			for (byte b : ba) 
				if('?'==b) count++;
			params=new Object[count];
			System.arraycopy(query.getParams(), 0, params, 0, count);
			countQuery.paramAddAll(params);
		}
		return countQuery;
	}
	
	@Override
	public <T> SqlQuery createSaveSql(T entity) throws Exception {
		Class<?> entityClass = entity.getClass();
		StringBuilder builder = new StringBuilder("insert into ");
		String tableName=camelConvertFieldName(entityClass.getSimpleName());
		builder.append(tableName).append(" ( ");
		List<Object> values = new ArrayList<Object>();
		Field[] fields = entityClass.getDeclaredFields();
		for (Field field : fields) {
			String key = camelConvertFieldName(field.getName());
			field.setAccessible(true);
			Object value = field.get(entity);
			if (value==null) continue;
			builder.append(key).append(" , ");
			values.add(value);
		}
		if (values.size()<1) return null;
		builder.delete(builder.lastIndexOf(" , "), builder.length());
		builder.append(" ) values ( ");
		for (int i = 0; i < values.size(); i++) {
			builder.append("? , ");
		}
		builder.delete(builder.lastIndexOf(" , "), builder.length());
		builder.append(" )");
		String sql=builder.toString();
		return new SqlQuery(sql, values.toArray());
	}
	
	/**
	 * id作为where条件
	 * 其他字段作为更新
	 */
	@Override
	public <T> SqlQuery createUpdateSql(T entity) throws Exception{
		Class<?> entityClass = entity.getClass();
		StringBuilder builder = new StringBuilder("update ");
		String tableName=camelConvertFieldName(entityClass.getSimpleName());
		builder.append(tableName).append(" set ");
		String idFieldName=null;
		Object idFieldValue=null;
		Field[] fields = entityClass.getDeclaredFields();
		List<Object> values = new ArrayList<Object>();
		for (Field field : fields) {
			String key = camelConvertFieldName(field.getName());
			field.setAccessible(true);
			Object value = field.get(entity);
			if (value==null) continue;
			if (field.isAnnotationPresent(Id.class)) {
				idFieldName=key;
				idFieldValue=value;
				continue;
			}
			builder.append(key).append(" = ? , ");
			values.add(value);
		}
		if (values.size()<1) return null;
		builder.delete(builder.lastIndexOf(" , "), builder.length());
		if (idFieldName!=null&&idFieldValue!=null) {
			builder.append(" where ").append(camelConvertFieldName(idFieldName)).append(" = ? ");
		}
		values.add(idFieldValue);
		String sql=builder.toString();
		return new SqlQuery(sql, values.toArray());
	}
	
	/**
	 * 实体作为where条件
	 */
	@Override
	public <T> SqlQuery createDeleteSql(T entity) throws Exception {
		Class<?> entityClass = entity.getClass();
		StringBuilder builder = new StringBuilder("delete from ");
		String tableName=camelConvertFieldName(entityClass.getSimpleName());
		builder.append(tableName).append(" where ");
		Field[] fields = entityClass.getDeclaredFields();
		List<Object> values = new ArrayList<Object>();
		for (Field field : fields) {
			String key = camelConvertFieldName(field.getName());
			field.setAccessible(true);
			Object value = field.get(entity);
			if (value==null) continue;
			builder.append(key).append(" = ? and ");
			values.add(value);
		}
		if (values.size()<1) return null;
		builder.delete(builder.lastIndexOf(" and "), builder.length());
		String sql=builder.toString();
		return new SqlQuery(sql, values.toArray());
	}
	
	/**
	 * 生成根据id查实体的query对象 
	 */
	@Override
	public <T> SqlQuery createFindByIdSql(Class<T> clazz,Object id) throws Exception {
		SqlQuery query=new SqlQuery();
		query.appendSql("select * from ").appendSql(camelConvertFieldName(clazz.getSimpleName()));
		Field[] fields=clazz.getDeclaredFields();
		String idField=null;
		for (Field field : fields) {
			if(field.isAnnotationPresent(Id.class)){
				idField=camelConvertFieldName(field.getName());
			}
		}
		query.appendSql(" where ").appendSql(idField).appendSql(" = ? limit 1");
		query.addParam(id);
		return query;
	}
	
	/**
	 * substring from 'form' to 'where' to get table name
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> Class<T> getClassFromSql(String sql,String modulePackage){
		try {
			String entityName=null;
			if (sql.indexOf("select")>-1) {
				int pos=sql.indexOf("from")+4;
				String temp=sql.substring(pos).trim();
				if((pos=temp.indexOf(" "))>0){
					entityName=temp.substring(0, temp.indexOf(" ")).trim();
				}else{
					entityName=temp;
				}
			}
			String tempEntityName=camelConvertColumnName(entityName);
			tempEntityName=tempEntityName.substring(0,1).toUpperCase()+tempEntityName.substring(1);
			Class<T> entityClass = (Class<T>) Class.forName(modulePackage + "." + tempEntityName);
			return entityClass;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}

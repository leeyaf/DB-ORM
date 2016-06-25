package com.gsteam.common.util.dao;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class SqlHelper {
	
	/**
	 * convert string like user_info to userInfo, USER_INFO to userInfo and password to password
	 * @param befor string like user_info
	 * @return a string like userInfo, null if catch exception
	 */
	public static String camelConvertColumnName(String befor) {
		if(befor==null) return null;
		
		befor=befor.toLowerCase();
		
		if (befor.indexOf("_")>0) {
			String[] words=befor.split("_");
			StringBuilder finalWord=new StringBuilder(words[0]);
			for (int i = 1; i < words.length; i++) {
				String itemWord=words[i];
				String first,rest;
				first=itemWord.substring(0, 1).toUpperCase();
				rest=itemWord.substring(1,itemWord.length());
				finalWord.append(first).append(rest);
			}
			return finalWord.toString();
		}
		else return befor;
	}
	
	/**
	 * convert string like userInfo to user_info, password to password , UserEntity to user_entity
	 * @param befor a string like userInfo
	 * @return a string like user_info, null if catch exception
	 */
	public static String camelConvertFieldName(String befor) {
		if(befor==null) return null;

		char[] characters = befor.toCharArray();
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < characters.length; i++) {
			char c = characters[i];
			if (c >= 65 && c <= 90) {
				char tempc = (char) ((int) c + 32);
				if(i==0) builder.append(tempc);
				else builder.append("_").append(tempc);
			}
			else builder.append(c);
		}
		return builder.toString();
	}
	
	private String packageName;
	public SqlHelper(String entityPackageName){
		this.packageName=entityPackageName;
	}
	
	public <T> SqlValue createSaveSql(T entity) throws Exception {
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
		return new SqlValue(sql, values);
	}
	
	/**
	 * id作为where条件
	 * 其他字段作为更新
	 */
	public <T> SqlValue createUpdateSql(T entity) throws Exception{
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
		return new SqlValue(sql, values);
	}
	
	/**
	 * 实体作为where条件
	 */
	public <T> SqlValue createDeleteSql(T entity) throws Exception {
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
		return new SqlValue(sql, values);
	}
	
	@SuppressWarnings("unchecked")
	public <T> Class<T> getClassFromSql(String sql){
		try {
			String entityName=null;
			
			int begin=-1,end=-1;
			if (sql.indexOf("select")>-1) {
				begin=sql.indexOf("from")+4;
				end=sql.indexOf("where");
			}
			
			if (end<0) end=sql.length();
			entityName=sql.substring(begin,end).trim();
			
			String tempEntityName=camelConvertColumnName(entityName);
			tempEntityName=tempEntityName.substring(0,1).toUpperCase()+tempEntityName.substring(1);
			Class<T> entityClass = (Class<T>) Class.forName(packageName + "." + tempEntityName);
			return entityClass;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}

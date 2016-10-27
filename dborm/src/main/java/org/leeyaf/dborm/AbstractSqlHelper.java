package org.leeyaf.dborm;

public abstract class AbstractSqlHelper {

	public abstract SqlQuery getCountSql(SqlQuery query);
	
	public abstract <T> SqlQuery createSaveSql(T entity) throws Exception;
	
	public abstract <T> SqlQuery createUpdateSql(T entity) throws Exception;
	
	public abstract <T> SqlQuery createDeleteSql(T entity) throws Exception;
	
	public abstract <T> SqlQuery createFindByIdSql(Class<T> clazz,Object id) throws Exception;
	
	public abstract <T> Class<T> getClassFromSql(String sql,String modulePackage);
	
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
	
	/**
	 * convert user_info to UserInfo, password to Password
	 * @param befor
	 * @return
	 */
	public static String camelConvertClassName(String befor) {
		String after=camelConvertColumnName(befor);
		return firstCharToUpperCase(after);
	}
	
	public static String firstCharToUpperCase(String befor){
		char first=Character.toUpperCase(befor.charAt(0));
		return first+befor.substring(1);
	}
}

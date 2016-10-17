package org.leeyaf.dborm;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtil {
	private static Properties properties;
	
	static{
		init();
	}
	
	public static Object getObject(String key){
		return properties.get(key);
	}
	
	public static String getString(String key){
		return properties.getProperty(key);
	}
	
	public static boolean getBoolean(String key){
		return Boolean.parseBoolean(properties.getProperty(key));
	}
	
	public static int getInt(String key){
		return Integer.parseInt(properties.getProperty(key));
	}
	
	private static void init(){
		InputStream inputStream=PropertiesUtil.class.getResourceAsStream("/jdbc.properties");
		if (properties==null) {
			properties=new Properties();
		}
		try {
			properties.load(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

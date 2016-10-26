package org.leeyaf.dborm;

import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple way to convert table struct to class struct in java.
 * @author leeyaf
 *
 */
public class ModuleGenerator {
	private static Dao dao=Dao.getInstances();
	
	public static void generate(String table){
		try {
			List<Field> fields=new ArrayList<>();
			Connection conn=dao.getConnection();
			PreparedStatement pstm=conn.prepareStatement("desc "+table);
			ResultSet rs=pstm.executeQuery();
			while (rs.next()) {
				String fieldName=AbstractSqlHelper.camelConvertColumnName(rs.getString(1));
				String fieldType=typeConvert(rs.getString(2));
				fields.add(new Field(fieldName,fieldType));
			}
			Dao.close(rs);
			Dao.close(pstm);
			Dao.close(conn);
			
			String className=AbstractSqlHelper.camelConvertClassName(table);
			
			StringBuilder classCode=new StringBuilder();
			classCode.append("package ").append(Dao.getModulePackage()).append(";\r\n\r\n");
			classCode.append("import java.util.Date;\r\n");
			classCode.append("import ").append(Id.class.getName()).append(";\r\n\r\n");
			classCode.append("public class ").append(className).append("{\r\n");
			classCode.append("\t@Id\r\n");
			
			for (Field field : fields) {
				classCode.append("\tprivate ").append(field.fieldType).append(" ").append(field.fieldName).append(";\r\n");
			}
			
			classCode.append("\r\n");
			
			for (Field field : fields) {
				classCode.append("\tpublic ").append(field.fieldType).append(" ")
					.append("get").append(AbstractSqlHelper.camelConvertClassName(field.fieldName)).append("(){\r\n");
				classCode.append("\t\treturn this.").append(field.fieldName).append(";\r\n");
				classCode.append("\t}\r\n");
			}
			classCode.append("}");
			
			String basePath=System.getProperty("user.dir");
			String modulePath=basePath+"\\src\\main\\java\\";
			modulePath+=Dao.getModulePackage().replaceAll("\\.", "\\\\");
			String classPath=modulePath+"\\"+className+".java";
			
			File file=new File(classPath);
			FileWriter fw=new FileWriter(file);
			fw.write(classCode.toString());
			fw.close();
			
			System.out.println(classPath);
			System.out.println();
			System.out.println(classCode.toString());
			System.out.println();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static String typeConvert(String befor){
		if(befor.indexOf("int")>-1) return "Integer";
		if(befor.indexOf("varchar")>-1) return "String";
		if(befor.indexOf("timestamp")>-1) return "Date";
		return "Object";
	}
	
	private static class Field{
		private String fieldName;
		private String fieldType;

		public Field(String fieldName, String fieldType) {
			this.fieldName = fieldName;
			this.fieldType = fieldType;
		}
	}
}

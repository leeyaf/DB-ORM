package org.leeyaf.dborm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * A simple way to convert table struct to class struct in java.
 * @author leeyaf
 *
 */
public class ModuleGenerator {
	private static Dao dao=Dao.getInstances();
	
	public static void generate(String table){
		try {
			System.out.println("fields of table <"+table+">:");
			System.out.println("@Id");
			Connection conn=dao.getConnection();
			PreparedStatement pstm=conn.prepareStatement("desc "+table);
			ResultSet rs=pstm.executeQuery();
			while (rs.next()) {
				String fieldName=AbstractSqlHelper.camelConvertColumnName(rs.getString(1));
				String fieldType=typeConvert(rs.getString(2));
				System.out.println("private "+fieldType+" "+fieldName+";");
			}
			Dao.close(rs);
			Dao.close(pstm);
			Dao.close(conn);
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
}

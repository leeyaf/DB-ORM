package org.leeyaf.dborm.core;

import java.beans.PropertyDescriptor;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;

import org.apache.commons.dbutils.BeanProcessor;
import org.leeyaf.dborm.SqlHelper;

public class CustomBeanProcessor extends BeanProcessor{

	@Override
	protected int[] mapColumnsToProperties(ResultSetMetaData rsmd, PropertyDescriptor[] props) throws SQLException {
		int cols = rsmd.getColumnCount();
        int[] columnToProperty = new int[cols + 1];
        Arrays.fill(columnToProperty, PROPERTY_NOT_FOUND);

        for (int col = 1; col <= cols; col++) {
            String columnName = rsmd.getColumnLabel(col);
            if (null == columnName || 0 == columnName.length()) {
              columnName = rsmd.getColumnName(col);
            }
            String propertyName = SqlHelper.camelConvertColumnName(columnName);
            if (propertyName == null) {
                propertyName = columnName;
            }
            for (int i = 0; i < props.length; i++) {
                if (propertyName.equalsIgnoreCase(props[i].getName())) {
                    columnToProperty[col] = i;
                    break;
                }
            }
        }
        return columnToProperty;
	}
}

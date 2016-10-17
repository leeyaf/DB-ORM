package org.leeyaf.dborm.core;

import org.apache.commons.dbutils.BasicRowProcessor;

public class CustomBasicRowProcessor extends BasicRowProcessor{
	
	public CustomBasicRowProcessor() {
        super(new CustomBeanProcessor());
    }
}

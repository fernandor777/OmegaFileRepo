
package com.origami.service;

import javax.annotation.Resource;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;

/**
 *
 * @author Fernando
 */
@Singleton
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
@Lock(LockType.READ)
public class OmegaConfigs {
    
    @Resource
    private String dburl;
    @Resource
    private String dbuser;
    @Resource
    private String dbpass;
    @Resource
    private String tableName;

    public String getDburl() {
        return dburl;
    }

    public String getDbuser() {
        return dbuser;
    }

    public String getDbpass() {
        return dbpass;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    
    
    
}

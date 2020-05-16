package com.lagou.edu.utils;

import com.alibaba.druid.pool.DruidDataSource;

/**
 * @author 应癫
 */
public class DruidUtils {

    private DruidUtils(){
    }

    private static DruidDataSource druidDataSource = new DruidDataSource();


    static {
        druidDataSource.setDriverClassName("com.mysql.jdbc.Driver");
        druidDataSource.setUrl("jdbc:mysql://10.6.21.25:3301/ad_data?useUnicode=true&amp;characterEncoding=utf-8");
        druidDataSource.setUsername("root");
        druidDataSource.setPassword("root1357");

    }

    public static DruidDataSource getInstance() {
        return druidDataSource;
    }

}

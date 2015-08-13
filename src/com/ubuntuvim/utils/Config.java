package com.ubuntuvim.utils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 读取数据库的配置文件
 * @author ubuntuvim
 * @email 1527254027@qq.com
 * @datatime 2015-7-23 下午9:28:06
 */
public class Config {
	
	private static Connection conn = null;
	private static Properties props = null;

	static {
		props = new Properties();
		try {
			props.load(Config.class.getResourceAsStream("/dbconfig.properties"));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			Class.forName(props.getProperty("driverClass"));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	

	public static Connection getConn(){
		try {
			conn = DriverManager.getConnection(props.getProperty("url"), props.getProperty("username"), props.getProperty("password"));
			conn.setAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conn;
	}

	
	
	public void closeConn(){
		try {
			if (conn != null)
				conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
}

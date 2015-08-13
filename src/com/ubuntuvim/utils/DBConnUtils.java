package com.ubuntuvim.utils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 获取数据链接
 * @author ubuntuvim
 * @email 1527254027@qq.com
 * @datatime 2015-7-23 下午10:03:32
 */
public class DBConnUtils {
	
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
	

	public static Connection getConnection(){
		try {
			conn = DriverManager.getConnection(props.getProperty("url"));
			conn.setAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conn;
	}

	
	
	public static void closeConn(Connection conn){
		try {
			if (conn != null)
				conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
}

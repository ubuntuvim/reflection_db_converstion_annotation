package com.ubuntuvim.service;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;

import com.ubuntuvim.core.CoreService;
import com.ubuntuvim.dao.Usertest1Dao;
import com.ubuntuvim.model.Usertest1;

public class Usertest1Service extends CoreService<Usertest1> {

	private Usertest1Dao userDao = null;
	
	public Usertest1Service() {
		userDao = new Usertest1Dao();
	}
	
	/**
	 * 根据sql查询
	 * @param sql 查询的sql
	 * @param params 查询参数
	 * @return 匹配的list数组
	 */
	public List<Usertest1> findBySql(String sql, Object[] params) {
		try {
			return userDao.findBySql(sql, params);
		} catch (InstantiationException | IllegalAccessException
				| NoSuchFieldException | SecurityException | SQLException e) {
			//  处理异常信息，
			log.error("执行《 "+sql+" 》出错！");
			log.error(e.toString());
		}
		return null;
	}
	
	/**
	 * 新增数据
	 * @param user 需要新增的数据对象
	 * @return true-新增成功；false-新增失败
	 */
	public boolean insertObj(Usertest1 u) {
			try {
				return userDao.insertObj(u);
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		
		return false;
	}
	
}

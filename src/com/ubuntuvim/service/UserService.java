package com.ubuntuvim.service;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;

import com.ubuntuvim.core.CoreService;
import com.ubuntuvim.dao.UserDao;
import com.ubuntuvim.model.User;

public class UserService extends CoreService<User> {

	private UserDao userDao = null;
	
	public UserService() {
		userDao = new UserDao();
	}
	
	/**
	 * 根据sql查询
	 * @param sql 查询的sql
	 * @param params 查询参数
	 * @return 匹配的list数组
	 */
	public List<User> findBySql(String sql, Object[] params) {
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
	public boolean insertObj(User user) {
			try {
				return userDao.insertObj(user);
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

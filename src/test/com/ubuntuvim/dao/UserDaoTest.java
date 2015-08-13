package test.com.ubuntuvim.dao;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ubuntuvim.dao.UserDao;
import com.ubuntuvim.model.User;

public class UserDaoTest {

	private static UserDao userDao;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		userDao = new UserDao();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		userDao = null;
	}


	@Test
	public void testFindBySql() {
		String sql = "select * from user";
		try {
			List<User> users = userDao.findBySql(sql, null);
			for (User u : users) {
				System.out.println(u);
			}
		} catch (InstantiationException | IllegalAccessException
				| NoSuchFieldException | SecurityException | SQLException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testUpdateBySql() {
		//  更新 id为33的数据
		String sql = "update user set username = ? where id = ?";
		Object[] params = { "testUpdate", 33 };
		if (userDao.updateBySql(sql, params)) 
			System.out.println("更新成功......");
		else 
			System.out.println(" 更新失败。。。。");
	}

	@Test
	public void testInsertObj() {
		User u = new User();
		u.setBirth(new Date());
		u.setDetail_time(new Date());
		u.setUsername("addtest");
		try {
			if (userDao.insertObj(u))
				System.out.println(" 插入成功....");
			else 
				System.out.println("插入失败。。。。");
				
		} catch (NoSuchMethodException | SecurityException
				| IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | SQLException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testInsertObjCompStr() {
		//  此方法是针对 Oracle 数据库设置的。Oracle 数据库组装的 SQL 需要双引号
	}

	@Test
	public void testUpdateObj() {
		User u = new User();
		//  设置要更新的值
		u.setUsername("update second time...");
		//  设置更新的字段和对应的值
		HashMap<String, Object> whereConditionMap = new HashMap<String, Object>();
		whereConditionMap.put("id", 33);  //更新 id 为33的记录
		try {
			if (userDao.updateObj(u, whereConditionMap)) 
				System.out.println("更新成功......");
			else 
				System.out.println(" 更新失败。。。。");
		} catch (NoSuchMethodException | SecurityException
				| IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | SQLException e) {
			e.printStackTrace();
		}
	}

}

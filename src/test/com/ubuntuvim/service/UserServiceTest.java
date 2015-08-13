package test.com.ubuntuvim.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ubuntuvim.model.User;
import com.ubuntuvim.service.UserService;

public class UserServiceTest {

	private static UserService userService;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		userService = new UserService();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		userService = null;
	}

	@Test
	public void testInsertObj() {
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		User u = new User();
//		try {
//			u.setBrith(sdf.parse("2015-09-01"));
//			u.setDateil_time(sdf2.parse("2014-07-26 11:14:04"));
//		} catch (ParseException e) {
//			e.printStackTrace();
//		}
////		u.setBrith(new Date(System.currentTimeMillis()));
////		u.setDateil_time(new Date(System.currentTimeMillis()));
//		u.setUsername("ubuntuvim222222");
//		System.out.println(" ==== " + userService.insertObj(u));;
	}

	@Test
	public void testTestFindHasParams() {
//		String sql = "select * from test_reflection where username = ? and id = ?";
//		Object[] params = { "username", 1 };
//		List<User> users = userService.findBySql(sql, params);
//		for (User u : users) {
//			System.out.println("id = " + u.getId() 
//					+ ", username = " + u.getUsername() 
//					+ ", brith = " + u.getBrith() 
//					+ ", dateil time = " + u.getDateil_time());
//		}
	}
	

}

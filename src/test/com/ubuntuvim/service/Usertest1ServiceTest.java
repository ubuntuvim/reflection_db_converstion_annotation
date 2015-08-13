package test.com.ubuntuvim.service;

import java.text.SimpleDateFormat;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ubuntuvim.model.User;
import com.ubuntuvim.model.Usertest1;
import com.ubuntuvim.service.UserService;
import com.ubuntuvim.service.Usertest1Service;

public class Usertest1ServiceTest {

	private static Usertest1Service userService;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		userService = new Usertest1Service();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		userService = null;
	}

	@Test
	public void testInsertObj() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Usertest1 u = new Usertest1();
		u.setUsername("ubuntuvim222222");
		System.out.println(" ==== " + userService.insertObj(u));;
	}

//	@Test
//	public void testTestFindHasParams() {
//		String sql = "select * from test_reflection where username = ? and id = ?";
//		Object[] params = { "username", 1 };
//		List<User> users = userService.findBySql(sql, params);
//		for (User u : users) {
//			System.out.println("id = " + u.getId() 
//					+ ", username = " + u.getUsername() 
//					+ ", brith = " + u.getBrith() 
//					+ ", dateil time = " + u.getDateil_time());
//		}
//	}
	

}

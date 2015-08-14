package com.ubuntuvim.core;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import oracle.sql.TIMESTAMP;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ubuntuvim.annotation.PersistenceAnnotation4Cls;
import com.ubuntuvim.annotation.PersistenceAnnotation4Method;
import com.ubuntuvim.utils.DBConnUtils;
import com.ubuntuvim.utils.Utils;

/**
 * 使用要求实体类（model）中的属性名一定要和数据库中的字段名一定要严格相同（包括大小写）,
 * 
 * @author ubuntuvim
 * @email 1527254027@qq.com
 * @datatime 2015-7-23 下午11:21:14
 * @param <T>
 */
public class CoreDao<T> {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	private Class<T> clazz;
	// 定义数据库的链接
	protected Connection connection;
	// 定义sql语句的执行对象
	protected PreparedStatement pstmt;
	// 定义查询返回的结果集合
	protected ResultSet resultSet;

	/**
	 * 子类初始时根据子类的泛型参数决定model的类型,这个构造函数不能直接调用
	 */
	@SuppressWarnings("unchecked")
	protected CoreDao() {
		this.clazz = (Class<T>) ClassGenericsUtil.getGenericClass(getClass());
	}

	/**
	 * 使用反射机制获取泛型参数的属性，组装成model的getter/setter方法。 通过反射调用形成model的对象数组返回，不需要一个个设置值
	 * 
	 * @param sql 查询的sql
	 * @param obj 查询的sql参数
	 * @return 匹配的数据list
	 * @throws SQLException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 */
	public List<T> findBySql(String sql, Object[] params) throws SQLException, InstantiationException, IllegalAccessException, NoSuchFieldException, SecurityException {
		List<T> list = new ArrayList<T>();
		connection = DBConnUtils.getConnection();

		T newObj = null;
		int index = 1;
		pstmt = connection.prepareStatement(sql);
		if (null != params) {
			int len = params.length;
			if (len > 0) {
				for (int i = 0; i < len; i++) {
					pstmt.setObject(index++, params[i]);
				}
			}
		}
//		pstmt.set
		log.info("执行SQL >>> " + sql);
		resultSet = pstmt.executeQuery();
		ResultSetMetaData metaData = resultSet.getMetaData();
		int colsLen = metaData.getColumnCount();
		while (resultSet.next()) {
			// 通过反射机制创建实例
			newObj = (T) clazz.newInstance();
			for (int i = 0; i < colsLen; i++) {
				String colsName = metaData.getColumnName(i + 1);
				Object colsValue = resultSet.getObject(colsName);

				// 返回一个 Field 对象，该对象反映此 Class 对象所表示的类或接口的指定已声明字段。
				Field field = clazz.getDeclaredField(colsName.toLowerCase());
				//  有注解
				if (field.isAnnotationPresent(PersistenceAnnotation4Method.class)) {
					//  获取 model 类上的注解
					PersistenceAnnotation4Cls an1 = clazz.getAnnotation(PersistenceAnnotation4Cls.class);
					//  如果是 Oracle 数据库需要做类型的转换；MySQL 不需要
					if (an1.dbType().equals(PersistenceAnnotation4Cls.DBType.oracle)) {
						if (null != colsValue) {
							//  在oracle 字段设置的类型是 number，但是这里得到的是 java.math.BigDecimal
							if ("BigDecimal".equals(colsValue.getClass().getSimpleName())) {
								BigDecimal db = (BigDecimal) colsValue;
								colsValue = db.intValue();  // Converts to int
							}
							//  oracle的时间戳类型 class oracle.sql.TIMESTAMP 需要转换为普通java.util.Date
							if ("TIMESTAMP".equals(colsValue.getClass().getSimpleName())) {
								oracle.sql.TIMESTAMP ts = (TIMESTAMP) colsValue;
								colsValue = ts.dateValue();
							}
						}
					}
				}
				
				field.setAccessible(true);// 打开javabean的访问private权限
				// 执行setter方法
				field.set(newObj, colsValue);
			}

			list.add(newObj);
		}
		//  执行完成，关闭数据库链接
		DBConnUtils.closeConn(connection);
		
		return list;
	}
	
	/**
	 * 根据更新少数字段、删除数据库数据
	 * @param sql 更新的sql
	 * @param params 参数
	 * @return true-更新、删除成功；false-更新、删除失败
	 * @throws SQLException
	 */
	public boolean updateBySql(String sql, Object[] params) {
		// 表示当用户执行添加删除和修改的时候所影响数据库的行数
        int result = -1;
		connection = DBConnUtils.getConnection();
        int index = 1;  // 占位符下标从1开始
        // 填充sql语句中的占位符
        try {
//    		sql = "insert into user( username,brith,dateil_time ) values( ?,?,? )";
            pstmt = connection.prepareStatement(sql);
			if (null != params) {
				int len = params.length;
			    if (len > 0) {
			        for (int i = 0; i < len; i++) {
			        	log.info(" 参数类型： " + params[i].getClass());
			        	log.info("参数值 = " + params[i]);
			        	if (null != params[i]) {
			                pstmt.setObject(index++, params[i]);
			        	}
			        }
			    }
			}
			System.out.println("\n");
			log.info("执行SQL >>> " + sql);
//        System.out.println(pstmt);
			//  执行更新
			result = pstmt.executeUpdate();
			
			try {
				// 提交
				connection.commit();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				//  出错回滚
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}  //
		}
          
		//  执行完成，关闭数据库链接
        DBConnUtils.closeConn(connection);
        
        return result > 0;
    }
	
	/**
	 * 保存对象
	 * 使用反射机制获取泛型参数的属性，组装成model的getter/setter方法。 通过反射调用形成model的对象数组返回，不需要一个个设置值
	 * 
	 * @param sql 查询的sql
	 * @param obj 查询的sql参数
	 * @return true-插入成功；false-插入失败
	 */
	public boolean insertObj(T entity) 
			throws NoSuchMethodException, 
			SecurityException, IllegalAccessException, 
			IllegalArgumentException, 
			InvocationTargetException, 
			SQLException {

//		connection = DBConnUtils.getConntion();
		String fieldStr = "";
		String qm = "";
		StringBuffer sb = new StringBuffer();
		PersistenceAnnotation4Cls an1 = clazz.getAnnotation(PersistenceAnnotation4Cls.class);
		Object tableName = "";
		if (null == an1.tableName() || "".equals(an1.tableName())) {
			tableName  = entity.getClass().getSimpleName();
		} else {
			tableName = an1.tableName();
		}
		
		//  insert into tableName(
		sb.append("insert into ").append(tableName).append("( ");
		// 返回一个 Field 对象，该对象反映此 Class 对象所表示的类或接口的指定已声明字段。
		Class<? extends Object> clazz = entity.getClass();
		List<Object> params = new ArrayList<Object>();
		
		Field[] fields = clazz.getDeclaredFields();
		int len = fields.length;
		String getter = "";
		Method mt = null;
		String fieldName = "";
		for(int i = 0; i < len; i++) {
			//  组装getter 方法
			getter = "get" + Utils.toUpperCase(fields[i].getName());
			mt = clazz.getDeclaredMethod(getter);  //执行 getter 方法
//			System.out.println("mt.invoke(entity) = " + mt.invoke(entity));
			fieldName = fields[i].getName();
			if (mt.isAnnotationPresent(PersistenceAnnotation4Method.class)) {  //  判断方法上是否有注解
				//  得到方法的注解类
				PersistenceAnnotation4Method n = mt.getAnnotation(PersistenceAnnotation4Method.class);
				//  没有注解noPersistenField的方法，这些方法需要组装成 SQL
				//  获取使用了注解的方法
				if (n.noPersistenField()) {
					//  对于是主键的方法要根据主键的类型生成SQL
					if (n.isPrimaryKey()) {  //如果此方法是主键的 get 方法
//						System.out.println(" 选中的主键策略 》》》》》》" + n.primaryKeyType());
						if (n.primaryKeyType().equals(PersistenceAnnotation4Method.PrimaryKeyType.autoInc)) {
							//  如果选择的是自增的，比如 MySQL 数据库，不需要指定字段名和对应的值
							log.info("主键使用的自增方式...");
						} else if (n.primaryKeyType().equals(PersistenceAnnotation4Method.PrimaryKeyType.sequence)) {
							if (null == n.sequenceName() || "".equals(n.sequenceName())) {
								log.error("请指定序列名！！！");
								try {
									throw new Exception("请指定序列名");
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							
							//如果主键使用的是序列要给出序列名字,组装成 sequenceName.nextval
							String sequenceName = n.sequenceName() + ".nextval";  
							// 如果选择的是序列方式，比如 Oracle 数据库，需要指定字段名称和对应的序列名
							fieldStr += (fieldName + ",");
							qm += (sequenceName + ",");
							//  由于执行新增时候主键名字和对应的序列名都是固定的所以不需要再执行 get 方法
						} else {
							log.info("请指定注解生成策略为 autoInc 或者是 sequence");
						}
					} else if(n.fieldType().equals(PersistenceAnnotation4Method.FieldType.dateType)) {  //如果插入的是时间
						SimpleDateFormat sdf = new SimpleDateFormat(n.dateFormat());
						if (null != mt.invoke(entity)) {  //  传入的时间参数不为空才保存
							log.info("字段【"+fieldName+"】是date【"+n.dateFormat()+"】 类型...");
							String dateStr = sdf.format(mt.invoke(entity));
							// MySQL 不需要特殊处理，但是 Oracle 要添加 SQL 函数
							if (an1.dbType().equals(PersistenceAnnotation4Cls.DBType.oracle)) {
								fieldStr += (fieldName + ",");
								qm = makeDateFormat(qm, n);
								params.add(dateStr);
							}
						}
					} else {  //其他类型的注解
						fieldStr += (fieldName + ",");  //  拼接sql语句中 tableName(xxx, xxx) values()
						qm += ("?,");//  拼接sql语句中 values(?,?,?)
						params.add(mt.invoke(entity));
					}
				}
			} else {  //无注解的方法
				if (null != mt.invoke(entity)) {
					// 得到成员属性名
					fieldStr += (fieldName + ",");  //  拼接sql语句中 tableName(xxx, xxx) values()
					qm += ("?,");//  拼接sql语句中 values(?,?,?)
					//  执行getter得到对象内的值
//					System.out.println("getter value = " + mt.invoke(entity));
					params.add(mt.invoke(entity));
				}

			}
//			System.out.println("fieldStr = " + fieldStr);
//			System.out.println("qm = " + qm);
		}
		
		// 去掉最后一个逗号
		fieldStr = Utils.removeLastChar(fieldStr, ",");
		qm = Utils.removeLastChar(qm, ",");
		// 拼接字符串得到执行的sql语句
		sb.append(fieldStr).append(" ) values( ").append(qm).append(" )");
//		log.info("执行SQL >>> " + sb.toString());
		return this.updateBySql(sb.toString(), params.toArray());
	}
    
	

	/**
	 * 保存对象 （组成的SQL包含双引号）
	 * 使用反射机制获取泛型参数的属性，组装成model的getter/setter方法。 通过反射调用形成model的对象数组返回，不需要一个个设置值
	 * （此方法与insertObj方法功能类似，但是此方法会组装成类似：insert into "HIYDZZ"( "transid" ) values( '4403001508040000000330' );的sql语句）
	 * @param entity 保存的实体类
	 * @return
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws SQLException
	 */
	public boolean insertObjCompStr(T entity) 
			throws NoSuchMethodException, 
			SecurityException, IllegalAccessException, 
			IllegalArgumentException, 
			InvocationTargetException, 
			SQLException {

//		connection = DBConnUtils.getConntion();
		String fieldStr = "";
		String qm = "";
		StringBuffer sb = new StringBuffer();
		PersistenceAnnotation4Cls an1 = clazz.getAnnotation(PersistenceAnnotation4Cls.class);
		Object tableName = "";
		if (null == an1.tableName() || "".equals(an1.tableName())) {
			tableName  = entity.getClass().getSimpleName();
		} else {
			tableName = an1.tableName();
		}
		
		//  insert into tableName(
		sb.append("insert into \"").append(tableName).append("\"( ");
		// 返回一个 Field 对象，该对象反映此 Class 对象所表示的类或接口的指定已声明字段。
		Class<? extends Object> clazz = entity.getClass();
		List<Object> params = new ArrayList<Object>();
		
		Field[] fields = clazz.getDeclaredFields();
		int len = fields.length;
		String getter = "";
		Method mt = null;
		String fieldName = "";
		for(int i = 0; i < len; i++) {
			//  组装getter 方法
			getter = "get" + Utils.toUpperCase(fields[i].getName());
			mt = clazz.getDeclaredMethod(getter);  //执行 getter 方法
//			System.out.println("mt.invoke(entity) = " + mt.invoke(entity));
			fieldName = fields[i].getName();
			if (mt.isAnnotationPresent(PersistenceAnnotation4Method.class)) {  //  判断方法上是否有注解
				//  得到方法的注解类
				PersistenceAnnotation4Method n = mt.getAnnotation(PersistenceAnnotation4Method.class);
				//  没有注解noPersistenField的方法，这些方法需要组装成 SQL
				//  获取使用了注解的方法
				if (n.noPersistenField()) {
					//  对于是主键的方法要根据主键的类型生成SQL
					if (n.isPrimaryKey()) {  //如果此方法是主键的 get 方法
//						System.out.println(" 选中的主键策略 》》》》》》" + n.primaryKeyType());
						if (n.primaryKeyType().equals(PersistenceAnnotation4Method.PrimaryKeyType.autoInc)) {
							//  如果选择的是自增的，比如 MySQL 数据库，不需要指定字段名和对应的值
							log.info("主键使用的自增方式...");
						} else if (n.primaryKeyType().equals(PersistenceAnnotation4Method.PrimaryKeyType.sequence)) {
							if (null == n.sequenceName() || "".equals(n.sequenceName())) {
								log.error("请指定序列名！！！");
								try {
									throw new Exception("请指定序列名");
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							
							//如果主键使用的是序列要给出序列名字,组装成 sequenceName.nextval
							String sequenceName = n.sequenceName() + ".nextval";  
							// 如果选择的是序列方式，比如 Oracle 数据库，需要指定字段名称和对应的序列名
							fieldStr += (fieldName + ",");
							qm += (sequenceName + ",");
							//  由于执行新增时候主键名字和对应的序列名都是固定的所以不需要再执行 get 方法
						} else {
							log.info("请指定注解生成策略为 autoInc 或者是 sequence");
						}
					} else if(n.fieldType().equals(PersistenceAnnotation4Method.FieldType.dateType)) {  //如果插入的是时间
						SimpleDateFormat sdf = new SimpleDateFormat(n.dateFormat());
						if (null != mt.invoke(entity)) {  //  传入的时间参数不为空才保存
							String dateStr = sdf.format(mt.invoke(entity));
							log.info("字段【"+fieldName+"】是date【"+n.dateFormat()+"】 类型，字段值为【"+dateStr+"】");
							// MySQL 不需要特殊处理，但是 Oracle 要添加 SQL 函数
							if (an1.dbType().equals(PersistenceAnnotation4Cls.DBType.oracle)) {
								fieldStr = makeFieldName(fieldStr, fieldName);
								qm = makeDateFormat(qm, n);  //  特殊处理oracle的时间格式
								params.add(dateStr);
							} else {
								fieldStr = makeFieldName(fieldStr, fieldName);
								qm += ("?,");//  拼接sql语句中 values(?,?,?)
								//  执行getter得到对象内的值
								params.add(mt.invoke(entity));
							}
						}
					} else {  //其他类型的注解
						fieldStr += (fieldName + ",");  //  拼接sql语句中 tableName(xxx, xxx) values()
						qm += ("?,");//  拼接sql语句中 values(?,?,?)
						params.add(mt.invoke(entity));
					}
				}
			} else {  //无注解的方法
				if (null != mt.invoke(entity)) {
					fieldStr = makeFieldName(fieldStr, fieldName);
					qm += ("?,");//  拼接sql语句中 values(?,?,?)
					//  执行getter得到对象内的值
					params.add(mt.invoke(entity));
				}

			}
		}
		
		// 去掉最后一个逗号
		fieldStr = Utils.removeLastChar(fieldStr, ",");
		qm = Utils.removeLastChar(qm, ",");
		// 拼接字符串得到执行的sql语句
		sb.append(fieldStr).append(" ) values( ").append(qm).append(" )");
//		log.info("执行SQL >>> " + sb.toString());
		return this.updateBySql(sb.toString(), params.toArray());
	}
	
	/**
	 * 以对象的方式更新数据
	 * @param entity 更新的对象值
	 * @param whereConditionMap 更新的条件 key为字段的名称，value为条件值
	 * @return true-更新成功；false-更新失败
	 */
	public boolean updateObj(T entity, HashMap<String, Object> whereConditionMap) 
			throws NoSuchMethodException, 
			SecurityException, IllegalAccessException, 
			IllegalArgumentException, 
			InvocationTargetException, 
			SQLException {

		System.out.println("entity = " + entity);
		
		String fieldStr = "";
		StringBuffer sb = new StringBuffer();
		PersistenceAnnotation4Cls an1 = clazz.getAnnotation(PersistenceAnnotation4Cls.class);
		Object tableName = "";
		if (null == an1.tableName() || "".equals(an1.tableName())) {
			tableName  = entity.getClass().getSimpleName();
		} else {
			tableName = an1.tableName();
		}
		
		//  update tableName( set 
		if (an1.dbType().equals(PersistenceAnnotation4Cls.DBType.oracle)) {
			//  Oracle 数据组装成update "tableName"( set
			sb.append("update \"").append(tableName).append("\" set ");
		} else {
			sb.append("update ").append(tableName).append(" set ");
		}
		// 返回一个 Field 对象，该对象反映此 Class 对象所表示的类或接口的指定已声明字段。
		Class<? extends Object> clazz = entity.getClass();
		List<Object> params = new ArrayList<Object>();
		
		Field[] fields = clazz.getDeclaredFields();
		int len = fields.length;
		String getter = "";
		Method mt = null;
		String fieldName = "";
		PersistenceAnnotation4Method n = null;
		SimpleDateFormat sdf = null;
		String dateValue = "";
		String dateFormat = "";
		//  update tableName set fieldName = ? where id = ? and 
		for(int i = 0; i < len; i++) {
			//  组装getter 方法
			getter = "get" + Utils.toUpperCase(fields[i].getName());
			mt = clazz.getDeclaredMethod(getter);  //执行 getter 方法
//			System.out.println("mt.invoke(entity) = " + mt.invoke(entity));
			fieldName = fields[i].getName();
			if (mt.isAnnotationPresent(PersistenceAnnotation4Method.class)) {  //  判断方法上是否有注解
				//  得到方法的注解类
				n = mt.getAnnotation(PersistenceAnnotation4Method.class);
				//  没有注解noPersistenField的方法，这些方法需要组装成 SQL
				//  获取使用了注解的方法，并且不是主键，不允许更新主键值
				if (n.noPersistenField() && !n.isPrimaryKey()) {
					if(n.fieldType().equals(PersistenceAnnotation4Method.FieldType.dateType)) {  //如果插入的是时间
						sdf = new SimpleDateFormat(n.dateFormat());
						if (null != mt.invoke(entity)) {  //  传入的时间参数不为空才保存
							dateValue = sdf.format(mt.invoke(entity));
							dateFormat = n.dateFormat();
							log.info("字段【"+fieldName+"】是date【"+n.dateFormat()+"】 类型，字段值为【"+dateValue+"】");
							// MySQL 不需要特殊处理，但是 Oracle 要添加 SQL 函数
							if (an1.dbType().equals(PersistenceAnnotation4Cls.DBType.oracle)) {
								//  特殊处理oracle的时间格式
								if (n.dateFormat().contains("HH:mm:ss")) {   // 如果model类定义的是获取时分秒的时间，
									dateFormat = "yyyy-MM-dd HH24:mi:ss";
								} else {
									dateFormat = n.dateFormat();
								}
								
								fieldStr += "\"" + fieldName + "\" = " + "to_date(?, '"+dateFormat+"'),";  //  fieldName = ?, fieldName2 = ?, 
								params.add(dateValue);
							} else {
								fieldStr = setValToSql4Update(entity, fieldStr,
										params, mt, fieldName, an1);
							}
						}
					} else {  //其他类型的注解
						fieldStr = setValToSql4Update(entity, fieldStr, params,
								mt, fieldName, an1);
					}
				}
			} else {  //无注解的方法
				if (null != mt.invoke(entity)) {
					fieldStr = setValToSql4Update(entity, fieldStr, params, mt,
							fieldName, an1);
				}

			}
		}
//		System.out.println("fieldStr = " + fieldStr);
		
		// 去掉最后一个逗号
		fieldStr = Utils.removeLastChar(fieldStr, ",");
		// 拼接字符串得到执行的sql语句
		//sb.append(fieldStr).append(" ) values( ").append(qm).append(" )");
		//  组装where条件
		String wc = " where 1 = 1"; 
		if (null != whereConditionMap && whereConditionMap.size() > 0) {
			Set<String> tableFieldName = whereConditionMap.keySet(); //得到key，就是字段名称
			for (String f : tableFieldName) {
//				wc = " where " + f + " = " + whereConditionMap.get(f);
				if (an1.dbType().equals(PersistenceAnnotation4Cls.DBType.oracle)) {
					wc += " and " + "\"" + f + "\" = ?,";
				} else {
					wc += " and " + f + " = ?,";
				}
				params.add(whereConditionMap.get(f));
			}
			//  去掉最后一个逗号
			wc = Utils.removeLastChar(wc, ",");
//			System.out.println("whereConditionMap = " + wc);
			sb.append(fieldStr).append(wc);
		} else {
			sb.append(fieldStr);
		}
//		log.info("执行SQL >>> " + sb.toString());
		return this.updateBySql(sb.toString(), params.toArray());
	}

	
	private String setValToSql4Update(T entity, String fieldStr,
			List<Object> params, Method mt, String fieldName, PersistenceAnnotation4Cls n)
			throws IllegalAccessException, InvocationTargetException {
			
		if (n.dbType().equals(PersistenceAnnotation4Cls.DBType.oracle)) {
			fieldStr += ("\"" + fieldName + "\" = " + "?,");  //  "fieldName" = ?, "fieldName2" = ?,
		} else {
			fieldStr += (fieldName + " = " + "?,");  //  fieldName = ?, fieldName2 = ?,
		}
	
		params.add(mt.invoke(entity));
		
		return fieldStr;
	}

	private String makeDateFormat(String qm, PersistenceAnnotation4Method n) {
		//  oracle的date类型是 yyyy-MM-dd hh24:mi:ss
		//  但是MySQL是  yyyy-MM-dd HH:mm:ss
		//  获取到时分秒的时候两者的时间格式是不一样的，要处理oracle的类型
		String dateStr = "";
		if (n.dateFormat().contains("HH:mm:ss")) {   // 如果model类定义的是获取时分秒的时间，
			dateStr = "yyyy-MM-dd HH24:mi:ss";
		} else {
			dateStr = n.dateFormat();
		}
		qm += ("to_date(?, '"+dateStr+"'),");
		return qm;
	}

	private String makeFieldName(String fieldStr, String fieldName) {
		fieldStr += ("\""+fieldName+"\"" + ",");
		return fieldStr;
	}
	
}


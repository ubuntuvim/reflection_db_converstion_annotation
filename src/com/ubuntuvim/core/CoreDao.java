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
	 * @throws SQLException | IllegalAccessException | InstantiationException | SecurityException | NoSuchFieldException 
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

				// 根据字段名称得到实体类属性
				Field field = clazz.getDeclaredField(colsName.toLowerCase());
				//  获取 model 类上的注解
				PersistenceAnnotation4Cls annotation4Cls = clazz.getAnnotation(PersistenceAnnotation4Cls.class);
				//  如果是 Oracle 数据库需要做类型的转换；MySQL 不需要
				if (oracleDB(annotation4Cls)) {
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
	 * 是否是 Oracle 数据库
	 * @param annotation4Cls
	 * @return true-Oracle 数据库；false-其他数据库
	 */
	private boolean oracleDB(PersistenceAnnotation4Cls annotation4Cls) {
		return annotation4Cls.dbType().equals(PersistenceAnnotation4Cls.DBType.oracle);
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
			        	log.info("更新的参数类型： " + params[i].getClass());
			        	log.info("更新的参数值 = " + params[i]);
			        	if (null != params[i]) {
			                pstmt.setObject(index++, params[i]);
			        	}
			        }
			    }
			}
			System.out.println("\n");
			log.info("执行SQL >>> " + sql);
			//  执行更新
			result = pstmt.executeUpdate();
			
			try {
				// 提交
				connection.commit();
			} catch (SQLException e) {
				e.printStackTrace();
				rollback3Error();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			rollback3Error();
		} finally {
		//  执行完成，关闭数据库链接
	        DBConnUtils.closeConn(connection);
		}
          
        return result > 0;
    }

	
	/**
	 * 保存对象
	 * 使用反射机制获取泛型对象的属性，并执行model的getter方法获取对象里的值再组装成 SQL。 
	 * 
	 * @return true-插入成功；false-插入失败
	 */
	public boolean insertObj(T entity) 
			throws NoSuchMethodException, SecurityException, IllegalAccessException, 
			IllegalArgumentException, InvocationTargetException, SQLException {


		checkObjIsNull(entity, "插入的对象不能为 null ！！");
		
		String fieldStr = "";
		String qm = "";
		StringBuffer sb = new StringBuffer();
		PersistenceAnnotation4Cls annotation4Cls = clazz.getAnnotation(PersistenceAnnotation4Cls.class);
		Object tableName = "";
		if (null == annotation4Cls.tableName() || "".equals(annotation4Cls.tableName())) {
			tableName  = entity.getClass().getSimpleName();
		} else {
			tableName = annotation4Cls.tableName();
		}
		
		//  insert into tableName(
		if (oracleDB(annotation4Cls)) {
			//  若为 Oracle 数据库，在表明上加双引号
			sb.append("insert into ").append("\"" + tableName +"\"").append("( ");
		} else {
			sb.append("insert into ").append(tableName).append("( ");
		}
		Class<? extends Object> clazz = entity.getClass();
		List<Object> params = new ArrayList<Object>();
		//  获取实体类的所有属性
		Field[] fields = clazz.getDeclaredFields();
		int len = fields.length;
		String getter = "";
		Method mt = null;
		String fieldName = "";
		for(int i = 0; i < len; i++) {
			//  组装getter 方法
			getter = "get" + Utils.toUpperCase(fields[i].getName());
			mt = clazz.getDeclaredMethod(getter);  //执行 getter 方法
			fieldName = fields[i].getName();
			if (mt.isAnnotationPresent(PersistenceAnnotation4Method.class)) {  //  判断方法上是否有注解
				//  得到方法的注解类
				PersistenceAnnotation4Method annotation4Method = mt.getAnnotation(PersistenceAnnotation4Method.class);
				//  没有注解noPersistenField的方法，这些方法需要组装成 SQL
				//  获取使用了注解的方法
				if (!annotation4Method.noPersistenField()) {
					//  对于是主键的方法要根据主键的类型生成SQL
					if (annotation4Method.isPrimaryKey()) {  //如果此方法是主键的 get 方法
						if (annotation4Method.primaryKeyType().equals(PersistenceAnnotation4Method.PrimaryKeyType.sequence)) {
							if (null == annotation4Method.sequenceName() || "".equals(annotation4Method.sequenceName())) {
								log.error("请指定序列名！！！");
								try {
									throw new Exception("请指定序列名");
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							
							//如果主键使用的是序列要给出序列名字,组装成 sequenceName.nextval
							String sequenceName = annotation4Method.sequenceName() + ".nextval";  
							// 如果选择的是序列方式，比如 Oracle 数据库，需要指定字段名称和对应的序列名
							fieldStr += ("\"" + fieldName + "\",");
							qm += (sequenceName + ",");
							//  由于执行新增时候主键名字和对应的序列名都是固定的所以不需要再执行 get 方法
						} else if (annotation4Method.primaryKeyType().equals(PersistenceAnnotation4Method.PrimaryKeyType.autoInc)) {
							//  如果选择的是自增的，比如 MySQL 数据库，不需要指定字段名和对应的值
							log.info("主键使用的自增方式...");
						} else {
							log.info("请指定注解生成策略为 autoInc 或者是 sequence");
						}
					} else if(annotation4Method.fieldType().equals(PersistenceAnnotation4Method.FieldType.dateType)) {  //如果插入的是时间
						SimpleDateFormat sdf = new SimpleDateFormat(annotation4Method.dateFormat());
						if (null != mt.invoke(entity)) {  //  传入的时间参数不为空才保存
							log.info("字段【"+fieldName+"】是date【"+annotation4Method.dateFormat()+"】 类型...");
							String dateStr = sdf.format(mt.invoke(entity));
							// MySQL 不需要特殊处理，但是 Oracle 要添加 SQL 函数
							if (oracleDB(annotation4Cls)) {
								fieldStr += ("\"" + fieldName + "\",");
								qm = makeDateFormat(qm, annotation4Method);
								params.add(dateStr);
							} else {
								fieldStr += (fieldName + ",");
								qm += ("?,");//  拼接sql语句中 values(?,?,?)
								params.add(mt.invoke(entity));
							}
						}
					} else {  //其他类型的注解
						fieldStr = checkDBTypeAndGetFieldStr(fieldStr, annotation4Cls,
								fieldName);
						qm += ("?,");//  拼接sql语句中 values(?,?,?)
						params.add(mt.invoke(entity));
					}
				}
			} else {  //无注解的方法
				if (null != mt.invoke(entity)) {
					fieldStr = checkDBTypeAndGetFieldStr(fieldStr, annotation4Cls,
							fieldName);
					qm += ("?,");//  拼接sql语句中 values(?,?,?)
					//  执行getter得到对象内的值
//					System.out.println("getter value = " + mt.invoke(entity));
					params.add(mt.invoke(entity));
				}

			}
			System.out.println("fieldStr = " + fieldStr);
			System.out.println("qm = " + qm);
		}
		
		// 去掉最后一个逗号
		fieldStr = Utils.removeLastChar(fieldStr, ",");
		qm = Utils.removeLastChar(qm, ",");
		// 拼接字符串得到执行的sql语句
		sb.append(fieldStr).append(" ) values( ").append(qm).append(" )");
//		log.info("执行SQL >>> " + sb.toString());
		return this.updateBySql(sb.toString(), params.toArray());
	}

	//  判断数据库类型并返回有字段组成的 SQL 片段
	private String checkDBTypeAndGetFieldStr(String fieldStr,
			PersistenceAnnotation4Cls annotation4Cls, String fieldName) {
		if (oracleDB(annotation4Cls)) {
			 //  拼接sql语句中 "tableName"("xxx1", "xxx2") values()
			fieldStr += ("\"" + fieldName + "\","); 
		} else {
			 //  拼接sql语句中 tableName(xxx, xxx) values()
			fieldStr += (fieldName + ",");
		}
		return fieldStr;
	}
    
	
	/**
	 * 更新对象
	 * @param entity 更新的对象值
	 * @param whereConditionMap 更新的条件 key为字段的名称，value为条件值，
	 * 			会组成 where 1 = 1 and key1 = value1, key2 = value2形式
	 * @return true-更新成功；false-更新失败
	 */
	public boolean updateObj(T entity, HashMap<String, Object> whereConditionMap) 
			throws NoSuchMethodException, 
			SecurityException, IllegalAccessException, 
			IllegalArgumentException, 
			InvocationTargetException, 
			SQLException {

		checkObjIsNull(entity, "不允许更新为 null 的对象！！");
		
//		System.out.println("entity = " + entity);
		
		String fieldStr = "";
		StringBuffer sb = new StringBuffer();
		PersistenceAnnotation4Cls annotation4Cls = clazz.getAnnotation(PersistenceAnnotation4Cls.class);
		Object tableName = "";
		if (null == annotation4Cls.tableName() || "".equals(annotation4Cls.tableName())) {
			tableName  = entity.getClass().getSimpleName();
		} else {
			tableName = annotation4Cls.tableName();
		}
		
		//  update tableName( set 
		if (oracleDB(annotation4Cls)) {
			//  Oracle 数据组装成update "tableName"( set
			sb.append("update \"").append(tableName).append("\" set ");
		} else {
			sb.append("update ").append(tableName).append(" set ");
		}
		// 
		Class<? extends Object> clazz = entity.getClass();
		List<Object> params = new ArrayList<Object>();
		
		Field[] fields = clazz.getDeclaredFields();
		int len = fields.length;
		String getter = "";
		Method mt = null;
		String fieldName = "";
		PersistenceAnnotation4Method annotation4Method = null;
		SimpleDateFormat sdf = null;
		String dateValue = "";
		String dateFormat = "";
		//  update tableName set fieldName = ? where id = ? and 
		for(int i = 0; i < len; i++) {
			//  组装getter 方法
			getter = "get" + Utils.toUpperCase(fields[i].getName());
			mt = clazz.getDeclaredMethod(getter);  //执行 getter 方法
			fieldName = fields[i].getName();
			if (mt.isAnnotationPresent(PersistenceAnnotation4Method.class)) {  //  判断方法上是否有注解
				//  得到方法的注解类
				annotation4Method = mt.getAnnotation(PersistenceAnnotation4Method.class);
				//  注解noPersistenField为 true的方法， 不持久化到数据库，直接忽略
				//  获取使用了注解的方法，并且不是主键，不允许更新主键值
				if (!annotation4Method.noPersistenField() && !annotation4Method.isPrimaryKey()) {
					if(annotation4Method.fieldType().equals(PersistenceAnnotation4Method.FieldType.dateType)) {
						if (null != mt.invoke(entity)) {  //  传入的时间参数不为空才保存
							sdf = new SimpleDateFormat(annotation4Method.dateFormat());
							dateValue = sdf.format(mt.invoke(entity));
							dateFormat = annotation4Method.dateFormat();
							log.info("字段【"+fieldName+"】是date【"+annotation4Method.dateFormat()+"】 类型，字段值为【"+dateValue+"】");
							// MySQL 不需要特殊处理，但是 Oracle 要添加 SQL 函数
							if (oracleDB(annotation4Cls)) {
								//  特殊处理oracle的时间格式
								if (annotation4Method.dateFormat().contains("HH:mm:ss")) {   // 如果model类定义的是获取时分秒的时间，
									dateFormat = "yyyy-MM-dd HH24:mi:ss";
								} else {
									dateFormat = annotation4Method.dateFormat();
								}
								//  "fieldName" = ?, "fieldName2" = ?,
								fieldStr += ("\"" + fieldName + "\" = to_date(?, '"+dateFormat+"'),");   
								params.add(dateValue);
							} else {
								fieldStr = setValToSql4Update(entity, fieldStr, params, mt, fieldName, annotation4Cls);
							}
						}
					} else {  //其他类型的注解
						fieldStr = setValToSql4Update(entity, fieldStr, params, mt, fieldName, annotation4Cls);
					}
				}
			} else {  //无注解的方法
				if (null != mt.invoke(entity)) {
					fieldStr = setValToSql4Update(entity, fieldStr, params, mt, fieldName, annotation4Cls);
				}

			}
		}
//		System.out.println("fieldStr = " + fieldStr);
		
		// 去掉最后一个逗号
		fieldStr = Utils.removeLastChar(fieldStr, ",");
		//  组装where条件
		String wc = " where 1 = 1"; 
		if (null != whereConditionMap && whereConditionMap.size() > 0) {
			Set<String> tableFieldName = whereConditionMap.keySet(); //得到key，就是字段名称
			for (String f : tableFieldName) {
//				wc = " where " + f + " = " + whereConditionMap.get(f);
				if (oracleDB(annotation4Cls)) {
					wc += " and \"" + f + "\" = ?,";
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

		return this.updateBySql(sb.toString(), params.toArray());
	}
	
	/**
	 * 根据 SQL 删除数据
	 * @param sql 
	 * @param params 删除的条件参数
	 * @return true-删除成功；false-删除失败
	 */
	public boolean delete(String sql, Object[] params) {
		return this.updateBySql(sql, params);
	}
	
	/**
	 * 删除一个对象
	 * @return true-删除成功；false-删除失败
	 */
	public boolean delete(T entity) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		checkObjIsNull(entity, "删除的对象不能为 null ！！");
			
		//  首先获取主键字段，根据主键删除
		Method mt = null;
		PersistenceAnnotation4Method annotation4Method = null;
		StringBuffer sql = new StringBuffer();
		PersistenceAnnotation4Cls annotation4Cls = clazz.getAnnotation(PersistenceAnnotation4Cls.class);
		Class<? extends Object> clazz = entity.getClass();
		Field[] fields = clazz.getDeclaredFields();
		int len = fields.length;
		boolean usePrimaryAnnotaction = false;
		for(int i = 0; i < len; i++) {
			//得到 getter 方法
			mt = clazz.getDeclaredMethod("get" + Utils.toUpperCase(fields[i].getName()));  
			//  判断所有的 getter 方法，检查实体类是否声明了主键注解
			if (mt.isAnnotationPresent(PersistenceAnnotation4Method.class)) {  //  判断方法上是否有注解
				//  得到方法的注解
				annotation4Method = mt.getAnnotation(PersistenceAnnotation4Method.class);
				//  得到使用主键注解的 getter 方法
				if (!annotation4Method.noPersistenField() && annotation4Method.isPrimaryKey()) {
					usePrimaryAnnotaction = true;
					//  获取对象内主键值
					if (null != mt.invoke(entity)) {
						//  组装 SQL：delete from user where id = ?
						if (oracleDB(annotation4Cls)) {
							sql.append("delete from \"")
							.append(annotation4Cls.tableName())
							.append("\" where \"")
							.append(fields[i].getName())
							.append("\" = ")
							.append("?");
						} else {
							sql.append("delete from ")
							.append(annotation4Cls.tableName())
							.append(" where ")
							.append(fields[i].getName())
							.append(" = ")
							.append("?");
						}
						
					} else {
						try {
							throw new Exception("请指定主键的值，否则无法执行删除操作！");
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					
					break;
				} else {
					usePrimaryAnnotaction = false;
				}
			}
		}
		if (usePrimaryAnnotaction) {
			//  执行删除
			return this.updateBySql(sql.toString(), new Object[] { mt.invoke(entity) });
		} else {
//			log.error("实体类【"+clazz+"】没有使用注解【PersistenceAnnotation4Method.isPrimaryKey】指定主键，"
//					+ "请使用方法delete(String sql, Object[] params)手动指定删除条件进行删除操作！");
			try {
				throw new Exception("实体类【"+clazz+"】没有使用注解【PersistenceAnnotation4Method.isPrimaryKey】指定主键，"
						+ "或者使用方法delete(String sql, Object[] params)手动指定删除条件进行删除操作！");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}
	}

	private void checkObjIsNull(T entity, String errorMsg) {
		if (null == entity) {
			try {
				throw new Exception(errorMsg);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	//  出错回滚事务
	private void rollback3Error() {
		try {
			connection.rollback();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}
	
	
	private String setValToSql4Update(T entity, String fieldStr,
			List<Object> params, Method mt, String fieldName, PersistenceAnnotation4Cls n)
			throws IllegalAccessException, InvocationTargetException {
			
		if (oracleDB(n)) {
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
	
}


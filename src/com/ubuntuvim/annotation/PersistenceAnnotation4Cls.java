package com.ubuntuvim.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 在 model 类上使用，标注与数据库关联的数据表的名字
 * @author ubuntuvim
 * @Email 1527254027@qq.com
 * @2015年7月26日 下午1:09:01
 */
@Target(ElementType.TYPE)  //  The definition of annotation using position, 
@Retention(RetentionPolicy.RUNTIME)  // the definition of time for analytical note, when running
@Documented
public @interface PersistenceAnnotation4Cls {
	
	String tableName() default "";  // 标记表名

	//  指定使用的数据库类型
	enum DBType {
		mysql, oracle, sqlserver
	};
	DBType dbType() default DBType.mysql;
	
}

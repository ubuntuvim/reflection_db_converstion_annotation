package com.ubuntuvim.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD})  //  The definition of annotation using position, can be used in METHOD statement
@Retention(RetentionPolicy.RUNTIME)  // the definition of time for analytical note, when running
@Documented
public @interface PersistenceAnnotation4Method {
	
	boolean noPersistenField() default false;  // 标记 model 类的属性不持久化到数据库
	
	boolean isPrimaryKey() default false;  //标记方法是主键的 get 方法，默认为否

	//   主键生成策略
	enum PrimaryKeyType {
		autoInc, sequence, other
	};
	//  type definition
	PrimaryKeyType primaryKeyType() default PrimaryKeyType.autoInc; // the default value is dynCls
	
	// 如果主键使用的是序列要给出序列名字
	//  并且要自行在数据定义好序列
	String sequenceName() default ""; 
	
//	boolean isDateField() default false;  //标记 model 类属性是 Date 类型
	//   yyyy-mm-dd hh24:mi:ss 或者 yyyy-mm-dd 获取其他的，默认为yyyy-mm-dd格式的时间
	String dateFormat() default "yyyy-mm-dd";  // 日期的类型
//	String date = "yyyy-mm-dd";
//	String dateTime = "yyyy-mm-dd HH:mm:ss";
//	//  为了防止用户输入时间格式出现错误，直接指定的格式
//	enum DateFormat {
//		date, dateTime
//	};
//	DateFormat dateFormat() default FieldType.stringType;
	
	//  指定属性的数据类型
	enum FieldType {
		integerType, stringType, dateType, longType
	};
	FieldType fieldType() default FieldType.stringType;
}

package com.ubuntuvim.core;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 得到泛型的class,类似于普通类的Class.getClass()
 * @author ubuntuvim
 * @email 1527254027@qq.com
 * @datatime 2015-7-23 下午11:58:08
 */
public class ClassGenericsUtil {
	   
		/**
		 * 子类初始化的时候获取子类的Class，根据获取的Class获取类的方法
		 * 使用反射机制执行getter/setter方法操作model类
		 * @param clazz
		 * @return 
		 */
	    public static Class<?> getGenericClass(Class<?> clazz) {
	        return getGenericClass(clazz, 0);
	    }

	    /**
	     * Locates  generic declaration by index on a class.
	     *
	     * @param clazz clazz The class to introspect
	     * @param index the Index of the generic ddeclaration,start from 0.
	     */
	    public static Class<?> getGenericClass(Class<?> clazz, int index) {
	        Type genType = clazz.getGenericSuperclass();

	        if (genType instanceof ParameterizedType) {
	            Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

	            if ((params != null) && (params.length >= (index - 1))) {
	                return (Class<?>) params[index];
	            }
	        }
	        return null;
	    }
}

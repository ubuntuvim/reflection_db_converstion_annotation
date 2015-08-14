package com.ubuntuvim.model;


import java.util.Date;

import com.ubuntuvim.annotation.PersistenceAnnotation4Cls;
import com.ubuntuvim.annotation.PersistenceAnnotation4Method;
import com.ubuntuvim.annotation.PersistenceAnnotation4Cls.DBType;
import com.ubuntuvim.annotation.PersistenceAnnotation4Method.FieldType;
import com.ubuntuvim.annotation.PersistenceAnnotation4Method.PrimaryKeyType;

@PersistenceAnnotation4Cls(tableName="user", dbType=DBType.mysql)
public class User {
	
	
	private int id;
	
	private String username;
	
	@PersistenceAnnotation4Method(dateFormat="yyyy-MM-dd", fieldType=FieldType.dateType)
	private Date birth;
	
	@PersistenceAnnotation4Method(dateFormat="yyyy-MM-dd HH:mm:ss", fieldType=FieldType.dateType)
	private Date detail_time;
	
//	@PersistenceAnnotation4Method(noPersistenField=true)
	private String noPersistence;
	
	@PersistenceAnnotation4Method(primaryKeyType=PrimaryKeyType.autoInc, isPrimaryKey=true)
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	@PersistenceAnnotation4Method(noPersistenField=true)
	public String getNoPersistence() {
		return noPersistence;
	}
	
	public void setNoPersistence(String noPersistence) {
		this.noPersistence = noPersistence;
	}
	
//	@PersistenceAnnotation4Method(noPersistenField=false)
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	
	@PersistenceAnnotation4Method(dateFormat="yyyy-MM-dd", fieldType=FieldType.dateType)
	public Date getBirth() {
		return birth;
	}

	public void setBirth(Date birth) {
		this.birth = birth;
	}

	@PersistenceAnnotation4Method(dateFormat="yyyy-MM-dd HH:mm:ss", fieldType=FieldType.dateType)
	public Date getDetail_time() {
		return detail_time;
	}

	public void setDetail_time(Date detail_time) {
		this.detail_time = detail_time;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", username=" + username + ", birth=" + birth
				+ ", detail_time=" + detail_time + ", noPersistence="
				+ noPersistence + "]";
	}
}

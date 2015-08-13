package com.ubuntuvim.model;

import java.util.Date;

import com.ubuntuvim.annotation.PersistenceAnnotation4Cls;
import com.ubuntuvim.annotation.PersistenceAnnotation4Method;
import com.ubuntuvim.annotation.PersistenceAnnotation4Method.PrimaryKeyType;

@PersistenceAnnotation4Cls(tableName="USERTEST1")
public class Usertest1 {
	private int stuno;
	private String username;
	private Date birth;
	
	@PersistenceAnnotation4Method(isPrimaryKey=true, 
			sequenceName="USER_SEQ", 
			primaryKeyType=PrimaryKeyType.sequence)
	public int getStuno() {
		return stuno;
	}
	public void setStuno(int stuno) {
		this.stuno = stuno;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public Date getBirth() {
		return birth;
	}
	public void setBirth(Date birth) {
		this.birth = birth;
	}
	
	
}

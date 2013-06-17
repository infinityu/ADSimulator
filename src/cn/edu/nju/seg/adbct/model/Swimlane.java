package cn.edu.nju.seg.adbct.model;

import java.util.ArrayList;

/**  
* @Title: Swimlane.java
* @Package model
* @Description: 
* @author ericyu.nju@gmail.com  
* @date 2012-2-21 下午08:22:50
* @version V1.0  
*/
public class Swimlane {
	private String ID;
	private String name;
	
	public Swimlane(String ID, String name){
		this.ID = ID;
		this.name = name;
	}
	public void setID(String iD) {
		ID = iD;
	}
	public String getID() {
		return ID;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public static Swimlane search(String targetID, ArrayList<Swimlane> list){
		for(Swimlane s :list){
			if(s.ID.equals(targetID))return s;
		}
		return null;
	}
	@Override
	public String toString () {
		if(this == null)return null;
		else return this.name;
	}
}

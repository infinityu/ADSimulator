package cn.edu.nju.seg.adbct.model;

import java.util.ArrayList;

/**
 * @description 活动图活动节点封装类 Triple.java Create on 2012-2-21
 * @author ericyu.nju@gmail.com
 * @lastEdit 2012-12-20
 */
public class Triple {
	private String ID; // 唯一标识符id
	private String name;// 图形显示时节点上的内容
	private String document;// 详细描述，可为空
	private String type;// 节点类型
	private String stereotype;// 构造型，标识输入输出节点？
	private String swimlaneID;// 所属泳道id
	private Swimlane swimlane;// 泳道类
	private ArrayList<String> incoming = new ArrayList<String>();// 入边id表
	private ArrayList<String> outgoing = new ArrayList<String>();// 出边id表
	private ArrayList<Transition> inTransitions = new ArrayList<Transition>();// 入边类列表
	private ArrayList<Transition> outTransitions = new ArrayList<Transition>();// 出边类列表

	public Triple(String ID, String name, String type, String inPartition) {
		this.ID = ID;
		this.name = name;
		this.type = type;
		this.swimlaneID = inPartition;
	}

	public Triple(String ID, String name, String type, String docu, String swimlaneID, ArrayList<String> incoming, ArrayList<String> outgoing) {
		this.ID = ID;
		this.name = name;
		this.type = type;
		this.document = docu;
		this.swimlaneID = swimlaneID;
		this.incoming = incoming;
		this.outgoing = outgoing;
	}

	public void setID(String iD) {
		ID = iD;
	}

	public String getID() {
		return ID;
	}

	public void setDocument(String document) {
		this.document = document;
	}

	public String getDocument() {
		return document;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setSwimlane(Swimlane swimlane) {
		this.swimlane = swimlane;
	}

	public Swimlane getSwimlane() {
		return swimlane;
	}

	public void setInTransitions(ArrayList<Transition> inTranstions) {
		this.inTransitions = inTranstions;
	}

	public ArrayList<Transition> getInTransitions() {
		return inTransitions;
	}

	public void setOutTransitions(ArrayList<Transition> outTransitions) {
		this.outTransitions = outTransitions;
	}

	public ArrayList<Transition> getOutTransitions() {
		return outTransitions;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setIncoming(ArrayList<String> incoming) {
		this.incoming = incoming;
	}

	public ArrayList<String> getIncoming() {
		return incoming;
	}

	public void setOutgoing(ArrayList<String> outgoing) {
		this.outgoing = outgoing;
	}

	public ArrayList<String> getOutgoing() {
		return outgoing;
	}

	public void setSwimlaneID(String SwimlaneID) {
		this.swimlaneID = SwimlaneID;
	}

	public String getSwimlaneID() {
		return swimlaneID;
	}

	public void setStereotype(String stereotype) {
		this.stereotype = stereotype;
	}

	public String getStereotype() {
		return stereotype;
	}

	@Override
	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append("ID: " + this.ID + "\n");
		s.append("name: " + this.name + "\n");
		s.append("type" + this.type + "\n");
		s.append("stereotype: " + this.stereotype + "\n");
		s.append("document: " + this.document + "\n");
		String str = null;
		if (this.swimlane != null)
			str = this.swimlane.toString();
		s.append("swimlane:" + str + "\n");
		s.append("inTranstions: \n");
		for (Transition tr : this.inTransitions) {
			s.append("   " + tr.getName() + "\n");
		}
		s.append("outTranstions: \n");
		for (Transition tr : this.outTransitions) {
			s.append("   " + tr.getName() + "\n");
		}
		s.append("\n");
		return s.toString();
	}
}

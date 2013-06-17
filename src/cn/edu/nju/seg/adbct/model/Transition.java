package cn.edu.nju.seg.adbct.model;

/**
 * @description 活动图边封装类 Transition.java Create on 2012-2-21
 * @author ericyu.nju@gmail.com
 * @lastEdit 2012-12-20
 */
public class Transition {
	private String ID;
	private String name;
	private String sourceID;
	private String targetID;
	private Triple sourceNode;
	private Triple destNode;
	private String guardCondition;

	public Transition(String ID, String name, String source, String target, String guard) {
		this.ID = ID;
		this.name = name;
		this.sourceID = source;
		this.targetID = target;
		this.guardCondition = guard;
	}

	public void setID(String iD) {
		ID = iD;
	}

	public String getID() {
		return ID;
	}

	public void setSourceNode(Triple sourceNode) {
		this.sourceNode = sourceNode;
	}

	public Triple getSourceNode() {
		return sourceNode;
	}

	public void setGuardCondition(String guardCondition) {
		this.guardCondition = guardCondition;
	}

	public String getGuardCondition() {
		return guardCondition;
	}

	public void setDestNode(Triple destNode) {
		this.destNode = destNode;
	}

	public Triple getDestNode() {
		return destNode;
	}

	public void setSourceID(String source) {
		this.sourceID = source;
	}

	public String getSourceID() {
		return sourceID;
	}

	public void setTargetID(String target) {
		this.targetID = target;
	}

	public String getTargetID() {
		return targetID;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append("ID: " + this.ID + "\n");
		s.append("name: " + this.name + "\n");
		s.append("guardCondotion: " + this.guardCondition + "\n");
		if (this.sourceNode != null)
			s.append("source: " + this.sourceNode.getName() + "\n");
		if (this.destNode != null)
			s.append("target: " + this.destNode.getName() + "\n");
		return s.toString();
	}
}

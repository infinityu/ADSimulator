package cn.edu.nju.seg.adbct.model;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @Title: ActivityGraph.java
 * @Package model
 * @Description: ActivityDiagram
 * @author ericyu.nju@gmail.com
 * @date 2012-2-21 下午08:19:43
 * @version V1.0
 */
public class ActivityDiagram {
	private Triple initState;
	private Triple finalState;
	private ArrayList<Triple> activities = new ArrayList<Triple>();
	private ArrayList<Transition> transitions = new ArrayList<Transition>();
	private ArrayList<Swimlane> swimlanes = new ArrayList<Swimlane>();
	private HashMap<String, Integer> inputHashMap = new HashMap<String, Integer>();

	/**
	 * ActivityDiagram构造函数
	 * 
	 * @param filename
	 */
	public ActivityDiagram(String filename) {
		ModelBuilder modelBuilder = new ModelBuilder(filename);
		this.activities = modelBuilder.getNodes();
		this.transitions = modelBuilder.getTransitions();
		this.initState = modelBuilder.getInitNode();
		this.finalState = modelBuilder.getFinalNode();
		this.inputHashMap = modelBuilder.getInputHashMap();
	}

	public ActivityDiagram(ModelBuilder modelBuilder) {
		this.activities = modelBuilder.getNodes();
		this.transitions = modelBuilder.getTransitions();
		this.initState = modelBuilder.getInitNode();
		this.finalState = modelBuilder.getFinalNode();
		this.inputHashMap = modelBuilder.getInputHashMap();
	}

	public ActivityDiagram(ArrayList<Triple> activities, ArrayList<Transition> transitions) {
		this.activities = activities;
		this.transitions = transitions;

		for (Triple t : this.activities) {
			if (t.getType().equals("uml:InitialNode")) {
				this.initState = t;
			}
		}

		for (Triple t : this.activities) {
			if (t.getType().equals("uml:ActivityFinalNode"))
				this.finalState = t;
		}
	}

	public ArrayList<Swimlane> getSwimlanes() {
		return swimlanes;
	}

	public void setSwimlanes(ArrayList<Swimlane> swimlanes) {
		this.swimlanes = swimlanes;
	}

	public ArrayList<Triple> getActivities() {
		return activities;
	}

	public void setActivities(ArrayList<Triple> activities) {
		this.activities = activities;
	}

	public void setTransitions(ArrayList<Transition> transitions) {
		this.transitions = transitions;
	}

	public ArrayList<Transition> getTransitions() {
		return transitions;
	}

	public Triple getInitState() {
		return initState;
	}

	public void setInitState(Triple initState) {
		this.initState = initState;
	}

	public Triple getFinalState() {
		return finalState;
	}

	public void setFinalState(Triple finalState) {
		this.finalState = finalState;
	}

	public ArrayList<Triple> getNodes() {
		return activities;
	}

	public void setNodes(ArrayList<Triple> nodes) {
		this.activities = nodes;
	}

	public HashMap<String, Integer> getInputHashMap() {
		return inputHashMap;
	}

	public void setInputHashMap(HashMap<String, Integer> inputHashMap) {
		this.inputHashMap = inputHashMap;
	}
}

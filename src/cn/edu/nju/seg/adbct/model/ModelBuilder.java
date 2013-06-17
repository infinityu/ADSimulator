package cn.edu.nju.seg.adbct.model;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @Title: ADModelReader.java
 * @Package model
 * @Description:
 * @author ericyu.nju@gmail.com
 * @date 2012-2-21 下午08:10:40
 * @lastModified 2012-3-17
 * @version V1.0
 */
/**
 * @description ModelBuilder.java Create on 2012-12-24
 * @use 
 * @author ericyu.nju@gmail.com
 */
public class ModelBuilder {
	private String filename;
	private Triple initNode;
	private Triple finalNode;
	private ArrayList<Triple> nodes = new ArrayList<Triple>();
	private ArrayList<Transition> transitions = new ArrayList<Transition>();
	private ArrayList<Swimlane> swimlanes = new ArrayList<Swimlane>();
	private HashMap<String, Integer> inputHashMap = new HashMap<String, Integer>();

	public ModelBuilder(String filename) {
		this.filename = filename;
		this.init();
	}

	public ActivityDiagram getActivityDiagram() {
		return new ActivityDiagram(this);
	}

	/**
	 * @Title: init
	 * @Description: initialize the model by reading file.uml, invoked by the
	 *               constructor method.
	 * @param
	 * @return void
	 * @throws
	 */
	private void init() {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		try {
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			// 定义 API， 使其从 XML文档获取 DOM文档实例。使用此类，应用程序员可以从 XML获取一个 Document。
			InputStream inputStream = new FileInputStream(filename);
			Document doc = documentBuilder.parse(inputStream);
			Element root = doc.getDocumentElement();
			// 当含有构造型等其他扩展时，模型根元素为"xmi:XMI"
			if (root.getNodeName().equals("xmi:XMI")) {
				NodeList modelAndProfiles = root.getChildNodes();
				for (int index = 0; index < modelAndProfiles.getLength(); index++) {
					Node n = modelAndProfiles.item(index);
					// System.out.println(n.getNodeName());
					if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals("uml:Model")) {
						// Model ->Package->Activity
						// ->Profile
						this.handlCurrentModel(n);
					} else if (n.getNodeType() == Node.ELEMENT_NODE && !n.getNodeName().equals("uml:Model")) {
						String stereotype = n.getNodeName().split(":", 2)[1];// "Output","Input",
																				// "Operation"
						String nodeID = n.getAttributes().getNamedItem("base_CallOperationAction").getNodeValue();
						for (Triple t : this.getNodes()) {
							if (t.getID().equals(nodeID)) {
								t.setStereotype(stereotype);
							}
						}
					}
				}

			} else if (root.getNodeName().equals("uml:Model")) {
				this.handlCurrentModel(root);
			} else {
				System.out.println("xmi error");
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.relateSwimlaneToNodes();
		this.relateEdgesToNodes();
	}

	/**
	 * @Title: handlCurrentModel
	 * @Description: // [Model] ->Package ->Activity // ->Profile
	 * @param @param model(root node of a model)
	 * @return void
	 * @throws
	 */
	private void handlCurrentModel(Node model) {
		if (model != null) {
			NodeList elements = model.getChildNodes();
			if (elements != null) {
				Node currentRoot = null;// root node of the activity diagram
				for (int i = 0; i < elements.getLength(); i++) {
					Node packageElement = elements.item(i);
					if (packageElement.getNodeType() == Node.ELEMENT_NODE && packageElement.getNodeName().equals("packagedElement") && packageElement.getAttributes().getNamedItem("xmi:type").getNodeValue().equals("uml:Package")) {
						elements = null;// ?
						elements = packageElement.getChildNodes();
						if (elements != null) {
							for (int j = 0; j < elements.getLength(); j++) {
								// There may be several Activity Diagrams in one
								// package.
								Node n = elements.item(j);
								if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals("packagedElement") && n.getAttributes().getNamedItem("xmi:type").getNodeValue().equals("uml:Activity")) {
									currentRoot = n;
									// System.out.println("currentRoot = uml:Activity");
								}
							}
						}
					} else if (packageElement.getNodeType() == Node.ELEMENT_NODE && packageElement.getNodeName().equals("profileApplication")) {
					} else {
					}
				} // end for
				if (currentRoot != null)
					handleCurrentRoot(currentRoot);
			}
		}

	}

	/**
	 * @Title: handleCurrentRoot
	 * @Description: // Model ->Package ->[Activity] // ->Profile
	 * @param @param currentRoot
	 * @return void
	 * @throws
	 */
	private void handleCurrentRoot(Node currentRoot) {
		NodeList elements = currentRoot.getChildNodes();
		for (int i = 0; i < elements.getLength(); i++) {
			Node n = elements.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals("node")) {
				this.handleDiagramNode(n);
			} else if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals("edge")) {
				this.handleDiagramEdge(n);
			}
			if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals("group")) {
				this.handleDiagramGroup(n);
			}
		}

	}

	/**
	 * 针对不同的节点类型进行处理
	 * 
	 * @param n
	 */
	private void handleDiagramNode(Node n) {
		String ID = n.getAttributes().getNamedItem("xmi:id").getNodeValue();
		String type = n.getAttributes().getNamedItem("xmi:type").getNodeValue();
		String name = n.getAttributes().getNamedItem("name").getNodeValue();
		String inPartition = null;
		if (n.getAttributes().getNamedItem("inPartition") != null) {
			inPartition = n.getAttributes().getNamedItem("inPartition").getNodeValue();
		}
		String docu = null;
		ArrayList<String> outgoing = new ArrayList<String>();
		ArrayList<String> incoming = new ArrayList<String>();
		if (n.getAttributes().getNamedItem("outgoing") != null) {
			StringTokenizer out = new StringTokenizer(n.getAttributes().getNamedItem("outgoing").getNodeValue());
			while (out.hasMoreTokens())
				outgoing.add(out.nextToken());
		}
		if (n.getAttributes().getNamedItem("incoming") != null) {
			StringTokenizer in = new StringTokenizer(n.getAttributes().getNamedItem("incoming").getNodeValue());
			while (in.hasMoreTokens())
				incoming.add(in.nextToken());
		}
		NodeList tempList = n.getChildNodes();
		// There may be annotations on the nodes.
		if (tempList != null)
			for (int i = 0; i < tempList.getLength(); i++) {
				Node tempNode = tempList.item(i);
				if (tempNode.getNodeName().equals("eAnnotations")) {
					NodeList list = tempNode.getChildNodes();
					for (int j = 0; j < list.getLength(); j++) {
						Node node = list.item(j);
						if (node.getNodeName().equals("details") && node.getAttributes().getNamedItem("key").getNodeValue().equals("documentation")) {
							docu = node.getAttributes().getNamedItem("value").getNodeValue();
							break;
						}
					}
					break;
				}
			}
		Triple triple = new Triple(ID, name, type, docu, inPartition, incoming, outgoing);
		this.getNodes().add(triple);

		// statements above read a node into ArrayList<Triple>states.
		// statements below handle the node according to the node's different
		// type.

		if (n.getAttributes().getNamedItem("xmi:type").getNodeValue().equals("uml:InitialNode")) {
			this.setInitNode(triple);
		}
		// initialize the inputHashMap
		else if (n.getAttributes().getNamedItem("xmi:type").getNodeValue().equals("uml:ReadVariableAction")) {
			String doc = triple.getDocument();
			String[] inputVars = doc.split(",");
			System.out.println("ModelBuilder\n" + "Initial input table:");
			for (String var : inputVars) {
				inputHashMap.put(var.trim(), 0);
				System.out.println(var + "=" + 0);
			}
			System.out.println("----");
		}
		// concrete operation,such as "x  = x + y"
		else if (n.getAttributes().getNamedItem("xmi:type").getNodeValue().equals("uml:CallOperationAction")) {

		}
		// method invoke
		else if (n.getAttributes().getNamedItem("xmi:type").getNodeValue().equals("uml:CallBehaviorAction")) {

		} else if (n.getAttributes().getNamedItem("xmi:type").getNodeValue().equals("uml:DecisionNode")) {

		} else if (n.getAttributes().getNamedItem("xmi:type").getNodeValue().equals("uml:ForkNode")) {

		} else if (n.getAttributes().getNamedItem("xmi:type").getNodeValue().equals("uml:JoinNode")) {

		} else if (n.getAttributes().getNamedItem("xmi:type").getNodeValue().equals("uml:ActivityFinalNode")) {
			this.setFinalNode(triple);
		}
	}

	private void handleDiagramEdge(Node n) {
		String ID = n.getAttributes().getNamedItem("xmi:id").getNodeValue();
		String name = n.getAttributes().getNamedItem("name").getNodeValue();
		String source = n.getAttributes().getNamedItem("source").getNodeValue();
		String target = n.getAttributes().getNamedItem("target").getNodeValue();
		String guard = null;
		NodeList children = n.getChildNodes();
		if (children != null) {
			for (int i = 0; i < children.getLength(); i++) {
				Node child = children.item(i);
				if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals("guard")) {
					if (child.getAttributes().getNamedItem("xmi:type").getNodeValue().equals("uml:LiteralString")) {
						guard = child.getAttributes().getNamedItem("value").getNodeValue();
					}
					break;
				}
			}
		}
		this.getTransitions().add(new Transition(ID, name, source, target, guard));
	}

	private void handleDiagramGroup(Node n) {
		String ID = n.getAttributes().getNamedItem("xmi:id").getNodeValue();
		String name = n.getAttributes().getNamedItem("name").getNodeValue();
		this.getSwimlanes().add(new Swimlane(ID, name));
	}

	private void relateSwimlaneToNodes() {
		for (Triple t : this.getNodes()) {
			for (Swimlane s : this.getSwimlanes()) {
				if (t.getSwimlaneID() != null && t.getSwimlaneID().equals(s.getID())) {
					t.setSwimlane(s);
					break;
				}
			}
		}
	}

	private void relateEdgesToNodes() {
		for (Triple triple : this.getNodes()) {
			for (String out : triple.getOutgoing()) {
				for (Transition transition : this.getTransitions()) {
					if (transition.getID().equals(out)) {
						triple.getOutTransitions().add(transition);
						transition.setSourceNode(triple);
						break;
					}
				}
			}
			for (String in : triple.getIncoming()) {
				for (Transition transition : this.getTransitions()) {
					if (transition.getID().equals(in)) {
						triple.getInTransitions().add(transition);
						transition.setDestNode(triple);
						break;
					}
				}
			}
		}
	}

	public HashMap<String, Integer> getInputHashMap() {
		return inputHashMap;
	}

	public void setInputHashMap(HashMap<String, Integer> inputHashMap) {
		this.inputHashMap = inputHashMap;
	}

	public Triple getInitNode() {
		return initNode;
	}

	public void setInitNode(Triple startState) {
		this.initNode = startState;
	}

	public Triple getFinalNode() {
		return finalNode;
	}

	public void setFinalNode(Triple endState) {
		this.finalNode = endState;
	}

	public void setSwimlanes(ArrayList<Swimlane> swimlanes) {
		this.swimlanes = swimlanes;
	}

	public ArrayList<Swimlane> getSwimlanes() {
		return swimlanes;
	}

	public void setTransitions(ArrayList<Transition> transitions) {
		this.transitions = transitions;
	}

	public ArrayList<Transition> getTransitions() {
		return transitions;
	}

	public void setNodes(ArrayList<Triple> states) {
		this.nodes = states;
	}

	public ArrayList<Triple> getNodes() {
		return nodes;
	}

	public static void main(String[] args) {
		String fp = "E:\\workspace\\MBT\\";
		String fn = "AD.uml";
		String file = fp + fn;
		ModelBuilder ad = new ModelBuilder(file);
		for (Transition t : ad.getTransitions()) {
			System.out.println(t.toString());
		}
	}
}

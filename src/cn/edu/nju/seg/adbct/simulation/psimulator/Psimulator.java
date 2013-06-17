package cn.edu.nju.seg.adbct.simulation.psimulator;

import java.awt.Color;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

import org.eclipse.ui.IViewReference;
import org.eclipse.ui.PlatformUI;

import simulator.view.treeview.views.TraceView;
import simulator.view.treeview.views.TreeObject;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;
import choco.kernel.solver.Solver;
import cn.edu.nju.seg.adbct.demo.Animation;
import cn.edu.nju.seg.adbct.demo.TraceRecord;
import cn.edu.nju.seg.adbct.model.ActivityDiagram;
import cn.edu.nju.seg.adbct.model.ModelBuilder;
import cn.edu.nju.seg.adbct.model.Transition;
import cn.edu.nju.seg.adbct.model.Triple;
import cn.edu.nju.seg.adbct.simulation.BranchChooser;
import cn.edu.nju.seg.adbct.simulation.ConcreteExe;
import cn.edu.nju.seg.adbct.simulation.Simulator;
import cn.edu.nju.seg.adbct.simulation.SymbolicExe;
import cn.edu.nju.seg.adbct.solver.ChocoExpParser;
import cn.edu.nju.seg.adbct.solver.ChocoSolver;
import cn.edu.nju.seg.adbct.solver.GuardForSolver;

public class Psimulator {

	private static Psimulator simulator;

	private Psimulator(String uml, Animation animation) {
		this.animation = animation;
		tr = new TraceRecord(uml);
		this.uml = uml;
		ModelBuilder modelBuilder = new ModelBuilder(uml);
		this.activityDiagram = modelBuilder.getActivityDiagram();
		this.inputHashMap = activityDiagram.getInputHashMap();
		this.finalState = activityDiagram.getFinalState();
		this.concreteExecutor = new ConcreteExe(inputHashMap);
		this.symbolicExecutor = new SymbolicExe(inputHashMap);
		this.guardForSolver = new GuardForSolver(inputHashMap);
	}

	private Psimulator(String uml) {
		tr = new TraceRecord(uml);
		this.uml = uml;
		ModelBuilder modelBuilder = new ModelBuilder(uml);
		this.activityDiagram = modelBuilder.getActivityDiagram();
		this.inputHashMap = activityDiagram.getInputHashMap();
		this.finalState = activityDiagram.getFinalState();
		this.concreteExecutor = new ConcreteExe(inputHashMap);
		this.symbolicExecutor = new SymbolicExe(inputHashMap);
		this.guardForSolver = new GuardForSolver(inputHashMap);
	}

	public static Psimulator getInstance() {
		if (simulator == null) {
			synchronized (Psimulator.class) {
				if (simulator == null) {
				}
			}
		}
		return simulator;
	}

	public static Psimulator getInstance(String uml, Animation animation) {
		if (simulator == null) {
			synchronized (Psimulator.class) {
				if (simulator == null) {
					if (uml != null && animation != null) {
						simulator = new Psimulator(uml, animation);
					}
				}
			}
		}
		return simulator;
	}

	private Animation animation;

	private ActivityDiagram activityDiagram;

	// 输入变量的具体值表
	private HashMap<String, Integer> inputHashMap;

	// 记录节点处符号变量
	private HashMap<Triple, HashMap<String, Integer[]>> dNodeVarCoeHashMap = new HashMap<Triple, HashMap<String, Integer[]>>();

	// 记录节点取反次数
	private HashMap<Triple, Integer> decisionNodeHashMap = new HashMap<Triple, Integer>();

	// 保存路径条件，每一次完整的concolic对应一条获取的路径条件
	private Stack<Transition> guardStack;

	// 当前执行的节点（不考虑并发情形）
	private Triple currentActivity = null;
	private Triple nextActivity;

	/**
	 * 用于记录当前所有的执行轨迹
	 */
	private List<List<TreeObject>> traceRecord = new ArrayList<List<TreeObject>>();
	/**
	 * 用于记录当前执行路径的轨迹
	 */
	private List<TreeObject> traceList = new ArrayList<TreeObject>();

	private String errorInfo;

	private Triple finalState;

	// 具体值执行器
	private ConcreteExe concreteExecutor;

	// 符号值执行器
	private SymbolicExe symbolicExecutor;

	// 分支选择器
	private BranchChooser branchChooser;

	private GuardForSolver guardForSolver;

	private ChocoSolver chocoSolver;

	private ArrayList<String> constraints;

	// 记录可行测试用例集的个数，即最终结果个数。
	private int count = 1;

	// 记录被访问过的结点个数
	private int visitedNodesNum = 0;

	// 记录节点深度
	private HashMap<Triple, Integer> depthMap = new HashMap<Triple, Integer>();

	// 记录元素访问次数，用于统计覆盖度信息
	private HashMap<String, Integer> traceMap = new HashMap<String, Integer>();

	// 记录遇到的join节点（可能成为循环的入口）
	private Stack<Triple> entryStack = new Stack<Triple>();

	// 记录与循环入口对应循环体中的执行语句
	private HashMap<Triple, ArrayList<Triple>> loopStatementsMap = new HashMap<Triple, ArrayList<Triple>>();

	private Queue<ArrayList<Triple>> loopQueue = new LinkedList<ArrayList<Triple>>();

	// 当前循环的入口点
	private Triple currentEntry;

	// 嵌套循环内部入口点
	private Set<Triple> innerEntrySet;

	private boolean isLoopEnd = false;
	private boolean isJionMet = false;
	private boolean isInnerLoop = false;

	private FileWriter fileWriter;

	// 执行轨迹记录
	private TraceRecord tr;

	// 源uml文件名
	private String uml;

	// 自动执行
	public void run() {
		try {
			fileWriter = new FileWriter("exeTrace.txt");
			tr.umlBackUp();
			executeOnePath();
			while (updateInputTable()) {
				tr.reLoad(this.uml);
				animation.resetColor();
				count++;
				executeOnePath();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fileWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println(count + " results.");
	}

	/**
	 * 单步执行
	 * @throws IOException 
	 */
	public void stepExecute()  {
		if (currentActivity == null) {
			currentActivity = activityDiagram.getInitState();
		} else if (currentActivity.getType().equals("uml:ActivityFinalNode")) {
			traceList.add(new TreeObject(currentActivity.getName()));
			traceRecord.remove(traceList);
			traceRecord.add(new ArrayList<TreeObject>(traceList));
			currentActivity = null;
			simulator.updateInputTable();
		} else {
			visitActivity(currentActivity);
			nodeCoverageStatistic(currentActivity.getID());
			color(currentActivity, null);
			traceList.add(new TreeObject(currentActivity.getName()));
			currentActivity = nextActivity;
		}
		IViewReference[] refs = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getViewReferences();
		for (IViewReference viewReference : refs) {
			if (viewReference.getId().equals("simulator.view.treeview.views.TraceView")) {
				((TraceView) viewReference.getView(true)).updateUI(traceRecord);
			}
		}
	}

	private void updateView(Triple currentActivity) {

	}

	/*
	 * 单路径执行
	 */
	public void executeOnePath() throws IOException {
		animation.resetColor();
		traceList.clear();
		// 用于保存路径条件（Path conditions）
		this.guardStack = new Stack<Transition>();
		this.concreteExecutor.setInitialVariableHashMap(inputHashMap);
		this.symbolicExecutor.initializeVarCoeHashMap(inputHashMap);
		currentActivity = activityDiagram.getInitState();
		while (currentActivity != finalState && visitActivity(currentActivity)) {
			nodeCoverageStatistic(currentActivity.getID());
			color(currentActivity, null);
			if (currentActivity.getType() != "uml:CallBehaviorAction") {
				traceList.add(new TreeObject(currentActivity.getName()));
			}
			currentActivity = nextActivity;
		}
		if (currentActivity.getType().equals("uml:ActivityFinalNode")) {
			// 找到了一条新的执行路径
			// fileWriter.write("Node: ActivityFinalNode" + "\n");
			nodeCoverageStatistic(currentActivity.getID());
			System.out.println("Node: ActivityFinalNode");
			// 执行轨迹写入文件
			tr.makeColor(currentActivity.getID());
			animation.makeColor(currentActivity.getName());
			tr.saveTraceFile();
		}
		traceList.add(new TreeObject(currentActivity.getName()));
		traceRecord.add(new ArrayList<TreeObject>(traceList));
		// 更新执行轨迹视图
		IViewReference[] refs = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getViewReferences();
		for (IViewReference viewReference : refs) {
			if (viewReference.getId().equals("simulator.view.treeview.views.TraceView")) {
				((TraceView) viewReference.getView(true)).updateUI(traceRecord);
			}
		}
		this.printResult();
	}

	// 访问当前节点
	private boolean visitActivity(Triple currentActivity) {
		// fileWriter.write("Node:" + currentActivity.getName() + "\n");
		if (currentActivity.getType().equals("uml:InitialNode")) {
			nextActivity = currentActivity.getOutTransitions().get(0).getDestNode();
		} else if (currentActivity.getType().equals("uml:CallOperationAction")) {
			concolicExecute(currentActivity);
			nextActivity = currentActivity.getOutTransitions().get(0).getDestNode();
		}
		// TODO 子活动图调用
		else if (currentActivity.getType().equals("uml:CallBehaviorAction")) {
			nextActivity = currentActivity.getOutTransitions().get(0).getDestNode();
		}
		// 变量输入
		else if (currentActivity.getType().equals("uml:ReadVariableAction")) {
			nextActivity = currentActivity.getOutTransitions().get(0).getDestNode();
		}
		// 判定节点
		else if (currentActivity.getType().equals("uml:DecisionNode")) {
			color(currentActivity, null);
			// 保存最后一个分支的变量符号表
			this.dNodeVarCoeHashMap.put(currentActivity, new HashMap<String, Integer[]>(symbolicExecutor.getVarCoeHashMap()));
			branchChooser = new BranchChooser(this.concreteExecutor.getVariableHashMap());
			ArrayList<Transition> Transitions = currentActivity.getOutTransitions();
			for (Transition t : Transitions) {
				boolean ret = branchChooser.check(t.getGuardCondition());
				System.out.println("return: " + ret);
				if (ret) {
					guardStack.push(t);
					if (!decisionNodeHashMap.containsKey(t.getSourceNode())) {
						decisionNodeHashMap.put(t.getSourceNode(), 0);
					}
					nextActivity = t.getDestNode();
					break;
				}

			}
		}
		// 并发分支的处理,默认在分支中每个节点只有一个出边
		else if (currentActivity.getType().equals("uml:ForkNode")) {
			// 用于保存每条并发分支上到候选边。
			List<Transition> fireTransEnable = currentActivity.getOutTransitions();
			int count = fireTransEnable.size();
			Random r = new Random();
			while (count != 0) {
				int index = r.nextInt(count);
				currentActivity = fireTransEnable.get(index).getDestNode();
				tr.makeColor(currentActivity.getID());
				animation.makeColor(currentActivity.getName());
				// 每遇到一次join节点，则删除一个候选位。
				if (currentActivity.getType().equals("uml:JoinNode")) {
					fireTransEnable.remove(index);
					count--;
				} else {
					if (currentActivity.getType().equals("uml:CallOperationAction")) {
						concolicExecute(currentActivity);
					}
					fireTransEnable.set(index, currentActivity.getOutTransitions().get(0));
				}
			}
			// 退出循环时当前节点为joinNode。
			nextActivity = currentActivity.getOutTransitions().get(0).getDestNode();
		} else {
			System.out.println("Undefined Node");
			errorInfo = "Undefined Node";
			return false;
		}
		return true;
	}

	private void color(Triple currentActivity, Color color) {
		// 对当前节点进行着色。
		tr.makeColor(currentActivity.getID());
		animation.makeColor(currentActivity.getName());
	}

	public void handleLoop(Stack<HashMap<Triple, ArrayList<String>>> loopStack) {

	}

	public boolean isJion(Triple activity) {
		return activity.getInTransitions().size() > 1 ? true : false;
	}

	public void concolicExecute(Triple activity) {
		concreteExecutor.execute(activity.getName());
		symbolicExecutor.execute(activity.getName());
	}

	/**
	 * 更新输入变量表
	 * 
	 * @return
	 * @throws IOException
	 */
	public Boolean updateInputTable()  {
		constraints = new ArrayList<String>();
		Transition t = guardStack.pop();
		// 如果该分支节点被“取反”过，则忽略。
		while (decisionNodeHashMap.get(t.getSourceNode()).equals(1)) {
			if (guardStack.size() == 0) {
				break;
			} else {
				t = guardStack.pop();
			}
		}
		// guardStack中的最后一个。
		if (decisionNodeHashMap.get(t.getSourceNode()).equals(1)) {
			return false;
		} else {
			decisionNodeHashMap.put(t.getSourceNode(), 1);
			ArrayList<Transition> fireTransitions = t.getSourceNode().getOutTransitions();
			for (Transition fireTran : fireTransitions) {
				if (!t.equals(fireTran)) {
					guardStack.push(fireTran);
					break;
				}
			}
		}
		// fileWriter.write("Solving PathConstraint：\n");
		System.out.println("Solving PathConstraint：");

		for (Transition transition : guardStack) {
			HashMap<String, Integer[]> varCoeHashMap = this.dNodeVarCoeHashMap.get(t.getSourceNode());
			String constraint = guardForSolver.getGuardForSolver(transition.getGuardCondition(), varCoeHashMap);
			// fileWriter.write(constraint);
			System.out.println(constraint);
			constraints.add(constraint);
		}
		chocoSolver = new ChocoSolver();
		chocoSolver.setConstraints(constraints);
		Set<String> inputs = inputHashMap.keySet();
		Boolean rtn = chocoSolver.solve();
		// 约束求解，返回true表示找到一个可行解，返回false表示证明无解，返回null表示限定条件下没有解出。
		if (rtn == true) {
			System.out.println("New Inputs Generated:");
			// fileWriter.write("New Inputs Generated:\n");
			chocoSolver.printResult();
			// 更新输入值
			for (String input : inputs) {
				Solver solver = chocoSolver.getSolver();
				ChocoExpParser chocoExpParser = chocoSolver.getChocoExpParser();
				IntegerExpressionVariable variable = chocoExpParser.getVariableHashMap().get(input);
				String newInput = solver.getVar(variable).toString();
				String[] exps = newInput.split(":");
				try {
					this.inputHashMap.put(exps[0].trim(), Integer.parseInt(exps[1].trim()));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return true;
		} else if (rtn == false) {
			return false;
		} else {
			return null;
		}
	}

	public void printResult() {
		Set<String> inputVar = this.inputHashMap.keySet();
		System.out.print("Result" + count + "~~" + "Input:[");
		for (String var : inputVar) {
			System.out.print(var + "=" + inputHashMap.get(var) + ";");
		}
		System.out.print("]  Constraints:[");
		for (Transition transition : guardStack) {
			System.out.print(transition.getGuardCondition() + ";");
		}
		System.out.println("]");
	}

	// 更新节点覆盖度信息
	public void nodeCoverageStatistic(String currentId) {
		if (traceMap.containsKey(currentId)) {
			traceMap.put(currentId, traceMap.get(currentId) + 1);
		} else {
			traceMap.put(currentId, 1);
			visitedNodesNum++;
		}
	}

	public HashMap<String, Integer> getInputHashMap() {
		return inputHashMap;
	}

	public void setInputHashMap(HashMap<String, Integer> inputHashMap) {
		this.inputHashMap = inputHashMap;
	}

	public float getNodeCoverage() {
		System.out.println(this.visitedNodesNum);
		System.out.println(this.activityDiagram.getActivities().size());
		return (float) this.visitedNodesNum / this.activityDiagram.getActivities().size();
	}

	public HashMap<Triple, Integer> getDecisionNodeHashMap() {
		return decisionNodeHashMap;
	}

	public void setDecisionNodeHashMap(HashMap<Triple, Integer> decisionNodeHashMap) {
		this.decisionNodeHashMap = decisionNodeHashMap;
	}

	public static void main(String[] args) {
		String uml = "demo.uml";
		Simulator simulator = new Simulator(uml);
		simulator.run();
		System.out.println("node coverage:" + simulator.getNodeCoverage());
	}
}

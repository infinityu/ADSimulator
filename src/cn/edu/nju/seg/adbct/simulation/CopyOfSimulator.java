package cn.edu.nju.seg.adbct.simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import cn.edu.nju.seg.adbct.model.ActivityDiagram;
import cn.edu.nju.seg.adbct.model.Transition;
import cn.edu.nju.seg.adbct.model.Triple;
import cn.edu.nju.seg.adbct.solver.ChocoExpParser;
import cn.edu.nju.seg.adbct.solver.ChocoSolver;
import cn.edu.nju.seg.adbct.solver.GuardForSolver;

import choco.kernel.model.variables.integer.IntegerExpressionVariable;
import choco.kernel.solver.Solver;

public class CopyOfSimulator {
	private ActivityDiagram activityDiagram;
	private HashMap<String, Integer> inputHashMap;

	private HashMap<String, Integer> initialInputHashMap;
	private HashMap<String, Integer[]> varCoeMap;

	// 用于记录节点取反次数
	private HashMap<Triple, Integer> decisionNodeHashMap = new HashMap<Triple, Integer>();
	// 用于记录节点处符号变量
	private HashMap<Triple, HashMap<String, Integer[]>> dNodeVarCoeHashMap = new HashMap<Triple, HashMap<String, Integer[]>>();
	private Stack<Transition> guardStack;
	private Triple currentActivity;
	private Triple finalState;
	private ConcreteExe concreteExecutor;
	private SymbolicExe symbolicExecutor;
	private BranchChooser branchChooser;
	private GuardForSolver guardForSolver;
	private ChocoSolver chocoSolver;
	private ArrayList<String> constraints;
	// 记录可行测试用例集的个数
	private int count = 1;
	// 记录节点深度
	private HashMap<Triple, Integer> depthMap = new HashMap<Triple, Integer>();
	// 记录遇到的jion节点（可能成为循环的入口）
	private Stack<Triple> entryStack = new Stack<Triple>();
	// 记录与循环入口对应循环体中的执行语句
	private HashMap<Triple, ArrayList<Triple>> loopStatementsMap = new HashMap<Triple, ArrayList<Triple>>();
	private HashMap<Triple, HashMap<String, Integer[]>> loopSymVarMap = new HashMap<Triple, HashMap<String, Integer[]>>();
	private HashMap<Triple, Transition> loopBranchMap = new HashMap<Triple, Transition>();
	private Queue<ArrayList<Triple>> loopQueue = new LinkedList<ArrayList<Triple>>();
	// 当前循环的入口点
	private Triple currentEntry;
	// 嵌套循环内部入口点
	private Set<Triple> innerEntrySet = new HashSet<Triple>();
	private boolean isJionMet = false;

	public CopyOfSimulator(String file) {
		this.activityDiagram = new ActivityDiagram(file);
		this.inputHashMap = activityDiagram.getInputHashMap();
		this.finalState = activityDiagram.getFinalState();
		this.concreteExecutor = new ConcreteExe(inputHashMap);
		this.symbolicExecutor = new SymbolicExe(inputHashMap);
		this.guardForSolver = new GuardForSolver(inputHashMap);

		this.initialInputHashMap = new HashMap<String, Integer>(inputHashMap);
	}

	public void run() {
		concolicOnePath();
		while (updateInputTable()) {
			count++;
			concolicOnePath();
		}
		System.out.println(count + " results.");

		handleLoop(20);

	}

	/*
	 * 由输入变量驱动一条路径的concolic执行
	 */
	public void concolicOnePath() {
		// 用于保存路径条件（Path conditions）
		this.guardStack = new Stack<Transition>();
		this.concreteExecutor.setInitialVariableHashMap(inputHashMap);
		this.symbolicExecutor.initializeVarCoeHashMap(inputHashMap);
		currentActivity = activityDiagram.getInitState();

		Transition transition = null;

		while (currentActivity != finalState) {
			if (currentActivity.getType().equals("uml:InitialNode")) {
				currentActivity = currentActivity.getOutTransitions().get(0).getDestNode();
			} else if (currentActivity.getType().equals("uml:CallOperationAction")) {
				// concolic操作
				System.out.println("Node:" + currentActivity.getName());
				concreteExecutor.execute(currentActivity.getName());
				symbolicExecutor.execute(currentActivity.getName());

				// 有两条入边，则可能形成循环
				if (currentActivity.getInTransitions().size() > 1) {
					isJionMet = true;
					System.out.println("in+++");
					// 又一次访问到该节点,表示是一个循环
					if (entryStack.contains(currentActivity)) {
						// 停止记录循环体
						isJionMet = false;
						ArrayList<Triple> loopStatements = loopStatementsMap.get(currentActivity);
						loopQueue.offer(loopStatements);// bug,befor,after?
						innerEntrySet.add(entryStack.pop());
						// currentEntry = entryStack.peek();
						HashMap<String, Integer[]> symVarMap = new HashMap<String, Integer[]>(symbolicExecutor.getVarCoeHashMap());
						loopSymVarMap.put(currentActivity, symVarMap);
					} else if (innerEntrySet.contains(currentActivity)) {
						// isInnerLoop = true;

					} else {// 初次遇到双入边节点
						entryStack.push(currentActivity);
						currentEntry = currentActivity;
						ArrayList<Triple> loopElements = new ArrayList<Triple>();
						loopElements.add(currentActivity);
						loopStatementsMap.put(currentActivity, loopElements);
					}

				}
				if (isJionMet) {
					loopStatementsMap.get(currentEntry).add(currentActivity);
				}

				// 当前活动节点指向下一个
				currentActivity = currentActivity.getOutTransitions().get(0).getDestNode();
			} else if (currentActivity.getType().equals("uml:CallOperationAction")) {
				// 方法调用
				currentActivity = currentActivity.getOutTransitions().get(0).getDestNode();
			} else if (currentActivity.getType().equals("uml:ReadVariableAction")) {
				// 随机输入
				currentActivity = currentActivity.getOutTransitions().get(0).getDestNode();
			}
			// 分支节点
			else if (currentActivity.getType().equals("uml:DecisionNode")) {
				// 对于判定节点有两条入边，则一定形成循环
				if (currentActivity.getInTransitions().size() > 1) {
					isJionMet = true;
					// 又一次访问到该节点,表示是一个循环
					if (entryStack.contains(currentActivity)) {
						// 停止记录循环体
						isJionMet = false;
						ArrayList<Triple> loopStatements = loopStatementsMap.get(currentActivity);
						loopQueue.offer(loopStatements);// bug,befor,after?
						innerEntrySet.add(entryStack.pop());
						// currentEntry = entryStack.peek();
						HashMap<String, Integer[]> symVarMap = new HashMap<String, Integer[]>(symbolicExecutor.getVarCoeHashMap());
						loopSymVarMap.put(currentActivity, symVarMap);
						System.out.println("fired branch added");
						loopBranchMap.put(currentActivity, transition);
					} else if (innerEntrySet.contains(currentActivity)) {
						isJionMet = false;
						// isInnerLoop = true;

					} else {// 初次遇到双入边节点
						varCoeMap = new HashMap<String, Integer[]>(symbolicExecutor.getVarCoeHashMap());
						entryStack.push(currentActivity);
						currentEntry = currentActivity;
						ArrayList<Triple> loopElements = new ArrayList<Triple>();
						loopElements.add(currentActivity);
						loopStatementsMap.put(currentActivity, loopElements);
					}
				}
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
						currentActivity = t.getDestNode();
						// 保存具体执行时选择的分支
						transition = t;
						break;
					}

				}

			} else {
				System.out.println("Undefined Node");
			}
		}
		if (currentActivity.getType().equals("uml:ActivityFinalNode")) {
			System.out.println("Node: ActivityFinalNode");
		}

		this.printResult();
	}

	public void handleLoop(int times) {
		Set<Triple> entrySet = loopStatementsMap.keySet();
		int loopTimes = times;
		for (Triple key : entrySet) {
			ArrayList<Triple> loopList = loopStatementsMap.get(key);
			SymbolicExe symbolicExe = new SymbolicExe(initialInputHashMap);
			symbolicExe.initializeVarCoeHashMap(initialInputHashMap);
			symbolicExe.setVarCoeHashMap(varCoeMap);
			constraints = new ArrayList<String>();
			for (int j = 0; j < loopTimes; j++) {
				for (int i = 0; i < loopList.size(); i++) {
					if (loopList.get(i).getType().equals("uml:CallOperationAction")) {
						symbolicExe.execute(loopList.get(i).getName());
					} else {
						String cons = loopBranchMap.get(key).getGuardCondition();
						String constraint = guardForSolver.getGuardForSolver(cons, symbolicExe.getVarCoeHashMap());
						System.out.println(constraint);
						constraints.add(constraint);
					}
				}
			}

			List<Transition> transitions = key.getOutTransitions();
			for (Transition t : transitions) {
				if (loopBranchMap.get(key) != t) {
					constraints.add(guardForSolver.getGuardForSolver(t.getGuardCondition(), symbolicExe.getVarCoeHashMap()));
				}
			}
			chocoSolver = new ChocoSolver();
			chocoSolver.setConstraints(constraints);
			Set<String> inputs = inputHashMap.keySet();
			if (chocoSolver.solve()) {
				System.out.println("Loop times: " + loopTimes + " Inputs Generated:");
				chocoSolver.printResult();
			}
		}
	}

	public boolean isJion(Triple activity) {
		return activity.getInTransitions().size() > 1 ? true : false;
	}

	public boolean updateInputTable() {
		constraints = new ArrayList<String>();
		Transition t = guardStack.pop();
		while (decisionNodeHashMap.get(t.getSourceNode()).equals(1)) {
			if (guardStack.size() == 0) {
				break;
			} else {
				t = guardStack.pop();
			}
		}

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

		System.out.println("Solving PathConstraint：");
		for (Transition transition : guardStack) {
			HashMap<String, Integer[]> varCoeHashMap = this.dNodeVarCoeHashMap.get(t.getSourceNode());
			String constraint = guardForSolver.getGuardForSolver(transition.getGuardCondition(), varCoeHashMap);
			System.out.println(constraint);
			constraints.add(constraint);
		}
		chocoSolver = new ChocoSolver();
		chocoSolver.setConstraints(constraints);
		Set<String> inputs = inputHashMap.keySet();
		if (chocoSolver.solve()) {
			System.out.println("New Inputs Generated:");
			chocoSolver.printResult();
			// 更新输入值
			for (String input : inputs) {
				Solver solver = chocoSolver.getSolver();
				ChocoExpParser chocoExpParser = chocoSolver.getChocoExpParser();
				IntegerExpressionVariable variable = chocoExpParser.getVariableHashMap().get(input);
				String newInput = solver.getVar(variable).toString();
				// System.out.println(newInput);
				String[] exps = newInput.split(":");
				try {
					this.inputHashMap.put(exps[0].trim(), Integer.parseInt(exps[1].trim()));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return true;
		} else {
			return false;
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

	public HashMap<String, Integer> getInputHashMap() {
		return inputHashMap;
	}

	public void setInputHashMap(HashMap<String, Integer> inputHashMap) {
		this.inputHashMap = inputHashMap;
	}

	public static void main(String[] args) {
		String file = "ADloop.uml";
		CopyOfSimulator simulator = new CopyOfSimulator(file);
		simulator.run();
	}
}

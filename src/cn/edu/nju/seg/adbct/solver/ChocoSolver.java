package cn.edu.nju.seg.adbct.solver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Pattern;

import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;
import choco.kernel.solver.Solver;

public class ChocoSolver {
	private ArrayList<String> constraints;
	private HashMap<String, IntegerExpressionVariable> variableHashMap = new HashMap<String, IntegerExpressionVariable>();
	private Model model = new CPModel();
	private Solver solver = new CPSolver();
	private ChocoExpParser chocoExpParser = new ChocoExpParser(variableHashMap, model);

	public ChocoExpParser getChocoExpParser() {
		return chocoExpParser;
	}

	public void setChocoExpParser(ChocoExpParser chocoExpParser) {
		this.chocoExpParser = chocoExpParser;
	}

	public Solver getSolver() {
		return solver;
	}

	public Boolean solve() {
		for (String constraint : constraints) {
			Constraint cst = parse(constraint);
			if (cst != null) {
				model.addConstraint(cst);
			} else {
				return null;
			}
		}
		solver.read(model);
		return solver.solve();
	}

	public void printResult() {
		Set<String> varialbelSet = chocoExpParser.getVariableHashMap().keySet();
		for (String varName : varialbelSet) {
			IntegerExpressionVariable variable = chocoExpParser.getVariableHashMap().get(varName);
			System.out.println(solver.getVar(variable));
		}
	}

	private Constraint parse(String constraint) {
		String REGEX;
		if (constraint.contains("<") && !constraint.contains("=")) {
			REGEX = "<";
			Pattern p = Pattern.compile(REGEX);
			String[] operands = p.split(constraint);

			return Choco.lt(chocoExpParser.parse(operands[0].trim()), chocoExpParser.parse(operands[1].trim()));
		} else if (constraint.contains("<=")) {
			REGEX = "<=";
			Pattern p = Pattern.compile(REGEX);
			String[] operands = p.split(constraint);
			return Choco.leq(chocoExpParser.parse(operands[0].trim()), chocoExpParser.parse(operands[1].trim()));
		} else if (constraint.contains(">") && !constraint.contains("=")) {
			REGEX = ">";
			Pattern p = Pattern.compile(REGEX);
			String[] operands = p.split(constraint);
			// System.out.println(">");
			return Choco.gt(chocoExpParser.parse(operands[0].trim()), chocoExpParser.parse(operands[1].trim()));
		} else if (constraint.contains(">=")) {
			REGEX = ">=";
			Pattern p = Pattern.compile(REGEX);
			String[] operands = p.split(constraint);
			// System.out.println(">=");
			return Choco.geq(chocoExpParser.parse(operands[0].trim()), chocoExpParser.parse(operands[1].trim()));
		} else if (constraint.contains("==")) {
			REGEX = "==";
			Pattern p = Pattern.compile(REGEX);
			String[] operands = p.split(constraint);
			return Choco.eq(chocoExpParser.parse(operands[0].trim()), chocoExpParser.parse(operands[1].trim()));
		} else if (constraint.contains("!=")) {
			REGEX = "!=";
			Pattern p = Pattern.compile(REGEX);
			String[] operands = p.split(constraint);
			return Choco.neq(chocoExpParser.parse(operands[0].trim()), chocoExpParser.parse(operands[1].trim()));
		} else {
			System.out.println("Syntax Error!");
		}
		return null;
	}

	public void setConstraints(ArrayList<String> constraints) {
		this.constraints = constraints;
	}

}

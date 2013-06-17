package cn.edu.nju.seg.adbct.solver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @description GuardForSolver.java Create on 2012-12-24
 * @use 根据符号变量表更新分支条件
 * @author ericyu.nju@gmail.com
 */
public class GuardForSolver {
    private HashMap<String, Integer[]> varCoeHashMap = new HashMap<String, Integer[]>();
    static final int NO_RIGHT_BRANKET = 0;
    private int pos = 0;
    private int numOfInput = 0;
    private String[] expr;
    private ArrayList<String> inputVarList;

    public GuardForSolver(HashMap<String, Integer> inputHashMap) {
        Set<String> keySet = inputHashMap.keySet();
        this.inputVarList = new ArrayList<String>(keySet);

    }

    public String getGuardForSolver(String INPUT, HashMap<String, Integer[]> varCoeHashMap) {
        this.varCoeHashMap = varCoeHashMap;
        String REGEX = "true";
        if (INPUT.contains("<") && !INPUT.contains("=")) {
            REGEX = "<";
        }
        else if (INPUT.contains("<=")) {
            REGEX = "<=";
        }
        else if (INPUT.contains(">") && !INPUT.contains("=")) {
            REGEX = ">";
        }
        else if (INPUT.contains(">=")) {
            REGEX = ">=";
        }
        else if (INPUT.contains("==")) {
            REGEX = "==";
        }
        else if (INPUT.contains("!=")) {
            REGEX = "!=";
        }
        else {
            System.out.println("Syntax Error!");
        }

        String REGEX_BLANK = "\\s";
        Pattern p_EQUAL = Pattern.compile(REGEX);
        Pattern p_BLANK = Pattern.compile(REGEX_BLANK);
        String[] operand = p_EQUAL.split(INPUT.trim());
        String[] exprLeft = p_BLANK.split(operand[0].trim());
        String[] exprRight = p_BLANK.split(operand[1].trim());
        Integer[] coeLeft = parse(exprLeft);
        Integer[] coeRight = parse(exprRight);
        StringBuilder guard = new StringBuilder();
        for (int i = 0; i < coeLeft.length - 1; i++) {
            if (i == 0) {
                guard.append(coeLeft[i] + " * " + inputVarList.get(i));
            }
            else if (coeLeft[i].intValue() >= 0) {
                guard.append(" + " + coeLeft[i] + " * " + inputVarList.get(i));
            }
            else {
                int temp = coeLeft[i].intValue();
                guard.append(" - " + -temp + " * " + inputVarList.get(i));
            }
        }
        if(coeLeft[coeLeft.length - 1].intValue() <0){
        	guard.append(" - " + -coeLeft[coeLeft.length - 1]);
        }else{
        	guard.append(" + " + coeLeft[coeLeft.length - 1]);
        }
        guard.append(REGEX);
        for (int i = 0; i < coeRight.length - 1; i++) {
            if (i == 0) {
                guard.append(coeRight[i] + " * " + inputVarList.get(i));
            }
            else if (coeRight[i].intValue() >= 0) {
                guard.append(" + " + coeRight[i] + " * " + inputVarList.get(i));
            }
            else {
                int temp = coeRight[i].intValue();
                guard.append(" - " + -temp + " * " + inputVarList.get(i));
            }
        }
        if(coeRight[coeRight.length - 1].intValue() <0){
        	guard.append(" - " + -coeRight[coeRight.length - 1]);
        }else{
        	guard.append(" + " + coeRight[coeRight.length - 1]);
        }
        return guard.toString();
    }

    private Integer[] parse(String[] expr) {
        this.expr = expr;
        pos = 0;
        return E_AddSub();
    }

    // 产生式“E -> T+E | T-E | T”的函数，用来分析加减算术表达式。
    private Integer[] E_AddSub() {
        Integer[] rtn = T_MulDiv(); // 计算加减算术表达式的左元
        while ((pos != expr.length - 1) && (expr[pos + 1].equals("+") || expr[pos + 1].equals("-"))) {
            pos++;
            String op = expr[pos];
            pos++;
            Integer[] opr2 = T_MulDiv(); // 计算加减算术表达式的右元
            // 左右元返回后计算求值
            if (op.equals("+")) {
                for (int i = 0; i < rtn.length; i++) {
                    rtn[i] = rtn[i] + opr2[i];
                }
            }
            else {
                for (int i = 0; i < rtn.length; i++) {
                    rtn[i] = rtn[i] - opr2[i];
                }
            }
        }
        return rtn;
    }

    // 产生式“T -> F*T | F/T | F”的函数，用来分析乘除算术表达式。
    private Integer[] T_MulDiv() {
        Integer rtn[] = F_Number(); // 计算乘除算术表达式的左元
        while ((pos != expr.length - 1) && (expr[pos + 1].equals("*") || expr[pos + 1].equals("/"))) {
            System.out.println("unable to solve *or/ calc.");
            pos++;
            String op = expr[pos];
            pos++;
            Integer[] opr2 = F_Number(); // 计算乘除算术表达式的右元
            // 左右元返回后计算求值
            if (op.equals("*")) {
                // rtn = rtn * opr2;
            }
            else {
                // rtn = rtn / opr2;
            }
        }
        return rtn;
    }

    // 产生式“F -> i | (E)”的函数，用来分析数字和括号内的表达式。
    private Integer[] F_Number() {
        Integer[] rtn = null;
        // 用产生式F->(E)推导：
        if (expr[pos].equals("(")) // 如果字符缓冲区当前位置的符号是"("
        {
            pos++; // 则指示器加一指向下一个符号
            rtn = E_AddSub(); // 调用产生式“E -> T+E | T-E | T”的分析函数

            if (!expr[pos + 1].equals(")")) { // 如果没有与"("匹配的")"
                Error(NO_RIGHT_BRANKET); // 则产生错误
            }
            return rtn;
        }
        // 如果有多个(，不在此函数中循环判断，函数中只需判断一次，然后可以通过递归的方式判断
        try
        // 如果字符缓冲区中当前位置的字符为数字(终结字符)
        // YL左括号后应该为数字
        {
            // 则用产生式F -> i推导
            // 把字符缓冲区中当前位置的字符串转换为整数
            // rtn = Choco.makeIntVar(expr[pos], Choco.MIN_LOWER_BOUND,
            // Choco.MAX_UPPER_BOUND);

            if (isInteger(expr[pos])) {
                rtn = new Integer[numOfInput + 1];
                for (int i = 0; i < rtn.length; i++) {
                    if (i != rtn.length - 1) {
                        rtn[i] = 0;
                    }
                    else {
                        rtn[i] = Integer.parseInt(expr[pos]);
                    }
                }
                System.out.println("an integerVar made: " + expr[pos]);
            }
            else if (isDouble(expr[pos])) {
                System.out.println("input of real number can't be resolved!");
            }
            // 符号变量
            else {
                if (varCoeHashMap.containsKey(expr[pos])) {
                    rtn = varCoeHashMap.get(expr[pos]).clone();
                }
                else {
                    System.out.println("variable undefined:" + expr[pos]);
                }
                Integer[] coe = varCoeHashMap.get(expr[pos]);
                StringBuilder strCoe = new StringBuilder();
                for (Integer i : coe) {
                    strCoe = strCoe.append(i.toString() + ";");
                }
//                System.out.println("recognize:" + expr[pos] + " = " + strCoe);
            }

        }
        catch (NumberFormatException e) {
            System.out.println("ErrorNumber");
        }
        /*
         * else //如果不是数字则产生相应的错误 { if(expr[pos]==')') //如果发现一个")"
         * Error(EMPTY_BRACKET); //则是括号是空的，即括号内无算术表达式。 else if(expr[pos]=='\0')
         * //如果此时输入串结束 Error(UNEXPECTED_END); //则算术表达式非法结束 else
         * Error(INVALID_CHAR_IN); //否则输入字符串中含有非法字符 }
         */
        return rtn;

    }

    // 出错处理函数，可以指出错误位置，出错信息。
    static void Error(int ErrCode) {
        System.out.println("Error:" + ErrCode);

    }

    /**
     * 判断字符串是否是整数
     */
    public static boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        }
        catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 判断字符串是否是浮点数
     */
    public static boolean isDouble(String value) {
        try {
            Double.parseDouble(value);
            if (value.contains("."))
                return true;
            return false;
        }
        catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 判断字符串是否是数字
     */
    public static boolean isNumber(String value) {
        return isInteger(value) || isDouble(value);
    }
}
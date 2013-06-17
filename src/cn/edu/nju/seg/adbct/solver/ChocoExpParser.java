package cn.edu.nju.seg.adbct.solver;

import java.util.HashMap;
import java.util.regex.Pattern;

import choco.cp.model.CPModel;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;
import choco.Choco;
import choco.kernel.model.Model;

/**
 * @ClassName: ChocoExpParser
 * @Description: This class is used to parse a string of expression to
 *               ChocoExpression
 * @author YuLei ericyu.nju@gmail.com
 * @date 2012-3-14 下午09:48:59
 */
public class ChocoExpParser {

    private static ChocoExpParser instance = null;
    private HashMap<String, IntegerExpressionVariable> variableHashMap = new HashMap<String, IntegerExpressionVariable>();
    Model model = new CPModel();

    static final int NO_RIGHT_BRANKET = 0;
    private int pos = 0;
    private String[] expr;

    /**
     * @Title: getInstance
     * @Description: skeleton pattern to derive an object
     * @param @return
     * @return ChocoExpParser
     * @throws
     */
    // public static ChocoExpParser getInstance() {
    // if (instance == null) {
    // instance = new ChocoExpParser();
    // }
    // return instance;
    // }

    public ChocoExpParser(HashMap<String, IntegerExpressionVariable> variableHashMap, Model model) {
        this.variableHashMap = variableHashMap;
        this.model = model;
    }

    public void init(HashMap<String, IntegerExpressionVariable> variableHashMap, Model model) {
        this.variableHashMap = variableHashMap;
        this.model = model;
    }

    public HashMap<String, IntegerExpressionVariable> getVariableHashMap() {
        return variableHashMap;
    }

    public IntegerExpressionVariable parse(String INPUT) {
        pos = 0;
        String REGEX = "\\s";
        Pattern p = Pattern.compile(REGEX);
        expr = p.split(INPUT);
        return E_AddSub();
    }

    // 产生式“E -> T+E | T-E | T”的函数，用来分析加减算术表达式。
    private IntegerExpressionVariable E_AddSub() {
        // System.out.println("E_called");
        IntegerExpressionVariable rtn = T_MulDiv(); // 计算加减算术表达式的左元
        while ((pos != expr.length - 1) && (expr[pos + 1].equals("+") || expr[pos + 1].equals("-"))) {
            // System.out.println("+-");
            pos++;
            String op = expr[pos]; // 取字符缓冲区中当前位置的符号到op
            pos++;
            IntegerExpressionVariable opr2 = T_MulDiv(); // 计算加减算术表达式的右元
            // 计算求值
            if (op.equals("+")) {
                rtn = Choco.plus(rtn, opr2);
//                System.out.println(rtn.getVariable(0) + "+" + opr2.getVariable(0));
            }
            else { // 否则（是"-"号）
                rtn = Choco.minus(rtn, opr2);
//                System.out.println(rtn.getVariable(0) + "-" + opr2.getVariable(0));
            }
        }
        return rtn;
    }

    // 产生式“T -> F*T | F/T | F”的函数，用来分析乘除算术表达式。
    private IntegerExpressionVariable T_MulDiv() {
        // System.out.println("T_called");
        IntegerExpressionVariable rtn = F_Number(); // 计算乘除算术表达式的左元
        while ((pos != expr.length - 1) && (expr[pos + 1].equals("*") || expr[pos + 1].equals("/"))) {
            pos++;
            String op = expr[pos]; // 取字符缓冲区中当前位置的符号到op
            pos++;
            IntegerExpressionVariable opr2 = F_Number(); // 计算乘除算术表达式的右元
            // 计算求值
            if (op.equals("*")) { // 如果是"*"号
                rtn = Choco.mult(rtn, opr2); // 则用乘法计算
            }
            else {
                // 否则（是"\"号）
                rtn = Choco.div(rtn, opr2); // 用除法计算
            }
            // System.out.println(pos);
        }
        return rtn;
    }

    // 产生式“F -> i | (E)”的函数，用来分析数字和括号内的表达式。
    private IntegerExpressionVariable F_Number() {
        // System.out.println("F_called");
        IntegerExpressionVariable rtn = Choco.makeIntVar("varTemp", Choco.MIN_LOWER_BOUND, Choco.MAX_UPPER_BOUND);// 声明存储返回值的变量
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
        // YL如果有多个(，不在此函数中循环判断，函数中只需判断一次，然后可以通过递归的方式判断
        try
        // 如果字符缓冲区中当前位置的字符为数字(终结字符)
        // YL左括号后应该为数字
        {
            // 则用产生式F -> i推导
            // 把字符缓冲区中当前位置的字符串转换为整数
            // rtn = Choco.makeIntVar(expr[pos], Choco.MIN_LOWER_BOUND,
            // Choco.MAX_UPPER_BOUND);

            if (isInteger(expr[pos])) {
                rtn = Choco.constant(Integer.parseInt(expr[pos]));
                model.addVariable(rtn);
//                System.out.println("an integerVar made: " + expr[pos]);
            }
            else if (isDouble(expr[pos])) {
                // rtn = Choco.constant(Double.parseDouble(expr[pos]));
                System.out.println("input of real number can't be resolved!");
            }
            else {
                if (variableHashMap.containsKey(expr[pos])) {
                    rtn = variableHashMap.get(expr[pos]);
                }
                else {
                    // rtn = Choco.makeIntVar(expr[pos], Choco.MIN_LOWER_BOUND,
                    // Choco.MAX_UPPER_BOUND);
                    rtn = Choco.makeIntVar(expr[pos], -1000, 1000);
                    variableHashMap.put(expr[pos], rtn);
                    model.addVariable(rtn);
//                    System.out.println("a ChocoVar made: " + expr[pos]);
                }
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

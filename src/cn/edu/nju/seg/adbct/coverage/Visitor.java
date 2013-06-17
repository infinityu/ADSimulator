package cn.edu.nju.seg.adbct.coverage;

import cn.edu.nju.seg.adbct.model.Transition;
import cn.edu.nju.seg.adbct.model.Triple;

/**
 * 
 * @description 活动图访问者接口，用于定义模型建立和仿真时对节点与边的操作 Visitor.java Create on 2012-12-24
 * @author ericyu.nju@gmail.com
 * @lastEdit 2012-12-24
 */
public interface Visitor {

	/**
	 * 对应于活动节点的访问操作
	 */
	public void visit(Triple triple);

	/**
	 * 对应于转移边的访问操作
	 */
	public void visit(Transition transition);

}

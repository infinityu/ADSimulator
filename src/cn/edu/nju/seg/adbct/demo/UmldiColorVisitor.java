package cn.edu.nju.seg.adbct.demo;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.VisitorSupport;

/**
 * 
 * @author yulei
 * @description 用于对指定id的活动图节点着色。
 * @createTime 2013-2-26
 */
public class UmldiColorVisitor extends VisitorSupport {

	private String hrefUmlId;

	public UmldiColorVisitor(String id) {
		this.hrefUmlId = id;
	}

	public void visit(Element element) {
		if (element.getName().equals("element") && element.attribute("href").getValue().equals(hrefUmlId)) {
			// 在当前节点的爷爷节点添加颜色的属性。
			Element grandParent = element.getParent().getParent();
			Element propertyColor = grandParent.addElement("property");
			propertyColor.addAttribute("key", "foregroundColor");
			propertyColor.addAttribute("value", "0,255,0");
		}
	}

	public void visit(Attribute attr) {
	}
}

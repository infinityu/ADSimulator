package cn.edu.nju.seg.adbct.demo;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.uml2.uml.internal.impl.ActivityEdgeImpl;
import org.eclipse.uml2.uml.internal.impl.ActivityNodeImpl;
import org.topcased.modeler.di.model.Diagram;
import org.topcased.modeler.di.model.DiagramElement;
import org.topcased.modeler.di.model.GraphElement;
import org.topcased.modeler.di.model.util.DIUtils;

public class Animation {

	private Diagram diagram;

	List<DiagramElement> elements;

	/**
	 * 记录已经着色的节点
	 */
	private List<String> coloredList = new ArrayList<String>();

	public Animation(Diagram diagram) {
		this.diagram = diagram;
		this.elements = diagram.getContained();
	}

	/**
	 * 此方法要求活动图元素名称没有重复
	 * 
	 * @param name
	 */
	public void makeColor(String name) {
		for (DiagramElement e : elements) {
			if (e instanceof GraphElement) {
				EObject obj = org.topcased.modeler.utils.Utils.getElement((GraphElement) e);

				if (obj instanceof ActivityNodeImpl) {
					ActivityNodeImpl node = ((ActivityNodeImpl) obj);
					if (node.getName().equals(name)) {
						coloredList.add(name);
						DIUtils.setProperty(e, "foregroundColor", "255,0,0");
//						diagram.notifyAll();
					}
				} else if (obj instanceof ActivityEdgeImpl) {
					ActivityEdgeImpl node = ((ActivityEdgeImpl) obj);
					if (node.getName().equals(name)) {
						coloredList.add(name);
						DIUtils.setProperty(e, "foregroundColor", "255,0,0");
//						diagram.notifyAll();
					}
				}

			}

		}
	}

	/**
	 * 恢复被着色的节点与边
	 */
	public void resetColor() {
		for (DiagramElement e : elements) {
			if (e instanceof GraphElement) {
				EObject obj = org.topcased.modeler.utils.Utils.getElement((GraphElement) e);
				if (obj instanceof ActivityNodeImpl) {
					ActivityNodeImpl node = ((ActivityNodeImpl) obj);
					if (coloredList.contains(node.getName())) {
						coloredList.remove(node.getName());
						DIUtils.setProperty(e, "foregroundColor", "0,0,0");
//						diagram.notifyAll();
					}
				} else if (obj instanceof ActivityEdgeImpl) {
					ActivityEdgeImpl node = ((ActivityEdgeImpl) obj);
					if (coloredList.contains(node.getName())) {
						coloredList.remove(node.getName());
						DIUtils.setProperty(e, "foregroundColor", "0,0,0");
//						diagram.notifyAll();
					}
				}

			}

		}
	}
}

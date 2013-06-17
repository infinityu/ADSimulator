package simulator.actions;

import java.io.IOException;
import java.util.HashMap;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.topcased.modeler.di.model.Diagram;
import org.topcased.modeler.uml.editor.UMLEditor;

import cn.edu.nju.seg.adbct.demo.Animation;
import cn.edu.nju.seg.adbct.model.Triple;
import cn.edu.nju.seg.adbct.simulation.SingleStepSim;

public class PopupAction implements IEditorActionDelegate {

	private static HashMap<Triple, Integer> decisionNodeHashMap = new HashMap<Triple, Integer>();

	private static Diagram diagram;

	private static Animation animation;

	private static SingleStepSim simulator;

	static {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		UMLEditor activeEditor = (org.topcased.modeler.uml.editor.UMLEditor) page.getActiveEditor();
		page.getViewReferences();
		diagram = activeEditor.getActiveDiagram();
		animation = new Animation(diagram);
		simulator = new SingleStepSim("/home/yulei/runtime-EclipseApplication/" + diagram.getName() + ".uml", animation);

	}

	@Override
	public void run(IAction arg0) {
		// System.out.println(diagram.getName());

		simulator.setDecisionNodeHashMap(decisionNodeHashMap);
		try {
			simulator.concolicOnePath();
			simulator.updateInputTable();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.decisionNodeHashMap = simulator.getDecisionNodeHashMap();
		System.out.println("node coverage:" + simulator.getNodeCoverage());

	}

	@Override
	public void selectionChanged(IAction arg0, ISelection arg1) {

	}

	@Override
	public void setActiveEditor(IAction arg0, IEditorPart arg1) {

	}

}

package simulator.actions;

import java.io.IOException;
import java.util.HashMap;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import cn.edu.nju.seg.adbct.model.Triple;
import cn.edu.nju.seg.adbct.simulation.psimulator.Psimulator;

/**
 * Our sample action implements workbench action delegate. The action proxy will
 * be created by the workbench and shown in the UI. When the user tries to use
 * the action, this delegate will be created and execution will be delegated to
 * it.
 * 
 * @see IWorkbenchWindowActionDelegate
 */
public class ExeOnePathAction implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;

	private static HashMap<Triple, Integer> decisionNodeHashMap = new HashMap<Triple, Integer>();

	private static Psimulator simulator = Psimulator.getInstance();

	/**
	 * The constructor.
	 */
	public ExeOnePathAction() {
	}

	/**
	 * The action has been activated. The argument of the method represents the
	 * 'real' action sitting in the workbench UI.
	 * 
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		// MessageDialog.openInformation(window.getShell(), "Simulator",
		// "Hello, Eclipse world");
		// System.out.println(diagram.getName());

		simulator.setDecisionNodeHashMap(decisionNodeHashMap);
		try {
			// simulator.concolicOnePath();
			simulator.executeOnePath();
			simulator.updateInputTable();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.decisionNodeHashMap = simulator.getDecisionNodeHashMap();
		System.out.println("node coverage:" + simulator.getNodeCoverage());
	}

	/**
	 * Selection in the workbench has been changed. We can change the state of
	 * the 'real' action here if we want, but this can only happen after the
	 * delegate has been created.
	 * 
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * We can use this method to dispose of any system resources we previously
	 * allocated.
	 * 
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * We will cache window object in order to be able to provide parent shell
	 * for the message dialog.
	 * 
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}
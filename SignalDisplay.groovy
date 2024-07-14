import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import com.nomagic.magicdraw.actions.ActionsConfiguratorsManager;
import com.nomagic.magicdraw.plugins.Plugin;
import com.nomagic.magicdraw.ui.actions.DefaultDiagramAction;
import com.nomagic.magicdraw.ui.dialogs.MDDialogParentProvider;
import com.nomagic.magicdraw.uml.DiagramType;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import utils.Logger;

import cameoutils.DiagramConfigurator;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JButton;
import java.util.ArrayList;
import com.nomagic.magicdraw.core.Application;
import java.awt.event.ActionListener;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.ui.browser.Browser;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.magicdraw.ui.browser.Node;
import com.nomagic.magicdraw.ui.browser.Tree;


class SignalDisplayOB{

	JFrame guiFrame;
	/**
	 * Creates diagram action with name "Signal Display".
	 */
	Element[] selectedElements;
	Application application;
	SignalDisplayOB(Element[] se, Application app) {
		guiFrame = new JFrame("Title");
		selectedElements = se;
		application = app;
	}

	/**
	 *
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 *
	 *      Displaying how many symbols are selected in diagram.
	 *
	 */

	public void runSignalDisplay() {
		boolean gitout = true;
		JOptionPane.showMessageDialog(null, selectedElements.length + " elements selected");

		StringBuilder text = new StringBuilder("Selected elements:");
		JOptionPane.showMessageDialog(MDDialogParentProvider.getProvider().getDialogOwner(), text.toString());

		if (gitout) 
			return;

		JFrame guiFrame;

		guiFrame = new JFrame();
		guiFrame.setSize(800, 600);
		guiFrame.setVisible(true);

	}
}



/**
 * **********************************************************************************************
 * UNCLASSIFIED - This is a class that will pop up a small dialog allowing the user to select
 * the top level item to start doing some action. Since opaque behaviors (OB) often need to be 
 * selected and run the old fasioned way of running a plugin which performs operations on the 
 * currently selected object (package, block etc) will not work since the OB is selected. This
 * allows selection after the OB has been started. 
 * @author D99510
 * **********************************************************************************************
 */
class scopeAndDoSD implements ActionListener {
	Application app = Application.getInstance();
	Project p = app.getProject();
	Browser b = p.getBrowser();  
	JFrame frame = new JFrame();
	JDialog dialog = new JDialog(frame,"Select Scope Element",false);
	JPanel panel = new JPanel();
	JButton okButton = new JButton("OK");
	JButton cancelButton = new JButton("Cancel");

	/**
	 * **********************************************************************************************
	 * UNCLASSIFIED - Creates the dialog and sets it visible
	 * @author D99510
	 * **********************************************************************************************
	 */
	public void doIt() {
		panel.add(okButton);
		panel.add(cancelButton);
		dialog.add(panel);
		okButton.addActionListener(this);
		cancelButton.addActionListener(this);

		dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		dialog.setSize(200, 80);
		dialog.setVisible(true);
		frame.setAlwaysOnTop(true);
	}

	/**
	 * **********************************************************************************************
	 * UNCLASSIFIED - Gets the selected elements
	 * @return - the selected element 
	 * @author D99510
	 * **********************************************************************************************
	 */
	private Element[] getSelectedElements() {
		Tree t = b.getActiveTree();
		Node[] nodes = t.getSelectedNodes();
		ArrayList<Element> elems = new ArrayList<Element>();
		for (Node n: nodes) {
			Object o = n.getUserObject();
			if (o instanceof Element)
				elems.add((Element)o);
		}
		return (Element[]) elems.toArray();
	}

	/**
	 * **********************************************************************************************
	 * UNCLASSIFIED - This is the area that YOU must put some action. Look at QuickSync for exmple
	 * if needed
	 * @author D99510
	 * **********************************************************************************************
	 */
	private void doSomething() {
		Element[] se = getSelectedElements();
		if (se != null)  {
			SignalDisplayOB sd = new SignalDisplayOB(se,app);
			sd.runSignalDisplay();
			for (int i = 0; i < se.length; i++)
				app.getGUILog().log("Doing Something with " + se[i].getHumanName());
		} else {
			app.getGUILog().log("Selection is NULL or Nothing Selected");
		}
	}

	/**
	 * **********************************************************************************************
	 * UNCLASSIFIED - Handles the selection then runs the intended action
	 * @param e - the event 
	 * @author D99510
	 * **********************************************************************************************
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == okButton) {
			doSomething();
			app.getGUILog().log("Action Done");
		} else {
			app.getGUILog().log("Action Canceled");
		}
		dialog.dispose();
	}
}

scopeAndDoSD sAD = new scopeAndDoSD();
sAD.doIt();
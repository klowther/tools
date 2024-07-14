import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.ui.browser.Browser;
import com.nomagic.magicdraw.ui.browser.Node;
import com.nomagic.magicdraw.ui.browser.Tree;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdbasicbehaviors.Behavior;

/**
 * **********************************************************************************************
 * UNCLASSIFIED - This is a class that will pop up a small dialog allowing the user to select
 * the top level item to start doing some action. Since opaque behaviors (OB) often need to be 
 * selected and run the old fashioned way of running a plugin which performs operations on the 
 * currently selected object (package, block etc) will not work since the OB is selected. This
 * allows selection after the OB has been started. 
 * @author D99510
 * **********************************************************************************************
 */
class scopeAndDo implements ActionListener {
	Application app = Application.getInstance();
	Project p = app.getProject();
	Browser b = p.getBrowser();  

	JFrame frame = new JFrame();
	JDialog dialog = new JDialog(frame,"Opaque Behavior Runner",false);
	JPanel instructionsPanel = new JPanel();
	JPanel selectionPanel = new JPanel();
	JPanel runPanel = new JPanel();
	JPanel containerPanel = new JPanel();

	JButton selectButton = new JButton("Select");
	JButton runButton = new JButton("Run");
	JButton cancelButton = new JButton("Cancel");

	JLabel instructionsLabel = new JLabel("Select element(s) for running opaque behavior: ");
	JLabel selectedElementsLabel = new JLabel("");

	/**
	 * **********************************************************************************************
	 * UNCLASSIFIED - Creates the dialog and sets it visible
	 * @author D99510
	 * **********************************************************************************************
	 */
	public void doIt() {
		Object obj = b.getActiveTree().getSelectedNode().getUserObject();	
		Behavior element = (Behavior)obj;
		String elementName = element.getHumanName().trim();
		String type = element.getHumanType();
		instructionsLabel.setText(instructionsLabel.getText() + elementName.substring(type.length()).trim());
		
		instructionsPanel.setLayout(new FlowLayout());
		selectionPanel.setLayout(new FlowLayout());
		runPanel.setLayout(new FlowLayout());
		
		instructionsPanel.add(instructionsLabel);
		selectionPanel.add(selectButton);
		selectionPanel.add(selectedElementsLabel);
		runPanel.add(runButton);
		runPanel.add(cancelButton);
		
		containerPanel.add(instructionsPanel);
		containerPanel.add(selectionPanel);
		containerPanel.add(runPanel);
		
		containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.Y_AXIS));
		dialog.add(containerPanel);
		
		runButton.addActionListener(this);
		cancelButton.addActionListener(this);
		selectButton.addActionListener(this);

		dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		dialog.pack();
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
	 * UNCLASSIFIED - This is the area that YOU must put some action. Look at CPLSearchAndReplace for example
	 * if needed
	 * @author D99510
	 * **********************************************************************************************
	 */
	private void runButtonSelected() {
		app.getGUILog().log("Executing OB");
	}
	
	/**
	 * **********************************************************************************************
	 * UNCLASSIFIED - Action listener for the Select button. Gets the selected elements from the tree
	 * and sets the selected elements label
	 * @author N23056
	 * **********************************************************************************************
	 */
	private void selectButtonSelected() {
		Element[] se = getSelectedElements();
		if (se != null)  {
			for (int i = 0; i < se.length; i++) {
				String type = se[i].getHumanType();
				app.getGUILog().log("Selected element: " + se[i].getHumanName());
				if(i > 0) {
					selectedElementsLabel.setText(selectedElementsLabel.getText() + 
							"," + 
							se[i].getHumanName().substring(type.length()).trim());
				}
				else{
					selectedElementsLabel.setText(se[i].getHumanName().substring(type.length()).trim());
				}
			}
		} else {
			app.getGUILog().log("Selection is NULL or Nothing Selected");
		}
	}
	/**
	 * **********************************************************************************************
	 * UNCLASSIFIED - Action listener for the Cancel button. Exits the program
	 * @author N23056
	 * **********************************************************************************************
	 */
	private void cancelButtonSelected() {
		dialog.dispose();	
	}

	/**
	 * **********************************************************************************************
	 * UNCLASSIFIED - Handles the selection and calls the correct action listener
	 * @param e - the event 
	 * @author D99510
	 * **********************************************************************************************
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == runButton) {
			runButtonSelected();
			app.getGUILog().log("Action Done");
		} else if(e.getSource() == cancelButton){
			cancelButtonSelected();
			app.getGUILog().log("Action Canceled");
		} else if(e.getSource() == selectButton){
			selectButtonSelected();
		}
	}
}
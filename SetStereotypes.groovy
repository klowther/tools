import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JButton;
import java.util.ArrayList;
import com.nomagic.magicdraw.core.Application;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.ui.browser.Browser;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.magicdraw.ui.browser.Node;
import com.nomagic.magicdraw.ui.browser.Tree;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import com.nomagic.magicdraw.actions.ActionsConfiguratorsManager;
import com.nomagic.magicdraw.plugins.Plugin;

import com.nomagic.magicdraw.ui.browser.actions.DefaultBrowserAction;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

import cameoutils.BrowserConfigurator;
import cameoutils.CamUtils;
import utils.HTMLUtils;
import utils.PasteTable;

/**
 ***********************************************************************
 * UNCLASSIFIED Allows "batch" setting of stereotypes and associated tags to
 * elements
 *
 * The utility presents a table where a stereotype can be selected. when
 * selected the tags are presented and can be filled in on the table. Tag values
 * can then be assigned. When the OK button is pressed the stereotypes will be
 * applied with the given tag values to all selected object and will allow
 * recursion into containers if check box selected.
 */

class SetStereoTypesOB{

	Element[] selectedElements;
	Application application;
	SetStereoTypesOB(Element[] se, Application app){
		selectedElements = se;
		application = app;
	}


	class MainDlg extends JFrame implements ActionListener {
		private static final long serialVersionUID = 1L;
		JButton stypeButton = new JButton("Select Stereotype");
		JLabel stypeLabel = new JLabel("None Selected");
		Stereotype stype = null;
		JCheckBox recurse = new JCheckBox("Recurse");
		JButton okButton = new JButton("OK");
		JCheckBox append = new JCheckBox("Append Values");
		PasteTable pt;


		/**
		 ***********************************************************************
		 * UNCLASSIFIED Apply the value to the tag
		 *
		 */
		void setTagVal(Element elem, Stereotype st, String tagName, String value, String link) {
			String tagVal = value;

			if (link != null && link.length()>0)
				tagVal = HTMLUtils.makeLink(value,link);

			if(append.isSelected() && tagVal.length() > 0) {
				CamUtils.setTagVal(elem, st, tagName, value, true);
			}
			else if(!append.isSelected()) {
				CamUtils.setTagVal(elem, st, tagName, value, false);
			}
		}

		/**
		 ***********************************************************************
		 * UNCLASSIFIED Apply the stereotype values from the table to the selected
		 * elements
		 *
		 */
		void applyStereotypes() {
			String[][] vals = pt.stringValues();
			final int NAME = 0;
			final int VAL = 1;
			final int HYP = 2;
			boolean linksExist = vals[0].length > 2;

			for (int i = 1; i < selectedElements.length; i++) {
				StereotypesHelper.addStereotype(selectedElements[i], stype);
				for (int j = 0; j < vals.length; j++) {
					String link = linksExist ? vals[j][HYP] : null;
					setTagVal(selectedElements[i], stype, vals[j][NAME], vals[j][VAL], link);
				}
			}
		}

		ArrayList<String> getTagNames(Stereotype ster) {
			ArrayList<String> tagNames = new ArrayList<String>();

			List<?> tags = ster.getAttribute();
			for (Object ot : tags) {
				Property p = (Property)ot;
				String name = p.getName();
				if (name.equals("base_Element"))
					continue;
				tagNames.add(name);
			}			
			return tagNames;
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			if (event.getSource() == okButton)
				// if stype selected
				if (stype != null) {
					// get the selected elements in the tree
					applyStereotypes();
				}
			System.out.println("OK");
			if (event.getSource() == stypeButton) {
				Object obj = (Object)selectedElements[0];
				if (!(obj instanceof Stereotype))
					return;

				
				Stereotype ster = (Stereotype)obj;
				ArrayList<String> tN = getTagNames(ster);
				if (tN == null)
					return;

				stype = ster;
				stypeLabel.setText(stype.getHumanName());
				for (int i = 0; i < tN.size(); i++) 
					pt.setValueAt(tN.get(i), i, 0);
			}
			System.out.println("recurse = " + recurse.isSelected());

		}

		public MainDlg() {
			super();
			this.setTitle("Set Stereotypes");
			this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

			pt = new PasteTable(100, 3);
			pt.getColumnModel().getColumn(0).setHeaderValue("Tag Name");
			pt.getColumnModel().getColumn(1).setHeaderValue("Tag Value");
			pt.getColumnModel().getColumn(2).setHeaderValue("Tag HyperLink");

			JScrollPane jsp = new JScrollPane(pt, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			stypeButton.addActionListener(this);
			okButton.addActionListener(this);
			this.add(jsp);

			JPanel buttonPanel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
			JPanel buttonPanel2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
			buttonPanel1.add(stypeButton);
			buttonPanel1.add(stypeLabel);
			buttonPanel2.add(recurse);
			buttonPanel2.add(append);
			buttonPanel2.add(okButton);
			buttonPanel.add(buttonPanel1);
			buttonPanel.add(buttonPanel2);
			this.add(buttonPanel, BorderLayout.SOUTH);
			this.setSize(640, 480);
			this.setVisible(true);

		}
	}


	/**
	 * **********************************************************************************************
	 * 
	 */
	public void runSelectStereotypes() {
		MainDlg md = new MainDlg();
		md.setVisible(true);
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
class scopeAndDoSS implements ActionListener {
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
			SetStereoTypesOB ss = new SetStereoTypesOB(se, app);
			ss.runSelectStereotypes();
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

scopeAndDoSS sAD = new scopeAndDoSS();
sAD.doIt();
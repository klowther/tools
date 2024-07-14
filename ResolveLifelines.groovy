import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.nomagic.magicdraw.actions.ActionsConfiguratorsManager;
import com.nomagic.magicdraw.plugins.Plugin;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.ui.browser.Browser;
import com.nomagic.magicdraw.ui.browser.ContainmentTree;
import com.nomagic.uml2.ext.jmi.helpers.InteractionHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.ConnectableElement;
import com.nomagic.magicdraw.ui.browser.Node;
import com.nomagic.magicdraw.ui.browser.Tree;
import com.nomagic.magicdraw.ui.browser.actions.DefaultBrowserAction;
import com.nomagic.uml2.ext.magicdraw.interactions.mdbasicinteractions.Lifeline;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Type;

import cameoutils.BrowserConfigurator;
import cameoutils.FindUtils;
import utils.Logger;


/**
 ***********************************************************************
 * UNCLASSIFIED Resolves lifelines that are not attached to anything
 */
class ResolveLifelinesAction {

	private static final long serialVersionUID = 1L;
	Project project;
	ContainmentTree contTree;
	ArrayList<Lifeline> llList;
	
	Element[] selectedElements;
	Application application;

	ResolveLifelinesAction(Element[] se, Application app){
		selectedElements = se;
		application = app;
	}

	/**
	 ***********************************************************************
	 * UNCLASSIFIED Gets the type of item the lifeline represents
	 * 
	 * @param ll The lifeline
	 * @return the Type or null if none
	 */
	Type getLLType(Lifeline ll) {
		Type tp = null;
		ConnectableElement ce = ll.getRepresents();
		if (ce != null && ce instanceof Property) {
			Property p = (Property) ce;
			tp = p.getType();
		}
		return tp;
	}


	/** *********************************************************************
	 * UNCLASSIFIED Gets the lifelines given at least one selected object
	 * the given sequence diagram
	 *********************************************************************** */
	ArrayList<Lifeline> getLifelines() {

		ArrayList<Lifeline> rv = new ArrayList<Lifeline>();
		Node[] selected = contTree.getSelectedNodes();

		if (selected.length == 0)
			return rv;

		for (Node n : selected) {
			Object o = n.getUserObject();
			if (o instanceof Element) {
				Element oe = (Element)o;
				Collection<Element> elems = FindUtils.findElements(oe,true);
				for (Element elem : elems) {
					if (elem instanceof Lifeline)
						if (!rv.contains(elem))
							rv.add((Lifeline) elem);
				}
			}
		}
		return rv;
	}		
	/** *********************************************************************
	 * UNCLASSIFIED Finds the first element with a matching name and is of
	 * type Type. given the parent and the name 
	 * 
	 * Note the found element must be a namedelement and of type Type
	 * 
	 * @param parent Place to begin search
	 * @param name the name of the element to look for
	 * @return the NamedElement or null
	 *********************************************************************** */
	NamedElement findMatch(Element parent, String name) {
		if (name == null || name.length() == 0)
			return null;
		Collection<Element> elems = FindUtils.findElements(parent, false);
		for (Element e : elems) {
			// want to make sure we don't find the lifeline we are matching
			if (e instanceof Lifeline)
				continue;
			if (e instanceof NamedElement) {
				NamedElement ne = (NamedElement) e;
				String eName = ne.getName();
				if (eName != null && eName.equals(name) && 
						ne instanceof Type)
					return ne;						
			}
		}
		return null;
	}

	/** *********************************************************************
	 * UNCLASSIFIED Performs preliminary operation to get the lifelines from
	 * the diagram the plugin was launched from. It then creates the dialog
	 * where the user is to select the package to search for the Type
	 * elements.
	 *********************************************************************** */
	void prepIt() {
		llList = getLifelines();
		Logger.templog("Number of Lifelines " + llList.size());
		for (Lifeline ll : llList) {
			Logger.templog("Lifeline: " + ll.getHumanName());
			Type tp = getLLType(ll);
			if (tp != null)
				Logger.templog("    Property class " + tp.getHumanName());
		}
		smallDlg dlg = new smallDlg();
		dlg.setVisible(true);
	}

	/** *********************************************************************
	 * UNCLASSIFIED Performs the search by calling findMatch and then
	 * attempt to set the lifeline type
	 *********************************************************************** */
	void linkEm() {
		Element choice = selectedElements[0];
		NamedElement ne;

		for (Lifeline ll : llList) {
			String lookName = getLLType(ll).getName();
			Logger.templog("Looking for match to " + lookName);
			ne = findMatch(choice,lookName);
			if (ne == null)
				continue;
			Logger.templog("   Found element matching : <" + ne.getName() + ">  : " + ne.getQualifiedName());
			if (ne instanceof Type) {
				Type it = (Type)ne;
				Logger.templog("     Attempting to set lifeline type to" + ne.getQualifiedName());
				InteractionHelper.setLifelineType(ll, it);
			}
			ne = null;
		}
	}

	/** *********************************************************************
	 * UNCLASSIFIED A minimal dialog that has one ok button. the use is suppose
	 * to select a package or some container and then hit ok
	 *********************************************************************** */
	class smallDlg extends JFrame implements ActionListener {

		private static final long serialVersionUID = 1L;
		JButton ok = new JButton("OK");
		@Override
		public void actionPerformed(ActionEvent event) {
			if (event.getSource() == ok)
				linkEm();
		}

		smallDlg () {
			super();
			this.setAlwaysOnTop(true);
			ok.addActionListener(this);
			JLabel instructions = new JLabel("Select where to search\n and press OK");
			this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			this.add(instructions);
			JPanel tmp = new JPanel();
			tmp.setLayout(new BoxLayout(tmp,BoxLayout.X_AXIS));

			tmp.add(new JLabel("   "));
			tmp.add(ok);
			tmp.add(new JLabel("   "));
			this.add(tmp,BorderLayout.SOUTH);
			this.setSize(250, 100);
		}

	}
	public void runResolveLifelines() {
		prepIt();
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
class scopeAndDoRL implements ActionListener {
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
			ResolveLifelinesAction rla = new ResolveLifelinesAction(se, app);
			rla.runResolveLifelines();
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

scopeAndDoRL sAD = new scopeAndDoRL();
sAD.doIt();	
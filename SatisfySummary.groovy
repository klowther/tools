import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.nomagic.magicdraw.actions.ActionsConfiguratorsManager;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.plugins.Plugin;
import com.nomagic.magicdraw.ui.browser.Browser;
import com.nomagic.magicdraw.ui.browser.Node;
import com.nomagic.magicdraw.ui.browser.Tree;
import com.nomagic.magicdraw.ui.browser.actions.DefaultBrowserAction;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.uml2.ext.jmi.helpers.CoreHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.EnumerationLiteral;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.impl.ElementsFactory;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Relationship;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DirectedRelationship;

import cameoutils.*;

/**
 * **********************************************************************************************
 * 
 */
class SatisfySummary{
	ArrayList<Element> DoorsModules;
	Project project;
	ElementsFactory factory;
	Tree tree;
	final String drModuleName = "DOORS Module";
	final String drStName = "DOORS Requirement";
	final String arStName = "AbstractRequirement";
	final String relName = "SatisfySummary";
	Stereotype drSt;
	Stereotype dmSt;

	Stereotype arSt;
	Stereotype blk;
	Profile sysmlProfile;
	final String reqTag = "Object Identifier";
	final String[] DoorsTags = [ 'Allocation', 'Allocation Rationale', 'Comments', 'Compliance Assessment',
			'Compliance Justification', 'Cybersecurity', 'Cybersecurity Rationale', 'DerivedFromLink', 'Link_VE',
			'Object Heading', 'Object Identifier', 'Object Number', 'Object Type', 'Parent_Link', 'Rationale',
			'Requirement Status', 'Requirement Type', 'Safety Impact', 'Safety Impact Rationale', 'TBX Burndown', 'TPM',
			'Verification Comments', 'Verification Level', 'Verification Method', 'Verification Method Rationale',
			'Verification Success Criteria', 'ComplianceRationale', 'Draft requirement maturity', 'JIRA_Number',
			'Level 5 Spec Allocation', 'Projected lvl 4 Spec', 'SAM_Analysis', 'notes', 'Effectivity' ];

	final String[] AbsReqTags = [ 'base_NamedElement', 'Derived', 'DerivedFrom', 'Master', 'RefinedBy', 'SatisfiedBy',
			'TracedTo', 'VerifiedBy', 'Id', 'Text' ];
	final String ConvertFileName = "C:\\temp\\ReplaceRequirementsNG.log";
	boolean cFlag = false;
	FileWriter convertFile = null;
	private static final long serialVersionUID = 1L;
	
	Element[] selectedElements;
	Application application;
	SatisfySummary(Element[] se, Application app){
		selectedElements = se;
		application = app;
		project = app.getProject();
	}


	/**
	 * **********************************************************************************************
	 * UNCLASSIFIED Get the String representing the Compliance Assessment tag if possible.
	 * 
	 * @apiNote The value returned is the ".toString()" method call or the human string representation
	 * of an enumeration literal. 
	 * 
	 * @param doorsReq - the doorsReq, although this should work on any element that has
	 * the applied stereotype "DOORS Requirement". 
	 * @return The string representation of the value
	 ********************************************************************************************** */
	String getCompliance(Element doorsReq) {
		final String compAssess = "Compliance Assessment";
		if (doorsReq == null)
			return null;
		Object o = CamUtils.getTagVal(doorsReq, drSt, compAssess);
		if (o == null)
			return null;
		String tv = o.toString();
		
		if (o instanceof EnumerationLiteral) {
			EnumerationLiteral el = (EnumerationLiteral) o;
			tv = CamUtils.humanName(el);
		}
		return tv;
	}
	
	int satCount(Element doorsReq) {
		final String tagName = "SatisfiedBy";
		List<?> satisfied =  CamUtils.getTagVals(doorsReq, arSt, tagName);
		if (satisfied == null)
			return 0;
		return satisfied.size();
	}
	
	int refCount(Element doorsReq) {
		final String tagName = "RefinedBy";
		List<?> refined =  CamUtils.getTagVals(doorsReq, arSt, tagName);
		if (refined == null)
			return 0;
		return refined.size();
	}
	
	/**
	 * **********************************************************************************************
	 * Process the requirements under the module
	 * 
	 * whatToProcess - the Module Containing the door reqs
	 * whereToWrite - The location to write the information to
	 */
	void processReqs(Element whatToProcess, Element whereToWrite) {
		ArrayList<Element> reqs = FindUtils.findByHType(whatToProcess, drStName, true);
		
		int tracedCount = 0;
		int grCount = 0;
		int yeCount = 0;
		int reCount = 0;
		int naCount = 0; 
		String tagName = "BaselineNumber";
		String doorsBaseline = "None";

		Object tmp = CamUtils.getTagVal(whatToProcess,dmSt,tagName);
		
		if (tmp != null)
			doorsBaseline = (String) tmp;

		for (Element e : reqs) {
			int rCount = refCount(e);
			int sCount = satCount(e);
			int tCount = (rCount + sCount);
			if (tCount == 0) {
			} else
				tracedCount++;
			String tv = getCompliance(e);
			if (tv == null) {
				continue;
			}
			tv = tv.toLowerCase();	
			if (tv.contains("green"))
				grCount++;
			if (tv.contains("yellow"))
				yeCount++;
			if (tv.contains("red"))
				reCount++;
			if (tv.contains("n/a"))
				naCount++;
		}

		int totalReqs = reqs.size();
		double pCent = 0.00;
		if (totalReqs > 0)
			pCent = ((double)tracedCount/(double)totalReqs)*100.0;
		final DecimalFormat df = new DecimalFormat("0.00");
		String h = "<html><h2>";
		h += "Baseline Number:&nbsp " + doorsBaseline + "<br></br>";
		h += "DOORS Requirements:&nbsp " + totalReqs + "<br></br>";
		h += "Relations to Model Elements :&nbsp " + tracedCount + 
				" &nbsp (" + df.format(pCent) + "%)<br></br>";
		h += "<hr>";
		h += "Green:&nbsp " + grCount + "<br></br>";
		h += "Yellow:&nbsp " + yeCount + "<br></br>";
		h += "Red:&nbsp " + reCount + "<br></br>";
		h += "N/A:&nbsp " + naCount + "<br></br>";
		h += "</h2></html>";
		
		CoreHelper.setComment(whereToWrite,h);

	}
	
	Relationship createTrace(Element source, Element target) {
		Stereotype sType = StereotypesHelper.getStereotype(project, "Trace", sysmlProfile);
		Relationship r = factory.createAbstractionInstance();
		NamedElement ne = (NamedElement)r;
		ne.setName("SatisfySummary");
		CoreHelper.setClientElement(r, (Element) source);
		CoreHelper.setSupplierElement(r, (Element) target);
		StereotypesHelper.addStereotype(r,sType);
		return r;
	}

	Element getTracedElement(Element e) {
		Element rv = null;
		Collection<DirectedRelationship> r = e.get_directedRelationshipOfSource();
		for (DirectedRelationship dr : r) {
			String name = CamUtils.humanName((Element) dr);
			name = name.trim();
			if (name.equals(relName)) {
				Collection<Element> tgts = dr.getTarget();
				if (tgts.size() > 0) {
					rv = tgts.iterator().next();
				}
			}
		}
		return rv;
	}
	
	Class createContainer(Element e) {
		Class newContainer = factory.createClassInstance();
		Stereotype s = CamUtils.appliedStereotype(e);
		StereotypesHelper.addStereotype(newContainer,s);
		newContainer.setName(CamUtils.humanName(e));
		return newContainer;
	}

	
	void processSelections() {
		Package pkg = null;
		Package newPkg = null;
		Element whatToProcess = null;
		Element whereToWrite = null;
		ArrayList<BaseElement> selections = getSelectedElements();

		for (BaseElement be : selections) {
			if (be instanceof Element) {
				Element e = (Element) be;
				// case 1 an already existing metric
				Element tE = getTracedElement((Element) e);
				
				if (tE != null) {
					pkg = (Package) e.getOwner();
					whatToProcess = tE;
					whereToWrite = e;
				} else {
					// case 2 must create a new metric
					if (newPkg == null) {
						newPkg = factory.createPackageInstance();
						newPkg.setName("000NewMetrics");
						newPkg.setOwner(project.getPrimaryModel());
					}
					pkg = newPkg;
					whatToProcess = e;
					whereToWrite = createContainer(e);
					whereToWrite.setOwner(pkg);
					Relationship r = createTrace(whereToWrite,whatToProcess);
					r.setOwner(whereToWrite.getOwner());
				}
				processReqs(whatToProcess, whereToWrite);
			}
		}
	}
	
	/**
	 * **********************************************************************************************
	 * 
	 */
	void Doit() {
		arSt =  StereotypesHelper.getStereotype(project, arStName, (Profile) null);
		drSt =  StereotypesHelper.getStereotype(project, drStName, (Profile) null);
		dmSt =  StereotypesHelper.getStereotype(project, drModuleName, (Profile) null);
		sysmlProfile = StereotypesHelper.getProfile(project, "SysML");
		blk = StereotypesHelper.getStereotype(project, "Block", sysmlProfile);
		
		
		SessionManager sessionManager = SessionManager.getInstance();
		sessionManager.createSession(project, relName);

		processSelections();
		sessionManager.closeSession(project);
	}

	/**
	 * **********************************************************************************************
	 * 
	 */
	ArrayList<BaseElement> getSelectedElements() {
		ArrayList<BaseElement> rv = new ArrayList<BaseElement>();
		if (tree == null)
			return null;
		Node[] selectedNodes = tree.getSelectedNodes();
		for (Node selectedNode : selectedNodes) {
			Object userObject = selectedNode.getUserObject();
			if (userObject instanceof BaseElement) {
				BaseElement be = (BaseElement) userObject;
				rv.add(be);
			}
		}
		return rv;
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
class scopeAndDoSatisfySummary implements ActionListener {
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
	   SatisfySummary ssa = new SatisfySummary(se,app);
	   ssa.Doit();
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

scopeAndDoSatisfySummary sAD = new scopeAndDoSatisfySummary();
sAD.doIt();

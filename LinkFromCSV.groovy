import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.ui.browser.Browser;
import com.nomagic.magicdraw.ui.browser.Node;
import com.nomagic.magicdraw.ui.browser.Tree;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DirectedRelationship;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Relationship;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdbasicbehaviors.Behavior;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.impl.ElementsFactory;

import utils.GenericTableCSVfile;
import utils.Logger;
import utils.StringUtils;
import cameoutils.CamUtils;
import cameoutils.FindUtils;

import com.nomagic.uml2.ext.jmi.helpers.CoreHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;


/** *****************************************************************
 * A simple internal data class with all public members that holds 
 * the information for mapping one relation
 * @author D99510
 */
class ELinkData {
	boolean problem;
	String profile;
	String linkType;
	String source;
	String[] target;
	String owner;

	public String toString() {
		String rv = profile + "," + linkType + "," + source;
		for (String s : target) {
			rv += "," + s;
		}
		rv += "," + owner;
		return rv;
	}
}

/**
 */
class LinkFromCSVAction 
{
	Project project;
	ElementsFactory factory;
	private static final long serialVersionUID = 1L;
	boolean skipExisting = true;
	boolean useNamesOnly = false;
	int relsCreated = 0;
	boolean useSourceID = true;
	boolean useTargetID = true;

	static Object linklog;
	static Object CamUtils;
	static Object csvFile;

	/**
	 ***********************************************************************
	 * UNCLASSIFIED Read the csv file and store the data in a List
	 * of data
	 * 
	 * @param filename - the filename to read
	 * @return  ArrayList of ELinkData 
	 * @author D99510
	 ************************************************************************ */   
	static ArrayList<ELinkData> readData(String filename) throws IOException {
		//GenericTableCSVfile gtc = new GenericTableCSVfile(filename, GenericTableCSVfile.sOptions.opencsv);
		csvFile.setFileName(filename);
		ArrayList<ELinkData> lData = new ArrayList<ELinkData>();
		ArrayList<String[]> lines = csvFile.getLines();
		int numCols = lines.get(0).length;
		for (String[] sdata : lines) {
			ELinkData data = new ELinkData();
			data.problem = sdata.length != numCols;
			if (sdata.length > 2)
				data.linkType = StringUtils.unquote(sdata[2]);
			if (sdata.length > 3)
				data.profile = StringUtils.unquote(sdata[3]);
			if (sdata.length > 4)
				data.source = StringUtils.unquote(sdata[4]);
			if (sdata.length > 5) {
				data.target = new String[1];
				data.target[0] = StringUtils.unquote(sdata[5]);
			}
			if (sdata.length > 6)
				data.owner = StringUtils.unquote(sdata[6]);
			lData.add(data);
		}
		return lData;
	}

	/**
	 ***********************************************************************
	 * UNCLASSIFIED Return the string stereotype stripped from the
	 * human stereotyp
	 * 
	 * @param sType - the human stereotype
	 * @return the string stereotype
	 * @author D99510
	 ************************************************************************ */   
	String getActualSType(String sType) {
		String rv = sType;

		int index = sType.indexOf(" [");
		if (index > -1)
			rv = sType.substring(0, index);
		return rv;
	}

	/**
	 ***********************************************************************
	 * UNCLASSIFIED Tests if the relation exists
	 * @author D99510
	 */
	boolean relationExists(Element s, Element t,  String strType) {
		Collection<DirectedRelationship> r = s.get_directedRelationshipOfSource();
		for (DirectedRelationship dr : r) {
			Collection<Element> tgts = dr.getTarget();
			for (Element e : tgts) {
				if (e.equals(t)) {
					if (CamUtils.isStereo(dr,strType) != null)
						return true;
				}
			}
		}
		return false;
	}

	/**
	 ***********************************************************************
	 * UNCLASSIFIED creates the relationship between the source and target
	 * with the given owner for the relationship and string representation
	 * of the stereotype.
	 * @param s - the source element
	 * @param t - the target element
	 * @param owner - the owner of the relationship
	 * @param strTyp - the string representation of the stereotype
	 * @param profile - the string representation of the profile containing 
	 * the stereotype
	 * @author D99510
	 ************************************************************************ */   
	void createRelation(Element s, Element t, Element owner, String strType, String profile) {
		Relationship r = factory.createAbstractionInstance();
		String actualType = null;

		actualType = getActualSType(strType);
		if (skipExisting && relationExists(s,t,actualType)) {
			linklog.log("Relation <" + actualType + "> exists between " + 
					s.getHumanName() + " and " + t.getHumanName() + " skipping.");
			return;
		}

		if (actualType == null) {
			linklog.log("ERROR: LinkFromCSV could not determine sterotype : <" + strType + ">");
		}
		Profile pFile = StereotypesHelper.getProfile(project, profile.trim());
		if (pFile == null) {
			linklog.log("ERROR: Could not find profile <" + profile + "> associated with stereotype <" + actualType + ">");
		}
		Stereotype sType = StereotypesHelper.getStereotype(project, actualType, pFile);
		if (sType == null) {
			linklog.log("ERROR: Could not find stereotype <" + actualType + "> associated with profile <" + profile +  ">");
		}
		linklog.log("Creating relationship between " + s.getHumanName() + 
				" to " + t.getHumanName() + " with stereoType " + sType.getHumanName());

		CoreHelper.setSupplierElement(r, (Element) t);
		CoreHelper.setClientElement(r, (Element) s);

		if (sType != null)
			StereotypesHelper.addStereotype(r,sType);
		try {
			if (owner != null)
				r.setOwner(owner);
			else
				r.setOwner(s.getOwner());
		} catch (Exception e) {
			r.setOwner((Element) project);
		}
		relsCreated++;
	}   

	/**
	 ***********************************************************************
	 * UNCLASSIFIED creates the relationship between the source and target
	 * with the given owner for the relationship and string representation
	 * of the stereotype.
	 * @param fullPath - the full qualified name (path)
	 * @param t - the target element
	 * @param owner - the owner of the relationship
	 * @param strTyp - the string representation of the stereotype
	 * @param profile - the string representation of the profile containing 
	 * the stereotype
	 * @author D99510
	 ************************************************************************ */   
	String stripPath(String fullPath) {
		String[] parts = fullPath.split("::");
		if (parts.length > 0)
			return parts[parts.length-1];
		return null;
	}

	void logUnable(String error, ELinkData eld) {
		String s = error;
		s += "," + eld.linkType;
		s += "," + eld.profile;
		s += "," + eld.source;
		s += "," + eld.target;
		s += "," + eld.owner;

		linklog.log(error + s);
	}

	public void performAction()
	{
		project = Application.getInstance().getProject();
		factory = project.getElementsFactory();
		linklog.setName("C:\\temp\\linkfromcsv.log");
		linklog.log("Starting LinkFromCSV");

		if (project == null)
			linklog.log("Error Project is null");

		JFileChooser fc = new JFileChooser();
		File f = null;
		//...
		// Response to button click
		fc = new JFileChooser("");
		fc.setDialogTitle("Please Select csv file");

		int val = fc.showOpenDialog(fc);
		if (val== JFileChooser.APPROVE_OPTION) { 
			f = fc.getSelectedFile();
		}   

		if (f == null)
			return;

		int jrv = JOptionPane.showConfirmDialog(null,"Skip already existing relations?","Skip Option", JOptionPane.YES_NO_OPTION);
		skipExisting = (jrv == JOptionPane.YES_OPTION);

		jrv = JOptionPane.showConfirmDialog(null,"Use Element ID for SOURCE?\n Yes for Element ID\n No for Qualified path",
				"SOURCE Option",JOptionPane.YES_NO_OPTION);
		useSourceID = (jrv == JOptionPane.YES_OPTION);

		jrv = JOptionPane.showConfirmDialog(null,"Use Element ID for TARGET?\n Yes for Element ID\n No for Qualified path",
				"TARGET Option",JOptionPane.YES_NO_OPTION);
		useTargetID = (jrv == JOptionPane.YES_OPTION);

		ArrayList<ELinkData> linkData = null;
		try {
			linklog.log("Reading file " + f.getAbsolutePath());
			linkData = readData(f.getAbsolutePath());
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(null, "Trouble importing file " + f.getAbsolutePath()
			+ " format must be \n"
			+ "linenumber, Relation name, Relation StereoType, target, source, owner"); 
		}
		SessionManager sessionManager = SessionManager.getInstance();
		sessionManager.createSession(project, "LinkFromCSV");
		int itemcount = 0;
		for (int i = 0; i < linkData.size(); i++) {
			ELinkData d = linkData.get(i);
			linklog.log("Processing item " + itemcount++);
			try {
				if (d.problem) {
					linklog.log("Error processing on or around line " + (i+1) + "skipping");
					continue;
				}

				Element sourceElement = null;
				if (useSourceID) {
					BaseElement be = project.getElementByID(d.source);
					if (be instanceof Element)
						sourceElement = (Element) be;
				} else {
					sourceElement = FindUtils.FindByQualifiedPath(d.source);
				}

				Element ownerElement = FindUtils.FindByQualifiedPath(d.owner);
				if (sourceElement == null) {
					linklog.log("WARNING: Unable to find source <" + d.source + "> skipping");
					linklog.log("  INFO: " + d.toString());
					continue;
				}

				for (String t : d.target) {
					Element targetElement = null;
					if (useNamesOnly) {
						String name = stripPath(StringUtils.unquote(t));
						targetElement = FindUtils.FindByName(project.getPrimaryModel(), name);
						if (targetElement == null) {
							linklog.log("WARNING: Unable to find target <" + name + "> skipping");
							linklog.log("  INFO: " + d.toString());
							continue;
						}
					} else {
						if (useTargetID) {
							BaseElement be = project.getElementByID(t);
							if (be instanceof Element)
								targetElement = (Element) be;
						} else {
							targetElement = FindUtils.FindByQualifiedPath(t);
						}

						if (targetElement == null) {
							linklog.log("WARNING: Unable to find target <" + t + "> skipping");
							linklog.log("  INFO: " + d.toString());
							continue;
						}
					}
					createRelation(sourceElement, targetElement, ownerElement, d.linkType, d.profile);
				}
			} catch (Exception ex) {
				linklog.log("ERROR: Exception processing item number " + itemcount + "skipping");
				continue;
			}
		}

		sessionManager.closeSession(project);
		linklog.log("Ending LinkFromCSV");
		JOptionPane.showMessageDialog(null, "Created " + relsCreated + " relations from " + (itemcount-1) + " items.");
	}
}



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
	 * UNCLASSIFIED - This is the area that YOU must put some action. Look at QuickSync for example
	 * if needed
	 * @author D99510
	 * **********************************************************************************************
	 */
	private void runButtonSelected() {
		app.getGUILog().log("Executing OB");
		LinkFromCSVAction lfcsv = new LinkFromCSVAction(); 
		app.getGUILog().log("executing LinkFromCSV");
		lfcsv.performAction();

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

LinkFromCSVAction.linklog = linklog;
LinkFromCSVAction.CamUtils = CamUtils;
LinkFromCSVAction.CamUtils.notify("Starting LinkFromCSV");
LinkFromCSVAction.csvFile = csvFile;

scopeAndDo sAD = new scopeAndDo();
sAD.doIt();
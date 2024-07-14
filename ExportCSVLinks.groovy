
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import java.util.ArrayList;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.ui.browser.Browser;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.magicdraw.ui.browser.Node;
import com.nomagic.magicdraw.ui.browser.Tree;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DirectedRelationship;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdbasicbehaviors.Behavior;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

/**
 * ***********************************************************************************
 * Unclassified - Class to export all relationships of a given type(s) to a CSV file
 * 
 * @author D99510
 */
class ExportCSVLinks {
	Project project;
	Element selected = null;
	String[] searchTypes;
	final String searchProfile = "SysML";
	ArrayList<Element> searchResults;
	FileWriter CSVLinksFile = null;
	String CSVLinksFilename = "";
	boolean useSourceID = true;
	boolean useTargetID = false;
	boolean useOwnerID = false;
	boolean canceled = false;
	static Object elog;
	static Object CamUtils;
	static Object FindUtils;
	ArrayList<BaseElement> es = new ArrayList<BaseElement>();


	/**
	 * ***********************************************************************************
	 * Unclassified - Finds elements by the Human Types (plural not singular
	 * 
	 * Note : Slightly more optimized than calling CamUtils.findByHType for each
	 * type.
	 * 
	 * @param start where to start searching
	 * @param types the types to search for
	 * @return the list of Elements of the types
	 * @author D99510
	 */
	private ArrayList<Element> findByHTypeList(Element start, String[] types) {
		int count = 0;
		if (start == null)
			logIt("Start is null",false);
		Collection<Element> elements = FindUtils.findElementsnew(start, true);
		ArrayList<Element> rv = new ArrayList<Element>();
		if (elements == null) {
			logIt("elements is null",false);
			return rv;
		}
		logIt("Searching for types specified please wait",false);

		boolean blanktypes = (types.length == 0) || (types.length == 1 && types[0].length() == 0);
		if (!blanktypes) {
			for (Element elem : elements) {
				String hType = elem.getHumanType();
				for (String s : types) {
					if (s.contains(hType)) {
						if (count++ % 100 == 0)
							logIt("Found and added " + count,false);
						rv.add(elem);
					}
				}
			}
		} else {
			for (Element elem : elements) {
				if (elem instanceof DirectedRelationship) {
					if (count++ % 100 == 0)
						logIt("Found and added " + count,false);
					rv.add(elem);
				}
			}
		}
		return rv;
	}

	/**
	 * ***********************************************************************************
	 * Unclassified - Logs the directed relationship
	 * 
	 * @param dr       the directed relationship
	 * @param countcol a count to put into the # column
	 * @author D99510
	 */
	void logDR(DirectedRelationship dr, int countcol) {
		try {
			String name = CamUtils.humanName(dr);
			String type = dr.getHumanType();
			Stereotype s = CamUtils.appliedStereotype(dr);
			String profile = "";
			if (s != null) {
				Profile tmpprofile = s.getProfile();
				profile = CamUtils.humanName(tmpprofile);
			}
			Collection<Element> sources = dr.getSource();
			logIt("sources.size() = " + sources.size(),false);
			Element source = null;
			for (Element e1 : sources) {
				source = e1;
				break;
			}
			if (source == null) {
				logIt("Source is null",false);
				//logIt("ERROR: No sources for dr = " + dr.getHumanName() + " id = " + dr.getID(),false);
				return;
			}

			String src = "not a Named Element";
			if (useSourceID) {
				src = source.getID();
			} else {
				if (source instanceof NamedElement) {
					NamedElement ne = (NamedElement) source;
					src = ne.getQualifiedName();
				}
			}
			Collection<Element> targets = dr.getTarget();
			Element target = null;
			for (Element e2 : targets) {
				target = e2;
				break;
			}                     

			if (target == null) {
				logIt("ERROR: No targets for dr = " + dr.getHumanName() + " id = " + dr.getID(),false);
				return;
			}
			NamedElement tgt = (NamedElement) target;
			String tQname = tgt.getQualifiedName();
			NamedElement owner = (NamedElement) dr.getOwner();
			String oQname = owner.getQualifiedName();
			String logstr = "\"" + countcol + "\",\"" + name.trim() + "\",\"" + type.trim() + "\",\"" + profile.trim() +
					"\",\"" + src.trim() + "\",\"" + tQname.trim() + "\",\"" + oQname.trim() + "\"";
			writeIt(logstr);
		} catch (Exception e) {
			logIt(e.getMessage(),false);
		}
	}

	/**
	 * ***********************************************************************************
	 * Unclassified - Writes the message/line to the file
	 * 
	 * @param msg       the directed relationship
	 * @author D99510
	 */
	void writeIt(String msg) {
		try {
			CSVLinksFile.write(msg + System.lineSeparator());
		} catch (IOException e) {
			logIt("Trouble logging to " + CSVLinksFilename,true);
		}
	}


	/**
	 * ***********************************************************************************
	 * Unclassified - Wrapper to log information to log file and optionally to the
	 * notification area in Cameo
	 * 
	 * @param s - the information to log
	 * @param notify - boolean to indicate to also write the message to the notification area
	 * @author D99510
	 */  
	void logIt(String s, boolean notify) {
		elog.log(s);
		if (notify) 
			CamUtils.notify(s);    
	}


	/**
	 * ***********************************************************************************
	 * UNCLASSIFIED - Prompts the user for the file to write to
	 * 
	 * @return true if a file has been chosen false otherwise
	 * @throws IOException
	 * @author D99510
	 */
	boolean setFilename() {
		JFileChooser fc = new JFileChooser();
		File f = null;
		// ...
		// Response to button click
		fc = new JFileChooser("");
		fc.setDialogTitle("Please Select csv file");

		int val = fc.showOpenDialog(fc);
		if (val == JFileChooser.APPROVE_OPTION) {
			f = fc.getSelectedFile();
		}

		if (f == null)
			return false;
		CSVLinksFilename = f.getAbsolutePath();
		try {
			CSVLinksFile = new FileWriter(CSVLinksFilename);
		} catch (IOException e) {
			logIt("Trouble creating file " + CSVLinksFilename,true);
		}
		return true;
	}

	/**
	 * ***********************************************************************************
	 * UNCLASSIFIED - Initiates the export, basically the main of this class
	 *
	 * @author D99510
	 */
	public void export() {
		project = Application.getInstance().getProject();
		if (!setFilename())
			return;

		elog.setName("C:\\temp\\exportcsvlinks.log");    
		logIt("Starting ExportCSVLinks Wait about 30 seconds befor log file updates",true);  
		if (es.size() == 0)
			logIt("es.size = 0",true);
		else
			if (!(es.get(0) instanceof Element))
				logIt("es.get(0) not an Element");        
		if (es.size() > 0 && es.get(0) instanceof Element)
			selected = (Element) es.get(0);
		String tmp = JOptionPane
				.showInputDialog("Types to search for (comma separated) \n leave blank for all relationships");
		searchTypes = tmp.split(",");
		for (int i = 0; i < searchTypes.length; i++)
			searchTypes[i] = searchTypes[i].trim();


		int jrv = JOptionPane.showConfirmDialog(null,"Use Element ID for SOURCE?\n Yes for Element ID\n No for Qualified path",
				"SOURCE Option",JOptionPane.YES_NO_OPTION);
		useSourceID = (jrv == JOptionPane.YES_OPTION);

		jrv = JOptionPane.showConfirmDialog(null,"Use Element ID for TARGET?\n Yes for Element ID\n No for Qualified path",
				"TARGET Option",JOptionPane.YES_NO_OPTION);
		useTargetID = (jrv == JOptionPane.YES_OPTION);

		if (canceled)
			return;

		searchResults = findByHTypeList(selected, searchTypes);
		logIt("useSourceID = " + useSourceID,true);
		logIt("useTargetID = " + useTargetID,true);
		logIt("Starting Export",true);

		writeIt("\"#\",\"Name\",\"Applied Stereotype\",\"StypProfile\",\"Source ID\",\"Target QName\",\"Owner QName\"");
		int cnt = 1;
		logIt("Logging result for " + searchResults.size() + " found items",false);
		for (Element elem : searchResults) {
			if (!(elem instanceof DirectedRelationship))
				continue;
			if (cnt % 100 == 0)
				logIt("Logging directed relationships count = " + cnt,false);
			DirectedRelationship dr = (DirectedRelationship) elem;
			logDR(dr, cnt++);
		}
		try {
			CSVLinksFile.close();
		} catch (IOException e1) {
		}
		logIt("Export Done",true);
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
		ExportCSVLinks ecl = new ExportCSVLinks();
		ecl.es.addAll(getSelectedElements());
		if (ecl.es != null)  {
			ecl.export();
		} else {
			app.getGUILog().log("Selection is NULL or Nothing Selected");
		}
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

ExportCSVLinks ecl = new ExportCSVLinks();
ExportCSVLinks.elog = elog;
ExportCSVLinks.CamUtils = CamUtils;
ExportCSVLinks.FindUtils = FindUtils;

scopeAndDo sAD = new scopeAndDo();
sAD.doIt();
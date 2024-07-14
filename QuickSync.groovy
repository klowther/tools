import javax.swing.*;
import com.nomagic.magicdraw.core.Application;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.ui.browser.Browser;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.magicdraw.ui.browser.Node;
import com.nomagic.magicdraw.ui.browser.Tree;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import java.io.File;
import java.awt.FlowLayout;

import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdbasicbehaviors.Behavior;


/**
 * **********************************************************************************************
 * UNCLASSIFIED - The Action class to connect with the plugin
 * @author D99510
 */
class QuickSyncAction  { // DefaultBrowserAction {

    private static final long serialVersionUID = 1L;
    HashMap<String, Element> doorsHash;
    boolean doingDOORSReq = true;
    //GenericTableCSVfile reqFile;
    String idTag = "Object Identifier";
    int idColumn = -1;
    int poundColumn = -1;
    int nameColumn = -1;
    String[] header;
    Stereotype[] headerStype;

    // Summary information
    int tagsResolved = 0;
    int lineCount = 0;
    int linesProcessed = 0;
    int linesAllTagsProcessed = 0;
    int linesPartialTagsProcessed = 0;
    int lineFails = 0;

    Element selected = null;

    Project project;

    final String drStName = "DOORS Requirement";
    final String diStName = "DOORS Information";
    final String arStName = "AbstractRequirement";
    final String relName = "SatisfySummary";
    Stereotype drSt;
    Stereotype diSt;
    Stereotype arSt;
    static Object qlog;
    static Object CamUtils;
    static Object reqFile;
    static Object StringUtils;
    static Object FindUtils;
    //Logger qlog = new Logger();
//    Stereotype blk;
//    Profile sysmlProfile;

    private void resetVars() {
        doorsHash = new HashMap<String, Element>();
        doingDOORSReq = true;
        idTag = "Object Identifier";
        idColumn = -1;
        poundColumn = -1;
        nameColumn = -1;
        tagsResolved = 0;
        lineCount = 0;
        linesProcessed = 0;
        linesAllTagsProcessed = 0;
        linesPartialTagsProcessed = 0;
        lineFails = 0;        
    }
    /**
     * **********************************************************************************************
     * UNCLASSIFIED - Wrapper for log function so we can turn off with a comment
     * 
     * @param s the string to log
     * @author D99510
     * **********************************************************************************************
     */
    void logIt(String s) {
        qlog.log(s);
    }

    /**
     * **********************************************************************************************
     * UNCLASSIFIED - Prompts the user for a CSV file and attempts to read it in and
     * stores the contents to the class variable reqFile.
     * @author D99510
     * **********************************************************************************************
     */
    boolean getReqFile() {
        File f = null;
        qlog.log("1");
        JFileChooser fc = new JFileChooser("");
        qlog.log("2");
        fc.setDialogTitle("Please Select csv file");
        qlog.log("3");

        int val = fc.showOpenDialog(fc);
        qlog.log("4");
        if (val == JFileChooser.APPROVE_OPTION) {
        qlog.log("5");
            f = fc.getSelectedFile();
        qlog.log("6");
        }
        qlog.log("7");
        if (f == null)
            return false;
        qlog.log("8");

        try {
            //reqFile = new GenericTableCSVfile(f.getAbsolutePath(), GenericTableCSVfile.sOptions.opencsv);
            qlog.log("8a");
            reqFile.setFileName(f.getAbsolutePath());
            qlog.log("8b");
        } catch (IOException e) {
            qlog.log("8c");
            return false;
        }
        qlog.log("9");
        header = reqFile.getLine(0);
        logIt("header length is : " + header.length);

        
        Object selectionObject = JOptionPane.showInputDialog(null, 
                "Select the Identifier Column", "Select Identifier", JOptionPane.PLAIN_MESSAGE, null, header, header[1]);
        if (selectionObject == null)
            return false;
        idTag = selectionObject.toString();

        for (int i = 0; i < header.length; i++) {
            header[i] = StringUtils.unquote(header[i]);
            logIt("header[" + i + "] = <" + header[i] + ">");
            if (header[i].trim().equals(idTag))
                idColumn = i;
            if (header[i].trim().equals("#"))
                poundColumn = i;
            if (header[i].trim().equals("Name"))
                nameColumn = i;
        }

        logReqFile();

        if (idColumn == -1) {
            JOptionPane.showMessageDialog(null, "idColumn not found for " + idTag + "\n\nPlease check log file :\n"
                    + "C:\\temp\\quicksync.log\n" + " for header names read\n");
            return false;
        }
        return true;
    }

    /**
     * **********************************************************************************************
     * UNCLASSIFIED - logs the reqFile into the quicksync log file.
     * @author D99510
     * **********************************************************************************************
     */
    void logReqFile() {
        ArrayList<String[]> lines = reqFile.getLines();
        qlog.setAutoNewLine(false);
        for (int i = 0; i < lines.size(); i++) {
            String[] line = lines.get(i);
            for (int j = 0; j < line.length; j++) {
                qlog.log("<" + line[j] + "> ");
            }
            qlog.log(System.lineSeparator());
        }
        qlog.setAutoNewLine(true);
        qlog.log("idTag = <" + idTag + "> idColumn = " + idColumn);
    }

    /**
     * **********************************************************************************************
     * UNCLASSIFIED - Fills the Hashmap with the DOORS Requirement the hash key is
     * the Tag value with the specified tag name in the member variable idTag.
     * @author D99510
     * **********************************************************************************************
     */
    void fillDoorsHash() {
        ArrayList<BaseElement> tmp = CamUtils.getSelectedElements();
        // we only search below the first selected element, or start at the top
        // if no selection
        if (tmp.size() > 0 && tmp.get(0) instanceof Element)
            selected = (Element) tmp.get(0);
        else
            selected = project.getPrimaryModel();

        // Clear Doors Hash from previous fills
        doorsHash.clear();
        ArrayList<Element> doorsStuff;
        // find all the DOORS Requirements
        if (doingDOORSReq)
            doorsStuff = FindUtils.findByHType(selected, drStName, true);
        // or find all the DOOR Information
        else
            doorsStuff = FindUtils.findByHType(selected, diStName, true);

        boolean nameFlag = idTag.toLowerCase().equals("name");
        // now put them in a has using the idTag value as the key
        for (Element e : doorsStuff) {
            if (e == null) {
                qlog.log("ERROR element is null when looping throug Doors Requirements/Information");
                continue;
            }
            if (headerStype == null) {
                qlog.log("ERROR headerStype is null");
                continue;
            }
            if (headerStype[idColumn] == null && !nameFlag) {
                qlog.log("ERROR headerStype[idColumn] is null and nameFlag is false");
                continue;
            }

            String ObjIdent = null;
            if (nameFlag) {
                ObjIdent = CamUtils.humanName(e);
                if (e instanceof NamedElement) {
                    NamedElement ne = (NamedElement) e;
                    ObjIdent = ne.getName();
                }
            } else
                ObjIdent = (String) CamUtils.getTagVal(e, headerStype[idColumn], idTag);

            if (ObjIdent == null || ObjIdent.length() < 1) {
                qlog.log("Failed to get idTag <" + idTag + "> for element : <" + e.getHumanName() + "> Element ID : <"
                        + e.getID() + "> skipping!!");
                continue;
            }
            logIt("Hash put key = <" + ObjIdent + "> Element human name = <" + e.getHumanName() + ">");
            doorsHash.put(ObjIdent, e);
        }
    }


    /**
     * **********************************************************************************************
     * UNCLASSIFIED - Process the tag names in the header and determine whether the
     * tag is from the DOORS Requirement stereotype or AbstractRequirement
     * stereotype and store in headerStype
     * @author D99510
     * **********************************************************************************************
     */
    void resolveTagSTypes() {
        headerStype = new Stereotype[header.length];

        List<String> drTags = CamUtils.getTagNames(drSt);
        logIt("drTags size = " + drTags.size());
        for (String s : drTags)
            logIt("drTag : " + s);

        List<String> diTags = CamUtils.getTagNames(diSt);
        logIt("diTags size = " + diTags.size());
        for (String s : diTags)
            logIt("diTag : " + s);

        List<String> arTags = CamUtils.getTagNames(arSt);
        logIt("arTags size = " + arTags.size());
        for (String s : arTags)
            logIt("arTag : " + s);

        for (int i = 0; i < header.length; i++) {
            if (i == nameColumn || i == poundColumn)
                continue;
            if (doingDOORSReq) {
                if (drTags.contains(header[i])) {
                    headerStype[i] = drSt;
                    tagsResolved++;
                    logIt("  Tag: <"+ header[i] + " resolved as DOORS Requirement tag");
                    continue;
                }
            } else {
                if (diTags.contains(header[i])) {
                    headerStype[i] = diSt;
                    tagsResolved++;
                    logIt("  Tag: <"+ header[i] + " resolved as DOORS Information tag");
                    continue;
                }
            }

            if (arTags.contains(header[i])) {
                headerStype[i] = arSt;
                tagsResolved++;
                logIt("  Tag: <"+ header[i] + " resolved as Abstract Requirement tag");
                continue;
            } 
            qlog.log("ERROR Tag <" + header[i] + "> doesn't map to DOORS or Abstract Requirement stereotype");
        }
    }

    /**
     * **********************************************************************************************
     * UNCLASSIFIED - sets the tag value(s) given the element tag name and value(s)
     * 
     * @param e       the DOORs Requirement element to set the tag value in
     * @param st      the Stereotype applicable to the tag
     * @param tagName the name of the tag
     * @param tagVal  the value for the tag (could contain multiple values)
     * @return the tag was processed successfully
     * @author D99510
     *         **********************************************************************************************
     */
    boolean processTag(Element e, Stereotype st, String tagName, String tagVal) {
        if (e == null) {
            qlog.log("WARNING DOORS element null while processing Tag skipping");
            return false;
        }
        if (st == null) {
            qlog.log("WARNING Stereotype null while processing Tag skipping");
            return false;
        }
        if (tagName == null) {
            qlog.log("WARNING Tag Name null while processing Tag skipping");
            return false;
        }
        if (tagVal == null || tagVal.trim().length() == 0) {
            qlog.log("WARNING Tag Value null or blank while processing Tag skipping");
            return false;
        }

        logIt("    processing Tag <" + tagName + "> for <" + CamUtils.humanName(e) + ">  <" + st.getName() + "> <"
                + tagName + ">  <" + tagVal + ">");

        // set the first value with no append (replace)
        // for right now we only handle single items
        String[] vals = new String[1];
        vals[0] = tagVal;
        CamUtils.setTagVal(e, st, tagName, vals[0], false);
        // set any further values with append
        if (vals.length > 1)
            for (int i = 1; i < vals.length; i++)
                CamUtils.setTagVal(e, st, tagName, vals[i], true);
        return true;
    }

    /**
     * **********************************************************************************************
     * UNCLASSIFIED - Process one line of tags for the element specified by
     * idColumn/idTag
     *
     * @param index The index of the line in the reqFile
     * @author D99510
     * **********************************************************************************************
     */
    void processLine(int index) {
        String[] line = reqFile.getLine(index);
        String id = line[idColumn];
        Element elem = doorsHash.get(id);
        boolean allTagsProcessed = true;

        StringBuilder lineB = new StringBuilder();
        for (String s : line) {
            lineB.append("<");
            lineB.append(s);
            lineB.append("> ");
        }
        
        logIt("Processing line : <" + lineB.toString() + ">");
        
        if (elem == null)
            elem = doorsHash.get(id.trim());

        if (elem == null) {
            logIt("WARNING Requirement/Information null while processing line number = " + index + " id = " + id);
            logIt("-----------------------");
            lineFails++;
            return;
        }

        for (int i = 0; i < line.length; i++) {
            logIt("  Tag = <" + line[i] + ">");
            if (i == idColumn || i == poundColumn || i == nameColumn)
                continue;
            if (!processTag(elem, headerStype[i], header[i], line[i]))
                allTagsProcessed = false;
        }
        linesProcessed++;
        if (allTagsProcessed)
            linesAllTagsProcessed++;
        else
            linesPartialTagsProcessed++;
        logIt("-----------------------");

    }

    /**
     * **********************************************************************************************
     * UNCLASSIFIED - Cycles through the lines in the reqFile processing each line
     * except the header line.
     * @author D99510
     * **********************************************************************************************
     */
    void processChanges() {
        // skip index 0 which is the header line
        lineCount = reqFile.getLines().size();
        for (int i = 1; i < lineCount; i++)
            processLine(i);
    }

    /**
     * **********************************************************************************************
     * UNCLASSIFIED - The top level action to invoke when action is required
     * @author D99510
     * **********************************************************************************************
     */
    public void doIt() {
        project = Application.getInstance().getProject();
        drSt = StereotypesHelper.getStereotype(project, drStName, (Profile) null);
        diSt = StereotypesHelper.getStereotype(project, diStName, (Profile) null);
        arSt = StereotypesHelper.getStereotype(project, arStName, (Profile) null);
        
        resetVars();
        
        qlog.setName("C:\\temp\\quicksync.log");
        logIt("Starting SimpleSync");
        
        
        CamUtils.notify("Starting SimpleSync");
        if (drSt == null || diSt == null || arSt == null) {

            CamUtils.notify(
                    "Cannot find " + drStName + " or " + diStName + " or " + arStName + " stereotypes, exiting!");
            return;
        }

        Object[] options = new Object[2];
        options[0] = new String("DOORS Requirement");
        options[1] = new String("DOORS Information");
        //...and passing `frame` instead of `null` as first parameter
        Object selectionObject = JOptionPane.showInputDialog(null, 
                "Select type of Sync", "Type of Sync", JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        if (selectionObject == null) {
            logIt("SimpleSync canceled");
            CamUtils.notify("SimpleSync canceled");
            return;
        }
        String selectionString = selectionObject.toString();

        
        doingDOORSReq = (selectionString.equals(options[0]));

        // get the csv file and read it
        if (!getReqFile()) {
            logIt("Failed or Canceled reading CSV file");
            CamUtils.notify("Failed or Canceled reading CSV file");
            return;
        }
        CamUtils.notify("Successfully read CSV file" + reqFile.getFilename());

        CamUtils.notify("Resolving Tag Stereotypes");
        logIt("Resolving Tag Stereotypes");
        resolveTagSTypes();
        
        if (doingDOORSReq) {
            CamUtils.notify("Finding DOORS Requiremnets, this is the slow part");
            logIt("Finding DOORS Requirements, this is the slow part");
        } else {
            CamUtils.notify("Finding DOORS Information, this is the slow part");
            logIt("Finding DOORS Innformation, this is the slow part");
        }
        
        fillDoorsHash();

        CamUtils.notify("Applying changes from CSV file");
        logIt("Applying changes from CSV file");
        processChanges();
        CamUtils.notify("SimpleSync Complete!!!");
        logIt("SimpleSync Complete!!!");
        CamUtils.notify("------------------------Summary");
        CamUtils.notify("Resolved header tags (excludes Name and # column) = " + tagsResolved);
        CamUtils.notify("Total lines in file (minus header) = " + (lineCount -1));
        CamUtils.notify("Lines processed = " + linesProcessed);
        CamUtils.notify("Lines failed = " + lineFails);
        CamUtils.notify("Lines with tags fully processed = " + linesAllTagsProcessed);
        CamUtils.notify("Lines with tags partially processed = " + linesPartialTagsProcessed);
        
        logIt("------------------------Summary");
        logIt("Resolved header tags (excludes Name and # column) = " + tagsResolved);
        logIt("Total lines in file (minus header) = " + (lineCount - 1));
        logIt("Lines processed = " + linesProcessed);
        logIt("Lines failed = " + lineFails);
        logIt("Lines with tags fully processed = " + linesAllTagsProcessed);
        logIt("Lines with tags partially processed = " + linesPartialTagsProcessed);
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
		QuickSyncAction qsa = new QuickSyncAction(); 
		qsa.doIt();
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

scopeAndDo sAD = new scopeAndDo();
QuickSyncAction.qlog = qlog;
QuickSyncAction.reqFile = reqFile;
QuickSyncAction.CamUtils = CamUtils;
QuickSyncAction.StringUtils = StringUtils;
QuickSyncAction.FindUtils = FindUtils;

sAD.doIt();
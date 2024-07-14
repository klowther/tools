import javax.swing.*;
import java.awt.*;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdbasicbehaviors.Behavior;

import com.nomagic.magicdraw.core.Application;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.ui.browser.Browser;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralString;
import com.nomagic.magicdraw.ui.browser.Node;
import com.nomagic.magicdraw.ui.browser.Tree;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.SymbolElementMap;
import com.nomagic.uml2.ext.jmi.helpers.InteractionHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.jmi.helpers.TagsHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.OpaqueExpression;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Operation;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.StringTaggedValue;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ValueSpecification;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdsimpletime.Duration;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdsimpletime.DurationConstraint;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdsimpletime.TimeExpression;
import com.nomagic.uml2.ext.magicdraw.interactions.mdbasicinteractions.Message;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.impl.ElementsFactory;

import javax.swing.JFileChooser;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

class CPLSearchAndReplaceAction {
	final String baseTerm1 = "CPL_";
	final String baseTerm2 = "CPL-";
	final String doorsIndicator = "DOORS";
	
	ArrayList<Comment> ac = new ArrayList<Comment>();
	ArrayList<LiteralString> als = new ArrayList<LiteralString>();
	ArrayList<StringTaggedValue> astv = new ArrayList<StringTaggedValue>();
	ArrayList<OpaqueExpression> aoe = new ArrayList<OpaqueExpression>();
	ArrayList<DiagramPresentationElement> adgram = new ArrayList<DiagramPresentationElement>();
	ArrayList<Duration> adur = new ArrayList<Duration>();
	ArrayList<Operation> ops = new ArrayList<Operation>();
	ArrayList<Constraint> constraintArrayList = new ArrayList<Constraint>();
	ArrayList<Message> messageArrayList = new ArrayList<Message>();
	ArrayList<ValueSpecification> valueSpecificationArrayList = new ArrayList<ValueSpecification>();
	ArrayList<TimeExpression> timeExpressionArrayList = new ArrayList<TimeExpression>();
	
	ArrayList<String[]> lines = null;
	static Object cplLog;
	static Object CamUtils;
	static Object csvFile;
	SymbolElementMap sem;
	Project proj;
	ElementsFactory factory;
	HashSet<DiagramPresentationElement> affectedDiagrams = new HashSet<>(); 


	private static final long serialVersionUID = 1L;

	/**
	 * ***********************************************************************
	 * UNCLASSIFIED - This class implements a comparator to allow sorting of the
	 * array list of CPL and substitute pair by the length of the CPL key
	 * 
	 * @param newLine - flag indicating if newline will be appended
	 * @author D99510
	 * ***********************************************************************
	 */
	class comp implements Comparator<String[]> {

		@Override
		public int compare(String[] s1, String[] s2) {
			int l1 = s1[0].length();
			int l2 = s2[0].length();

			if (l1 < l2)
				return 1;
			if (l1 > l2)
				return -1;
			return 0;
		}

	}

	/**
	 * ***********************************************************************
	 * UNCLASSIFIED - This function prompts the user for a csv file name and reads
	 * the information from it if possible.
	 * 
	 * @return true if processed false otherwise (cancel)
	 * @author D99510
	 * ***********************************************************************
	 */
	private boolean getCSVdata() {
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

		try {
			csvFile.setFileName(f.getAbsolutePath());
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		if (csvFile == null)
			return false;

		lines = csvFile.getLines();
		lines.sort(new comp());
		cplLog.log("CSV Post sort keys");
		for (String[] s : lines) {
			cplLog.log(s[0]);
		}
		return true;
	}

	/**
	 * ***********************************************************************
	 * UNCLASSIFIED - This method attempts to find all the strings that are not
	 * under a DOORS item and add them to the either the Comment, LiteralString, or
	 * StringTaggedValue lists depending on the type.
	 * @author D99510
	 * ***********************************************************************
	 */
	private void getCPLelements() {
		Collection<BaseElement> bes = proj.getAllElements();
		int aoecount = 0;
		int ccount = 0;
		int lscount = 0;
		int stcount = 0;
		int dcount = 0;
		int durcount = 0;
		int opcount = 0;
		int constraintCount = 0;
		int messageCount = 0;
		int valueSpecificationCount = 0;
		int timeExpressionCount = 0;
		cplLog.log("---------------------------------------------------------");
		for (BaseElement be : bes) {
			if (be instanceof DiagramPresentationElement) {
				//log.log("Adding DiagramPresentationElement " + be.getHumanName());
				adgram.add((DiagramPresentationElement)be);
			}
			if (!(be instanceof Element))
				continue;
			Element e = (Element) be;
			String humantype = be.getHumanType().toLowerCase();
			//      if (humantype.equals("diagram")) {
			//        Diagram d = (Diagram)e;
			//        adgram.add(d);
			//      }
			if (humantype.equals("opaque expression")) {
				Element owner = e.getOwner();
				String oname = owner.getHumanType();
				if (oname.contains(doorsIndicator)) {
					dcount++;
					continue;
				}
				OpaqueExpression oe = (OpaqueExpression) be;
				List<String> vals = oe.getBody();
				for (String val : vals)
					if (val.contains(baseTerm1) || val.contains(baseTerm2)) {
						cplLog.log("Found Opaque Expression: " + val);
						aoe.add(oe);
						aoecount++;
					}
			}

			if (humantype.equals("comment")) {
				Element owner = e.getOwner();
				String oname = owner.getHumanType();
				if (oname.contains(doorsIndicator)) {
					dcount++;
					continue;
				}
				Comment c = (Comment) be;
				String val = c.getBody();
				if (val.contains(baseTerm1)  || val.contains(baseTerm2)) {
					cplLog.log("Found Comment: " + val);
					ac.add(c);
					ccount++;
				}
			}
			if (humantype.equals("literal string")) {
				BaseElement owner = e.getOwner();
				String oname = owner.getHumanType();
				if (oname.contains(doorsIndicator)) {
					dcount++;
					continue;
				}
				LiteralString ls = (LiteralString) be;
				String val = ls.getValue();
				if (val.contains(baseTerm1) || val.contains(baseTerm2)) {
					cplLog.log("Found Literal String: " + val);
					als.add(ls);
					lscount++;
				}
			}
			if (humantype.equals("string tagged value")) {
				Element owner = e.getOwner();
				String oname = owner.getHumanType();
				if (oname.contains(doorsIndicator)) {
					dcount++;
					continue;
				}
				StringTaggedValue stv = (StringTaggedValue) be;
				List<String> lstring = stv.getValue();
				for (String val : lstring)
					if (val.contains(baseTerm1) || val.contains(baseTerm2)) {
						cplLog.log("Found String Tagged Value: " + val);
						astv.add(stv);
						stcount++;
						break;
					}
			}
			if(humantype.equals("duration")) {
				Element owner = e.getOwner();
				String oname = owner.getHumanType();
				if (oname.contains(doorsIndicator)) {
					dcount++;
					continue;
				}
				Duration dur = (Duration) be;
				String lstring = dur.getExpr().toString();
				if(lstring.contains(baseTerm1) || lstring.contains(baseTerm2)) {
					cplLog.log("Found Duration: " + lstring);
					adur.add(dur);
					durcount++;

				}
			}
			if(humantype.equals("operation")) {
				Element owner = e.getOwner();
				String oname = owner.getHumanType();
				if (oname.contains(doorsIndicator)) {
					dcount++;
					continue;
				}
				Operation op = (Operation) be;
				String lstring = op.getName();
				if(lstring.contains(baseTerm1) || lstring.contains(baseTerm2)) {
					cplLog.log("Found Operation: " + lstring);
					ops.add(op);
					opcount++;
				}
			}
			if(humantype.equals("constraint")) {
				Element owner = e.getOwner();
				String oname = owner.getHumanType();
				if (oname.contains(doorsIndicator)) {
					constraintCount++;
					continue;
				}
				Constraint con = (Constraint) be;
				String lstring = con.getSpecification().toString();
				if(lstring.contains(baseTerm1) || lstring.contains(baseTerm2)) {
					cplLog.log("Found Constraint: " + lstring);
					constraintArrayList.add(con);
					constraintCount++;
				}
			}
			if(humantype.equals("message")) {
				Element owner = e.getOwner();
				String oname = owner.getHumanType();
				if (oname.contains(doorsIndicator)) {
					messageCount++;
					continue;
				}
				Message msg = (Message) be;
				String lstring = msg.getName();
				if(lstring.contains(baseTerm1) || lstring.contains(baseTerm2)) {
					cplLog.log("Found Message: " + lstring);
					messageArrayList.add(msg);
					messageCount++;
				}
			}
			if(humantype.equals("value specification")) {
				Element owner = e.getOwner();
				String oname = owner.getHumanType();				
				if (oname.contains(doorsIndicator)) {
					messageCount++;
					continue;
				}
				ValueSpecification valSpec = (ValueSpecification) be;
				String lstring = valSpec.getName();
				if(lstring.contains(baseTerm1) || lstring.contains(baseTerm2)) {
					cplLog.log("Found Value Specification " + lstring);
					valueSpecificationArrayList.add(valSpec);
					valueSpecificationCount++;
				}
			}
			if(humantype.equals("time expression")) {
				Element owner = e.getOwner();
				String oname = owner.getHumanType();
				if (oname.contains(doorsIndicator)) {
					messageCount++;
					continue;
				}
				TimeExpression timeExpression = (TimeExpression) be;
				String lstring = timeExpression.getName();
				if(lstring.contains(baseTerm1) || lstring.contains(baseTerm2)) {
					cplLog.log("Found Time Expression: " + lstring);
					timeExpressionArrayList.add(timeExpression);
					timeExpressionCount++;
				}
			}
		}
		cplLog.log("============================");
		cplLog.log("DOORS count = " + dcount);
		cplLog.log("Opaque Expression count = " + aoecount);
		cplLog.log("Comment count = " + ccount);
		cplLog.log("Literal String count = " + lscount);
		cplLog.log("String Tagged Value count = " + stcount);
		cplLog.log("Duration count = " + durcount);
		cplLog.log("Operation count = " + opcount);
		cplLog.log("Constraint count = " + constraintCount);
		cplLog.log("Message count = " + messageCount);
		cplLog.log("Value Specification count = " + valueSpecificationCount);
		cplLog.log("Time Expression count = " + timeExpressionCount);
	}

	/**
	 * ***********************************************************************
	 * UNCLASSIFIED - Creates a word only regular expression so as not to replace
	 * RMDCPL_1 with the substitution content for CPL_1
	 * @author D99510
	 ***********************************************************************/
	String regIt(String orig) {
		// OLDWAY return "\\b" + orig + "\\b";
		return "\\b" + orig + "(?![\\w=])";
	}



	/**
	 * ***********************************************************************
	 * UNCLASSIFIED - Performs the substitution on this Comment
	 * 
	 * @param item - the comment to replace text in
	 * @param orig - the original string to replace
	 * @param sub  - the replacement string
	 * @author D99510
	 ************************************************************************ 
	 **/
	private void substitute(Comment item, String orig, String sub) {
		if (item == null || orig == null || sub == null)
			return;
		String val = item.getBody();
		val = val.replaceAll(regIt(orig), orig + "=" + sub);

		item.setBody(val);
	}

	/**
	 * ***********************************************************************
	 * UNCLASSIFIED - Performs the substitution on this LiteralString
	 * 
	 * @param item - the literal string to replace text in
	 * @param orig - the original string to replace
	 * @param sub  - the replacement string
	 * @author D99510
	 ************************************************************************
	 */
	private void substitute(LiteralString item, String orig, String sub) {
		if (item == null || orig == null || sub == null)
			return;
		String val = item.getValue();
		val = val.replaceAll(regIt(orig), orig + "=" + sub);

		item.setValue(val);
	}

	/**
	 * ***********************************************************************
	 * UNCLASSIFIED - Performs the substitution on this StringTaggedValue
	 * 
	 * @param item - the string tagged value to replace text in
	 * @param orig - the original string to replace
	 * @param sub  - the replacement string
	 * @author D99510
	 * ***********************************************************************
	 */
	private void substitute(StringTaggedValue item, String orig, String sub) {
		if (item == null || orig == null || sub == null)
			return;

		Element owner = item.getTaggedValueOwner();
		Stereotype sType = TagsHelper.getTagDefinitionOwner(item);
		Property tagDef = item.getTagDefinition();
		if (owner == null || sType == null || tagDef == null)
			return;

		String tName = tagDef.getName();
		if (tName == null)
			return;

		List<String> sList = item.getValue();
		if (sList == null)
			return;
		for (int i = 0; i < sList.size(); i++) {
			String s = sList.get(i);
			if (s != null) {
				String repstr = s.replaceAll(regIt(orig), orig + "=" + sub);
				sList.set(i, repstr);
			}
		}
		StereotypesHelper.setStereotypePropertyValue(owner, sType, tName, sList, false);
	}

	/**
	 * ***********************************************************************
	 * UNCLASSIFIED - Performs the substitution on this OpaqueExpression
	 * 
	 * @param item - the string tagged value to replace text in
	 * @param orig - the original string to replace
	 * @param sub  - the replacement string
	 * @author D99510
	 * ***********************************************************************
	 */
	private void substitute(OpaqueExpression item, String orig, String sub) {
		if (item == null || orig == null || sub == null)
			return;
		Element owner = item.getOwner();
		Element ownerowner = owner.getOwner();

		if (owner == null || ownerowner == null)
			return;

		if (item.getBody() == null)
			return;

		int bodySize = item.getBody().size();

		if (bodySize == 0)
			return;

		for (int i = 0; i < bodySize; i++) {
			String s = item.getBody().get(i);
			if (s != null) {
				if (s.contains(orig)) {
					String repstr =  s.replaceAll(regIt(orig), orig + "=" + sub);
					item.getBody().set(i,repstr);
				}
			}
		}
	}

	/**
	 * ***********************************************************************
	 * UNCLASSIFIED - Performs the substitution on this Duration
	 * 
	 * @param item - the string tagged value to replace text in
	 * @param orig - the original string to replace
	 * @param sub  - the replacement string
	 * 
	 * ***********************************************************************
	 */
	private void substitute(Duration item, String orig, String sub) {
		if (item == null || orig == null || sub == null)
			return;
		Element owner = item.getOwner();
		Element ownerowner = owner.getOwner();

		if (owner == null || ownerowner == null)
			return;

		if (item.getExpr() == null)
			return;

		String s = item.getExpr().toString();
		if (s != null) {
			if (s.contains(orig)) {
				String repstr =  s.replaceAll(regIt(orig), orig + "=" + sub);
				DurationConstraint durationConstraint = factory.createDurationConstraintInstance();
				durationConstraint.setOwner(item);
				InteractionHelper.setDurationInterval(durationConstraint, null, repstr);
			}
		}
	}
	
	/**
	 * ***********************************************************************
	 * UNCLASSIFIED - Performs the substitution on this Operation
	 * 
	 * @param item - the string tagged value to replace text in
	 * @param orig - the original string to replace
	 * @param sub  - the replacement string
	 * 
	 * ***********************************************************************
	 */
	private void substitute(Operation item, String orig, String sub) {
		if (item == null || orig == null || sub == null)
			return;
		Element owner = item.getOwner();
		Element ownerowner = owner.getOwner();

		if (owner == null || ownerowner == null)
			return;

		if (item.getName() == null)
			return;

		String s = item.getName();
		if (s != null) {
			if (s.contains(orig)) {
				String repstr =  s.replaceAll(regIt(orig), orig + "=" + sub);
				item.setName(repstr);
			}
		}
	}
	
	/**
	 * ***********************************************************************
	 * UNCLASSIFIED - Performs the substitution on this Constraint
	 * 
	 * @param item - the string tagged value to replace text in
	 * @param orig - the original string to replace
	 * @param sub  - the replacement string
	 * 
	 * ***********************************************************************
	 */
	private void substitute(Constraint item, String orig, String sub) {
		if (item == null || orig == null || sub == null)
			return;
		Element owner = item.getOwner();
		Element ownerowner = owner.getOwner();

		if (owner == null || ownerowner == null)
			return;

		if (item.getName() == null)
			return;

		String s = item.getName();
		if (s != null) {
			if (s.contains(orig)) {
				String repstr =  s.replaceAll(regIt(orig), orig + "=" + sub);
				DurationConstraint durationConstraint = factory.createDurationConstraintInstance();
				durationConstraint.setOwner(item);
				InteractionHelper.setDurationInterval(durationConstraint, null, repstr);
			}
		}
	}
	
	/**
	 * ***********************************************************************
	 * UNCLASSIFIED - Performs the substitution on this Message
	 * 
	 * @param item - the string tagged value to replace text in
	 * @param orig - the original string to replace
	 * @param sub  - the replacement string
	 * 
	 * ***********************************************************************
	 */
	private void substitute(Message item, String orig, String sub) {
		if (item == null || orig == null || sub == null)
			return;
		Element owner = item.getOwner();
		Element ownerowner = owner.getOwner();

		if (owner == null || ownerowner == null)
			return;

		if (item.getName() == null)
			return;

		String s = item.getName();
		if (s != null) {
			if (s.contains(orig)) {
				String repstr =  s.replaceAll(regIt(orig), orig + "=" + sub);
				item.setName(repstr);
			}
		}
	}
	
	/**
	 * ***********************************************************************
	 * UNCLASSIFIED - Performs the substitution on this Value Specification
	 * 
	 * @param item - the string tagged value to replace text in
	 * @param orig - the original string to replace
	 * @param sub  - the replacement string
	 * 
	 * ***********************************************************************
	 */
	private void substitute(ValueSpecification item, String orig, String sub) {
		if (item == null || orig == null || sub == null)
			return;
		Element owner = item.getOwner();
		Element ownerowner = owner.getOwner();

		if (owner == null || ownerowner == null)
			return;

		if (item.getName() == null)
			return;

		String s = item.getName();
		if (s != null) {
			if (s.contains(orig)) {
				String repstr =  s.replaceAll(regIt(orig), orig + "=" + sub);
				item.setName(repstr);
			}
		}
	}
	
	/**
	 * ***********************************************************************
	 * UNCLASSIFIED - Performs the substitution on this Time Expression
	 * 
	 * @param item - the string tagged value to replace text in
	 * @param orig - the original string to replace
	 * @param sub  - the replacement string
	 * 
	 * ***********************************************************************
	 */
	private void substitute(TimeExpression item, String orig, String sub) {
		if (item == null || orig == null || sub == null)
			return;
		Element owner = item.getOwner();
		Element ownerowner = owner.getOwner();

		if (owner == null || ownerowner == null)
			return;

		if (item.getName() == null)
			return;

		String s = item.getName();
		if (s != null) {
			if (s.contains(orig)) {
				String repstr =  s.replaceAll(regIt(orig), orig + "=" + sub);
				item.setName(repstr);
			}
		}
	}

	/**
	 * ***********************************************************************
	 * UNCLASSIFIED - Performs the substitutions using the member variables ac, als
	 * and astv.
	 * @author D99510
	 * ***********************************************************************
	 */
	private void performSubstitutions() {
		for (String[] sa : lines) {
			String orig = sa[0];
			String sub = sa[1];

			cplLog.log("Performing substitution :-------------------------");
			cplLog.log("   Original :<" + orig + ">");
			cplLog.log("   Replace  :<" + sub + ">");

			for (Comment item : ac)
				substitute(item, orig, sub);
			for (LiteralString item : als)
				substitute(item, orig, sub);
			for (StringTaggedValue item : astv)
				substitute(item, orig, sub);
			for (OpaqueExpression item : aoe)
				substitute(item, orig, sub);
			for (Duration item : adur)
				substitute(item, orig, sub);
			for (Operation item : ops)
				substitute(item, orig, sub);
			for (Constraint item : constraintArrayList)
				substitute(item, orig, sub);
			for (Message item : messageArrayList)
				substitute(item, orig, sub);
			for (ValueSpecification item : valueSpecificationArrayList)
				substitute(item, orig, sub);
			for (TimeExpression item : timeExpressionArrayList)
				substitute(item, orig, sub);
		}
	}

	public void performAction() {
		proj = Application.getInstance().getProject();
		factory = proj.getElementsFactory();
		sem = proj.getSymbolElementMap();

		cplLog.setName("c:\\temp\\CPLSearchAndReplace.log");
		CamUtils.notify("SearchAndReplace Start");
		CamUtils.notify("  Getting CSV data Please wait");
		if (!getCSVdata()) {
			CamUtils.notify("    Cancelled or unable to continue");
			return;
		}
		CamUtils.notify("  Collecting Elements conaining " + baseTerm1 + " or "  + baseTerm2);
		getCPLelements();
		CamUtils.notify("  Performing substitutions");
		performSubstitutions();
		CamUtils.notify("SearchAndReplace Complete");
		cplLog.close();
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
	 * UNCLASSIFIED - This is the area that YOU must put some action. Look at QuickSync for exmple
	 * if needed
	 * @author D99510
	 * **********************************************************************************************
	 */
	private void runButtonSelected() {
		CPLSearchAndReplaceAction cpla = new CPLSearchAndReplaceAction (); 
		app.getGUILog().log("executing ob");
		cpla.performAction();
	}
	
	private void selectButtonSelected() {
		Element[] se = getSelectedElements();
		if (se != null)  {
			for (int i = 0; i < se.length; i++) {
				String type = se[i].getHumanType();
				app.getGUILog().log("Selected element: " + se[i].getHumanName() + " i = " + i.toString());
				if(i > 0) {
					selectedElementsLabel.setText(selectedElementsLabel.getText() + 
							"," + 
							se[i].getHumanName().substring(type.length()).trim());
					app.getGUILog().log("if");
				}
				else{
				    app.getGUILog().log("else");
					selectedElementsLabel.setText("Selected elements: " + se[i].getHumanName().substring(type.length()).trim());
				}
			}
		} else {
			app.getGUILog().log("Selection is NULL or Nothing Selected");
		}
	}
	
	private void cancelButtonSelected() {
		dialog.dispose();
		
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
		if (e.getSource() == runButton) {
			runButtonSelected();
			app.getGUILog().log("Action Done");
		} else if(e.getSource() == cancelButton){
			cancelButtonSelected();
			app.getGUILog().log("Action Canceled");
		} else if(e.getSource() == selectButton){
			selectButtonSelected();
		}
		//dialog.dispose();
	}
}


CPLSearchAndReplaceAction.cplLog = cpllog;
CPLSearchAndReplaceAction.CamUtils = CamUtils;
CPLSearchAndReplaceAction.csvFile = csvFile;

scopeAndDo sAD = new scopeAndDo();
sAD.doIt();
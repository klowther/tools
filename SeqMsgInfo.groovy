import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.nomagic.magicdraw.actions.ActionsConfiguratorsManager;
import com.nomagic.magicdraw.plugins.Plugin;
import com.nomagic.magicdraw.ui.browser.Node;
import com.nomagic.magicdraw.ui.browser.Tree;
import com.nomagic.magicdraw.ui.browser.actions.DefaultBrowserAction;
import com.nomagic.uml2.ext.jmi.helpers.InteractionHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.interactions.mdbasicinteractions.Lifeline;
import com.nomagic.uml2.ext.magicdraw.interactions.mdbasicinteractions.Message;
import com.nomagic.uml2.ext.magicdraw.interactions.mdbasicinteractions.MessageSort;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.magicdraw.uml.DiagramType;

import cameoutils.BrowserConfigurator;
import cameoutils.FindUtils;
import utils.Logger;
import utils.PasteTable;


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

class SeqMsgInfoOB
{
	class sData {
		Diagram sd;
		//ArrayList<Element> lls;
		ArrayList<Element> msgs;
	}

	ArrayList<sData> sDgrams = new ArrayList<sData>();

	Element[] selectedElements;
	Application application;
	Tree tree;
	SeqMsgInfoOB(Element[] se, Application app, Tree t){
		selectedElements = se;
		application = app;
		tree = t;
	}

	void getSeqDiagrams(Element start) {
		sDgrams.clear();
		ArrayList<Element> dgrams;

		Logger.templog("Finding elements by type Diagram");
		dgrams = FindUtils.findByHType(start, "Diagram", true);
		Logger.templog(dgrams.size() + " elements of type diagram found");

		for (Element e : dgrams) {
			Logger.templog("Testing Element " + e.getHumanName());
			if (e instanceof Diagram) {
				Logger.templog("  Found diagrame instance");
				Diagram de = (Diagram)e;
				DiagramType dType = DiagramType.getDiagramType(de);
				Logger.templog("  Found diagram type of " + dType.getType());
				if (dType.getType().equals("SysML Sequence Diagram")) {
					Logger.templog("    Adding item");
					sData item = new sData();
					item.sd = de;
					sDgrams.add(item);
				}
			}
		}
	}

	private String llName(Element lle) {
		if (lle == null)
			return "null";
		String rv = lle.getHumanName();
		//InstanceSpecification  iS = ll.getAppliedStereotypeInstance();
		if (lle instanceof Lifeline) {
			Lifeline ll = (Lifeline)lle;
			Classifier c = InteractionHelper.getLifelineType(ll);
			if (c != null)
				rv = c.getHumanName();
		}
		return rv;
	}

	class MainDlg extends JFrame implements ActionListener {

		private static final long serialVersionUID = 1L;
		PasteTable pt;
		Element[][] eArray;
		JButton locate = new JButton("Locate");
		JLabel spacer = new JLabel("  ");

		@Override
		public void actionPerformed(ActionEvent event) {
			if (event.getSource() == locate) {
				int row = pt.getSelectedRow();
				int col = pt.getSelectedColumn();

				if (row > 0 && row < eArray.length) {
					if (col > 0 && col < eArray[0].length) {
						Element fe = eArray[row][col];
						if (fe != null) {
							tree.openNode((BaseElement)fe);
						}
					}
				}
			}
		}

		public MainDlg() {
			int rowcount = 0;
			for (sData sd : sDgrams) {
				Element interaction = sd.sd.getOwner();
				sd.msgs = FindUtils.findByHType(interaction, "Message", true);
				Logger.templog("  Found " + sd.msgs.size() + " messages in " + interaction.getHumanName());
				rowcount += sd.msgs.size();
			}
			pt = new PasteTable(rowcount, 6);
			eArray = new Element[rowcount][6];
			pt.getColumnModel().getColumn(0).setHeaderValue("Sequence Diagram");
			pt.getColumnModel().getColumn(1).setHeaderValue("Message");
			pt.getColumnModel().getColumn(2).setHeaderValue("Msg Sort");
			pt.getColumnModel().getColumn(3).setHeaderValue("Sender");
			pt.getColumnModel().getColumn(4).setHeaderValue("Receiver");
			pt.getColumnModel().getColumn(5).setHeaderValue("Description");
			rowcount = 0;
			for (sData sd : sDgrams) {
				for (Element e : sd.msgs) {
					Message m = (Message) e;
					Element sender = InteractionHelper.getSendElement(m);
					Element receiver = InteractionHelper.getReceiveElement(m);
					pt.setValueAt(sd.sd.getName(), rowcount, 0);
					eArray[rowcount][0] = (Element) sd.sd.getOwner();
					pt.setValueAt(m.getName(), rowcount, 1);
					eArray[rowcount][1] = m;
					MessageSort ms = m.getMessageSort();
					String txt = "";
					if (ms != null)
						txt = ms.toString();
					Logger.templog("Adding <" + txt + "> to " + rowcount + ",2");
					pt.setValueAt(txt, rowcount, 2);
					pt.setValueAt(llName(sender), rowcount, 3);
					eArray[rowcount][3] = sender;
					pt.setValueAt(llName(receiver), rowcount, 4);
					eArray[rowcount][4] = receiver;
					Collection<Comment> description = m.getOwnedComment();
					if (description != null) {
						String dtext = "";
						for (Comment c : description) {
							String ctext = c.getBody();
							if (ctext != null)
								dtext += ctext;
						}
						pt.setValueAt(dtext, rowcount, 5);
					}
					rowcount++;
				}
			}

			this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			locate.addActionListener(this);
			JScrollPane jsp = new JScrollPane(pt, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			this.add(jsp);
			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
			buttonPanel.add(spacer);
			buttonPanel.add(locate);
			buttonPanel.add(spacer);
			this.add(buttonPanel,BorderLayout.SOUTH);
			this.setSize(640, 480);
		}
	}

	void createTableDialog() {
		MainDlg md = new MainDlg();
		md.setVisible(true);
	}

	public void runSeqMsgInfo()
	{

		Logger.templog("Getting sequence diagrams");
		getSeqDiagrams(selectedElements[0]);
		Logger.templog("Creating table");
		createTableDialog();
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
class scopeAndDoSMI implements ActionListener {
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
			Tree t = b.getActiveTree();
			SeqMsgInfoOB smi = new SeqMsgInfoOB(se,app,t);
			smi.runSeqMsgInfo();
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

scopeAndDoSMI sAD = new scopeAndDoSMI();
sAD.doIt();
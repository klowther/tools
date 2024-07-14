import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.*;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.PresentationElementsManager;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.ui.browser.Browser;
import com.nomagic.magicdraw.ui.browser.Node;
import com.nomagic.magicdraw.ui.browser.Tree;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.magicdraw.uml.DiagramType;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.magicdraw.uml.symbols.shapes.ShapeElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdbasicbehaviors.Behavior;


class ReclassifyDiagrams{
  /**
  * ***********************************************************************
  * UNCLASSIFIED - This class is used to find indicators in a diagram
  * that contain the strings CPL_*= or CPL-*= and reclassify the diagram 
  * to "SECRET"
  * @author D99510
  * ***********************************************************************
  */

    static Object rdLog;
    static Object CamUtils;

    /**
    * ***********************************************************************
    * UNCLASSIFIED - Helper routine that removes newlines to then test the string 
    * @param str - the string to test
    * @param regex - the expression to test for
    * @return - true if match false otherwise
    * @author D99510
    * ***********************************************************************
    */  
    private boolean myMatch(String str, String regex) {
      String teststr = str.replace("\n", "").replace("\r", "");
      return teststr.matches(regex);
    }

    /**
    * ***********************************************************************
    * UNCLASSIFIED - Returns if the diagram is a table
    * @param dpe - the string to test
    * @return true if table false otherwise
    * @author D99510
    * ***********************************************************************
    */  
    private boolean isTable(DiagramPresentationElement dpe) {
      Diagram d = dpe.getDiagram();
      DiagramType dType = DiagramType.getDiagramType(d);
      return (dType.getType().contains("Table"));
    }

    /**
    * ***********************************************************************
    * UNCLASSIFIED - Returns a list of NontableDiagrams from a collection of 
    * BaseElements.
    * @param bes - a collection of BaseElements
    * @return - a List of Diagrams from the input less any Tables
    * @author D99510
    * ***********************************************************************
    */  
    private ArrayList<DiagramPresentationElement> getNonTableDiagrams(Collection<BaseElement> bes) {
      ArrayList<DiagramPresentationElement> dList = new ArrayList<DiagramPresentationElement>();
      for (BaseElement be : bes) {
        if (be instanceof DiagramPresentationElement) {
          DiagramPresentationElement dpe = (DiagramPresentationElement) be;
          if (isTable(dpe)) {
            continue;
          }
          dList.add(dpe);
        }
      }
      return dList;
    }

    /**
    * ***********************************************************************
    * UNCLASSIFIED - Returns a string representing all of the text in a  
    * Presentation Element
    * @param pe - the presentation element
    * @return - the string representing all text in pe
    * @author D99510
    * ***********************************************************************
    */  
    String screenText(PresentationElement pe) {
      String peStr = pe.toString();
      String screenStr = peStr.substring(peStr.indexOf('(') + 1, peStr.lastIndexOf(')'));
      return screenStr;
    }

    /**
    * ***********************************************************************
    * UNCLASSIFIED - This is the main working routine for the class  
    * @author D99510
    * ***********************************************************************
    */  
    private void ReclassDiagrams() {
      final String matchExp1 = ".*\\bCPL_[0-9]+?=.*";
      final String matchExp2 = ".*\\bCPL-[0-9]+?=.*";
      
      rdLog.log("testAction1 --- matchExpression = <" + matchExp1 + "> or <" + matchExp2 + ">");
      Project proj = Application.getInstance().getProject();
      rdLog.log("Acquired Project ");
      Collection<BaseElement> bes = proj.getAllElements();
      ArrayList<DiagramPresentationElement> adgram = new ArrayList<DiagramPresentationElement>();

      int count = 0;
      int changedCount = 0;
      rdLog.log("Creating session ");

      SessionManager sessionManager = SessionManager.getInstance();
      sessionManager.createSession(proj, "session 1");
      rdLog.log("Analyzing " + bes.size() + " Elements");
      adgram = getNonTableDiagrams(bes);

      rdLog.log("Processing " + adgram.size() + " Non Table Diagrams ");

      for (DiagramPresentationElement dpe : adgram) {
        boolean closeme = false;
        count++;
        
        // logging all was SLOW
        if (count % 10 == 0)
          rdLog.log("" + count);

        if (count % 50 == 0) {
          sessionManager.closeSession(proj);
          sessionManager.createSession(proj, "session 1");        
        }

        if (!dpe.isLoaded()) {
          closeme = true;
          dpe.ensureLoaded();
        }
        Collection<PresentationElement> cpe = dpe.collectPresentationElementsRecursively();
        ArrayList<PresentationElement> cuiPEs = new ArrayList<PresentationElement>();
        boolean CPLfound = false;
        for (PresentationElement pe : cpe) {
          String hn = screenText(pe);
          if (hn.contains("CUI"))
            cuiPEs.add(pe);
          if (myMatch(hn, matchExp1) || myMatch(hn, matchExp2)) {
            CPLfound = true;
            rdLog.log("CPL_*= found in diagram " + dpe.getHumanName());
            rdLog.log("   in String : " + hn);
            break;
          }
        }

        if (CPLfound) {
          try {
            if (removeClassification(dpe, "CUI"))
              removeCUIComment(dpe);
            addClassification(dpe, "SECRET");
            rdLog.log("  replace CUI with SECRET " + dpe.getHumanName());
            changedCount++;
          } catch (ReadOnlyElementException e) {
            rdLog.log("ERROR: Encountered Read Only Element. Could not change.");
          }
        }

        if (closeme) {
          dpe.close();
        }
        if (count > 5000)
          break;
      }
      sessionManager.closeSession(proj);
      rdLog.log("Count = " + count + " Changed Count = " + changedCount);
    }

    /**
    * ***********************************************************************
    * UNCLASSIFIED - Takes a string and colors it red if secret  
    * @param s - the string
    * @return - the HTMLized string, red if secret
    * @author D99510
    * ***********************************************************************
    */  
    String toClassHTML(String s) {
      String h = "";
      if (s.equalsIgnoreCase("secret")) {
        h += "<html><h1 style=\"color: red;\">" + s + "
</br>";
        h += "</h1></html>";
      } else {
        h += "<html><h1>" + s + "
</br>";
        h += "</h1></html>";
      }
      return h;
    }
    
    /**
    * ***********************************************************************
    * UNCLASSIFIED - Adds classification text box to top and bottom of
    * diagram.   
    * @param dpe - the diagram
    * @param s - the string to add to top and bottom
    * @author D99510
    * ***********************************************************************
    */
    private void addClassification(DiagramPresentationElement dpe, String s) throws ReadOnlyElementException {
      PresentationElementsManager pem = PresentationElementsManager.getInstance();
      Rectangle bounds = dpe.getBounds();
      int x = bounds.x;
      int y = bounds.y;
      int height = bounds.height;
      int cx = x + bounds.width / 2;
      ShapeElement thetop = pem.createTextBox(dpe, new Point(cx, y));
      ShapeElement thebottom = pem.createTextBox(dpe, new Point(cx, y + height));
      
      Rectangle tBounds = new Rectangle(cx,y,200,60);
      Rectangle bBounds = new Rectangle(cx,y + height,200,60);
      pem.reshapeShapeElement(thetop, tBounds);
      pem.reshapeShapeElement(thebottom, bBounds);
      
      pem.setText(thetop, toClassHTML(s));
      pem.setText(thebottom, toClassHTML(s));
    }

    /**
    * ***********************************************************************
    * UNCLASSIFIED - Removes classification text box to top and bottom of
    * diagram given the string.   
    * @param dpe - the diagram
    * @param s - the string to remove to top and bottom
    * @author D99510
    * ***********************************************************************
    */
    private boolean removeClassification(DiagramPresentationElement dpe, String s) throws ReadOnlyElementException {
      PresentationElementsManager pem = PresentationElementsManager.getInstance();
      Collection<PresentationElement> cpe = dpe.collectPresentationElementsRecursively();
      boolean removed = false;
      for (PresentationElement pe : cpe) {
        String hn = pe.getHumanName();
        if (hn.contains(s)) {
          rdLog.log("      Found CUI in : " + hn);
          if (hn.contains("Rectangle with Text") || 
              hn.contains("Text Box")) {
            rdLog.log("        Deleting CUI in : " + hn);
            pem.deletePresentationElement(pe);
            removed = true;
          }
        }
      }
      return removed;
    }
    
    /**
    * ***********************************************************************
    * UNCLASSIFIED - Removes specific CUI commment from diagrram
    * @param dpe - the diagram
    * @author D99510
    * ***********************************************************************
    */
    private void removeCUIComment(DiagramPresentationElement dpe) throws ReadOnlyElementException {
      final String cstr = "CONTROLLED UNCLASSIFIED INFORMATION (CUI) // EXPORT CONTROLLED INFORMATION // SEE COVER SHEET";
      Collection<PresentationElement> cpe = dpe.collectPresentationElementsRecursively();

      for (PresentationElement pe : cpe) {
        PresentationElementsManager pem = PresentationElementsManager.getInstance();
        String hn = pe.getHumanName();
        hn = hn.replaceAll("\n","");
        hn = hn.replaceAll("\r","");
        if (hn.contains("Text Area")) {
          if (hn.contains(cstr)) {
            PresentationElement parent = pe.getParent();
            if (parent.getHumanType().equals("Comment"))
              pem.deletePresentationElement(parent.getParent());
          }
        }
      }
      
    }

    public void performAction() {
      CamUtils.notify("Starting Reclassify Diagrams this will take a few minutes");
         int result = JOptionPane.showConfirmDialog(null, 
             "This will find all diagrams with\n"
            + "CPL_= or CPL-= subtext in them and\n"
          + "attempt to change them from CUI to SECRET.\n "
             + "      Do you wish to contintue?", "Continue", 
             JOptionPane.YES_NO_OPTION);
         
        if (result == JOptionPane.YES_OPTION) {
          rdLog.setName("c:\\temp\\ReclassifyDiagrams.log");
          ReclassDiagrams();
          rdLog.close();
          CamUtils.notify("Reclassification Complete.");
        }
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
		ReclassifyDiagrams rd = new ReclassifyDiagrams(); 
        rd.performAction();
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

ReclassifyDiagrams.rdLog = rdlog;
ReclassifyDiagrams.CamUtils = CamUtils;

scopeAndDo sAD = new scopeAndDo();
sAD.doIt();
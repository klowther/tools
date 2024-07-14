import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
import com.nomagic.magicdraw.uml.DiagramType;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import com.nomagic.magicdraw.uml.symbols.shapes.StereotypesDisplayModeOwner;

class TurnOffStypes {
  static Object CamUtils;
  static Object FindUtils;
  ArrayList<Diagram> diagrams = new ArrayList<Diagram>();
  ArrayList<PresentationElement> pes = new ArrayList<PresentationElement>();


 
  
  void performAction() {
    Application app = Application.getInstance();
    Project p = app.getProject();
    ArrayList<BaseElement> ses = CamUtils.getSelectedElements();
    ArrayList<BaseElement> rChildren = new ArrayList<BaseElement>();
    // get the children
    for (BaseElement be : ses) {
       if (be instanceof Element)
        rChildren.addAll(FindUtils.findElementsnew((Element)be,true));
    }

    // get all non table diagrams
    for (BaseElement be : rChildren) {
      if (be instanceof Diagram) {
        Diagram d = (Diagram)be;
        DiagramType dType = DiagramType.getDiagramType(d);
	if (dType.getType().contains("Table"))
          continue;
        diagrams.add(d);
        DiagramPresentationElement dpe = p.getDiagram(d);
        dpe.setStereotypesDisplayMode(StereotypesDisplayModeOwner.STEREOTYPE_DISPLAY_MODE_DO_NOT_DISPLAY_STEREOTYPES);
        dpe.setDSLStereotypesDisplayMode(StereotypesDisplayModeOwner.DSL_STEREOTYPE_DISPLAY_MODE_NONE);
        pes.addAll(dpe.collectPresentationElementsRecursively());
      }
    }
    for (PresentationElement pe : pes) {
      if (pe instanceof StereotypesDisplayModeOwner) {
        pe.setStereotypesDisplayMode(StereotypesDisplayModeOwner.STEREOTYPE_DISPLAY_MODE_DO_NOT_DISPLAY_STEREOTYPES);
        pe.setDSLStereotypesDisplayMode(StereotypesDisplayModeOwner.DSL_STEREOTYPE_DISPLAY_MODE_NONE);
      }
    }
    CamUtils.notify("I Ran");
  }
}


class scopeAndDo implements ActionListener {
 Application app = Application.getInstance();
 Project p = app.getProject();
 Browser b = p.getBrowser();  
 JFrame frame = new JFrame();
 JDialog dialog = new JDialog(frame,"Select Scope Element",false);
 JPanel panel = new JPanel();
 JButton okButton = new JButton("OK");
 JButton cancelButton = new JButton("Cancel");
 boolean rv = false;

 public doIt() {

   panel.add(okButton);
   panel.add(cancelButton);
   dialog.add(panel);
   okButton.addActionListener(this);
   cancelButton.addActionListener(this);

   dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
   dialog.setSize(200, 80);
   dialog.setVisible(true);
   frame.setAlwaysOnTop(true);
   return rv;
 }

 private Element getSelectedElement() {
   Tree t = b.getActiveTree();
   Node n = t.getSelectedNode();
   Object o = n.getUserObject();
   if (o instanceof Element)
     return (Element)o;
   return null;  
 }
 
 private void doSomething() {
   Element se = getSelectedElement();
   if (se != null) {
     TurnOffStypes rd = new TurnOffStypes(); 
     app.getGUILog().log("executing rd");
     rd.performAction();
   }
   else
     app.getGUILog().log("Selection is NULL");
 }

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

TurnOffStypes.CamUtils = CamUtils;
TurnOffStypes.FindUtils = FindUtils;

scopeAndDo sAD = new scopeAndDo();
sAD.doIt();
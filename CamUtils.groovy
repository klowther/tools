import java.util.ArrayList;
import java.util.List;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.ui.browser.Browser;
import com.nomagic.magicdraw.ui.browser.ContainmentTree;
import com.nomagic.magicdraw.ui.browser.Node;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

/**
 ***********************************************************************
 * UNCLASSIFIED A collection of general Cameo utilities
 * 
 * @author D99510 
 */
public class CamUtils {
  
  /**
   ***********************************************************************
   * UNCLASSIFIED Gets the containment tree for the projects browser
   * 
   * @return - an ContainmentTree for the project browser
   * @author D99510 
   */
  public static ContainmentTree getTree( ) {
    Project project = Application.getInstance().getProject();
    if (project == null)
      return null;
    Browser b = project.getBrowser();
    if (b == null)
      return null;
      
    return b.getContainmentTree();
  }
  
  /**
   ***********************************************************************
   * UNCLASSIFIED Gets the selected elements in the containment tree
   * 
   * @return - an Arraylist with the selected base elements
   * @author D99510 
   */
  public static ArrayList<BaseElement> getSelectedElements() {
    ArrayList<BaseElement> rv = new ArrayList<BaseElement>();
    Project project = Application.getInstance().getProject();
    if (project == null)
      return rv;
    Browser b = project.getBrowser();
    if (b == null)
      return rv;
      
    ContainmentTree contTree = b.getContainmentTree();
    
    if (contTree == null)
      return rv;
    
    Node[] selectedNodes = contTree.getSelectedNodes();
    for (Node selectedNode : selectedNodes) {
      Object userObject = selectedNode.getUserObject();
      if (userObject instanceof BaseElement) {
        BaseElement be = (BaseElement) userObject;
        rv.add(be);
      }
    }
    return rv;
  }
  

  /**
   ***********************************************************************
   * UNCLASSIFIED Calls getHumanName but strips the preceding type.
   * 
   * @param e - the element to get the type stripped human name
   * @return - the name portion only of getHumanName
   * @author D99510 
   */
  public static String humanName(Element e) {
    if (e == null)
      return null;
    String name = e.getHumanName();
    if (name == null)
      return null;
    String type = e.getHumanType();
    if (type == null)
      return null;
    
    return name.substring(type.length()).trim();
  }

  /**
   ***********************************************************************
   * UNCLASSIFIED Gets the applied stereotype using null for the profile.
   * 
   * @param e - the element to get the type stripped human name
   * @return - the stereotype
   * @author D99510 
   */
  public static Stereotype appliedStereotype(Element e) {

    Project project = Application.getInstance().getProject();
    if (project == null)
      return null;

    String htype = e.getHumanType().trim();
    if (htype == null)
      return null;

    Stereotype s = StereotypesHelper.getStereotype(project, htype, (Profile)null);
    if (s == null)
      return null;
    return s;
  }

  /**
   * *********************************************************************
   * setTagVal - sets the tag value from the stereotype
   * 
   * @param elem
   * @param st
   * @param tagName
   * @author D99510 
   */
  public static void setTagVal(Element elem, Stereotype st, String tagName, Object value) {
    StereotypesHelper.setStereotypePropertyValue(elem, st, tagName, value, true);
  }

  /**
   * *********************************************************************
   * setTagVal - sets the tag value from the stereotype
   * 
   * @param elem
   * @param st
   * @param tagName
   * @param append
   * @author D99510 
   */
  public static void setTagVal(Element elem, Stereotype st, String tagName, Object value, boolean append) {
    StereotypesHelper.setStereotypePropertyValue(elem, st, tagName, value, append);
  }

  /**
   * *********************************************************************
   * getTagVal - gets the first tag value from the stereotype
   * 
   * @param elem the element
   * @param st the stereotype
   * @param tagName the tagName
   * @return the tagVal
   * @author D99510 
   */
  public static Object getTagVal(Element elem, Stereotype st, String tagName) {
    if (st == null)
      return null;
    List<?> l = StereotypesHelper.getStereotypePropertyValue(elem, st, tagName);
    if (l.size() == 0)
      return null;
    return l.get(0);
  }

  /**
   * *********************************************************************
   * getTagVal - gets the first tag value from the stereotype
   * 
   * @param elem the element
   * @param sName the stereotype name
   * @param tagName the tagName
   * @return the tagVal
   * @author D99510 
   */
  public static Object getTagVal(Element elem, String sName, String tagName) {
    Project project = Application.getInstance().getProject();
    Stereotype st = StereotypesHelper.getStereotype(project,sName,(Profile)null);
    return getTagVal(elem,st,tagName);
  }
  
  /**
   * *********************************************************************
   * getTagVals - gets the tag values from the stereotype
   * 
   * @param elem - the element
   * @param st - the applied stereotype
   * @param tagName - the name of the tag
   * @return - the values associated with the tagName
   * @author D99510 
   */
  public static List<?> getTagVals(Element elem, Stereotype st, String tagName) {
    if (st == null)
      return null;
    return StereotypesHelper.getStereotypePropertyValue(elem, st, tagName);
  }

  /**
   * *********************************************************************
   * getTagVals - gets the tag values from the stereotype
   * 
   * @param elem - the element
   * @param sNmae - the applied stereotype name
   * @param tagName - the name of the tag
   * @return - the values associated with the tagName
   * @author D99510 
   */
  public static List<?> getTagVals(Element elem, String sName, String tagName) {
    Project project = Application.getInstance().getProject();
    Stereotype st = StereotypesHelper.getStereotype(project,sName,(Profile)null);
    return getTagVals(elem,st,tagName);
  }


  /**
   * *********************************************************************
   * getTagVal - gets the tag value from the stereotype
   * 
   * @param sType the stereotype
   * @return a list of the Tag Names for the sName
   * @author D99510 
   */
  public static List<String> getTagNames(Stereotype sType) {
    ArrayList<String> tagNames = new ArrayList<String>();

    List<?> tags = sType.getAttribute();
    for (Object ot : tags) {
      Property p = (Property)ot;
      String name = humanName(p);
      if (name.equals("base_Element"))
        continue;
      tagNames.add(name);
    }      
    return tagNames;
  }
  
  /**
   * *********************************************************************
   * getTagVal - gets the tag value from the stereotype
   * 
   * @param sName
   * @return a list of the Tag Names for the sName
   * @author D99510 
   */
  public static List<String> getTagNames(String sName) {
    Project project = Application.getInstance().getProject();
    Stereotype sType = StereotypesHelper.getStereotype(project,sName,(Profile)null);
    return getTagNames(sType);
  }
  
  /**
   * *********************************************************************
   * UNCLASSIFIED - Checks if the name is a stereotype
   * 
   * @param e
   * @param sName
   * @return
   * @author D99510 
   */
  public static Stereotype isStereo(Element e, String sName) {
    List<Stereotype> sList = StereotypesHelper.getStereotypes(e);
    for (Stereotype s : sList) {
      String sn = s.getName();
      if (sn != null && sn.equals(sName)) {
        return s;
      }
    }
    return null;
  }
  
  /**
   * *********************************************************************
   * UNCLASSIFIED - Puts a message to cameo's notification window
   * 
   * @param s the string
   * @author D99510 
   */
  public static void notify(String s) {
    Application.getInstance().getGUILog().log(s);
  }
}

CamUtils c = new CamUtils();
c
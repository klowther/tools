import com.nomagic.magicdraw.uml.Finder;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import java.util.ArrayList;
import java.util.Collection;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;

/**
 ***********************************************************************
 * UNCLASSIFIED A collection of static find routines
 * 
 * @author D99510
 */
public class FindUtils {

  static Finder.ByScopeFinder f = Finder.byScope();
  static Finder.ByNameFinder nf = Finder.byName();
  static Finder.ByQualifiedNameFinder qf = Finder.byQualifiedName();

  /**
   ***********************************************************************
   * UNCLASSIFIED Finds all elements under the parent
   *
   * @param parent  - the element to search under
   * @param recurse - not used (leftover)
   * @author D99510
   */
  static public Collection<Element> findElements(Element parent, boolean recurse) {
    return findElementsnew(parent, recurse);
  }

  /**
   ***********************************************************************
   * UNCLASSIFIED Finds all elements under the parent using the 
   * scope finder. Scope finder is recursive 
   *
   * @param parent  - the element to search under
   * @param recurse - not used (leftover)
   * @author D99510
   */
  static public Collection<Element> findElementsnew(Element parent, boolean recurse) {
    Collection<Element> c;
    if (parent == null)
      return null;
    if (parent instanceof Project) {
      Project p = (Project)parent;
      c = f.find(p);
    } else {
      c = f.find(parent);
    }
    return c;
  }

  /**
   * ***********************************************************************************
   * Unclassified - Finds elements by the Human Type, slower that built in finds but
   * sometimes more useful.
   * 
   * @param start - the starting tree element
   * @param type - the type
   * @param recurse - ignored always recurses leftover
   * @return - the list of Elements
   * @author D99510
   */
  public static ArrayList<Element> findByHType(Element start, String type, boolean recurse) {
    // JOptionPane.showMessageDialog(null, "Im here findByType");

    Collection<Element> elements = findElementsnew(start, recurse);
    ArrayList<Element> rv = new ArrayList<Element>();
    if (elements == null)
      return rv;

    for (Element elem : elements)
      if (elem.getHumanType().equals(type))
        if (!rv.contains(elem))
          rv.add(elem);
    return rv;
  }

  /**
   * ***********************************************************************************
   * Unclassified - Finds elements by the Human name
   * 
   * @param path - the fully qualified path string token = "::"
   * @return the found element null otherwise
   * @author D99510
   */
  public static Element FindByQualifiedPath(String path) {
    if (path == null)
      return null;
    Project project = Application.getInstance().getProject();
    if (project == null)
      return null;
    return qf.find(project, path);
  }

  /**
   * ***********************************************************************************
   * Unclassified - Finds elements by the Human name
   * 
   * @param path - the fully qualified path string token = "::"
   * @return the found element null otherwise
   * @author D99510
   */
  public static Element FindByName(Element parent, String name) {
    if (parent == null || name == null)
      return null;
    return nf.find(parent, Element.class, name);
  }
}

FindUtils fu = new FindUtils();
fu
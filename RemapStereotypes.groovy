import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.BoxLayout;
import javax.swing.JButton;

import com.nomagic.magicdraw.core.Application;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.ui.browser.Browser;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdbasicbehaviors.Behavior;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

import utils.Logger;
import utils.PasteTable;

import com.nomagic.magicdraw.ui.browser.Node;
import com.nomagic.magicdraw.ui.browser.Tree;

class RemapStereotypesAction{
	Element[] selectedElements;
	Application application;
	RemapStereotypesAction(Element[] se, Application app){
		selectedElements = se;
		application = app;
	}

	/**
	 ***********************************************************************
	 *
	 */
	class StereotypeInfo {
		public String stereotypeName;
		public Vector<String> tagNames;
		public Vector<String> tagTypes;
		public Vector<String> tagValues;
		public Vector<Object> otherValues;
	};
	/**
	 ***********************************************************************
	 *
	 */
	class MainDlg extends JFrame implements ActionListener {
		private static final long serialVersionUID = 1L;
		Stereotype stype = null;
		JButton okButton = new JButton("OK");
		PasteTable pt;
		PasteTable pt2;
		Project project = Application.getInstance().getProject();
		Collection<Stereotype>	allStypes = StereotypesHelper.getAllStereotypes(project);
		Vector<Stereotype> selectedStereotypes = new Vector<Stereotype>();

		Stereotype getStereotype(String name) {
			for(Stereotype s : selectedStereotypes) {
				if (s.getName().equals(name)) {
					return s;
				}
			}
			return null;
		}
		/**
		 ***********************************************************************
		 *
		 */
		void remapStereotypes() {
			for (int i = 0; i < selectedElements.length; i++) {
				
				if(!selectedElements[i].hasAppliedStereotype()) {
					continue;
				}
				
				Element e = selectedElements[i];
				Collection<Element> elements = new Vector<Element>();
				elements.add(selectedElements[i]);
				
				Collection<Stereotype> stereoTypes = StereotypesHelper.getAllAssignedStereotypes(elements);
				for (Stereotype st : stereoTypes) {
					Stereotype localST = getStereotype(st.getName());

					if(st.equals(localST) || localST == null) {
						continue;
					}

					ArrayList<String> tagNames = getTagNames(st);
					ArrayList<String> tagTypes = getTagTypes(st);

					StereotypeInfo stStringInfo = new StereotypeInfo();
					stStringInfo.tagNames = new Vector<String>();
					stStringInfo.tagTypes = new Vector<String>();
					stStringInfo.tagValues = new Vector<String>();
					stStringInfo.otherValues = new Vector<Object>();
					stStringInfo.stereotypeName = st.getName();

					StereotypeInfo stObjectInfo = new StereotypeInfo();
					stObjectInfo.tagNames = new Vector<String>();
					stObjectInfo.tagTypes = new Vector<String>();
					stObjectInfo.tagValues = new Vector<String>();
					stObjectInfo.otherValues = new Vector<Object>();
					stObjectInfo.stereotypeName = st.getName();

					for(int j = 0; j < tagNames.size(); j++) {
						if(tagTypes.get(j).toString().equals("String")) {
							List<String> tagValue = StereotypesHelper.getStereotypePropertyValue(e, st, tagNames.get(j).toString());
							
							stStringInfo.tagNames.add(tagNames.get(j).toString());
							stStringInfo.tagTypes.add(tagTypes.get(j).toString());
							stStringInfo.tagValues.add(tagValue.toString());
						}
						else {
							List<Object> tagValue = StereotypesHelper.getStereotypePropertyValue(e, st, tagNames.get(j).toString());
							for(Object val : tagValue) {
								
								stObjectInfo.otherValues.add(val);
								stObjectInfo.tagNames.add(tagNames.get(j).toString());
								stObjectInfo.tagTypes.add(tagTypes.get(j).toString());
							}
						}
					}
					remapStereotype(e, st ,localST, stStringInfo, stObjectInfo);

				}
			}
		}

		/**
		 ***********************************************************************
		 *
		 */
		void remapStereotype(Element e, Stereotype olds , Stereotype news, StereotypeInfo stString, StereotypeInfo stObject) {
			StereotypesHelper.removeStereotype(e, olds);
			StereotypesHelper.addStereotype(e, news);

			for(int i = 0; i < stString.tagNames.size(); i++) {
				StereotypesHelper.setStereotypePropertyValue(e, news, stString.tagNames.get(i), stString.tagValues.get(i).substring(1, stString.tagValues.get(i).length() - 1), true);
			}

			for(int j = 0; j < stObject.tagNames.size(); j++) {
				StereotypesHelper.setStereotypePropertyValue(e, news, stObject.tagNames.get(j), stObject.otherValues.get(j));
			}

		}


		/**
		 ***********************************************************************
		 *
		 */
		void getSelectedStereotypes() {
			int i = 0;
			for(Element node : selectedElements) {
				if (node == null)
					continue;
				Object o = (Object)node;
				if ((o == null) || !(o instanceof Stereotype))
					continue;

				node.getObjectParent();
				Stereotype ster = (Stereotype)node;
				selectedStereotypes.add(ster);

				pt2.setValueAt(ster.getName(), i, 0);
				pt2.setValueAt(ster.getQualifiedName(), i, 1);
				i++;
			}

		}
		
		void getSelectedElement() {
			int i = 0;
			for(Element node : selectedElements) {
				if (node == null)
					continue;
				Object o = (Object)node;
				if ((o == null) || (o instanceof Stereotype))
					continue;
				
				if(!(o instanceof Stereotype)) {
					String elementName = node.getHumanName().trim();
					String type = node.getHumanType();
					
					pt.setValueAt(elementName.substring(type.length()).trim(), i, 0);
					
					if(!node.hasAppliedStereotype()) {
						continue;
					}
					Collection<Element> elements = new Vector<Element>();
					elements.add(node);
					
					Collection<Stereotype> stereoTypes = StereotypesHelper.getAllAssignedStereotypes(elements);
					for (Stereotype st : stereoTypes) {
						pt.setValueAt(st.getName(), i, 1);
						pt.setValueAt(st.getQualifiedName(), i, 2);
						if(i < stereoTypes.size() - 1) {
							DefaultTableModel model = (DefaultTableModel) pt.getModel();
							model.addRow(new Object[]{"", "", ""});
						}
						i++;
					}
					
					i++;
				}
				else {
					
				}

			}
		}

		/**
		 ***********************************************************************
		 *
		 */
		ArrayList<String> getTagNames(Stereotype ster) {
			ArrayList<String> tagNames = new ArrayList<String>();

			List<?> tags = ster.getAttribute();
			for (Object ot : tags) {
				Property p = (Property)ot;
				if(p.getType() != null) {
					String name = p.getName();
					if (name.equals("base_Element"))
						continue;
					tagNames.add(name);
				}
			}			
			return tagNames;
		}

		/**
		 ***********************************************************************
		 *
		 */
		ArrayList<String> getTagTypes(Stereotype ster) {
			ArrayList<String> tagTypes = new ArrayList<String>();

			List<?> tags = ster.getAttribute();
			for (Object ot : tags) {
				Property p = (Property)ot;
				
				if(p.getType() != null) {
					String type = p.getType().getName();

					if(!type.equals("String")) {
						Logger.templog("Adding type: " + p.getType().getQualifiedName());
						type = p.getType().getQualifiedName();
					}
					tagTypes.add(type);
				}
			}			
			return tagTypes;
		}

		/**
		 ***********************************************************************
		 *
		 */
		@Override
		public void actionPerformed(ActionEvent event) {
			if (event.getSource() == okButton){
				remapStereotypes();
				getSelectedElement();
			}
		}
		/**
		 ***********************************************************************
		 *
		 */
		public MainDlg() {
			super();
			this.setTitle("Copy Stereotypes");
			this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			JPanel containerPanel = new JPanel();
			
			JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
			
			JLabel tableLabel1 = new JLabel("Selected Element:");
			tableLabel1.setAlignmentX(Component.CENTER_ALIGNMENT);
			JLabel tableLabel2 = new JLabel("Selected Stereotype:");
			tableLabel2.setAlignmentX(Component.CENTER_ALIGNMENT);
			
			pt = new PasteTable(1,3);
			pt.getColumnModel().getColumn(0).setHeaderValue("Selected Element");
			pt.getColumnModel().getColumn(1).setHeaderValue("Stereotype Name");
			pt.getColumnModel().getColumn(2).setHeaderValue("Stereotype Location");
			JScrollPane jsp = new JScrollPane(pt, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			
			pt2 = new PasteTable(1,2);
			pt2.getColumnModel().getColumn(0).setHeaderValue("Selected Stereotype");
			pt2.getColumnModel().getColumn(1).setHeaderValue("Stereotype Location");

			JScrollPane jsp2 = new JScrollPane(pt2, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			
			getSelectedElement();
			getSelectedStereotypes();

			jsp.setPreferredSize(pt.getPreferredSize());
			jsp2.setPreferredSize(pt2.getPreferredSize());
			
			pt.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
			pt2.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

			okButton.addActionListener(this);
			mainPanel.add(tableLabel1);
			mainPanel.add(jsp);
			mainPanel.add(tableLabel2);
			mainPanel.add(jsp2);

			JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			buttonPanel.add(okButton);
			
			containerPanel.add(mainPanel);
			containerPanel.add(buttonPanel);
			
			containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.Y_AXIS));
			this.add(containerPanel);
			
			this.setAlwaysOnTop(true);
			this.pack();
			this.setVisible(true);

		}
	}
	/**
	 * **********************************************************************************************
	 * 
	 */
	public void doIt() {
		MainDlg md = new MainDlg();
		md.setVisible(true);
		md.pack();
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
		Element[] se = getSelectedElements();
		app.getGUILog().log("Executing OB");
		RemapStereotypesAction csa = new RemapStereotypesAction(se,app);
		csa.doIt();
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
sAD.doIt();
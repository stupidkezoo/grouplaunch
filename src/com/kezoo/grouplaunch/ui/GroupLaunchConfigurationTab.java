package com.kezoo.grouplaunch.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.ILaunchConfiguration;

import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsMessages;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.PlatformUI;
import com.kezoo.grouplaunch.core.*;
import com.kezoo.grouplaunch.ui.UIProps.ButtonType;

public class GroupLaunchConfigurationTab extends AbstractLaunchConfigurationTab implements ButtonGroupEventListener {
	
    private ButtonGroupController buttonGroupController;
    private TreeViewController treeViewController;
	private List<ItemLaunchConfiguration> configurations = new ArrayList<ItemLaunchConfiguration>();

	@Override
	public void createControl(Composite parent) {
	    Composite comp = new Composite(parent, SWT.NONE);
        setControl(comp);
        comp.setLayout(new GridLayout());
        Group group = new Group(comp, SWT.SHADOW_IN);
        group.setLayout(new RowLayout(SWT.HORIZONTAL));
        treeViewController = new TreeViewController(createTreeViewComponent(comp));
        buttonGroupController = new ButtonGroupController(group);
        buttonGroupController.addListener(this);
        buttonGroupController.addListener(treeViewController);
        treeViewController.setContent(configurations);
	}
	
	private TreeViewer createTreeViewComponent(Composite comp) {
		TreeViewer treeViewer = new CheckboxTreeViewer(comp, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		Tree addressTree = treeViewer.getTree();
	
        addressTree.setHeaderVisible(true);
        addressTree.setLinesVisible(true);
        addressTree.setLayoutData(new GridData(GridData.FILL_BOTH));
     
        TreeColumn column0 = new TreeColumn(addressTree, SWT.LEFT);
        column0.setAlignment(SWT.LEFT);
        column0.setText("#");
        column0.setWidth(160);
      TreeColumn column1 = new TreeColumn(addressTree, SWT.LEFT);
      column1.setAlignment(SWT.LEFT);
      column1.setText("Name");
      column1.setWidth(160);
      
      TreeColumn column2 = new TreeColumn(addressTree, SWT.RIGHT);
      column2.setAlignment(SWT.LEFT);
      column2.setText("Exec count");
      column2.setWidth(100);
      TreeColumn column3 = new TreeColumn(addressTree, SWT.RIGHT);
      column3.setAlignment(SWT.LEFT);
      column3.setText("m/w");
      column3.setWidth(35);
      int currentWidth = addressTree.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
//      comp.addControlListener(new ControlAdapter() {
//          @Override
//          public void controlResized(ControlEvent e) {
//             
//              int newWidth = addressTree.getSize().x;
//              if (currentWidth != 0 && newWidth != 0) {
//              double percent = newWidth / (double) currentWidth;
//              for (TreeColumn column : addressTree.getColumns()) {
//                  column.setWidth((int) Math.round(column.getWidth() * percent));
//              }
//          
//              }
//          }
//      });
 
//      m_treeViewer.setContentProvider(new AddressContentProvider());
//      m_treeViewer.setLabelProvider(new TableLabelProvider());
//      List<City> cities = new ArrayList<City>();
//      cities.add(new City());
//      m_treeViewer.setInput(cities);
      treeViewer.expandAll();
      return treeViewer;
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		//nothing
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		// TODO Auto-generated method stub

	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
	    GroupLaunchConfigurationDelegate.storeConfiguration(configuration, configurations);
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "kokoko";
	}

	@Override
	public void onButtonPressed(ButtonType buttonType) {
	    
	}

}

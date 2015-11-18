package com.kezoo.grouplaunch.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;

import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.core.LaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
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
import org.eclipse.ui.activities.WorkbenchActivityHelper;

import com.kezoo.grouplaunch.core.*;
import com.kezoo.grouplaunch.core.ItemProps.Attr;
import com.kezoo.grouplaunch.ui.UIProps.ButtonType;

public class GroupLaunchConfigurationTab extends AbstractLaunchConfigurationTab
        implements ButtonGroupEventListener, TreeViewerEventListener {

    private ButtonGroupController buttonGroupController;
    private TreeViewController treeViewController;
    private List<ItemLaunchConfiguration> configurations = new ArrayList<ItemLaunchConfiguration>();

    @Override
    public void createControl(Composite parent) {
        Composite comp = new Composite(parent, SWT.NONE);
        setControl(comp);
        comp.setLayout(new GridLayout());
        buttonGroupController = new ButtonGroupController(comp);
        treeViewController = new TreeViewController(createTreeViewComponent(comp));      
        buttonGroupController.addListener(this);
        buttonGroupController.addListener(treeViewController);
        treeViewController.setContent(configurations);  
        treeViewController.addListener(this);
        treeViewController.addListener(buttonGroupController);
        setErrorMessage(null);
    }

    private CheckboxTreeViewer createTreeViewComponent(Composite comp) {
        CheckboxTreeViewer treeViewer = new CheckboxTreeViewer(comp, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        Tree addressTree = treeViewer.getTree();

        addressTree.setHeaderVisible(true);
        addressTree.setLinesVisible(true);
        addressTree.setLayoutData(new GridData(GridData.FILL_BOTH));

        TreeColumn column0 = new TreeColumn(addressTree, SWT.LEFT);
        column0.setAlignment(SWT.LEFT);
        column0.setText(UIProps.COLUMN_NAME_0);
        column0.setWidth(200);

        TreeColumn column1 = new TreeColumn(addressTree, SWT.LEFT);
        column1.setAlignment(SWT.LEFT);
        column1.setText(UIProps.COLUMN_NAME_1);
        column1.setWidth(200);

        TreeColumn column2 = new TreeColumn(addressTree, SWT.RIGHT);
        column2.setAlignment(SWT.LEFT);
        column2.setText(UIProps.COLUMN_NAME_2);
        column2.setWidth(110);

        TreeColumn column3 = new TreeColumn(addressTree, SWT.RIGHT);
        column3.setAlignment(SWT.LEFT);
        column3.setText(UIProps.COLUMN_NAME_3);
        column3.setWidth(100);

        int currentWidth = addressTree.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
        // comp.addControlListener(new ControlAdapter() {
        // @Override
        // public void controlResized(ControlEvent e) {
        //
        // int newWidth = addressTree.getSize().x;
        // if (currentWidth != 0 && newWidth != 0) {
        // double percent = newWidth / (double) currentWidth;
        // for (TreeColumn column : addressTree.getColumns()) {
        // column.setWidth((int) Math.round(column.getWidth() * percent));
        // }
        //
        // }
        // }
        // });

        // m_treeViewer.setContentProvider(new AddressContentProvider());
        // m_treeViewer.setLabelProvider(new TableLabelProvider());
        // List<City> cities = new ArrayList<City>();
        // cities.add(new City());
        // m_treeViewer.setInput(cities);
        treeViewer.expandAll();
        return treeViewer;
    }

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        // nothing
    }

    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
        configurations.clear();
        configurations.addAll(GroupLaunchConfigurationDelegate.getConfigurations(configuration));
        treeViewController.refresh();
    }

    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        GroupLaunchConfigurationDelegate.storeConfiguration(configuration, configurations);
    }

    @Override
    public String getName() {
        return UIProps.TAB_NAME;
    }

    @Override
    public void onButtonPressed(ButtonType buttonType) {

    }

    @Override
    public boolean isValid(ILaunchConfiguration launchConfig) {
        if (configurations.isEmpty()) {
            setErrorMessage(UIProps.ERROR_EMPTY_CONFIGURATION);
            return false;
        }
        ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
        for (ItemLaunchConfiguration config : configurations) {
            try {
                ILaunchConfiguration configuration = manager
                        .getLaunchConfiguration(config.getConfig().get(Attr.MEMENTO));
                if (true == DebugUIPlugin.doLaunchConfigurationFiltering(configuration)
                        && !WorkbenchActivityHelper.filterItem(configuration)) {
                    setErrorMessage(null);
                    return true;
                }
            } catch (CoreException e) {
                e.printStackTrace();
            }
        }
        setErrorMessage(UIProps.ERROR_ALL_INVALID);
        return false;
    }

    @Override
    public void onError(String error) {
        setErrorMessage(error);
        updateLaunchConfigurationDialog();
    }

    @Override
    public void onChange() {
        updateLaunchConfigurationDialog();
    }

    @Override
    public void onButtonEnable(ButtonType[] button) {
        // nothing
    }

    @Override
    public void onButtonDisable(ButtonType[] button) {
        // nothing
    }

}

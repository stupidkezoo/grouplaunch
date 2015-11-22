package com.kezoo.grouplaunch.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;

import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.activities.WorkbenchActivityHelper;

import com.kezoo.grouplaunch.core.*;
import com.kezoo.grouplaunch.core.LaunchProps.Attr;
import com.kezoo.grouplaunch.ui.UIProps.ButtonType;
import com.kezoo.grouplaunch.ui.buttons.ButtonGroupController;
import com.kezoo.grouplaunch.ui.buttons.ButtonGroupEventListener;
import com.kezoo.grouplaunch.ui.tree.TreeViewController;
import com.kezoo.grouplaunch.ui.tree.TreeViewerEventListener;

public class GroupLaunchConfigurationTab extends AbstractLaunchConfigurationTab
        implements ButtonGroupEventListener, TreeViewerEventListener {

    private ButtonGroupController buttonGroupController;
    private TreeViewController treeViewController;
    private List<LaunchConfigurationWrapper> configurations = new ArrayList<LaunchConfigurationWrapper>();

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
        setErrorMessage(null);
        if (GroupLaunchConfigurationDelegate.detectOverflow(launchConfig)) {
            setErrorMessage(UIProps.WARNING_CYCLE_LINKS);
            return false;
        }
        if (configurations.isEmpty()) {
            setErrorMessage(UIProps.ERROR_EMPTY_CONFIGURATION);
            return false;
        }
        ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
        boolean hasValidCofiguration = false;
        for (LaunchConfigurationWrapper config : configurations) {
            try {

                ILaunchConfiguration configuration = manager.getLaunchConfiguration(config.get(Attr.MEMENTO));
                //some magic
                boolean valid = DebugUIPlugin.doLaunchConfigurationFiltering(configuration)
                        && !WorkbenchActivityHelper.filterItem(configuration);
                if (valid) {
                    config.put(Attr.INVALID, false + "");
                    hasValidCofiguration = true;
                } else {
                    config.put(Attr.ENABLED, false + "");
                    config.put(Attr.INVALID, true + "");
                }
                treeViewController.refreshOnlyView();
            } catch (CoreException e) {
                e.printStackTrace();
            }
        }
        if (!hasValidCofiguration) {
            setErrorMessage(UIProps.ERROR_ALL_INVALID);
            return false;
        } else {
            return true;
        }
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

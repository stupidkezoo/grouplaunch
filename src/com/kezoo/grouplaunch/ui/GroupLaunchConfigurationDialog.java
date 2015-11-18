package com.kezoo.grouplaunch.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationFilteredTree;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationManager;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchGroupFilter;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PatternFilter;

import com.kezoo.grouplaunch.core.ItemLaunchConfiguration;
import com.kezoo.grouplaunch.core.ItemProps;
import com.kezoo.grouplaunch.core.ItemProps.Attr;
import com.kezoo.grouplaunch.core.ItemProps.LaunchMode;

public class GroupLaunchConfigurationDialog extends TitleAreaDialog implements ISelectionChangedListener {

    private Map<Attr, String> currentConfig = new HashMap<Attr, String>();
    private Map<String, ILaunchGroup> modeToLaunchGroup = new HashMap<String, ILaunchGroup>();
    private List<String> modes = new ArrayList<String>();
    private int currentTabIndex;
    private List<LaunchConfigurationFilteredTree> trees = new ArrayList<LaunchConfigurationFilteredTree>();
    // LaunchConfigurationFilteredTree fTree;
    private ViewerFilter emptyTypeFilter;
    private boolean editMode;
    ILaunchManager manager;
    private List<ITreeSelection> currentSelectedItems = new ArrayList<ITreeSelection>();
    private TabFolder tabFolder;
    
    public GroupLaunchConfigurationDialog(Shell parent, Map<Attr, String> initialConfig) {
        super(parent);
        editMode = true;
        currentConfig = new HashMap<Attr, String>(initialConfig);
        init();
    }
    
    public GroupLaunchConfigurationDialog(Shell parent) {
        super(parent);
        editMode = false;
        initDefaultConfig();
        init();
    }

    private void init() {
        setShellStyle(getShellStyle() | SWT.RESIZE);
        manager = DebugPlugin.getDefault().getLaunchManager();
        LaunchConfigurationManager launchConfigurationManager = DebugUIPlugin.getDefault().getLaunchConfigurationManager();
        for (ILaunchGroup launchGroup : launchConfigurationManager.getLaunchGroups()) {
            String mode = launchGroup.getMode();
            if (!modes.contains(mode)) {
                modes.add(mode);
            }
            if (!modeToLaunchGroup.containsKey(mode)) {
                modeToLaunchGroup.put(mode, launchGroup);
            }
        }
        emptyTypeFilter = new ViewerFilter() {
            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                if (element instanceof ILaunchConfigurationType) {
                    try {
                        ILaunchConfigurationType type = (ILaunchConfigurationType) element;
                        return DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations(type).length > 0;
                    } catch (CoreException e) {
                        return false;
                    }
                } else if (element instanceof ILaunchConfiguration) {
                    return true;
                }
                return true;
            }
        };
    }

    private void initDefaultConfig() {
        currentConfig.put(Attr.LAUNCH_MODE, ItemProps.DEFAULT_LAUNCH_MODE.toString());
        currentConfig.put(Attr.POST_LAUNCH_ACTION, ItemProps.DEFAULT_POST_LAUNCH_ACTION.toString());
        currentConfig.put(Attr.ENABLED, true + "");

    }

    @Override
    protected Control createDialogArea(Composite parent) {
        // create the top level composite for the dialog area todo
        Composite composite = (Composite) super.createDialogArea(parent);
        setTitle(UIProps.ADD_DIALOG_TITLE);
        setMessage(UIProps.ADD_DIALOG_LABEL);
        createTree(composite);
        createCombo(composite);
        if (editMode) {
            selectItem();
        }
        return composite;
    }

    private void createTree(Composite composite) {
        tabFolder = new TabFolder(composite, SWT.NONE);
        tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

        for (String mode : modes) {

            TabItem tab = new TabItem(tabFolder, SWT.NONE);
            tab.setText(mode);

            Composite comp = new Composite(tabFolder, SWT.NONE);
            GridLayout layout = new GridLayout();
            layout.marginHeight = 0;
            layout.marginWidth = 0;
            layout.verticalSpacing = 0;
            layout.horizontalSpacing = 0;
            comp.setLayout(layout);
            comp.setLayoutData(new GridData(GridData.FILL_BOTH));

            LaunchConfigurationFilteredTree fTree = new LaunchConfigurationFilteredTree(comp,
                    SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, new PatternFilter(),
                    modeToLaunchGroup.get(mode), null);
            fTree.createViewControl();
            fTree.getViewer().addSelectionChangedListener(this);
            ViewerFilter[] filters = fTree.getViewer().getFilters();
            for (ViewerFilter viewerFilter : filters) {
                if (viewerFilter instanceof LaunchGroupFilter) {
                    fTree.getViewer().removeFilter(viewerFilter);
                }
            }
            fTree.getViewer().addFilter(emptyTypeFilter);
            trees.add(fTree);
            currentSelectedItems.add(null);
            tab.setControl(comp);
        }
        tabFolder.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent event) {
                TabItem tab = tabFolder.getSelection()[0];
                currentTabIndex = tabFolder.indexOf(tab);
                currentConfig.put(Attr.LAUNCH_MODE, tab.getText());
                validate();
            }
        });
    }

    private void createCombo(Composite parent) {
        final Combo c = new Combo(parent, SWT.READ_ONLY);
        c.setBounds(50, 50, 150, 65);
        c.setText(UIProps.POST_LAUNCH_COMBO);
        String items[] = ItemProps.getPostLaunchNameArray();
        c.setItems(items);
        c.select(1);
        c.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = c.getSelectionIndex();
                if (index != -1) {
                    currentConfig.put(Attr.POST_LAUNCH_ACTION,
                            ItemProps.getPostLaunchEnum(c.getItem(index)).toString());
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // TODO Auto-generated method stub

            }

        });
    }

    private void validate() {
        if (currentSelectedItems.get(currentTabIndex) != null && !currentSelectedItems.get(currentTabIndex).isEmpty()) {
            setErrorMessage(null);
        } else {
            setErrorMessage("vse ploho");
        }
    }

    @Override
    public void selectionChanged(SelectionChangedEvent event) {
        currentSelectedItems.set(currentTabIndex, (ITreeSelection) event.getSelection());
        validate();
    }

    public List<ILaunchConfiguration> getSelectedConfigurations() {
        List<ILaunchConfiguration> result = new ArrayList<ILaunchConfiguration>();
        ITreeSelection selections = currentSelectedItems.get(currentTabIndex);
        if (selections != null) {
            for (Object selection : selections.toList()) {
                if (selection instanceof ILaunchConfiguration) {
                    result.add((ILaunchConfiguration) selection);
                }
            }
        }
        return result;
    }
    
    public Map<Attr, String> getConfig() {
        return currentConfig;
    }
    
    private void selectItem() {
        try {
        ILaunchConfiguration configuration = manager.getLaunchConfiguration(currentConfig.get(Attr.MEMENTO));
        for (int i = 0; i < trees.size(); ++i) {
            if (currentConfig.get(Attr.LAUNCH_MODE).equals(tabFolder.getItem(i).getText())) {
                tabFolder.setSelection(i);
                StructuredSelection selection = new StructuredSelection(configuration);  
                trees.get(i).getViewer().setSelection(selection, true);
            }
        }
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }
}

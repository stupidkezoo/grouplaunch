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
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
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
import com.kezoo.grouplaunch.core.ItemProps.PostLaunchAction;
import com.kezoo.grouplaunch.ui.UIProps.ButtonType;

public class GroupLaunchConfigurationDialog extends TitleAreaDialog implements ISelectionChangedListener {

    private ItemLaunchConfiguration currentConfig;
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
    private Combo postLaunchCombo;
    private Text delayText;

    public GroupLaunchConfigurationDialog(Shell parent, ItemLaunchConfiguration initialConfig) {
        super(parent);
        editMode = true;
        currentConfig = initialConfig;
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
        LaunchConfigurationManager launchConfigurationManager = DebugUIPlugin.getDefault()
                .getLaunchConfigurationManager();
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
        currentConfig = new ItemLaunchConfiguration();
        currentConfig.put(Attr.LAUNCH_MODE, ItemProps.DEFAULT_LAUNCH_MODE.toString());
        currentConfig.put(Attr.POST_LAUNCH_ACTION, ItemProps.DEFAULT_POST_LAUNCH_ACTION.toString());
        currentConfig.put(Attr.ENABLED, true + "");

    }

    @Override
    protected Control createDialogArea(Composite parent) {
        // create the top level composite for the dialog area todo
        Composite composite = (Composite) super.createDialogArea(parent);
        setTitle(UIProps.ADD_DIALOG_TITLE);
        if (editMode) {
            setMessage(UIProps.ADD_DIALOG_LABEL_EDIT);
        } else {
            setMessage(UIProps.ADD_DIALOG_LABEL_ADD);
        }
        createTree(composite);
        createCombo(composite);
        if (editMode) {
            selectItem();
        }
        return composite;
    }
    
    @Override
    protected Control createButtonBar(Composite parent) {
        Control control = super.createButtonBar(parent);
        validate();
        return control;
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
            TreeViewer treeViewer = fTree.getViewer();
            treeViewer.addSelectionChangedListener(this);
            treeViewer.addDoubleClickListener(new IDoubleClickListener() {

                @Override
                public void doubleClick(final DoubleClickEvent event) {
                    final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                    if (selection == null || selection.isEmpty()) {
                        return;
                    }
                    final Object sel = selection.getFirstElement();
                    final ITreeContentProvider provider = (ITreeContentProvider) treeViewer.getContentProvider();
                    if (!provider.hasChildren(sel) && validate()) {
                        setOkAndClose();
                    }
                    if (treeViewer.getExpandedState(sel)) {
                        treeViewer.collapseToLevel(sel, AbstractTreeViewer.ALL_LEVELS);
                    } else {
                        treeViewer.expandToLevel(sel, 1);
                    }
                }
            });
            ViewerFilter[] filters = treeViewer.getFilters();
            for (ViewerFilter viewerFilter : filters) {
                if (viewerFilter instanceof LaunchGroupFilter) {
                    treeViewer.removeFilter(viewerFilter);
                }
            }
            treeViewer.addFilter(emptyTypeFilter);
            treeViewer.expandAll();

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
        Composite comp = new Composite(parent, SWT.NONE);
        comp.setLayout(new GridLayout(4, false));
        comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        new Label(comp, SWT.NULL).setText(UIProps.POST_LAUNCH_COMBO);
        postLaunchCombo = new Combo(comp, SWT.READ_ONLY);
        postLaunchCombo.setBounds(50, 50, 150, 65);
        postLaunchCombo.setItems(ItemProps.getPostLaunchNameArray());
        postLaunchCombo.select(postLaunchCombo.indexOf(
                ItemProps.getPostLaunchName(PostLaunchAction.valueOf(currentConfig.get(Attr.POST_LAUNCH_ACTION))), 0));

        Label delayLabel = new Label(comp, SWT.NULL);
        delayLabel.setText(UIProps.POST_LAUNCH_DELAY_COMBO);
        delayText = new Text(comp, SWT.SINGLE | SWT.BORDER);
        delayText.setSize(10, delayText.getSize().y);
        // is chosen delay action
        if (PostLaunchAction.DELAY == PostLaunchAction.valueOf(currentConfig.get(Attr.POST_LAUNCH_ACTION))) {
            delayLabel.setVisible(true);
            delayText.setVisible(true);
            delayText.setText(currentConfig.get(Attr.DELAY));
        } else {
            delayLabel.setVisible(false);
            delayText.setVisible(false);
        }
        delayText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                currentConfig.put(Attr.DELAY, delayText.getText());
                validate();
            }

        });
        postLaunchCombo.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = postLaunchCombo.getSelectionIndex();
                if (index != -1) {
                    currentConfig.put(Attr.POST_LAUNCH_ACTION,
                            ItemProps.getPostLaunchEnum(postLaunchCombo.getItem(index)).toString());
                    if (postLaunchCombo.getItem(index).equals(ItemProps.getPostLaunchName(PostLaunchAction.DELAY))) {
                        delayLabel.setVisible(true);
                        delayText.setVisible(true);
                    } else {
                        delayLabel.setVisible(false);
                        delayText.setVisible(false);
                    }
                }
                validate();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // TODO Auto-generated method stub

            }

        });

    }

    private boolean validate() {
        setErrorMessage(null);
        validateSelection();
        validateDelayField();
        boolean valid = getErrorMessage() == null;
        Button okButton = getButton(IDialogConstants.OK_ID);
        if (okButton != null) {
            okButton.setEnabled(valid);
        }
        return valid;
    }

    private void validateDelayField() {
        // selected delay action
        if (postLaunchCombo.getItem((postLaunchCombo.getSelectionIndex()))
                .equals(ItemProps.getPostLaunchName(PostLaunchAction.DELAY))) {
            if (delayText.getText() == null || delayText.getText().trim().equals("")) {
                setErrorMessage(UIProps.ERROR_BLANK_DELAY_FIELD);
            } else if (!delayText.getText().matches("-?\\d+(\\.\\d+)?")) {
                setErrorMessage(UIProps.ERROR_BAD_DELAY_FIELD);
            }
        }
    }

    private void validateSelection() {
        ITreeSelection selections = currentSelectedItems.get(currentTabIndex);
        if (selections == null || selections.isEmpty()) {
            setErrorMessage(UIProps.ERROR_DIALOG_NOTHING_SELECTED);
            return;
        }
        for (Object selection : selections.toList()) {
            if (!(selection instanceof ILaunchConfiguration)) {
                setErrorMessage(UIProps.ERROR_DIALOG_BAD_SELECTED);
                break;
            }
        }
        if (editMode) {
            if (selections.size() > 1) {
                setErrorMessage(UIProps.ERROR_DIALOG_TOO_MANY_SELECTED);
            }
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

    public ItemLaunchConfiguration getConfig() {
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

    private void setOkAndClose() {
        this.setReturnCode(OK);
        this.close();
    }
}

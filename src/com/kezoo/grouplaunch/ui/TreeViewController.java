package com.kezoo.grouplaunch.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.launch.internal.MultiLaunchConfigurationDelegate;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TreeItem;

import com.kezoo.grouplaunch.core.GroupLaunchConfigurationDelegate;
import com.kezoo.grouplaunch.core.ItemLaunchConfiguration;
import com.kezoo.grouplaunch.core.ItemProps;
import com.kezoo.grouplaunch.core.ItemProps.Attr;
import com.kezoo.grouplaunch.core.ItemProps.PostLaunchAction;
import com.kezoo.grouplaunch.ui.UIProps.ButtonType;

public class TreeViewController implements ButtonGroupEventListener {

    private CheckboxTreeViewer treeViewer;
    protected List<ItemLaunchConfiguration> configurations;
    private List<TreeViewerEventListener> listeners = new LinkedList<TreeViewerEventListener>();

    public TreeViewController(CheckboxTreeViewer treeViewer) {
        this.treeViewer = treeViewer;
        init();
    }

    public void addListener(TreeViewerEventListener newListener) {
        if (newListener != null) {
            listeners.add(newListener);
        }
    }

    public void setContent(List<ItemLaunchConfiguration> configurations) {
        this.configurations = configurations;
        treeViewer.setInput(configurations);
    }

    private void init() {
        treeViewer.setContentProvider(new ContentProvider());
        treeViewer.setLabelProvider(new LabelProvider());
        treeViewer.addCheckStateListener(new ICheckStateListener() {
            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                ((ItemLaunchConfiguration) event.getElement()).put(Attr.ENABLED, event.getChecked() + "");
                treeViewer.refresh();
            }
        });
        treeViewer.setCheckStateProvider(new ICheckStateProvider() {

            @Override
            public boolean isChecked(Object element) {
                return Boolean.parseBoolean(((ItemLaunchConfiguration) element).get(Attr.ENABLED));
            }

            @Override
            public boolean isGrayed(Object element) {
                return false;// TODO Auto-generated method stub
            }

        });

        treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                updateButtonsState();
            }

        });
    }

    @Override
    public void onButtonPressed(ButtonType buttonType) {
        switch (buttonType) {
        case ADD: {
            GroupLaunchConfigurationDialog addDialog = new GroupLaunchConfigurationDialog(
                    treeViewer.getControl().getShell());
            if (Window.OK == addDialog.open()) {
                List<ILaunchConfiguration> launchConfigurations = addDialog.getSelectedConfigurations();
                int addIndex = configurations.size();
                int[] indexes = new int[launchConfigurations.size()];
                // creating sequence of int starting with index of data end
                for (int i = 0; i < launchConfigurations.size(); ++i) {
                    indexes[i] = addIndex++;
                }
                List<ItemLaunchConfiguration> list = GroupLaunchConfigurationDelegate
                        .populateItemLaunchConfigurations(launchConfigurations, addDialog.getConfig(), indexes);
                configurations.addAll(list);
            }
            // configurations.add(configurations.size(), new
            // ItemLaunchConfiguration());
            break;
        }
        case REMOVE: {
            List<Integer> indexes = getMultiSelectedIndex();
            for (int i = indexes.size() - 1; i >= 0; --i) {
                configurations.remove((int) indexes.get(i));
            }
            break;
        }
        case EDIT: {
            int index = getSingleSelectedIndex();
            GroupLaunchConfigurationDialog editDialog = new GroupLaunchConfigurationDialog(
                    treeViewer.getControl().getShell(), configurations.get(index));
            if (Window.OK == editDialog.open()) {
                ILaunchConfiguration launchConfiguration = editDialog.getSelectedConfigurations().get(0);
                configurations.set(index, GroupLaunchConfigurationDelegate
                        .populateItemLaunchConfiguration(launchConfiguration, editDialog.getConfig(), index));
            }
            break;
        }
        case UP: {
            int index = getSingleSelectedIndex();
            Collections.swap(configurations, index, index - 1);
            break;
        }
        case DOWN: {
            int index = getSingleSelectedIndex();
            Collections.swap(configurations, index, index + 1);
            break;
        }
        case CLEAR: {
            configurations.clear();
            break;
        }
        }
        refresh();
    }

    public void refresh() {
        treeViewer.refresh();
        updateButtonsState();
        notifyOnChange();
    }

    private List<Integer> getMultiSelectedIndex() {
        List<Integer> indexes = new ArrayList<Integer>();
        if (treeViewer.getTree().getSelectionCount() != 0) {
            TreeItem[] selection = treeViewer.getTree().getSelection();
            for (TreeItem item : selection) {
                indexes.add(treeViewer.getTree().indexOf(item));
            }
        }
        return indexes;
    }

    private int getSingleSelectedIndex() {
        if (treeViewer.getTree().getSelectionCount() == 1) {
            TreeItem selection = treeViewer.getTree().getSelection()[0];
            return treeViewer.getTree().indexOf(selection);
        } else {
            return -1;
        }
    }
    
    private void notifyOnChange() {
        for (TreeViewerEventListener listener : listeners) {
            listener.onChange();
        }
    }

    private void updateButtonsState() {
        List<Integer> indexes = getMultiSelectedIndex();
        if (indexes.isEmpty()) {
            disableButtons(ButtonType.DOWN, ButtonType.UP, ButtonType.REMOVE, ButtonType.EDIT);
        } else if (indexes.size() > 1) {
            enableButtons(ButtonType.REMOVE);
            disableButtons(ButtonType.DOWN, ButtonType.UP, ButtonType.EDIT);
        } else if (indexes.size() == 1) {
            enableButtons(ButtonType.DOWN, ButtonType.UP, ButtonType.REMOVE, ButtonType.EDIT);
            if (indexes.get(0) == 0) {
                disableButtons(ButtonType.UP);
            }
            if (indexes.get(0) == configurations.size() - 1) {
                disableButtons(ButtonType.DOWN);
            }
        }

        if (configurations.isEmpty()) {
            disableButtons(ButtonType.CLEAR);
        } else {
            enableButtons(ButtonType.CLEAR);
        }

    }

    private void disableButtons(ButtonType... buttons) {
        for (TreeViewerEventListener listener : listeners) {
            listener.onButtonDisable(buttons);
        }
    }

    private void enableButtons(ButtonType... buttons) {
        for (TreeViewerEventListener listener : listeners) {
            listener.onButtonEnable(buttons);
        }
    }

    static class ContentProvider implements IStructuredContentProvider, ITreeContentProvider {
        protected List<ItemLaunchConfiguration> configurations;

        @Override
        public Object[] getElements(Object inputElement) {
            return getChildren(inputElement);
        }

        @Override
        public void dispose() {
            configurations = null;
        }

        @Override
        @SuppressWarnings("unchecked") // nothing we can do about this
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            if (newInput instanceof List<?>)
                configurations = (List<ItemLaunchConfiguration>) newInput;
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            return (parentElement == configurations) ? configurations.toArray() : null;
        }

        @Override
        public Object getParent(Object element) {
            return (element == configurations) ? null : configurations;
        }

        @Override
        public boolean hasChildren(Object element) {
            return (element == configurations) ? (configurations.size() > 0) : false;
        }
    }

    static class LabelProvider extends BaseLabelProvider implements ITableLabelProvider {
        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            if (columnIndex == 0) {
                return DebugPluginImages.getImage(((ItemLaunchConfiguration) element).get(Attr.ICON_ID));
            } else {
                return null;
            }
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            ItemLaunchConfiguration config = ((ItemLaunchConfiguration) element);
            switch (columnIndex) {
            case 0:
                return config.get(Attr.GROUP);
            case 1:
                return config.get(Attr.NAME);
            case 2:
                return config.get(Attr.LAUNCH_MODE);
            case 3:
                return ItemProps.getPostLaunchName(PostLaunchAction.valueOf(config.get(Attr.POST_LAUNCH_ACTION)));
            default:
                return "";
            }
        }
    }
}

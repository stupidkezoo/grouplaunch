package com.kezoo.grouplaunch.ui.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.TreeItem;

import com.kezoo.grouplaunch.core.GroupLaunchConfigurationDelegate;
import com.kezoo.grouplaunch.core.LaunchConfigurationWrapper;
import com.kezoo.grouplaunch.core.LaunchProps;
import com.kezoo.grouplaunch.core.LaunchProps.Attr;
import com.kezoo.grouplaunch.core.LaunchProps.PostLaunchAction;
import com.kezoo.grouplaunch.ui.GroupLaunchConfigurationDialog;
import com.kezoo.grouplaunch.ui.UIProps.ButtonType;
import com.kezoo.grouplaunch.ui.buttons.ButtonGroupEventListener;

public class TreeViewController implements ButtonGroupEventListener {

    private CheckboxTreeViewer treeViewer;
    protected List<LaunchConfigurationWrapper> configurations;
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

    public void setContent(List<LaunchConfigurationWrapper> configurations) {
        this.configurations = configurations;
        treeViewer.setInput(configurations);
    }

    private void init() {
        treeViewer.setContentProvider(new ContentProvider());
        treeViewer.setLabelProvider(new LabelProvider());
        treeViewer.addCheckStateListener(new ICheckStateListener() {
            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                if (!elementInvalid((LaunchConfigurationWrapper) event.getElement())) {
                    ((LaunchConfigurationWrapper) event.getElement()).put(Attr.ENABLED, event.getChecked() + "");
                }
                refresh();
            }
        });
        treeViewer.setCheckStateProvider(new ICheckStateProvider() {

            @Override
            public boolean isChecked(Object element) {
                if (elementInvalid(element)) {
                    return false;
                }
                return Boolean.parseBoolean(((LaunchConfigurationWrapper) element).get(Attr.ENABLED).trim());
            }

            @Override
            public boolean isGrayed(Object element) {
                if (((LaunchConfigurationWrapper) element).containsKey(Attr.INVALID)) {
                    return Boolean.parseBoolean(((LaunchConfigurationWrapper) element).get(Attr.INVALID).trim());
                }
                return false;
            }

        });

        treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                updateButtonsState();
            }

        });
        treeViewer.addDoubleClickListener(new IDoubleClickListener() {

            @Override
            public void doubleClick(final DoubleClickEvent event) {
                onButtonPressed(ButtonType.EDIT);
            }
        });
    }

    private boolean elementInvalid(Object element) {
        if (((LaunchConfigurationWrapper) element).containsKey(Attr.INVALID)) {
            return Boolean.parseBoolean(((LaunchConfigurationWrapper) element).get(Attr.INVALID).trim());
        } else {
            return false;
        }

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
                List<LaunchConfigurationWrapper> list = GroupLaunchConfigurationDelegate
                        .populateItemLaunchConfigurations(launchConfigurations, addDialog.getConfig(), indexes);
                configurations.addAll(list);
            }
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
            if (index == -1) {
                return;
            }
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

    public void refreshOnlyView() {
        treeViewer.refresh();
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

    private static class ContentProvider implements IStructuredContentProvider, ITreeContentProvider {
        protected List<LaunchConfigurationWrapper> configurations;

        @Override
        public Object[] getElements(Object inputElement) {
            return getChildren(inputElement);
        }

        @Override
        public void dispose() {
            configurations = null;
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            if (newInput instanceof List<?>)
                configurations = (List<LaunchConfigurationWrapper>) newInput;
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

    private static class LabelProvider extends BaseLabelProvider implements ITableLabelProvider {
        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            if (columnIndex == 0) {
                return DebugPluginImages.getImage(((LaunchConfigurationWrapper) element).get(Attr.ICON_ID));
            } else {
                return null;
            }
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            LaunchConfigurationWrapper config = ((LaunchConfigurationWrapper) element);
            switch (columnIndex) {
            case 0:
                return config.get(Attr.GROUP);
            case 1:
                return config.get(Attr.NAME);
            case 2:
                return config.get(Attr.LAUNCH_MODE);
            case 3:
                String columnText = LaunchProps
                        .getPostLaunchName(PostLaunchAction.valueOf(config.get(Attr.POST_LAUNCH_ACTION)));
                if (config.get(Attr.POST_LAUNCH_ACTION).equals(PostLaunchAction.DELAY)) {
                    columnText += " " + config.get(Attr.DELAY) + " seconds";
                }
                return columnText;
            default:
                return "";
            }
        }
    }
}

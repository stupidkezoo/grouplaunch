package com.kezoo.grouplaunch.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.TreeItem;

import com.kezoo.grouplaunch.core.GroupItemLaunchConfiguration;
import com.kezoo.grouplaunch.ui.UIProps.ButtonType;

public class TreeViewController implements ButtonGroupEventListener {
    
    private TreeViewer treeViewer;
    protected ArrayList<GroupItemLaunchConfiguration> configurations;

    public TreeViewController(TreeViewer treeViewer) {
        this.treeViewer = treeViewer;
        init();
    }
    
    public void setContent(ArrayList<GroupItemLaunchConfiguration> configurations) {
        this.configurations = configurations;
        treeViewer.setInput(configurations);
    }
    
    private void init() {
        treeViewer.setContentProvider(new ContentProvider());
        treeViewer.setLabelProvider(new LabelProvider());
    }
    
    @Override
    public void onButtonPressed(ButtonType buttonType) {
        switch(buttonType) {
        case ADD:
            configurations.add(configurations.size(), new GroupItemLaunchConfiguration());
            treeViewer.refresh();
            break;
        case REMOVE:
            List<Integer> indexes = getMultiSelectedIndex();
            for (int i = indexes.size() - 1; i >= 0; --i) {
                configurations.remove((int)indexes.get(i));
            }
            treeViewer.refresh();
            break;
        case EDIT:
            
            break;
        case UP: {
            int index = getSingleSelectedIndex();
            Collections.swap(configurations, index, index - 1);
            treeViewer.refresh();
            break;
        }
        case DOWN: {
            int index = getSingleSelectedIndex();
            Collections.swap(configurations, index, index + 1);
            treeViewer.refresh();
            break;
        }
        case CLEAR: {
            configurations.clear();
            treeViewer.refresh();
            break;
        }
        }
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

        static class ContentProvider implements IStructuredContentProvider, ITreeContentProvider {
            protected List<GroupItemLaunchConfiguration> configurations;

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
                    configurations = (List<GroupItemLaunchConfiguration>) newInput;
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
                
                return null;
            }

            @Override
            public String getColumnText(Object element, int columnIndex) {
              return ((GroupItemLaunchConfiguration)element).getIndex() + "";
            }
        }
}


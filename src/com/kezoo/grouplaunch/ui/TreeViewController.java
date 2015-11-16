package com.kezoo.grouplaunch.ui;

import java.util.List;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;

import com.kezoo.grouplaunch.core.GroupItemLaunchConfiguration;
import com.kezoo.grouplaunch.ui.UIProps.ButtonType;

public class TreeViewController implements ButtonGroupEventListener {
    
    private TreeViewer treeViewer;
    protected List<GroupItemLaunchConfiguration> configurations;

    public TreeViewController(TreeViewer treeViewer) {
        this.treeViewer = treeViewer;
        init();
    }
    
    public void setContent(List<GroupItemLaunchConfiguration> configurations) {
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
            
            break;
        case REMOVE:
            treeViewer.remove(configurations.remove(0));
            break;
        case EDIT:
            
            break;
        case UP:
    
            break;
        case DOWN:
    
            break;
        case CLEAR:
    
            break;
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
              return "kokok";
            }
        }
}


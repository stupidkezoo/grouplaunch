package com.kezoo.grouplaunch.ui;

import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.kezoo.grouplaunch.ui.UIProps.ButtonType;

public class ButtonGroupController implements TreeViewerEventListener {

    private List<ButtonGroupEventListener> listeners = new LinkedList<ButtonGroupEventListener>();

    private Map<ButtonType, ToolItem> buttons = new HashMap<ButtonType, ToolItem>();

    public ButtonGroupController(Composite comp) {
        
        initButtons(comp);
    }

    public void addListener(ButtonGroupEventListener newListener) {
        if (newListener != null) {
            listeners.add(newListener);
        }
    }

    private void initButtons(Composite comp) {
        SelectionListener selectionListener = new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                for (ButtonGroupEventListener listener : listeners) {
                    if (listener != null) {
                        listener.onButtonPressed((ButtonType) e.widget.getData());
                    }
                }
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            };
        };
        
        ToolBar toolBar = new ToolBar(comp, SWT.FLAT | SWT.WRAP | SWT.LEFT);
        for (ButtonType buttonType : UIProps.ButtonType.values()) {
            ToolItem button = new ToolItem(toolBar, SWT.PUSH);
            button.setImage(getImage(UIProps.getIcon(buttonType)));
            button.setData(buttonType);
            button.addSelectionListener(selectionListener);
            button.setToolTipText(UIProps.getTip(buttonType));
            buttons.put(buttonType, button);
        }
    }

    public void enableButton(ButtonType buttonType) {
        buttons.get(buttonType).setEnabled(true);
    }

    public void disableButton(ButtonType buttonType) {
        buttons.get(buttonType).setEnabled(false);
    }

    private Image getImage(String file) {
        URL url = ButtonGroupController.class.getClassLoader().getResource("icons/" + file);

        ImageDescriptor image = ImageDescriptor.createFromURL(url);
        return image.createImage();

    }

    @Override
    public void onError(String error) {
        // nothing
    }

    @Override
    public void onChange() {
        // nothing
    }

    @Override
    public void onButtonEnable(ButtonType[] buttons) {
        for (ButtonType button : buttons) {
            enableButton(button);
        }
    }

    @Override
    public void onButtonDisable(ButtonType[] buttons) {
        for (ButtonType button : buttons) {
            disableButton(button);
        }
    }

}

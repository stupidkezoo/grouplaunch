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

import com.kezoo.grouplaunch.ui.UIProps.ButtonType;

public class ButtonGroupController {
    
    private Group group;

    private List<ButtonGroupEventListener> listeners = new LinkedList<ButtonGroupEventListener>();
    
    private Map<ButtonType, Button> buttons = new HashMap<ButtonType, Button>();
    
    public ButtonGroupController(Group group) {
        this.group = group;
        initButtons();
    }
    
    public void addListener(ButtonGroupEventListener newListener) {
        if (newListener != null) {
            listeners.add(newListener);
        }
    }
    
    private void initButtons() {
        SelectionListener selectionListener = new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                for (ButtonGroupEventListener listener : listeners) {
                    if (listener != null) {
                        listener.onButtonPressed((ButtonType)e.widget.getData());
                    }
                }
              }
          public void widgetDefaultSelected(SelectionEvent e){};
      };
        for (ButtonType buttonType : UIProps.ButtonType.values()) {
            Button button = new Button(group, SWT.PUSH);
            button.setImage(getImage(UIProps.getIcon(buttonType)));
            button.setData(buttonType);
            button.addSelectionListener(selectionListener);
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
        URL url = ButtonGroupController.class.getClassLoader().getResource("icons/"+file);
        
        ImageDescriptor image = ImageDescriptor.createFromURL(url);
        return image.createImage();
        
    } 
   

}

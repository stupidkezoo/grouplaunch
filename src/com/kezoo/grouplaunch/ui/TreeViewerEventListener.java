package com.kezoo.grouplaunch.ui;

import com.kezoo.grouplaunch.ui.UIProps.ButtonType;

public interface TreeViewerEventListener {
    
    public abstract void onError(String error);
    public abstract void onChange();
    public abstract void onButtonEnable(ButtonType[] button);
    public abstract void onButtonDisable(ButtonType[] button);
}

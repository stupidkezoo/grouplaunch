package com.kezoo.grouplaunch.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.eclipse.debug.core.ILaunchConfiguration;

import com.kezoo.grouplaunch.core.ItemProps.Attr;
import com.kezoo.grouplaunch.core.ItemProps.LaunchMode;
import com.kezoo.grouplaunch.core.ItemProps.PostLaunchAction;
import com.kezoo.grouplaunch.ui.UIProps.ButtonType;

public class ItemLaunchConfiguration extends HashMap<Attr, String>{
    
    private String id = UUID.randomUUID().toString();
    

    public ItemLaunchConfiguration() {
        super();
    }
    
    public ItemLaunchConfiguration(Map<Attr, String> params) {
        super(params);
    }
    
    public String getId() {
        return id;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return id.equals(((ItemLaunchConfiguration)obj).getId()) && super.equals(obj);
    }
}

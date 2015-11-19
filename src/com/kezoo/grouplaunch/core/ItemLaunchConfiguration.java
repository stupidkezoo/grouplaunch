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

    public ItemLaunchConfiguration(Map<Attr, String> params) {
        super(params);
    }
}

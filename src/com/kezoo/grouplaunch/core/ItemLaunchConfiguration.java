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

public class ItemLaunchConfiguration {
    
    
    
    public ItemLaunchConfiguration(Map<Attr, String> config) {
        this.config = config;
    }
    
    private Map<Attr, String> config;
    
    private int index = UUID.randomUUID().hashCode();
    private String name;
    private PostLaunchAction postLaunchAction;
    private LaunchMode launchMode;
    private boolean enabled;
    private String memento;
//   getLaunchConfiguration(String memento) 
//    Returns a handle to the launch configuration specified by the given memento.
    
  
    
    public Map<Attr, String> getConfig() {
        return config;
    }
    
    public int getIndex() {
        return index;
    }
    public void setIndex(int index) {
        this.index = index;
    }
    public PostLaunchAction getPostLaunchAction() {
        return postLaunchAction;
    }
    public void setPostLaunchAction(PostLaunchAction postLaunchAction) {
        this.postLaunchAction = postLaunchAction;
    }
    public LaunchMode getLaunchMode() {
        return launchMode;
    }
    public void setLaunchMode(LaunchMode launchMode) {
        this.launchMode = launchMode;
    }
    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getMemento() {
        return memento;
    }
    public void setMemento(String memento) {
        this.memento = memento;
    }
    
    
}

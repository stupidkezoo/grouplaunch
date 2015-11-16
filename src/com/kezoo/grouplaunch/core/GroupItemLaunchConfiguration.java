package com.kezoo.grouplaunch.core;

import java.util.UUID;

import org.eclipse.debug.core.ILaunchConfiguration;

public class GroupItemLaunchConfiguration {
    
    public static enum PostLaunchAction {
        DELAY,
        WAIT_UNTIL_STOP,
        NONE
    }
    public static enum LaunchMode {
        RUN,
        DEBUG,
        PROFILE
    }
    
    private int index = UUID.randomUUID().hashCode();
    private PostLaunchAction postLaunchAction;
    private LaunchMode launchMode;
    private boolean enabled;
    private ILaunchConfiguration innerConfiguration;
    
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
    public ILaunchConfiguration getInnerConfiguration() {
        return innerConfiguration;
    }
    public void setInnerConfiguration(ILaunchConfiguration innerConfiguration) {
        this.innerConfiguration = innerConfiguration;
    }
    
    
}

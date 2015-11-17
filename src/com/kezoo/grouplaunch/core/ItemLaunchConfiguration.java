package com.kezoo.grouplaunch.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.eclipse.debug.core.ILaunchConfiguration;

import com.kezoo.grouplaunch.ui.UIProps.ButtonType;

public class ItemLaunchConfiguration {
    
    public static final String CONFIGURATIONS_MAP = "com.kezoo.grouplaunch.configurations.map";
    public static final String CONFIG_PREFIX = "com.kezoo.grouplaunch.config.prefix_";
    
    public static enum Attr {
        INDEX,
        NAME,
        POST_LAUNCH_ACTION,
        LAUNCH_MODE,
        ENABLED,
        MEMENTO

    }
    
    private static final Map<PostLaunchAction, String> postLaunchNames;
    static {
        postLaunchNames = new HashMap<PostLaunchAction, String>();
        postLaunchNames.put(PostLaunchAction.DELAY, "Delay before next configuration start");
        postLaunchNames.put(PostLaunchAction.WAIT_UNTIL_STOP, "Wait until this configuration stop");
        postLaunchNames.put(PostLaunchAction.NONE, "No additional action");
    }
    
    public static String getPostLaunchName(PostLaunchAction action) {
        return postLaunchNames.get(action);
    }
    
    public static String[] getPostLaunchNameArray() {
        return  postLaunchNames.values().toArray(new String[postLaunchNames.size()]);
    }
    
    public static PostLaunchAction getPostLaunchEnum(String name) {
        for (Entry<PostLaunchAction, String> entry : postLaunchNames.entrySet()) {
            if (name.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    public static enum PostLaunchAction {
        DELAY,
        WAIT_UNTIL_STOP,
        NONE
    }
    public static enum LaunchMode {
        run,
        debug,
        profile
    }
    
    public static final LaunchMode DEFAULT_LAUNCH_MODE = LaunchMode.run;
    public static final PostLaunchAction DEFAULT_POST_LAUNCH_ACTION = PostLaunchAction.NONE;
    
    public ItemLaunchConfiguration(Map<String, String> config) {
        this.config = config;
    }
    
    private Map<String, String> config = new HashMap<String, String>();
    
    private int index = UUID.randomUUID().hashCode();
    private String name;
    private PostLaunchAction postLaunchAction;
    private LaunchMode launchMode;
    private boolean enabled;
    private String memento;
//   String memento=config.getAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO
    
  
    
    public Map<String, String> getConfig() {
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

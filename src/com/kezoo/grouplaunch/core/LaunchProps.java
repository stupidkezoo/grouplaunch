package com.kezoo.grouplaunch.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class LaunchProps {
    // UI
    public static final String ERROR_LABEL = "Group launch configuration error";
    public static final String ERROR_CYCLE_LINK = "Configuration contains cycle links";

    // Keys to store in ILaunchConfiguration
    public static final String CONFIGURATIONS_LIST = "com.kezoo.grouplaunch.configurations.list";
    public static final String CONFIG_PREFIX = "com.kezoo.grouplaunch.config.prefix_";

    // Default config
    public static final LaunchMode DEFAULT_LAUNCH_MODE = LaunchMode.run;
    public static final PostLaunchAction DEFAULT_POST_LAUNCH_ACTION = PostLaunchAction.NONE;

    // Config attributes
    public static enum Attr {
        NAME, POST_LAUNCH_ACTION, LAUNCH_MODE, ENABLED, MEMENTO, ICON_ID, GROUP, DELAY, INVALID
    }

    public static enum LaunchMode {
        run, debug, profile
    }

    // Post launch utils
    public static enum PostLaunchAction {
        DELAY, WAIT_UNTIL_STOP, NONE
    }

    private static final Map<PostLaunchAction, String> postLaunchNames;

    static {
        postLaunchNames = new HashMap<PostLaunchAction, String>();
        postLaunchNames.put(PostLaunchAction.DELAY, "Delay after start");
        postLaunchNames.put(PostLaunchAction.WAIT_UNTIL_STOP, "Wait for stop");
        postLaunchNames.put(PostLaunchAction.NONE, "None");
    }

    public static String getPostLaunchName(PostLaunchAction action) {
        return postLaunchNames.get(action);
    }

    public static String[] getPostLaunchNameArray() {
        String[] result = new String[PostLaunchAction.values().length];
        PostLaunchAction[] values = PostLaunchAction.values();
        for (int i = 0; i < values.length; ++i) {
            result[i] = postLaunchNames.get(values[i]);
        }
        return result;
    }

    public static PostLaunchAction getPostLaunchEnum(String name) {
        for (Entry<PostLaunchAction, String> entry : postLaunchNames.entrySet()) {
            if (name.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

}

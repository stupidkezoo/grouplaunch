package com.kezoo.grouplaunch.ui;

import java.util.HashMap;
import java.util.Map;

public abstract class UIProps {

    private UIProps() {

    }

    public static final String TAB_NAME = "Group";

    public static final String COLUMN_NAME_0 = "Type";
    public static final String COLUMN_NAME_1 = "Name";
    public static final String COLUMN_NAME_2 = "Launch mode";
    public static final String COLUMN_NAME_3 = "Postlaunch action";

    public static final String ADD_DIALOG_TITLE = "Create, manage, and run configurations";
    public static final String ADD_DIALOG_LABEL_ADD = "Add configurations";
    public static final String ADD_DIALOG_LABEL_EDIT = "Edit configuration";
    public static final String POST_LAUNCH_COMBO = "Postlaunch action";
    public static final String POST_LAUNCH_DELAY_COMBO = "Time in seconds";

    public static final String ERROR_EMPTY_CONFIGURATION = "Add at least one launch configuration";
    public static final String ERROR_ALL_INVALID = "Add at least one valid launch configuration";
    public static final String ERROR_BLANK_DELAY_FIELD = "Delay seconds field is blank";
    public static final String ERROR_BAD_DELAY_FIELD = "Delay seconds field contains not number value";
    public static final String ERROR_DIALOG_NOTHING_SELECTED = "Select at least one launch configuration";
    public static final String ERROR_DIALOG_TOO_MANY_SELECTED = "Select only one configuration";
    public static final String ERROR_DIALOG_BAD_SELECTED = "Configuration type selected, select launch configuration";

    public static final String WARNING_CYCLE_LINKS = "Configuration contains cycle links";

    public static enum ButtonType {
        ADD, EDIT, REMOVE, UP, DOWN, CLEAR;
    }

    private static Map<ButtonType, String> ButtonIconMap;

    static {
        ButtonIconMap = new HashMap<ButtonType, String>();
        ButtonIconMap.put(ButtonType.ADD, "add.png");
        ButtonIconMap.put(ButtonType.REMOVE, "remove.png");
        ButtonIconMap.put(ButtonType.EDIT, "edit.png");
        ButtonIconMap.put(ButtonType.UP, "up.png");
        ButtonIconMap.put(ButtonType.DOWN, "down.png");
        ButtonIconMap.put(ButtonType.CLEAR, "clear.png");
    }

    public static String getIcon(ButtonType button) {
        return ButtonIconMap.get(button);
    }

    private static Map<ButtonType, String> ButtonTooltipMap;

    static {
        ButtonTooltipMap = new HashMap<ButtonType, String>();
        ButtonTooltipMap.put(ButtonType.ADD, "Add new launch configuration");
        ButtonTooltipMap.put(ButtonType.REMOVE, "Remove selected launch configurations");
        ButtonTooltipMap.put(ButtonType.EDIT, "Edit selected launch configuration");
        ButtonTooltipMap.put(ButtonType.UP, "Move selected configuration up");
        ButtonTooltipMap.put(ButtonType.DOWN, "Move selected configuration down");
        ButtonTooltipMap.put(ButtonType.CLEAR, "Remove all launch configurations");
    }

    public static String getTip(ButtonType button) {
        return ButtonTooltipMap.get(button);
    }

}

package com.kezoo.grouplaunch.ui;

import java.util.HashMap;
import java.util.Map;

public abstract class UIProps {
    
    private UIProps() {
        
    }
    
    public static enum ButtonType {
        ADD,
        REMOVE,
        EDIT,
        UP,
        DOWN,
        CLEAR
    }
    
    private static Map<ButtonType, String> idToIconMap;
    static {
        idToIconMap = new HashMap<ButtonType, String>();
        idToIconMap.put(ButtonType.ADD, "group.png");
        idToIconMap.put(ButtonType.REMOVE, "group.png");
        idToIconMap.put(ButtonType.EDIT, "group.png");
        idToIconMap.put(ButtonType.UP, "group.png");
        idToIconMap.put(ButtonType.DOWN, "group.png");
        idToIconMap.put(ButtonType.CLEAR, "group.png");
    }
    
    public static String getIcon(ButtonType button) {
        return idToIconMap.get(button);
    }

    
}

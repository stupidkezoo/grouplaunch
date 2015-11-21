package com.kezoo.grouplaunch.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate2;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;

import com.kezoo.grouplaunch.core.ItemProps.Attr;

public class GroupLaunchConfigurationDelegate extends LaunchConfigurationDelegate
        implements ILaunchConfigurationDelegate2 {

    protected static ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();

    @Override
    public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
            throws CoreException {
        ((GroupLaunch) launch).setMonitor(monitor);
        ((GroupLaunch) launch).launch();
    }

    public static void storeConfiguration(ILaunchConfigurationWorkingCopy configuration,
            List<ItemLaunchConfiguration> items) {
        deleteOldConfigurations(configuration);
        List<String> ids = new ArrayList<String>();
        for (int i = 0; i < items.size(); ++i) {
            String itemId = ItemProps.CONFIG_PREFIX + i;
            ids.add(itemId);
            for (Attr attr : Attr.values()) {
                configuration.setAttribute(itemId + attr, items.get(i).get(attr));
            }
        }
        configuration.setAttribute(ItemProps.CONFIGURATIONS_MAP, ids);
    }

    // not actually required
    private static void deleteOldConfigurations(ILaunchConfigurationWorkingCopy configuration) {
        try {
            List<String> ids = new ArrayList<String>();
            ids = configuration.getAttribute(ItemProps.CONFIGURATIONS_MAP, new ArrayList<String>());
            for (String id : ids) {
                for (Attr attr : Attr.values()) {
                    configuration.removeAttribute(id + attr);
                }
            }
            configuration.removeAttribute(ItemProps.CONFIGURATIONS_MAP);
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }

    public static List<ItemLaunchConfiguration> getConfigurations(ILaunchConfiguration configuration) {
        List<ItemLaunchConfiguration> result = new ArrayList<ItemLaunchConfiguration>();
        try {
            List<String> ids = new ArrayList<String>();
            ids = configuration.getAttribute(ItemProps.CONFIGURATIONS_MAP, new ArrayList<String>());
            for (String id : ids) {
                Map<Attr, String> config = new HashMap<Attr, String>();
                for (Attr attr : Attr.values()) {
                    config.put(attr, configuration.getAttribute(id + attr, ""));
                }
                result.add(new ItemLaunchConfiguration(config));
            }
        } catch (CoreException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static ILaunchConfiguration getLaunchConfiguration(ItemLaunchConfiguration itemConfiguration)
            throws CoreException {
        return getLaunchConfiguration(itemConfiguration.get(Attr.MEMENTO));
    }

    public static ILaunchConfiguration getLaunchConfiguration(String memento) throws CoreException {
        return manager.getLaunchConfiguration(memento);

    }

    public static List<ItemLaunchConfiguration> populateItemLaunchConfigurations(
            List<ILaunchConfiguration> configurations, ItemLaunchConfiguration config, int[] indexes) {
        List<ItemLaunchConfiguration> result = new ArrayList<ItemLaunchConfiguration>();
        for (int i = 0; i < configurations.size(); i++) {
            result.add(populateItemLaunchConfiguration(configurations.get(i), config, indexes[i]));
        }
        return result;
    }

    public static ItemLaunchConfiguration populateItemLaunchConfiguration(ILaunchConfiguration launchConfiguration,
            ItemLaunchConfiguration config, int index) {
        try {
            // Map<Attr, String> curConfig = new HashMap<Attr, String>(config);
            config.put(Attr.NAME, launchConfiguration.getName());
            config.put(Attr.MEMENTO, launchConfiguration.getMemento());
            config.put(Attr.ICON_ID, launchConfiguration.getType().getIdentifier());
            config.put(Attr.GROUP, launchConfiguration.getType().getName());
        } catch (CoreException e) {
            e.printStackTrace();
        }
        return config;
    }

    protected static boolean isGroupConfiguration(ILaunchConfiguration launchConfiguration) {
        try {
            return !launchConfiguration.getAttribute(ItemProps.CONFIGURATIONS_MAP, new ArrayList<String>()).isEmpty();
        } catch (CoreException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public ILaunch getLaunch(ILaunchConfiguration configuration, String mode) throws CoreException {
        // TODO Auto-generated method stub
        return new GroupLaunch(configuration);
    }

    @Override
    public boolean buildForLaunch(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
            throws CoreException {
        // TODO Auto-generated method stub
        return false;
    }

    public static boolean detectOverflow(ILaunchConfiguration configuration) {
        Stack<String> visited = new Stack<>();
        try {
            return gotCycle(visited, configuration);
        } catch (Exception e) {
            return false;
        }

    }

    private static boolean gotCycle(Stack<String> visited, ILaunchConfiguration configuration) throws Exception {
        String id = configuration.getMemento();
        if (visited.contains(id)) {
            return true;
        }
        if (isGroupConfiguration(configuration)) {
            visited.push(id);
            for (ItemLaunchConfiguration config : getConfigurations(configuration)) {
                if (Boolean.parseBoolean(config.get(Attr.ENABLED))
                        && gotCycle(visited, getLaunchConfiguration(config)) == true) {
                    return true;
                }
            }
            visited.pop();
        }
        return false;
    }

}

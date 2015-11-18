package com.kezoo.grouplaunch.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;

import com.kezoo.grouplaunch.core.ItemProps.Attr;

public class GroupLaunchConfigurationDelegate implements ILaunchConfigurationDelegate {

    @Override
    public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
            throws CoreException {
        // TODO Auto-generated method stub

    }

    public static void storeConfiguration(ILaunchConfigurationWorkingCopy configuration,
            List<ItemLaunchConfiguration> items) {
        deleteOldConfigurations(configuration);
        List<String> ids = new ArrayList<String>();
        for (int i = 0; i < items.size(); ++i) {
            Map<Attr, String> config = items.get(i).getConfig();
            String itemId = ItemProps.CONFIG_PREFIX + i;
            ids.add(itemId);
            for (Attr attr : Attr.values()) {
                configuration.setAttribute(itemId + attr, config.get(attr));
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

    public static List<ItemLaunchConfiguration> createItemLaunchConfigurations(
            List<ILaunchConfiguration> configurations, Map<Attr, String> config, int[] indexes) {
        List<ItemLaunchConfiguration> result = new ArrayList<ItemLaunchConfiguration>();
        for (int i = 0; i < configurations.size(); i++) {
            result.add(createItemLaunchConfiguration(configurations.get(i), config, indexes[i]));
        }
        return result;
    }

    public static ItemLaunchConfiguration createItemLaunchConfiguration(ILaunchConfiguration launchConfiguration,
            Map<Attr, String> config, int index) {
        try {
            Map<Attr, String> curConfig = new HashMap<Attr, String>(config);
            curConfig.put(Attr.NAME, launchConfiguration.getName());
            curConfig.put(Attr.MEMENTO, launchConfiguration.getMemento());
            curConfig.put(Attr.ICON_ID, launchConfiguration.getType().getIdentifier());
            curConfig.put(Attr.GROUP, launchConfiguration.getType().getName());
            return new ItemLaunchConfiguration(curConfig);
        } catch (CoreException e) {
            e.printStackTrace();
            return null;
        }
    }

}

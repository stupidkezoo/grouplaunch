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
import static com.kezoo.grouplaunch.core.ItemLaunchConfiguration.Attr;

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
        for (ItemLaunchConfiguration item : items) {
            Map<String, String> config = item.getConfig();
            String itemId = ItemLaunchConfiguration.CONFIG_PREFIX + config.get(Attr.INDEX);
            ids.add(itemId);
            for (Attr attr : Attr.values()) {
                configuration.setAttribute(itemId + attr, config.get(attr));
            }
        }
        configuration.setAttribute(ItemLaunchConfiguration.CONFIGURATIONS_MAP, ids);
    }

    // not actually required
    private static void deleteOldConfigurations(ILaunchConfigurationWorkingCopy configuration) {
        try {
            List<String> ids = new ArrayList<String>();
            ids = configuration.getAttribute(ItemLaunchConfiguration.CONFIGURATIONS_MAP, new ArrayList<String>());
            for (String id : ids) {
                for (Attr attr : Attr.values()) {
                    configuration.removeAttribute(id + attr);
                }
            }
            configuration.removeAttribute(ItemLaunchConfiguration.CONFIGURATIONS_MAP);
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }

    public static List<ItemLaunchConfiguration> getConfigurations(ILaunchConfiguration configuration) {
        List<ItemLaunchConfiguration> result = new ArrayList<ItemLaunchConfiguration>();
        try {
            List<String> ids = new ArrayList<String>();
            ids = configuration.getAttribute(ItemLaunchConfiguration.CONFIGURATIONS_MAP, new ArrayList<String>());
            for (String id : ids) {
                Map<String, String> config = new HashMap<String, String>();
                for (Attr attr : Attr.values()) {
                    config.put(attr.toString(), configuration.getAttribute(id + attr, ""));
                }
                result.add(new ItemLaunchConfiguration(config));
            }
        } catch (CoreException e) {
            e.printStackTrace();
        }
        return result;
    }

}

package com.kezoo.grouplaunch.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate2;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.PlatformUI;

import com.kezoo.grouplaunch.core.LaunchProps.Attr;

public class GroupLaunchConfigurationDelegate extends LaunchConfigurationDelegate
        implements ILaunchConfigurationDelegate2 {

    protected static ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();

    @Override
    public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
            throws CoreException {

        // saving old launches when new launch with same type launches
        IPreferenceStore preferenceStore = DebugUIPlugin.getDefault().getPreferenceStore();
        boolean dstore = preferenceStore.getBoolean(IDebugUIConstants.PREF_AUTO_REMOVE_OLD_LAUNCHES);
        preferenceStore.setValue(IDebugUIConstants.PREF_AUTO_REMOVE_OLD_LAUNCHES, false);
        try {
            monitor.beginTask("kokoko", 1000);
            if (detectOverflow(configuration)) {
                PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
                    public void run() {
                        MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                                LaunchProps.ERROR_LABEL, LaunchProps.ERROR_CYCLE_LINK);
                    }
                });
                launch.terminate();
                return;
            }

            ((GroupLaunch) launch).setMonitor(monitor);
            ((GroupLaunch) launch).launch();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            preferenceStore.setValue(IDebugUIConstants.PREF_AUTO_REMOVE_OLD_LAUNCHES, dstore);
            monitor.done();
        }
    }

    public static void storeConfiguration(ILaunchConfigurationWorkingCopy configuration,
            List<LaunchConfigurationWrapper> items) {
        deleteOldConfigurations(configuration);
        List<String> ids = new ArrayList<String>();
        for (int i = 0; i < items.size(); ++i) {
            String itemId = LaunchProps.CONFIG_PREFIX + i;
            ids.add(itemId);
            for (Attr attr : Attr.values()) {
                configuration.setAttribute(itemId + attr, items.get(i).get(attr));
            }
        }
        configuration.setAttribute(LaunchProps.CONFIGURATIONS_LIST, ids);
    }

    // not actually required
    private static void deleteOldConfigurations(ILaunchConfigurationWorkingCopy configuration) {
        try {
            List<String> ids = new ArrayList<String>();
            ids = configuration.getAttribute(LaunchProps.CONFIGURATIONS_LIST, new ArrayList<String>());
            for (String id : ids) {
                for (Attr attr : Attr.values()) {
                    configuration.removeAttribute(id + attr);
                }
            }
            configuration.removeAttribute(LaunchProps.CONFIGURATIONS_LIST);
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }

    public static List<LaunchConfigurationWrapper> getConfigurations(ILaunchConfiguration configuration) {
        List<LaunchConfigurationWrapper> result = new ArrayList<LaunchConfigurationWrapper>();
        try {
            List<String> ids = new ArrayList<String>();
            ids = configuration.getAttribute(LaunchProps.CONFIGURATIONS_LIST, new ArrayList<String>());
            for (String id : ids) {
                Map<Attr, String> config = new HashMap<Attr, String>();
                for (Attr attr : Attr.values()) {
                    config.put(attr, configuration.getAttribute(id + attr, ""));
                }
                result.add(new LaunchConfigurationWrapper(config));
            }
        } catch (CoreException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static ILaunchConfiguration getLaunchConfiguration(LaunchConfigurationWrapper itemConfiguration)
            throws CoreException {
        return getLaunchConfiguration(itemConfiguration.get(Attr.MEMENTO));
    }

    public static ILaunchConfiguration getLaunchConfiguration(String memento) throws CoreException {
        return manager.getLaunchConfiguration(memento);

    }

    public static List<LaunchConfigurationWrapper> populateItemLaunchConfigurations(
            List<ILaunchConfiguration> configurations, LaunchConfigurationWrapper config, int[] indexes) {
        List<LaunchConfigurationWrapper> result = new ArrayList<LaunchConfigurationWrapper>();
        for (int i = 0; i < configurations.size(); i++) {
            result.add(populateItemLaunchConfiguration(configurations.get(i), config, indexes[i]));
        }
        return result;
    }

    public static LaunchConfigurationWrapper populateItemLaunchConfiguration(ILaunchConfiguration launchConfiguration,
            LaunchConfigurationWrapper config, int index) {
        LaunchConfigurationWrapper curConfig = new LaunchConfigurationWrapper(config);
        try {
            curConfig.put(Attr.NAME, launchConfiguration.getName());
            curConfig.put(Attr.MEMENTO, launchConfiguration.getMemento());
            curConfig.put(Attr.ICON_ID, launchConfiguration.getType().getIdentifier());
            curConfig.put(Attr.GROUP, launchConfiguration.getType().getName());
        } catch (CoreException e) {
            e.printStackTrace();
        }
        return curConfig;
    }

    protected static boolean isGroupConfiguration(ILaunchConfiguration launchConfiguration) {
        try {
            return !launchConfiguration.getAttribute(LaunchProps.CONFIGURATIONS_LIST, new ArrayList<String>())
                    .isEmpty();
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
            for (LaunchConfigurationWrapper config : getConfigurations(configuration)) {
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

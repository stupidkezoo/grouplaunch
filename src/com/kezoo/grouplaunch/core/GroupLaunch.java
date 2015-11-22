package com.kezoo.grouplaunch.core;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.Launch;
import com.kezoo.grouplaunch.core.LaunchProps.Attr;
import com.kezoo.grouplaunch.core.LaunchProps.PostLaunchAction;

public class GroupLaunch extends Launch {

    ILaunchConfiguration initialConfig;
    List<LaunchConfigurationWrapper> configurations;
    List<ILaunch> children;
    int lastExecutedConfig = -1;
    IProgressMonitor monitor;

    boolean terminated = false;
    boolean launchedAllChildren = false;

    public GroupLaunch(ILaunchConfiguration launchConfiguration) {
        super(launchConfiguration, "run", null);
        configurations = GroupLaunchConfigurationDelegate.getConfigurations(launchConfiguration);
        children = new ArrayList<>(configurations.size());
        initialConfig = launchConfiguration;

    }

    public void setMonitor(IProgressMonitor monitor) {
        this.monitor = monitor;
    }

    public void launch() {
        monitor.subTask("Launching " + initialConfig.getName());
        while (!terminated && lastExecutedConfig < configurations.size() - 1) {
            lastExecutedConfig++;
            if (!Boolean.parseBoolean(configurations.get(lastExecutedConfig).get(Attr.ENABLED))) {
                continue;
            }
            try {
                ILaunch child = launch(configurations.get(lastExecutedConfig));
                if (child != null) {
                    monitor.subTask("Launching " + child.getLaunchConfiguration().getName());
                    children.add(child);
                    addProcesses(child.getProcesses());
                    processPostLaunchAction();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (children.isEmpty()) {
            try {
                terminate();
            } catch (Exception e) {

            }
        }
        launchedAllChildren = true;
    }

    private ILaunch launch(LaunchConfigurationWrapper configuration) throws CoreException {
        ILaunchConfiguration innerConfiguration = GroupLaunchConfigurationDelegate
                .getLaunchConfiguration(configuration);
        try {
            return innerConfiguration.launch(configuration.get(Attr.LAUNCH_MODE),
                    new SubProgressMonitor(monitor, 1000 / configurations.size()));
        } catch (Exception e) {

        }
        return null;

    }

    private void processPostLaunchAction() {
        LaunchConfigurationWrapper currentConfig = configurations.get(lastExecutedConfig);
        ILaunch currentLaunch = children.get(lastExecutedConfig);
        PostLaunchAction action = PostLaunchAction.valueOf(currentConfig.get(Attr.POST_LAUNCH_ACTION));
        switch (action) {
        case DELAY: {
            int delaySeconds = Integer.parseInt(currentConfig.get(Attr.DELAY));
            monitor.subTask("Waiting for  " + delaySeconds + " seconds");
            try {
                Thread.sleep(1000 * delaySeconds);
            } catch (Exception e) {

            }
            break;
        }
        case WAIT_UNTIL_STOP: {
            monitor.subTask("Waiting for  " + currentLaunch.getLaunchConfiguration().getName() + " stop");
            while (!currentLaunch.isTerminated()) {
                try {
                    Thread.sleep(1000 * 5);
                } catch (Exception e) {

                }
            }
            break;
        }
        default:
        case NONE:
            // nothing
            break;
        }
        monitor.subTask("");
    }

    @Override
    public void terminate() throws DebugException {
        terminated = true;
        super.terminate();
    }

    @Override
    public boolean isTerminated() {
        if (terminated == true) {
            return true;
        }
        if (!launchedAllChildren) {
            return false;
        }
        return super.isTerminated();
    }

    @Override
    public ILaunchConfiguration getLaunchConfiguration() {
        return initialConfig;
    }
}

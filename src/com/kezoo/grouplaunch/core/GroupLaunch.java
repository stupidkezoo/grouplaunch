package com.kezoo.grouplaunch.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.ISourceLocator;

import com.kezoo.grouplaunch.core.ItemProps.Attr;
import com.kezoo.grouplaunch.core.ItemProps.PostLaunchAction;

public class GroupLaunch extends Launch {

    ILaunchConfiguration initialConfig;
    List<ItemLaunchConfiguration> configurations;
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
        // start task
        while (!terminated && lastExecutedConfig < configurations.size() - 1) {
            lastExecutedConfig++;
            if (!Boolean.parseBoolean(configurations.get(lastExecutedConfig).get(Attr.ENABLED))) {
                continue;
            }
            try {
                ILaunch child = launch(configurations.get(lastExecutedConfig));
                if (child != null) {
                    children.add(child);
                    addProcesses(child.getProcesses());
                    processPostLaunchAction();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        launchedAllChildren = true;
        // end task
        // if !terminate
    }

    private ILaunch launch(ItemLaunchConfiguration configuration) throws CoreException {
        ILaunchConfiguration innerConfiguration = GroupLaunchConfigurationDelegate
                .getLaunchConfiguration(configuration);
        try {
            return innerConfiguration.launch(configuration.get(Attr.LAUNCH_MODE),
                    new SubProgressMonitor(monitor, 1000 / 5));
        } catch (Exception e) {

        }
        return null;

    }

    private void processPostLaunchAction() {
        ItemLaunchConfiguration currentConfig = configurations.get(lastExecutedConfig);
        ILaunch currentLaunch = children.get(lastExecutedConfig);
        PostLaunchAction action = PostLaunchAction.valueOf(currentConfig.get(Attr.POST_LAUNCH_ACTION));
        switch (action) {
        case DELAY: {
            try {
                Thread.currentThread().sleep(1000 * Integer.parseInt(currentConfig.get(Attr.DELAY)));
            } catch (Exception e) {

            }
            break;
        }
        case WAIT_UNTIL_STOP: {
            while (!currentLaunch.isTerminated()) {
                try {
                    Thread.currentThread().sleep(1000 * 5);
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
    }

    // @Override
    // public void launchConfigurationRemoved(ILaunchConfiguration
    // configuration) {
    //
    // }

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

    // private boolean isRemote(ILaunchConfiguration configuration) {
    // try {
    // return
    // configuration.getType().getIdentifier().equals("org.eclipse.jdt.launching.remoteJavaApplication");
    // } catch (Exception e) {
    // return false;
    // }
    // }
}

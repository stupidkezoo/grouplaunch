package com.kezoo.grouplaunch.core;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.kezoo.grouplaunch.core.LaunchProps.Attr;

public class LaunchConfigurationWrapper extends HashMap<Attr, String> {

    private String id = UUID.randomUUID().toString();

    public LaunchConfigurationWrapper() {
        super();
    }

    public LaunchConfigurationWrapper(Map<Attr, String> params) {
        super(params);
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) && id.equals(((LaunchConfigurationWrapper) obj).getId());
    }
}

/*
 * Copyright (c) 2009, 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
/*
 * $Id: Device.java 4973 2009-02-02 07:52:47Z lsantha $
 *
 * Copyright (C) 2003-2009 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.jnode.driver;

import java.util.HashMap;
import java.util.Set;

/**
 * A software representation of a hardware device.
 * <p/>
 * Every device is controlled by a Driver. These drivers are found by DeviceToDriverMapper
 * instances.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @see org.jnode.driver.Driver
 * @see org.jnode.driver.DeviceToDriverMapper
 */
public class Device {

    /**
     * My driver
     */
    private Driver driver;
    /**
     * My identifier
     */
    private String id;
    /**
     * Has this device been started?
     */
    private boolean started = false;
    /**
     * The API's implemented by this device
     */
    private final HashMap<Class<? extends DeviceAPI>, DeviceAPI> apis =
        new HashMap<Class<? extends DeviceAPI>, DeviceAPI>();

    /**
     * Create a new instance
     *
     * @param id
     */
    public Device(String id) {
        this.id = id;
    }

    /**
     * Gets the driver of this device.
     *
     * @return My driver, can be null
     */
    public final Driver getDriver() {
        return driver;
    }

    /**
     * Gets the classname of my driver.
     *
     * @return String can be null.
     */
    public final String getDriverClassName() {
        final Driver driver = this.driver;
        if (driver != null) {
            return driver.getClass().getName();
        } else {
            return null;
        }
    }

    /**
     * @param driver
     * @throws DriverException
     * @see org.jnode.driver.Device#setDriver(org.jnode.driver.Driver)
     */
    public void setDriver(Driver driver) throws DriverException {
        try {
            driver.connect(this);
            this.driver = driver;
        } catch (DriverException ex) {
            this.driver = null;
            throw new DriverException("Cannot set driver", ex);
        }
    }

    /**
     * @return The id of this device
     * @see org.jnode.driver.Device#getId()
     */
    public final String getId() {
        return id;
    }

    /**
     * Change the id of this device, only called by devicemanager
     *
     * @param newId
     */
    final void setId(String newId) {
        this.id = newId;
    }

    /**
     * Start this device.
     *
     * @throws DriverException
     */
    final void start() throws DriverException {
        if (driver == null) {
            throw new DriverException("Cannot start without a driver");
        } else if (!started) {
            // Let extensions do their start work
            onStartDevice();
            // Let the driver start me
            driver.startDevice();
            // I'm started
            started = true;
        }
    }

    /**
     * Start this device.
     *
     * @throws DriverException
     */
    final void stop(boolean unsetDriver) throws DriverException {
        if (driver == null) {
            throw new DriverException("Cannot stop without a driver");
        } else if (started) {
            // Let the driver stop me
            driver.stopDevice();
            // Let extensions do their stop work
            onStopDevice();
            // Remove the driver connection if requested
            if (unsetDriver) {
                this.driver = null;
            }
            // I'm stopped now.
            started = false;
        }
    }

    /**
     * Has this device been started?
     *
     * @return boolean
     */
    public final boolean isStarted() {
        return started;
    }

    /**
     * Add an API implementation to the list of API's implemented by this device.
     *
     * @param apiInterface
     * @param apiImplementation
     */
    public final <T extends DeviceAPI> void registerAPI(Class<T> apiInterface, T apiImplementation) {
        if (!apiInterface.isInstance(apiImplementation)) {
            throw new IllegalArgumentException("API implementation does not implement API interface");
        }
        if (!apiInterface.isInterface()) {
            throw new IllegalArgumentException("API interface must be an interface");
        }
        apis.put(apiInterface, apiImplementation);
        final Class[] interfaces = apiInterface.getInterfaces();
        if (interfaces != null) {
            for (Class intf : interfaces) {
                if (DeviceAPI.class.isAssignableFrom(intf)) {
                    if (!apis.containsKey(intf)) {
                        apis.put((Class<? extends DeviceAPI>) intf, apiImplementation);
                    }
                }
            }
        }
    }

    /**
     * Remove an API implementation from the list of API's implemented by this device.
     *
     * @param apiInterface
     */
    public final void unregisterAPI(Class<? extends DeviceAPI> apiInterface) {
        apis.remove(apiInterface);
    }

    /**
     * Does this device implement the given API?
     *
     * @param apiInterface
     * @return boolean
     */
    public final boolean implementsAPI(Class<? extends DeviceAPI> apiInterface) {
        //lookup is classname based to handle multi isolate uscases
        for (Class clazz : apis.keySet()) {
            if (clazz.getName().equals(apiInterface.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets all implemented API's?
     *
     * @return A set of Class instances
     */
    public final Set<Class<? extends DeviceAPI>> implementedAPIs() {
        return apis.keySet();
    }

    /**
     * Gets the implementation of a given API.
     *
     * @param apiInterface
     * @return The api implementation (guaranteed not null)
     * @throws ApiNotFoundException The given api has not been found
     */
    public final <T extends DeviceAPI> T getAPI(Class<T> apiInterface) throws ApiNotFoundException {
        //lookup is classname based to handle multi isolate uscases
        Class apiInterface2 = null;
        for (Class clazz : apis.keySet()) {
            if (clazz.getName().equals(apiInterface.getName())) {
                apiInterface2 = clazz;
                break;
            }
        }
        final T impl = apiInterface.cast(apis.get(apiInterface2));
        if (impl == null) {
            throw new ApiNotFoundException(apiInterface.getName());
        }
        return impl;
    }

    /**
     * This method is called during the start of the device. Just before the call to startDevice of
     * the connected driver.
     *
     * @throws DriverException
     */
    protected void onStartDevice() throws DriverException {
    }

    /**
     * This method is called during the stop of the device. Just after the call to stopDevice of
     * the connected driver.
     *
     * @throws DriverException
     */
    protected void onStopDevice() throws DriverException {
    }

    /**
     * @return The short description
     * @see org.jnode.system.ResourceOwner#getShortDescription()
     */
    public String getShortDescription() {
        return getId();
    }

}

// tag::copyright[]
/*******************************************************************************
 * Copyright (c) 2020 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial implementation
 *******************************************************************************/
// end::copyright[]
package io.openliberty.guides.system;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.reactivestreams.Publisher;

import io.openliberty.guides.models.CpuUsage;
import io.reactivex.rxjava3.core.Flowable;

@ApplicationScoped
public class SystemService {

    private static final OperatingSystemMXBean osMean = 
            ManagementFactory.getOperatingSystemMXBean();
    private static String hostname = null;

    private static String getHostname() {
        if (hostname == null) {
            try {
                return InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                return System.getenv("HOSTNAME");
            }
        }
        return hostname;
    }

    // tag::sendCpuUsage[]
    // tag::publishCpuUsage[]
    @Outgoing("cpuStatus")
    // end::publishCpuUsage[]
    public Publisher<CpuUsage> sendCpuUsage() {
        // tag::flowableInterval[]
        return Flowable.interval(15, TimeUnit.SECONDS)
                .map((interval -> new CpuUsage(getHostname(),
                        new Double(osMean.getSystemLoadAverage()))));
        // end::flowableInterval[]
    }
    // end::sendCpuUsage[]

}
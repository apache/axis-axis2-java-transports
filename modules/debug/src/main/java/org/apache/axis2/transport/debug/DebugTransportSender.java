/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.axis2.transport.debug;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.transport.TransportSender;
import org.apache.axis2.util.Loader;

public class DebugTransportSender extends AbstractHandler implements TransportSender {
    private static final String INVOCATION_KEY = DebugTransportSender.class.getName() + ".INVOCATION";
    
    private final Set<Invocation> invocations = Collections.synchronizedSet(new HashSet<Invocation>());
    private ReportScheduler scheduler;
    private TransportSender target;
    
    public void init(ConfigurationContext confContext, TransportOutDescription transportOut) throws AxisFault {
        Parameter targetClassParameter = transportOut.getParameter("targetClass");
        if (targetClassParameter == null) {
            throw new AxisFault("targetClass parameter is mandatory");
        }
        try {
            target = (TransportSender)Loader.loadClass((String)targetClassParameter.getValue()).newInstance();
        } catch (Exception ex) {
            throw new AxisFault("Unable to create target TransportSender", ex);
        }
        scheduler = new ReportScheduler(this);
        new Thread(scheduler).start();
        target.init(confContext, transportOut);
    }

    public void cleanup(MessageContext msgContext) throws AxisFault {
        Invocation invocation = (Invocation)msgContext.getProperty(INVOCATION_KEY);
        if (invocation == null) {
            System.out.println("TransportSender#cleanup called without corresponding call to TransportSender#invoke!");
        } else if (!invocations.remove(invocation)) {
            System.out.println("TransportSender#cleanup called twice for the same message context.");
        }
        scheduler.scheduleReport();
        target.cleanup(msgContext);
    }

    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
        Invocation invocation = new Invocation(Thread.currentThread().getStackTrace());
        invocations.add(invocation);
        msgContext.setProperty(INVOCATION_KEY, invocation);
        scheduler.scheduleReport();
        return target.invoke(msgContext);
    }

    public void stop() {
        target.stop();
        scheduler.stop();
        generateReport();
    }
    
    void generateReport() {
        synchronized (invocations) {
            int size = invocations.size();
            if (size > 0) {
                System.out.println("There is/are " + size
                        + " pending invocation(s) for which TransportSender#cleanup has not been called yet:");
                for (Invocation invocation : invocations) {
                    System.out.println();
                    for (StackTraceElement ste : invocation.getStackTrace()) {
                        System.out.println(ste);
                    }
                }
            }
        }
    }
}

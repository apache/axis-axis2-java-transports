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

package org.apache.axis2.transport.testkit.axis2.endpoint;

import java.net.URI;
import java.util.UUID;

import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.transport.base.event.TransportError;
import org.apache.axis2.transport.base.event.TransportErrorListener;
import org.apache.axis2.transport.base.event.TransportErrorSource;
import org.apache.axis2.transport.testkit.axis2.AxisServiceConfigurator;
import org.apache.axis2.transport.testkit.channel.Channel;
import org.apache.axis2.transport.testkit.name.Name;

@Name("axis")
public abstract class AxisTestEndpoint implements TransportErrorListener {
    private AxisTestEndpointContext context;
    private TransportErrorSource transportErrorSource;
    private AxisService service;
    
    @SuppressWarnings("unused")
    private void setUp(AxisTestEndpointContext context, Channel channel, AxisServiceConfigurator[] configurators) throws Exception {
        this.context = context;
        
        TransportListener listener = context.getTransportListener();
        if (listener instanceof TransportErrorSource) {
            transportErrorSource = (TransportErrorSource)listener;
            transportErrorSource.addErrorListener(this);
        } else {
            transportErrorSource = null;
        }
        
        String path = new URI(channel.getEndpointReference().getAddress()).getPath();
        String serviceName;
        if (path != null && path.startsWith(Channel.CONTEXT_PATH + "/")) {
            serviceName = path.substring(Channel.CONTEXT_PATH.length()+1);
        } else {
            serviceName = "TestService-" + UUID.randomUUID();
        }
        service = new AxisService(serviceName);
        service.addOperation(createOperation());
        // We want to receive all messages through the same operation:
        service.addParameter(AxisService.SUPPORT_SINGLE_OP, true);
        if (configurators != null) {
            for (AxisServiceConfigurator configurator : configurators) {
                configurator.setupService(service, false);
            }
        }
        context.getAxisConfiguration().addService(service);
    }
    
    @SuppressWarnings("unused")
    private void tearDown() throws Exception {
        if (transportErrorSource != null) {
            transportErrorSource.removeErrorListener(this);
            transportErrorSource = null;
        }
        context.getAxisConfiguration().removeService(service.getName());
        context = null;
        service = null;
    }
    
    public void error(TransportError error) {
        AxisService s = error.getService();
        if (s == null || s == service) {
            onTransportError(error.getException());
        }
    }

    protected abstract AxisOperation createOperation();
    
    protected abstract void onTransportError(Throwable ex);
}

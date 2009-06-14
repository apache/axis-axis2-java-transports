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
package org.apache.axis2.transport.base.datagram;

import java.io.IOException;
import java.net.SocketException;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.transport.base.AbstractTransportListenerEx;
import org.apache.axis2.transport.base.ParamUtils;

public abstract class AbstractDatagramTransportListener<E extends DatagramEndpoint>
        extends AbstractTransportListenerEx<E> {
    
	private DatagramDispatcher<E> dispatcher;
    private String defaultIp;
	
	@Override
    public void init(ConfigurationContext cfgCtx, TransportInDescription transportIn)
            throws AxisFault {
        
        super.init(cfgCtx, transportIn);
        DatagramDispatcherCallback callback = new DatagramDispatcherCallback() {
            public void receive(DatagramEndpoint endpoint, byte[] data, int length) {
                workerPool.execute(new ProcessPacketTask(endpoint, data, length));
            }
        };
        try {
            dispatcher = createDispatcher(callback);
        } catch (IOException ex) {
            throw new AxisFault("Unable to create selector", ex);
        }
        try {
            defaultIp = org.apache.axis2.util.Utils.getIpAddress(cfgCtx.getAxisConfiguration());
        } catch (SocketException ex) {
            throw new AxisFault("Unable to determine the host's IP address", ex);
        }
    }
	
    @Override
    protected void configureAndStartEndpoint(E endpoint, AxisService service) throws AxisFault {
        endpoint.setListener(this);
        endpoint.setContentType(ParamUtils.getRequiredParam(
                service, "transport." + getTransportName() + ".contentType"));
        endpoint.setMetrics(metrics);
        
        try {
            dispatcher.addEndpoint(endpoint);
        } catch (IOException ex) {
            throw new AxisFault("Unable to listen on endpoint "
                    + endpoint.getEndpointReferences(defaultIp)[0], ex);
        }
        if (log.isDebugEnabled()) {
            log.debug("Started listening on endpoint " + endpoint.getEndpointReferences(defaultIp)[0]
                    + " [contentType=" + endpoint.getContentType()
                    + "; service=" + service.getName() + "]");
        }
    }
    
    @Override
    protected void stopEndpoint(E endpoint) {
        try {
            dispatcher.removeEndpoint(endpoint);
        } catch (IOException ex) {
            log.error("I/O exception while stopping listener for service " + endpoint.getServiceName(), ex);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        try {
            dispatcher.stop();
        } catch (IOException ex) {
            log.error("Failed to stop dispatcher", ex);
        }
    }

	protected abstract DatagramDispatcher<E> createDispatcher(DatagramDispatcherCallback callback)
            throws IOException;
}

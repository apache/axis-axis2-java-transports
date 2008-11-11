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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.xml.namespace.QName;

import junit.framework.Assert;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.InOnlyAxisOperation;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.transport.testkit.axis2.MessageContextValidator;
import org.apache.axis2.transport.testkit.endpoint.AsyncEndpoint;
import org.apache.axis2.transport.testkit.message.AxisMessage;
import org.apache.axis2.transport.testkit.message.IncomingMessage;
import org.apache.axis2.transport.testkit.tests.Setup;
import org.apache.axis2.transport.testkit.tests.Transient;

public class AxisAsyncEndpoint extends AxisTestEndpoint implements AsyncEndpoint<AxisMessage>, MessageReceiver {
    private interface Event {
        IncomingMessage<AxisMessage> process() throws Throwable;
    }
    
    private @Transient AxisTestEndpointContext context;
    private @Transient MessageContextValidator[] validators;
    private @Transient BlockingQueue<Event> queue;
    
    @Setup @SuppressWarnings("unused")
    private void setUp(AxisTestEndpointContext context, MessageContextValidator[] validators) {
        this.context = context;
        this.validators = validators;
        queue = new LinkedBlockingQueue<Event>();
    }
    
    @Override
    protected AxisOperation createOperation() {
        AxisOperation operation = new InOnlyAxisOperation(new QName("default"));
        operation.setMessageReceiver(this);
        return operation;
    }

    public void receive(MessageContext messageCtx) throws AxisFault {
        final AxisMessage messageData;
        try {
            Assert.assertTrue(messageCtx.isServerSide());
            
            TransportInDescription transportIn = messageCtx.getTransportIn();
            Assert.assertNotNull("transportIn not set on message context", transportIn);
            Assert.assertEquals(context.getTransportName(), transportIn.getName());
            
            Assert.assertEquals(context.getTransportName(), messageCtx.getIncomingTransportName());
            
            for (MessageContextValidator validator : validators) {
                validator.validate(messageCtx, false);
            }
            messageData = new AxisMessage(messageCtx);
        }
        catch (final Throwable ex) {
            queue.add(new Event() {
                public IncomingMessage<AxisMessage> process() throws Throwable {
                    throw ex;
                }
            });
            return;
        }
        queue.add(new Event() {
            public IncomingMessage<AxisMessage> process() throws Throwable {
                return new IncomingMessage<AxisMessage>(null, messageData);
            }
        });
    }

    @Override
    protected void onTransportError(final Throwable ex) {
        queue.add(new Event() {
            public IncomingMessage<AxisMessage> process() throws Throwable {
                throw ex;
            }
        });
    }
    
    public void clear() throws Exception {
        queue.clear();
    }

    public IncomingMessage<AxisMessage> waitForMessage(int timeout) throws Throwable {
        Event event = queue.poll(timeout, TimeUnit.MILLISECONDS);
        return event == null ? null : event.process();
    }
}

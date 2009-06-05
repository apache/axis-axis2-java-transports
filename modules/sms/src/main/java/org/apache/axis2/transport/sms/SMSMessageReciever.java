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

package org.apache.axis2.transport.sms;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.SessionContext;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.transport.TransportListener;

public class SMSMessageReciever implements TransportListener {

    
    
    private SMSManager smsManeger;
    public void init(ConfigurationContext configurationContext, TransportInDescription transportInDescription) throws AxisFault {

        smsManeger = SMSManager.getSMSManager();
        smsManeger.init(transportInDescription.getParameters() , configurationContext);

    }

    public void start() throws AxisFault {
        if(smsManeger.isInited())
        {
            smsManeger.start();
        }

    }

    public void stop() throws AxisFault {

        if(smsManeger.isInited()) {
            smsManeger.stop();
        }
    }

    public EndpointReference getEPRForService(String s, String s1) throws AxisFault {
        return null;
    }

    public EndpointReference[] getEPRsForService(String s, String s1) throws AxisFault {
        return new EndpointReference[0];
    }

    public SessionContext getSessionContext(MessageContext messageContext) {
        return null;
    }

    public void destroy() {

    }
}

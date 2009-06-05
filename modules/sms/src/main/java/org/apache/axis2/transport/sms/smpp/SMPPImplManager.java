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
package org.apache.axis2.transport.sms.smpp;

import org.apache.axis2.transport.sms.SMSImplManager;
import org.apache.axis2.transport.sms.SMSTransportUtils;
import org.apache.axis2.transport.sms.SMSTransportConstents;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsmpp.session.SMPPSession;
import org.jsmpp.session.BindParameter;
import org.jsmpp.bean.BindType;
import org.jsmpp.bean.TypeOfNumber;
import org.jsmpp.bean.NumberingPlanIndicator;

import java.util.ArrayList;
import java.io.IOException;

public class SMPPImplManager implements SMSImplManager {

     /** the reference to the actual commons logger to be used for log messages */
    protected Log log = LogFactory.getLog(this.getClass());
    private String systemType ="cp";
    private String systemId;
    private String password;
    private String host="127.0.0.1";
    private int port = 2775;
    private volatile boolean stop=true;


    private SMPPSession session;


    public void start() {
        session = new SMPPSession();
        try {
            session.connectAndBind(host, port, new BindParameter(BindType.BIND_RX, systemId,
                        password, systemType , TypeOfNumber.UNKNOWN, NumberingPlanIndicator.UNKNOWN, null));

            SMPPListener listener = new SMPPListener();
            session.setMessageReceiverListener(listener);
            stop = false;
            System.out.println("[Axis2] bind and connect to " + port + "on SMPP Transport");
            
        } catch (IOException e) {
            log.error("Unable to conncet" + e);
        }

    }

    public void stop() {
        log.info("Stopping SMPP Transport...");
        stop = true;
        session.unbindAndClose();

    }

    public void setTransportParamters(ArrayList<Parameter> params) throws AxisFault {

        String tmp = (String) SMSTransportUtils.getParameterValue(params , SMSTransportConstents.SYSTEM_TYPE);

        if (tmp != null) {
            systemType = tmp;
        }

        tmp = (String) SMSTransportUtils.getParameterValue(params , SMSTransportConstents.SYSTEM_ID);

        if (tmp != null) {
            systemId = tmp;
        } else {
            throw new AxisFault("System Id not set");
        }

        tmp = (String) SMSTransportUtils.getParameterValue(params , SMSTransportConstents.PASSWORD);

        if (tmp != null) {
            password = tmp;
        } else {
            throw new AxisFault("password not set");
        }

        tmp = (String) SMSTransportUtils.getParameterValue(params , SMSTransportConstents.HOST);

        if (tmp != null) {
            host = tmp;
        }

        tmp = (String) SMSTransportUtils.getParameterValue(params , SMSTransportConstents.PORT);

        if (tmp != null) {
            port = Integer.parseInt(tmp);
        }
    }
}

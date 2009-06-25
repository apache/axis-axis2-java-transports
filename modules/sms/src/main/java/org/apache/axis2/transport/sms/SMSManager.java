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

import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.description.ParameterInclude;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.sms.smpp.SMPPImplManager;
import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisEngine;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;

/**
 * SMS manager will manage all SMS implementation managers
 * and it will dispatch the Message to the Axis2 Engine
 */
public class SMSManager {

    private SMSImplManager currentImplimentation;
    private ArrayList<Parameter> transportParameters;
    private boolean inited;
    private ConfigurationContext configurationContext;
    private SMSMessageBuilder messageBuilder;
    private SMSMessageFormatter messageFormatter;

     /** the reference to the actual commons logger to be used for log messages */
    protected Log log = LogFactory.getLog(this.getClass());

    private static SMSManager ourInstence;




    private SMSManager() {

    }

    /**
     * @return  singleton Object of the SMSManager
     */
    public static SMSManager getSMSManager() {
        if(ourInstence == null) {
            ourInstence = new SMSManager();
        }
        return ourInstence;
    }

    /**
     * initialize the SMS manager with TransportinDiscription
     * if Manager is already inited it will only set the TransportInDiscription
     * in the current Implimentation manager
     * @param transportInDescription
     * @param configurationContext
     * @throws AxisFault
     */
    public void init(TransportInDescription transportInDescription ,ConfigurationContext  configurationContext) throws
            AxisFault {
        if (!inited) {
            basicInit(transportInDescription , configurationContext);
        }


        Parameter builderClass = transportInDescription.getParameter(SMSTransportConstents.BUILDER_CLASS);

        if(builderClass == null) {
            messageBuilder = new DefaultSMSMessageBuilderImpl();
        } else {
            try {
                messageBuilder = (SMSMessageBuilder)Class.forName((String)builderClass.getValue()).newInstance();

            } catch (Exception e) {
               throw new AxisFault("Error while instentiating class " + builderClass.getValue() , e );
            }
        }
        currentImplimentation.setTransportInDetails(transportInDescription);
        inited = true;
    }

    /**
     * Initialize the SMS Maneger with TransportOutDiscription
     * if the Maneger is already inited  it will set the Transport Outdetails
     * in the Current Implimentation Manage
     * @param transportOutDescription
     * @param configurationContext
     */
    public void init(TransportOutDescription transportOutDescription , ConfigurationContext configurationContext) throws
            AxisFault {
        if(!inited) {
            basicInit(transportOutDescription , configurationContext);
        }

        Parameter formatterClass = transportOutDescription.getParameter(SMSTransportConstents.FORMATTER_CLASS);

        if(formatterClass == null) {
            messageFormatter = new DefaultSMSMessageFormatterImpl();
        }else {
            try {
                messageFormatter = (SMSMessageFormatter)Class.forName((String)formatterClass.getValue()).newInstance();
            } catch (Exception e) {
                throw new AxisFault("Error while instentiating the Class: " +formatterClass.getValue() ,e);
            }
        }
        currentImplimentation.setTransportOutDetails(transportOutDescription);
        inited = true;
    }

    private void basicInit(ParameterInclude transportDescription, ConfigurationContext configurationContext) throws
            AxisFault {
        this.configurationContext = configurationContext;
        Parameter p = transportDescription.getParameter(SMSTransportConstents.IMPLIMENTAION_CLASS);

        if (p == null) {
            currentImplimentation = new SMPPImplManager();
        } else {
            String implClass = (String) p.getValue();

            try {

                currentImplimentation = (SMSImplManager) Class.forName(implClass).newInstance();
            } catch (Exception e) {
                throw new AxisFault("Error while instentiating class " + implClass, e);
            }
        }

    }
    /**
     * Dispatch the SMS message to Axis2 Engine
     * @param message
     * @param sender
     */
    public void dispatchToAxis2(SMSMessage sms)  {
        try {
            MessageContext msgctx = messageBuilder.buildMessaage(sms.getContent() , sms.getSender() ,sms.getReceiver(),
                    configurationContext);
            AxisEngine.receive(msgctx);
        } catch (InvalidMessageFormatException e) {
            log.debug("Invalid message format " + e);

        } catch (AxisFault axisFault) {
            log.debug(axisFault);
        } catch (Throwable e) {
            log.debug("Unknown Exception " , e);
        }

    }

    /**
     * send a SMS form the message comming form the Axis2 Engine
     * @param messageContext that is comming form the Axis2
     */
    public void sendSMS(MessageContext messageContext) {
        try {
            SMSMessage sms = messageFormatter.formatSMS(messageContext);
            currentImplimentation.sendSMS(sms);
        } catch (Exception e) {
            log.error("Error while sending the SMS " , e);
        }

    }

    /**
     * send the information SMS messages other than messages comming form the Axis2 Engine
     * @param sms
     */
    public void sentInfo(SMSMessage sms) {
        currentImplimentation.sendSMS(sms);
    }
    public ArrayList<Parameter> getTransportParameters() {
        return transportParameters;
    }



    public SMSImplManager getCurrentImplimentation() {
        return currentImplimentation;
    }

    public void setCurrentImplimentation(SMSImplManager currentImplimentation) {
        this.currentImplimentation = currentImplimentation;
    }

    public void start() {
        currentImplimentation.start();
    }

    public void stop() {
        currentImplimentation.stop();
    }

    public boolean isInited() {
        return inited;
    }
}

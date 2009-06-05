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

     /** the reference to the actual commons logger to be used for log messages */
    protected Log log = LogFactory.getLog(this.getClass());

    private static SMSManager ourInstence;




    private SMSManager() {

    }

    public static SMSManager getSMSManager() {
        if(ourInstence == null) {
            ourInstence = new SMSManager();
        }

        return ourInstence;
    }


    public void init(ArrayList<Parameter> paramters , ConfigurationContext  configurationContext) throws AxisFault {
        this.configurationContext = configurationContext;
        String implClass = (String)SMSTransportUtils.getParameterValue(paramters ,
                SMSTransportConstents.IMPLIMENTAION_CLASS );

        if(implClass == null ) {
            currentImplimentation = new SMPPImplManager();
        } else {
            try {
                currentImplimentation = (SMSImplManager)Class.forName(implClass).newInstance();
            } catch (Exception e) {
                throw new AxisFault("Error while instentiating class " + implClass , e );
            }
        }

        currentImplimentation.setTransportParamters(paramters);


        String builderClass = (String)SMSTransportUtils.getParameterValue(paramters ,
                SMSTransportConstents.BUILDER_CLASS);

        if(builderClass == null) {
            messageBuilder = new DefaultSMSMessageBuilderImpl();
        } else {
            try {
                messageBuilder = (SMSMessageBuilder)Class.forName(builderClass).newInstance();

            } catch (Exception e) {
               throw new AxisFault("Error while instentiating class " + builderClass , e );
            }
        }
        inited = true;
    }

    /**
     * Dispatch the SMS message to Axis2 Engine
     * @param message
     * @param sender
     */
    public void dispatchToAxis2(String message , String sender)  {
        try {
            MessageContext msgctx = messageBuilder.buildMessaage(message , sender , configurationContext);
            AxisEngine.receive(msgctx);
        } catch (InvalidMessageFormatException e) {
            log.debug("Invalid message format " + e);
            //do some thing like send a errror message
        } catch (AxisFault axisFault) {
            System.out.println("Axis fault " + axisFault);
        } catch (Throwable e) {
            System.out.println("Unknown Exception " + e);
        }

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

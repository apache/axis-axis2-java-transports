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

import junit.framework.TestCase;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.engine.ListenerManager;
import org.apache.axis2.Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.sms.smpp.SimpleSMSC;
import org.apache.axis2.transport.sms.smpp.SMSCMessageNotifier;
import org.apache.axis2.transport.sms.smpp.MessageHolder;

import java.io.File;
import java.io.IOException;

public class SMSTransportTest extends TestCase {

    private SimpleSMSC smsc;
    private SMSCMessageNotifier notifier = SMSCMessageNotifier.getInstence();
    private MessageHolder holder = new MessageHolder();

    public SMSTransportTest() {
        super(SMSTransportTest.class.getName());
    }


    protected void setUp() throws Exception {
        smsc = new SimpleSMSC();
        smsc.startServer();
        int index = 0;
        Thread.sleep(2000);
        while (!smsc.isStarted()) {
            index++;
            Thread.sleep(2000);
            if(index >=10){
                throw new Exception("It Takes more than 10s to start The SMSC");
            }
        }
        //start the Axis2 inscence
        File file = new File(prefixBaseDirectory(Constants.TESTING_REPOSITORY));
        System.out.println(file.getAbsoluteFile());
        if (!file.exists()) {

            throw new Exception("Repository directory does not exist");
        }


        ConfigurationContext confContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                file.getAbsolutePath() + "/repository",
                file.getAbsolutePath() + "/conf/axis2.xml");


        ListenerManager lmaneger = new ListenerManager();

        lmaneger.startSystem(confContext);
        System.out.println(" [Axis2] test Server started on port 2776 ");


    }

    public void testSMPPImplimentaion() throws Exception {
        notifier.addObserver(holder);
        holder.setHaveMessage(false);
        smsc.deliverSMS("0896754535", "0676556367", "SampleService:SampleInOutOperation");

        int index = 0;
        while (!holder.isHaveMessage()) {

            Thread.sleep(1000);
            index++;
            if (index > 10) {
                throw new AxisFault("Server was shutdown as the async response take too long to complete");
            }
        }

        assertEquals("Sucess", holder.getSms());
        holder.setHaveMessage(false);

    }

    public static String prefixBaseDirectory(String path) {
        String baseDir;
        try {
            baseDir = new File(System.getProperty("basedir", ".")).getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return baseDir + "/" + path;
    }


}

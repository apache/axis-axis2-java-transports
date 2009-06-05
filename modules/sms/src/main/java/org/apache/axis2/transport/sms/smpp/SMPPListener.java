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


import org.jsmpp.bean.*;
import org.jsmpp.extra.ProcessRequestException;
import org.jsmpp.session.DataSmResult;
import org.jsmpp.session.MessageReceiverListener;
import org.jsmpp.util.InvalidDeliveryReceiptException;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SMPPListener implements  MessageReceiverListener{
     /** the reference to the actual commons logger to be used for log messages */
    protected Log log = LogFactory.getLog(this.getClass());
   

    public void onAcceptDeliverSm(DeliverSm deliverSm) throws ProcessRequestException {

        if (MessageType.SMSC_DEL_RECEIPT.containedIn(deliverSm.getEsmClass())) {
            // this message is delivery receipt
            try {
                DeliveryReceipt delReceipt = deliverSm.getShortMessageAsDeliveryReceipt();

                // lets cover the id to hex string format
                long id = Long.parseLong(delReceipt.getId()) & 0xffffffff;
                String messageId = Long.toString(id, 16).toUpperCase();

                /*
                 * you can update the status of your submitted message on the
                 * database based on messageId
                 */

                log.debug("Receiving delivery receipt for message '" + messageId +
                        " ' from " + deliverSm.getSourceAddr() + " to " + deliverSm.getDestAddress()
                        + " : " + delReceipt);
            } catch (InvalidDeliveryReceiptException e) {
                log.debug("Failed getting delivery receipt" , e);

            }
        } else {

            try {
                new SMPPDispatcher().dispatch(deliverSm.getSourceAddr() , new String(deliverSm.getShortMessage()));

            } catch (AxisFault axisFault) {
                log.debug("Error while dispatching SMPP message" , axisFault);

            }
            log.debug("Receiving message : " + new String(deliverSm.getShortMessage()));
        }

    }

    public void onAcceptAlertNotification(AlertNotification alertNotification) {

    }

    public DataSmResult onAcceptDataSm(DataSm dataSm) throws ProcessRequestException {
        return null;
    }


}

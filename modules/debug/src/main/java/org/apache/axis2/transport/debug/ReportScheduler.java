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

class ReportScheduler implements Runnable {
    private final DebugTransportSender sender;
    private long nextReport = -1;
    private boolean running;

    public ReportScheduler(DebugTransportSender sender) {
        this.sender = sender;
    }

    public synchronized void run() {
        while (running) {
            try {
                if (nextReport == -1) {
                    wait();
                } else {
                    long timeout = nextReport - System.currentTimeMillis();
                    if (timeout > 0) {
                        wait(timeout);
                    } else {
                        sender.generateReport();
                    }
                }
            } catch (InterruptedException ex) {
                break;
            }
        }
    }

    public synchronized void scheduleReport() {
        nextReport = System.currentTimeMillis() + 10000;
        notifyAll();
    }
    
    public synchronized void stop() {
        running = false;
        notifyAll();
    }
}

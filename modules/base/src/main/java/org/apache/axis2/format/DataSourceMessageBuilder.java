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
package org.apache.axis2.format;

import javax.activation.DataSource;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.context.MessageContext;

/**
 * Message builder able to build messages from {@link DataSource} objects.
 * This interface can be optionally implemented by {@link Builder}
 * implementations that support building messages from {@link DataSource} objects.
 * Since by definition the data from a {@link DataSource} can be read multiple
 * times, this interface can be used by message builders to avoid storing the
 * message content in memory.
 * <p>
 * If a message builder implements this interface and the transport is able to
 * provide the message payload as a data source, then the method defined by this
 * interface should be preferred over the method defined by {@link Builder}.
 * <p>
 * When a message builder is invoked through the basic {@link Builder} interface,
 * it is the responsibility of the transport to close the input stream once the
 * message has been processed, and the builder is not required to consume the input
 * stream immediately. On the other hand, when the builder is invoked through this extension
 * interface, the transport is only responsible for ensuring that the {@link DataSource}
 * remains valid for the whole lifecycle of the message. It is the responsibility of the
 * builder to acquire the input stream and to make sure that it is closed when no longer
 * needed. This important difference is the reason why there is no
 * DataSourceMessageBuilderAdapter class.
 * <p>
 * Implementing this interface helps optimizing message processing with transports
 * that use messaging providers that store messages in memory or on the file system.
 * Examples are JMS and VFS.
 */
public interface DataSourceMessageBuilder extends Builder {
    public OMElement processDocument(DataSource dataSource, String contentType,
            MessageContext messageContext) throws AxisFault;
}

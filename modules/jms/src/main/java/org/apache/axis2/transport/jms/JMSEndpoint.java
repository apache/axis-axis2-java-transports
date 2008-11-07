/*
* Copyright 2004,2005 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.axis2.transport.jms;

import org.apache.axis2.description.AxisService;
import org.apache.axis2.transport.jms.ctype.ContentTypeRuleSet;

/**
 * Class that links an Axis2 service to a JMS destination. Additionally, it contains
 * all the required information to process incoming JMS messages and to inject them
 * into Axis2.
 */
public class JMSEndpoint {
    private AxisService service;
    private String jndiDestinationName;
    private String destinationType;
    private String endpointReference;
    private ContentTypeRuleSet contentTypeRuleSet;

    public AxisService getService() {
        return service;
    }

    public void setService(AxisService service) {
        this.service = service;
    }
    
    public String getServiceName() {
        return service.getName();
    }

    public String getJndiDestinationName() {
        return jndiDestinationName;
    }

    public void setJndiDestinationName(String destinationJNDIName) {
        this.jndiDestinationName = destinationJNDIName;
    }

    public String getDestinationType() {
        return destinationType;
    }

    public void setDestinationType(String destinationType) {
        this.destinationType = destinationType;
    }

    public String getEndpointReference() {
        return endpointReference;
    }

    public void setEndpointReference(String endpointReference) {
        this.endpointReference = endpointReference;
    }

    public ContentTypeRuleSet getContentTypeRuleSet() {
        return contentTypeRuleSet;
    }

    public void setContentTypeRuleSet(ContentTypeRuleSet contentTypeRuleSet) {
        this.contentTypeRuleSet = contentTypeRuleSet;
    }
}

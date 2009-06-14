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

import org.apache.axis2.description.Parameter;
import org.apache.axis2.transport.base.ProtocolEndpoint;
import org.apache.axis2.transport.jms.ctype.ContentTypeRuleSet;
import org.apache.axis2.addressing.EndpointReference;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import javax.naming.Context;

/**
 * Class that links an Axis2 service to a JMS destination. Additionally, it contains
 * all the required information to process incoming JMS messages and to inject them
 * into Axis2.
 */
public class JMSEndpoint extends ProtocolEndpoint {
    private JMSConnectionFactory cf;
    private String jndiDestinationName;
    private int destinationType = JMSConstants.GENERIC;
    private Set<EndpointReference> endpointReferences = new HashSet<EndpointReference>();
    private ContentTypeRuleSet contentTypeRuleSet;
    private ServiceTaskManager serviceTaskManager;

    public String getJndiDestinationName() {
        return jndiDestinationName;
    }

    public void setJndiDestinationName(String destinationJNDIName) {
        this.jndiDestinationName = destinationJNDIName;
    }

    public void setDestinationType(String destinationType) {
        if (JMSConstants.DESTINATION_TYPE_TOPIC.equalsIgnoreCase(destinationType)) {
            this.destinationType = JMSConstants.TOPIC;
        } else if (JMSConstants.DESTINATION_TYPE_QUEUE.equalsIgnoreCase(destinationType)) {
            this.destinationType = JMSConstants.QUEUE;
        } else {
            this.destinationType = JMSConstants.GENERIC;
        }
    }

    @Override
    public EndpointReference[] getEndpointReferences(String ip) {
        return endpointReferences.toArray(new EndpointReference[endpointReferences.size()]);
    }

    public void computeEPRs() {
        List<EndpointReference> eprs = new ArrayList<EndpointReference>();
        for (Object o : getService().getParameters()) {
            Parameter p = (Parameter) o;
            if (JMSConstants.PARAM_PUBLISH_EPR.equals(p.getName()) && p.getValue() instanceof String) {
                if ("legacy".equalsIgnoreCase((String) p.getValue())) {
                    // if "legacy" specified, compute and replace it
                    endpointReferences.add(
                        new EndpointReference(getEPR()));
                } else {
                    endpointReferences.add(new EndpointReference((String) p.getValue()));
                }
            }
        }

        if (eprs.isEmpty()) {
            // if nothing specified, compute and return legacy EPR
            endpointReferences.add(new EndpointReference(getEPR()));
        }
    }

    /**
     * Get the EPR for the given JMS connection factory and destination
     * the form of the URL is
     * jms:/<destination>?[<key>=<value>&]*
     * Credentials Context.SECURITY_PRINCIPAL, Context.SECURITY_CREDENTIALS
     * JMSConstants.PARAM_JMS_USERNAME and JMSConstants.PARAM_JMS_USERNAME are filtered
     *
     * @return the EPR as a String
     */
    private String getEPR() {
        StringBuffer sb = new StringBuffer();

        sb.append(
            JMSConstants.JMS_PREFIX).append(jndiDestinationName);
        sb.append("?").
            append(JMSConstants.PARAM_DEST_TYPE).append("=").append(
            destinationType == JMSConstants.TOPIC ?
                JMSConstants.DESTINATION_TYPE_TOPIC : JMSConstants.DESTINATION_TYPE_QUEUE);

        if (contentTypeRuleSet != null) {
            String contentTypeProperty = contentTypeRuleSet.getDefaultContentTypeProperty();
            if (contentTypeProperty != null) {
                sb.append("&");
                sb.append(JMSConstants.CONTENT_TYPE_PROPERTY_PARAM);
                sb.append("=");
                sb.append(contentTypeProperty);
            }
        }

        for (Map.Entry<String,String> entry : cf.getParameters().entrySet()) {
            if (!Context.SECURITY_PRINCIPAL.equalsIgnoreCase(entry.getKey()) &&
                !Context.SECURITY_CREDENTIALS.equalsIgnoreCase(entry.getKey()) &&
                !JMSConstants.PARAM_JMS_USERNAME.equalsIgnoreCase(entry.getKey()) &&
                !JMSConstants.PARAM_JMS_PASSWORD.equalsIgnoreCase(entry.getKey())) {
                sb.append("&").append(
                    entry.getKey()).append("=").append(entry.getValue());
            }
        }
        return sb.toString();
    }

    public ContentTypeRuleSet getContentTypeRuleSet() {
        return contentTypeRuleSet;
    }

    public void setContentTypeRuleSet(ContentTypeRuleSet contentTypeRuleSet) {
        this.contentTypeRuleSet = contentTypeRuleSet;
    }

    public JMSConnectionFactory getCf() {
        return cf;
    }

    public void setCf(JMSConnectionFactory cf) {
        this.cf = cf;
    }

    public ServiceTaskManager getServiceTaskManager() {
        return serviceTaskManager;
    }

    public void setServiceTaskManager(ServiceTaskManager serviceTaskManager) {
        this.serviceTaskManager = serviceTaskManager;
    }
}

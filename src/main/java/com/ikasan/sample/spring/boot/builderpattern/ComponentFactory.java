/*
 * $Id$
 * $URL$
 *
 * ====================================================================
 * Ikasan Enterprise Integration Platform
 *
 * Distributed under the Modified BSD License.
 * Copyright notice: The copyright for this software and a full listing
 * of individual contributors are as shown in the packaged copyright.txt
 * file.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  - Neither the name of the ORGANIZATION nor the names of its contributors may
 *    be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 */
package com.ikasan.sample.spring.boot.builderpattern;

import org.ikasan.builder.BuilderFactory;
import org.ikasan.spec.component.endpoint.Broker;
import org.ikasan.spec.component.endpoint.Consumer;
import org.ikasan.spec.component.endpoint.Producer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import javax.annotation.Resource;
import javax.jms.DeliveryMode;
import javax.jms.Session;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.jms.listener.DefaultMessageListenerContainer.CACHE_CONSUMER;

/**
 * Sample component factory.
 *
 * @author Ikasan Development Team
 */
@Configuration
@ImportResource( {
        "classpath:ikasan-transaction-pointcut-jms.xml",
        "classpath:ikasan-transaction-pointcut-ikasanMessageListener.xml",
        "classpath:h2-datasource-conf.xml"
} )
public class ComponentFactory
{
    @Resource
    private BuilderFactory builderFactory;

    @Value("${jms.producer.configuredResourceId}")
    String jmsProducerConfiguredResourceId;

    @Value("${jms.provider.url}")
    private String jmsProviderUrl;

    private String destinationName = "jms.topic.test";

    Consumer getJmsConsumer(String clientId)
    {
        Map<String,String> jndi = jndiProperties(false,clientId);

       return builderFactory.getComponentBuilder().jmsConsumer()
                .setConnectionFactoryJndiProperties(jndi)
                .setDestinationJndiProperties(jndi)
                .setConnectionFactoryName("XAConnectionFactory")
                .setDestinationJndiName("dynamicTopics/"+destinationName)
                .setDurableSubscriptionName("testDurableSubscription")
                .setDurable(true)
                .setAutoContentConversion(true)
                .setAutoSplitBatch(true)
                .setBatchMode(false)
                .setBatchSize(1)
                .setCacheLevel(CACHE_CONSUMER)
                .setConcurrentConsumers(1)
                .setMaxConcurrentConsumers(1)
                .setSessionAcknowledgeMode(Session.SESSION_TRANSACTED)
                .setSessionTransacted(true)
                .setPubSubDomain(true)
                .build();
    }

    Broker getDbBroker()
    {
        return new DbBroker();
    }

    private Map<String,String> jndiProperties(boolean usePrefix,String clientId){
        Map<String,String> jndi = new HashMap<>();
        jndi.put("java.naming.factory.initial","org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        jndi.put("java.naming.provider.url",getBrokerUrl(usePrefix,clientId));

        return jndi;
    }

    private String getBrokerUrl(boolean usePrefix,String clientId)
    {
        String symbol;
        if(jmsProviderUrl.contains("?")){
            symbol = "&";
        }else{
            symbol = "?";
        }
        if ( usePrefix ){
            return jmsProviderUrl +symbol + "jms.clientIDPrefix="+clientId;
        }else {
            return jmsProviderUrl +symbol + "jms.clientID="+clientId;

        }
    }

    Producer getJmsProducer()
    {
        Map<String,String> jndi = jndiProperties(true,jmsProducerConfiguredResourceId);

        return builderFactory.getComponentBuilder().jmsProducer()
                .setConfiguredResourceId(jmsProducerConfiguredResourceId)
                .setConnectionFactoryJndiProperties(jndi)
                .setDestinationJndiProperties(jndi)
                .setConnectionFactoryName("XAConnectionFactory")
                .setDestinationJndiName("dynamicTopics/"+destinationName)
                .setSessionAcknowledgeMode(Session.SESSION_TRANSACTED)
                .setSessionTransacted(true)
                .setPubSubDomain(true)
                .setDeliveryPersistent(true)
                .setDeliveryMode(DeliveryMode.PERSISTENT)
                .setExplicitQosEnabled(true)
                .setMessageIdEnabled(true)
                .setMessageTimestampEnabled(true)
                .build();
    }

}

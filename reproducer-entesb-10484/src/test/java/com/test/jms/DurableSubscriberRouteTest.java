package com.test.jms;

import javax.jms.ConnectionFactory;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.component.jms.JmsConfiguration;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Test;

public class DurableSubscriberRouteTest {

    @Test
    public void testDurableSubscriberRouteStopStart() throws Exception {
        CamelContext camelContext = createCamelContext();
        camelContext.start();

        try {
            // Stop durable subs
            camelContext.stopRoute("durable1");
            camelContext.stopRoute("durable2");

            // Send message to durableInTopic
            ProducerTemplate template = camelContext.createProducerTemplate();
            template.sendBodyAndHeader("direct:sendMessageToTopic", "Test Message", "CamelJmsDestinationName", "durableInTopic");
            Thread.sleep(10000);

            // Start durable1
            MockEndpoint mockEndpoint = camelContext.getEndpoint("mock:durable1", MockEndpoint.class);
            mockEndpoint.expectedBodiesReceived("Reply 1: Test Message");
            camelContext.startRoute("durable1");
            mockEndpoint.assertIsSatisfied(10000);

            // Start durable2
            mockEndpoint = camelContext.getEndpoint("mock:durable2", MockEndpoint.class);
            mockEndpoint.expectedBodiesReceived("Reply 2: Test Message");
            camelContext.startRoute("durable2");
            mockEndpoint.assertIsSatisfied(10000);
        } finally {
            camelContext.stop();
        }
    }

    @Test
    public void testDurableSubscriberSuspendResumeStart() throws Exception {
        CamelContext camelContext = createCamelContext();
        camelContext.start();

        try {
            // Stop durable subs
            camelContext.suspendRoute("durable1");
            camelContext.suspendRoute("durable2");

            // Send message to durableInTopic
            ProducerTemplate template = camelContext.createProducerTemplate();
            template.sendBodyAndHeader("direct:sendMessageToTopic", "Test Message", "CamelJmsDestinationName", "durableInTopic");
            Thread.sleep(10000);

            // Start durable1
            MockEndpoint mockEndpoint = camelContext.getEndpoint("mock:durable1", MockEndpoint.class);
            mockEndpoint.expectedBodiesReceived("Reply 1: Test Message");
            camelContext.resumeRoute("durable1");
            mockEndpoint.assertIsSatisfied(10000);

            // Start durable2
            mockEndpoint = camelContext.getEndpoint("mock:durable2", MockEndpoint.class);
            mockEndpoint.expectedBodiesReceived("Reply 2: Test Message");
            camelContext.resumeRoute("durable2");
            mockEndpoint.assertIsSatisfied(10000);
        } finally {
            camelContext.stop();
        }
    }

    private CamelContext createCamelContext() throws Exception {
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616?enable1xPrefixes=true");

        JmsConfiguration jmsConfiguration = new JmsConfiguration();
        jmsConfiguration.setUsername("admin");
        jmsConfiguration.setPassword("admin123.");
        jmsConfiguration.setConnectionFactory(connectionFactory);

        JmsComponent jmsComponent = JmsComponent.jmsComponent(jmsConfiguration);
        jmsComponent.setUsername("admin");
        jmsComponent.setPassword("admin123.");

        CamelContext camelContext = new DefaultCamelContext();
        camelContext.addComponent("jms", jmsComponent);

        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("jms:topic:durableInTopic?durableSubscriptionName=durable1&clientId=1")
                    .routeId("durable1")
                    .log("=========> [DURABLE 1] Body " + body().toString())
                    .transform(simple("Reply 1: ${body}"))
                    .to("mock:durable1");

                from("jms:topic:durableInTopic?durableSubscriptionName=durable2&clientId=2")
                    .routeId("durable2")
                    .log("=========> [DURABLE 2] Body " + body().toString())
                    .transform(simple("Reply 2: ${body}"))
                    .to("mock:durable2");

                from("jms:topic:durableInTopic")
                    .routeId("nonDurable")
                    .log("=========> [NON-DURABLE] Body " + body().toString())
                    .transform(simple("Reply 3: ${body}"))
                    .to("mock:nonDurable");

                from("direct:sendMessageToTopic")
                    .to("jms:topic:overRideTopic?username=admin&password=admin123.");
            }
        });

        return camelContext;
    }

}

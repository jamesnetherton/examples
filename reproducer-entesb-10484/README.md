## Camel JMS with ActiveMQ Artemis & address prefixes

### Setting up a broker

```
docker run --rm -it -p 8161:8161 -p 61616:61616 -e AMQ_USER=admin -e AMQ_PASSWORD=admin123. -e AMQ_ROLE=admin -e AMQ_NAME=broker -e AMQ_TRANSPORTS='openwire,amqp,stomp,mqtt,hornetq' -e AMQ_ADDRESSES='durableInTopic' -e AMQ_REQUIRE_LOGIN=true -e AMQ_ANYCAST_PREFIX='jms.queue.' -e AMQ_MULTICAST_PREFIX='jms.topic.' registry.access.redhat.com/amq-broker-7/amq-broker-72-openshift:1.3-4
```

### Run tests

```
mvn clean test
```


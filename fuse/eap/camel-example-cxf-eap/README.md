# Camel CXF (code first) and JBoss EAP example

### Introduction

An example which uses a code-first approach to expose a web service in Camel running on JBoss EAP.

### Building

	mvn clean install

### Deploy

To deploy this application to an already running JBoss EAP instance do:

    mvn install -Pdeploy

Or you can copy the generated WAR file manually:

    cp target/camel-example-cxf-eap.war ${JBOSS_HOME}/standalone/deployments


### Testing the application

Once the application is deployed, you can test that the web service WSDL has been published by browsing to:

	http://localhost:8080/camel-example-cxf-eap/webservices/incident?wsdl

To test the web service endpoints, this example provides a client application which can generate some test messages. To run it do:

    mvn exec:java

Or you can use a tool like SoapUI and point it at the WSDL URL mentioned above.

### Undeploy

    mvn clean -Pdeploy

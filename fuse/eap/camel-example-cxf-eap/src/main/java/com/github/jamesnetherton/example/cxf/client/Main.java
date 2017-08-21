package com.github.jamesnetherton.example.cxf.client;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import com.github.jamesnetherton.example.cxf.incident.IncidentService;
import com.github.jamesnetherton.example.cxf.incident.InputReportIncident;
import com.github.jamesnetherton.example.cxf.incident.InputStatusIncident;
import com.github.jamesnetherton.example.cxf.incident.OutputStatusIncident;

public class Main {

    public static void main(String... args) throws Exception {
        QName serviceName = new QName("http://eap.camel.test.cxf", "IncidentServiceService");

        Service service = Service.create(new URL("http://localhost:8080/camel-example-cxf-eap/webservices/incident?wsdl"), serviceName);
        IncidentService incidentService = service.getPort(IncidentService.class);

        InputReportIncident incident = new InputReportIncident();
        incident.setIncidentId("123");
        incident.setIncidentDate("2017-08-21");
        incident.setGivenName("Some");
        incident.setFamilyName("Person");
        incident.setSummary("A test person");
        incident.setDetails("Some details about the person");
        incident.setEmail("some.person@test.com");
        incident.setPhone("1234 567 890");

        incidentService.reportIncident(incident);

        InputStatusIncident incidentStatus = new InputStatusIncident();
        incidentStatus.setIncidentId("123");

        OutputStatusIncident outputStatusIncident = incidentService.statusIncident(incidentStatus);
        System.out.println("");
        System.out.println("=============> Incident status: " + outputStatusIncident.getStatus());
        System.out.println("");
    }
}

package com.github.jamesnetherton.example.cxf.incident;

import javax.jws.WebService;

@WebService(targetNamespace = "http://eap.camel.test.cxf")
public interface IncidentService {

    OutputReportIncident reportIncident(InputReportIncident input);

    OutputStatusIncident statusIncident(InputStatusIncident input);
}

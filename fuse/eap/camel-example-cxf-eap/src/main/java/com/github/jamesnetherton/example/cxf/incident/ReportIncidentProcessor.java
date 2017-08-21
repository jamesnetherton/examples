package com.github.jamesnetherton.example.cxf.incident;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class ReportIncidentProcessor implements Processor {

    public void process(Exchange exchange) throws Exception {
        String id = exchange.getIn().getBody(InputReportIncident.class).getIncidentId();

        OutputReportIncident output = new OutputReportIncident();
        output.setCode("OK;" + id);
        exchange.getOut().setBody(output);
    }
}

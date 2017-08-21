package com.github.jamesnetherton.example.cxf.incident;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class StatusIncidentProcessor implements Processor {

    public void process(Exchange exchange) throws Exception {
        OutputStatusIncident output = new OutputStatusIncident();
        output.setStatus("IN PROGRESS");
        exchange.getOut().setBody(output);
    }
}

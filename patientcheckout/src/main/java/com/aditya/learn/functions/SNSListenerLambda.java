package com.aditya.learn.functions;

import com.aditya.learn.events.PatientCheckoutEvent;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class SNSListenerLambda {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void snsEventHandler(SNSEvent thisSnsEvent) {

        thisSnsEvent.getRecords().forEach(thisRecord ->
        {
            try {
                PatientCheckoutEvent thisEventReceivedThroughSNS =
                        objectMapper.readValue(thisRecord.getSNS().getMessage(), PatientCheckoutEvent.class);
                System.out.println(thisEventReceivedThroughSNS);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}


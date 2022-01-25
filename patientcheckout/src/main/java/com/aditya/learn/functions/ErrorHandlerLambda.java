package com.aditya.learn.functions;

import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorHandlerLambda {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void errorHandler(SNSEvent thisSnsEvent) {

        Logger logger = LoggerFactory.getLogger(ErrorHandlerLambda.class);

        thisSnsEvent.getRecords().forEach(
                thisRecord -> logger.info("Dead Letter Queue Event received here : "
                        + thisRecord.toString()));
    }
}

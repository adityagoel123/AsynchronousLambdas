package com.aditya.learn.functions;

import com.aditya.learn.events.PatientCheckoutEvent;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


public class PatientCheckoutLambda {

    private final AmazonS3 s3Instance = AmazonS3ClientBuilder.defaultClient();
    private final AmazonSNS snsInstance = AmazonSNSClientBuilder.defaultClient();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void s3EventHandler(S3Event thisS3Event, Context context) {
        LambdaLogger lambdaLogger = context.getLogger();
        Logger thisLogger = LoggerFactory.getLogger(PatientCheckoutLambda.class);

        //lambdaLogger.log("Starting to consume the S3Event: " + thisS3Event.toString() + "\n");
        thisLogger.info("Starting to consume the S3Event: " + thisS3Event.toString() + "\n");

        thisS3Event.getRecords().forEach(thisRecord ->
        {
           // lambdaLogger.log("S3 Event received at Lambda is: " +thisRecord.toString() + "\n");
            thisLogger.info("S3 Event received at Lambda is: " +thisRecord.toString() + "\n");
            S3ObjectInputStream s3ObjectInputStream =
                    s3Instance.getObject(thisRecord.getS3().getBucket().getName(),
                            thisRecord.getS3().getObject().getKey()).getObjectContent();

            //lambdaLogger.log("Succesfully formed the InputStream: " +s3ObjectInputStream.toString() + "\n");
            thisLogger.info("Succesfully formed the InputStream: " +s3ObjectInputStream.toString() + "\n");
            try {
                //lambdaLogger.log("Starting to form the JSONArray now from the afore-received Stream.\n");
                PatientCheckoutEvent[] listOfEvnts = objectMapper.readValue(
                        s3ObjectInputStream, PatientCheckoutEvent[].class);

                s3ObjectInputStream.close();

                List<PatientCheckoutEvent> listOfEvents = Arrays.asList(listOfEvnts);

                //lambdaLogger.log("Content received in regards to this EVENT are : " + listOfEvents + "\n");
                listOfEvents.forEach(checkoutEvent -> pushMessageToSNSTopic(thisLogger, checkoutEvent));
                
            } catch (JsonProcessingException e) {
                thisLogger.error("Exception this received is : ", e);
                throw new RuntimeException("Error while processing S3 Event here");
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void pushMessageToSNSTopic(Logger thisLogger, PatientCheckoutEvent checkoutEvent) {
        try {
            snsInstance.publish(System.getenv("PATIENT_CHECKOUT_TOPIC"),
                    objectMapper.writeValueAsString(checkoutEvent));
            /*lambdaLogger.log("Succesfully wrote this Event to SNS Topic : "
                    + checkoutEvent.toString() + "\n");*/
            thisLogger.info("Succesfully wrote this Event to SNS Topic : "
                    + checkoutEvent.toString() + "\n");

        } catch (JsonProcessingException e) {
            thisLogger.error("Exception this received is : ", e);
            throw new RuntimeException("Error while publishing event to SNS.");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}


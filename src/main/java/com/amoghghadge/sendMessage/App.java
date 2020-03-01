package com.amoghghadge.sendMessage;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.SubscribeResult;

public class App implements RequestHandler <Map <String, Object>, String> {
    
    /**
     * Says hello to the world.
     * @param args The arguments of the program.
     */
    public static void main(String[] args) {
        System.out.println("hi");
    }
    
    public String handleRequest (Map<String, Object> event, Context context) {
        
        int invokeNumber = 0;

        //Prints out input (event)
        System.out.println(event);

        //Client for accessing Amazon SNS
        @SuppressWarnings("deprecation")
		AmazonSNSClient snsClient = new AmazonSNSClient();

        //Creates SNS topic and gets stores the topic ARN
        String topicArn = createSNSTopic(snsClient, invokeNumber);

        //Subscribes phone numbers to SNS topic
        String ph1 = "+1" + event.getOrDefault("ph1", "7033004197").toString();
        String ph2 = "+1" + event.getOrDefault("ph2", "7039997246").toString();
        String ph3 = "+1" + event.getOrDefault("ph3", "7039999195").toString();
        
        subscribeToTopic(snsClient, topicArn, "sms", ph1);
        subscribeToTopic(snsClient, topicArn, "sms", ph2);
        subscribeToTopic(snsClient, topicArn, "sms", ph3);

        //Sets SMS Message Attributes
        Map<String, MessageAttributeValue> smsAttributes = new HashMap<String, MessageAttributeValue>();

        smsAttributes.put("AWS.SNS.SMS.SenderID", new MessageAttributeValue()

            .withStringValue("mySenderID") //The sender ID shown on the device.
            .withDataType("String"));
            
        smsAttributes.put("AWS.SNS.SMS.MaxPrice", new MessageAttributeValue()

            .withStringValue("0.50") //Sets the max price to 0.50 USD.
            .withDataType("Number"));

        smsAttributes.put("AWS.SNS.SMS.SMSType", new MessageAttributeValue()

            .withStringValue("Promotional") //Sets the type to promotional.
            .withDataType("String"));

        //Sends a message to the SNS topic
        String message = event.getOrDefault("message", "Message").toString();
        sendSMSMessageToTopic(snsClient, topicArn, message, smsAttributes);
        invokeNumber++;

        //API return
        return "Sent the message '" + message + "' to " + ph1 + ", " + ph2 + ", and " + ph3;
        
        // return "Hello " + System.getenv().getOrDefault("name", "Hello") + "!"; --> uses environment variables set up in the lambda function configuration
        // event.get("KEY_NAME") --> gets the value of the key - from the key pair - from the event map (compares to the json doc that is passed in lambda)
        // event.size() returns the number of key-value mappings in the event map

    }

    //Method to create SNS topic
    public static String createSNSTopic (AmazonSNSClient snsClient, int invokeNumber) {

        CreateTopicRequest createTopic = new CreateTopicRequest("mySNSTopic_" + invokeNumber);
        CreateTopicResult result = snsClient.createTopic(createTopic);
        System.out.println("Create topic request: " + snsClient.getCachedResponseMetadata(createTopic));
        System.out.println("Create topic result: " + result);
        return result.getTopicArn();

    }

    //Method to add SMS subscriptions to the topic
    public static void subscribeToTopic (AmazonSNSClient snsClient, String topicArn, String protocol, String endpoint) {

        SubscribeRequest subscribe = new SubscribeRequest(topicArn, protocol, endpoint);
        SubscribeResult subscribeResult = snsClient.subscribe(subscribe);
        System.out.println("Subscribe request: " + snsClient.getCachedResponseMetadata(subscribe));
        System.out.println("Subscribe result: " + subscribeResult);

    }

    //Method to send SMS message to the topic
    public static void sendSMSMessageToTopic (AmazonSNSClient snsClient, String topicArn, String message, Map<String, MessageAttributeValue> smsAttributes) {

        PublishResult result = snsClient.publish(new PublishRequest()
            .withTopicArn(topicArn)
            .withMessage(message)
            .withMessageAttributes(smsAttributes));

        System.out.println(result);

    }

}

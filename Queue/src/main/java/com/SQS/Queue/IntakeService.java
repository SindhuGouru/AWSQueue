package com.SQS.Queue;

import com.SQS.Queue.model.SQSQueue;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Component
public class IntakeService {
    @Autowired
    private AmazonSQS amazonSQS;


    @Scheduled(fixedRate = 1000)
    public void receiveMsgs() {
        String queueName = "sindhuQueue";
        GetQueueUrlResult queueUrl = amazonSQS.getQueueUrl(queueName);
        List<Message> processMessage = new ArrayList<>();
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest();
        receiveMessageRequest.setQueueUrl(queueName);
        receiveMessageRequest.setMaxNumberOfMessages(10);
        List<Message> receiveMessageResult = amazonSQS.receiveMessage(receiveMessageRequest).getMessages();
        System.out.println("Reveiving message request");
        for (Message m : receiveMessageResult) {
            processMessage.add(m);
            System.out.println(m);
            String receiptHandle = m.getReceiptHandle();
            amazonSQS.deleteMessage(new DeleteMessageRequest().withQueueUrl(queueName).withReceiptHandle(receiptHandle));
        }
    }

    @PostConstruct
    public String createqueue(){
        String queueName = "ShravanQueue";
        try
        {
            GetQueueUrlResult queueExsists = amazonSQS.getQueueUrl(queueName);
            System.out.println("Source Queue already exsists");
            return "Source Queue already exsists";
        }
        catch (QueueDoesNotExistException e){
            CreateQueueResult createQueueResult = amazonSQS.createQueue(queueName);
            System.out.println(createQueueResult + " queue created successfully ");
            return "queue created successfully";
        }
    }

    @PostConstruct
    public String createDlQueue(){
        String queueName = "ShravanQueue";
        String dlQueueName = "ShravanDlQueue";
        try{
            GetQueueUrlResult DlQueueExsists = amazonSQS.getQueueUrl(dlQueueName);
            System.out.println("Dead Letter Queue already exsists");
            return "Dead Letter Queue already exsists";
        } catch (QueueDoesNotExistException e) {
            CreateQueueResult createDlQueueResult = amazonSQS.createQueue(dlQueueName);
            System.out.println(createDlQueueResult + "Dead Letter Queue created successfully");
            String dlQueueUrl = amazonSQS.getQueueUrl(dlQueueName).getQueueUrl();
            GetQueueAttributesResult queue_attrs = amazonSQS.getQueueAttributes(new GetQueueAttributesRequest(dlQueueUrl).withAttributeNames("QueueArn"));
            String dlQueueArn = queue_attrs.getAttributes().get("QueueArn");
            String srcQueueUrl = amazonSQS.getQueueUrl(queueName).getQueueUrl();
            SetQueueAttributesRequest request = new SetQueueAttributesRequest().withQueueUrl(srcQueueUrl).addAttributesEntry("RedrivePolicy",
                    "{\"maxReceiveCount\":\"5\", \"deadLetterTargetArn\":\"" + dlQueueArn + "\"}");
            amazonSQS.setQueueAttributes(request);
            return "Dead Letter Queue created successfully";
        }

    }
}

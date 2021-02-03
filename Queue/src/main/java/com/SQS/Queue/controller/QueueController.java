package com.SQS.Queue.controller;

import com.SQS.Queue.model.SQSQueue;
import com.SQS.Queue.model.SQSSendMessageRequest;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class QueueController<Ec2Client> {

  @Autowired
private AmazonSQS amazonSQS;

  @PostMapping("/createQueue")
  public String createqueue(@RequestBody SQSQueue sqsQueue){
    try
    {
      GetQueueUrlResult queueExsists = amazonSQS.getQueueUrl(sqsQueue.getQueueName());
      System.out.println("Source Queue already exsists");
      return "Source Queue already exsists";
    }
    catch (QueueDoesNotExistException e){
      CreateQueueResult createQueueResult = amazonSQS.createQueue(sqsQueue.getQueueName());
      System.out.println(createQueueResult + " queue created successfully ");
      return "queue created successfully";
    }
  }

  @PostMapping("/createDlQueue")
  public String createDlQueue(@RequestBody SQSQueue sqsQueue){
    try{
      GetQueueUrlResult DlQueueExsists = amazonSQS.getQueueUrl(sqsQueue.getDlQueueName());
      System.out.println("Dead Letter Queue already exsists");
      return "Dead Letter Queue already exsists";
    } catch (QueueDoesNotExistException e) {
      CreateQueueResult createDlQueueResult = amazonSQS.createQueue(sqsQueue.getDlQueueName());
      System.out.println(createDlQueueResult + "Dead Letter Queue created successfully");
      String dlQueueUrl = amazonSQS.getQueueUrl(sqsQueue.getDlQueueName()).getQueueUrl();
      GetQueueAttributesResult queue_attrs = amazonSQS.getQueueAttributes(new GetQueueAttributesRequest(dlQueueUrl).withAttributeNames("QueueArn"));
      String dlQueueArn = queue_attrs.getAttributes().get("QueueArn");
      String srcQueueUrl = amazonSQS.getQueueUrl(sqsQueue.getQueueName()).getQueueUrl();
      SetQueueAttributesRequest request = new SetQueueAttributesRequest().withQueueUrl(srcQueueUrl).addAttributesEntry("RedrivePolicy",
              "{\"maxReceiveCount\":\"5\", \"deadLetterTargetArn\":\"" + dlQueueArn + "\"}");
      amazonSQS.setQueueAttributes(request);
      return "Dead Letter Queue created successfully";
    }

  }

  @GetMapping("/getQueues")
  public List<String> getQueues(){
    List<String> queueUrls = amazonSQS.listQueues().getQueueUrls();
    return queueUrls;
  }

  @PostMapping("/sendMessage")
  public String sendMessage(@RequestBody SQSSendMessageRequest sqsSendMessageRequest) {
    GetQueueUrlResult queueUrl = amazonSQS.getQueueUrl(sqsSendMessageRequest.getQueueName());
    for (int i=0;i<=10;i++){
      amazonSQS.sendMessage(queueUrl.getQueueUrl(),sqsSendMessageRequest.getMessage()+i);
    }
    return "Sent message successfully";
  }

  @PostMapping ("/receiveMessages")
  public List<Message> receiveMessages(@RequestBody SQSQueue sqsQueue){
    GetQueueUrlResult queueUrl = amazonSQS.getQueueUrl(sqsQueue.getQueueName());
    List<Message> processMessage = new ArrayList<>();
    ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest();
    receiveMessageRequest.setQueueUrl(sqsQueue.getQueueName());
    receiveMessageRequest.setMaxNumberOfMessages(10);
    List<Message> receiveMessageResult = amazonSQS.receiveMessage(receiveMessageRequest).getMessages();
    for (Message m : receiveMessageResult){
      processMessage.add(m);
      System.out.println(m);
      String receiptHandle = m.getReceiptHandle();
      amazonSQS.deleteMessage(new DeleteMessageRequest().withQueueUrl(sqsQueue.getQueueName()).withReceiptHandle(receiptHandle));
    }
    return receiveMessageResult;
  }

}

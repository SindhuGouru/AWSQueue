package com.SQS.Queue.model;

public class SQSQueue {

    public String getQueueName() {
        return srcQueueName;
    }

    public void setQueueName(String queueName) {
        this.srcQueueName = queueName;
    }

    private String srcQueueName;

    public String getDlQueueName() {
        return dlQueueName;
    }

    public void setDlQueueName(String dlQueueName) {
        this.dlQueueName = dlQueueName;
    }

    private String dlQueueName;
}

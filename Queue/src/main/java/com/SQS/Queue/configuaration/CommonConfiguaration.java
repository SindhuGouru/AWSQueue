package com.SQS.Queue.configuaration;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonConfiguaration {

    @Bean
    public AmazonSQS amazonSQS(){

        BasicAWSCredentials basicAWSCredentials = new BasicAWSCredentials("AKIA4UON4ALHTJLNCNNQ", "UOGRvRYD292PQ8UsizI0gP6Psvp7Fkay0/sUy6ZG");
       return AmazonSQSClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(basicAWSCredentials)).withRegion(Regions.US_EAST_2).build();

    }
}

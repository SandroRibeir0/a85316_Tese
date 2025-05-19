package com.mvn_tomcat_webapp.servlets;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class Consumer extends Thread{
    Properties kafkaProperties;
    String topic;
    // Setup instance to allow access to the simulation status
    private Simulator setup;

    public Consumer(String host, String topic, Simulator setup){

        kafkaProperties = new Properties();
        //Kafka consumer configuration
        kafkaProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, host);
        kafkaProperties.put(ConsumerConfig.GROUP_ID_CONFIG, "group1");
        kafkaProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        kafkaProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        //kafkaProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        this.topic = topic;
        //System.out.println("construir consumidor");

        this.setup = setup;
    }
    
    public void run(){
    
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(kafkaProperties)) {
            //System.out.println("CONSUMIDOR KAFKA AFTER TRY");
            // Subscreve o tópico
            consumer.subscribe(Collections.singletonList(topic));
            //System.out.println("antes de loop");

            // Loop para consumir mensagens
            while (setup.getSimulationSatus()) {
                //System.out.println("CICLO WHILE");
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                records.forEach(record -> System.out.println("Mensagem recebida de tópico: " + topic + " mensagem: " + record.value()));
                //records.forEach(record -> a.setTopicMessage(record.value()));
            }

            // Close the consumer once the simulation ends
            consumer.close();
        }    
    }
}

package com.mvn_tomcat_webapp.servlets;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.clients.consumer.OffsetAndTimestamp;
import java.time.Duration;
import java.util.*;

public class KafkaInterfaceConsumerTimestamps {
    Properties props;
    
    public KafkaInterfaceConsumerTimestamps (){
        props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "interface_consumer");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("enable.auto.commit", "false");
        props.put("auto.offset.reset", "earliest"); // "earliest" garante leitura desde o início caso necessário
    }

    public List<String> getSensorStatusHistoryBetweenTimestamps(String topic, long startTimestamp, long endTimestamp) {
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(topic));

        // Aguarda a atribuição das partições
        consumer.poll(Duration.ofSeconds(1));
        Set<TopicPartition> assignedPartitions = consumer.assignment();

        // Mapeia os timestamps para encontrar os offsets correspondentes
        Map<TopicPartition, Long> timestampsToSearch = new HashMap<>();
        for (TopicPartition partition : assignedPartitions) {
            timestampsToSearch.put(partition, startTimestamp);
        }

        // Obtém os offsets para os timestamps iniciais
        Map<TopicPartition, OffsetAndTimestamp> offsets = consumer.offsetsForTimes(timestampsToSearch);

        // Posiciona o consumidor nos offsets corretos
        for (TopicPartition partition : assignedPartitions) {
            OffsetAndTimestamp offsetAndTimestamp = offsets.get(partition);
            if (offsetAndTimestamp != null) {
                consumer.seek(partition, offsetAndTimestamp.offset());
            } else {
                consumer.seekToBeginning(Collections.singletonList(partition));
            }
        }

        // Lista para armazenar as mensagens filtradas
        List<String> messages = new ArrayList<>();

        boolean keepReading = true;
        while (keepReading) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5));

            if (records.isEmpty()) {
                keepReading = false; // Para se não houver mais mensagens
            } else {
                for (ConsumerRecord<String, String> record : records) {
                    long recordTimestamp = record.timestamp();
                    
                    // Filtra as mensagens dentro do intervalo desejado
                    if (recordTimestamp >= startTimestamp && recordTimestamp <= endTimestamp) {
                        messages.add(String.format("value = %s, timestamp = %d", record.value(), recordTimestamp));
                    } else if (recordTimestamp > endTimestamp) {
                        keepReading = false; // Se o timestamp já ultrapassou o limite, pode parar
                        break;
                    }
                }

                // Comita manualmente os deslocamentos processados
                consumer.commitSync();
            }
        }

        consumer.close();
        return messages;
    }
}

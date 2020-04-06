// tag::copyright[]
/*******************************************************************************
 * Copyright (c) 2020 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial implementation
 *******************************************************************************/
// end::copyright[]
package it.io.openliberty.guides.inventory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.microshed.testing.SharedContainerConfig;
import org.microshed.testing.jaxrs.RESTClient;
import org.microshed.testing.jupiter.MicroShedTest;
import org.microshed.testing.kafka.KafkaConsumerConfig;
import org.microshed.testing.kafka.KafkaProducerConfig;

import io.openliberty.guides.inventory.InventoryResource;
import io.openliberty.guides.models.CpuUsage;
import io.openliberty.guides.models.CpuUsage.CpuUsageDeserializer;
import io.openliberty.guides.models.CpuUsage.JsonbSerializer;

@MicroShedTest
@SharedContainerConfig(AppContainerConfig.class)
@TestMethodOrder(OrderAnnotation.class)
public class InventoryEndpointIT {

    @RESTClient
    public static InventoryResource inventoryResource;

    @KafkaProducerConfig(valueSerializer = JsonbSerializer.class)
    public static KafkaProducer<String, CpuUsage> cpuProducer;

    @KafkaConsumerConfig(valueDeserializer = CpuUsageDeserializer.class, 
            groupId = "cpu-status", topics = "cpuStatusTopic", 
            properties = ConsumerConfig.AUTO_OFFSET_RESET_CONFIG + "=earliest")
    public static KafkaConsumer<String, CpuUsage> cpuConsumer;

    @AfterAll
    public static void cleanup() {
        inventoryResource.resetSystems();
    }

    @Test
    public void testCpuUsage() throws InterruptedException {
        CpuUsage c = new CpuUsage("localhost", new Double(1.1));
        cpuProducer.send(new ProducerRecord<String, CpuUsage>("cpuStatusTopic", c));
        Thread.sleep(5000);
        Response response = inventoryResource.getSystems();
        List<Properties> systems = response.readEntity(new GenericType<List<Properties>>() {});
        Assertions.assertEquals(200, response.getStatus(),
                "Response should be 200");
        Assertions.assertEquals(systems.size(), 1);
        for (Properties system : systems) {
            Assertions.assertEquals(c.hostId, system.get("hostname"), "HostId not match!");
            BigDecimal cpu = (BigDecimal) system.get("cpuUsage");;
            Assertions.assertEquals(c.cpuUsage.doubleValue(), cpu.doubleValue(), "CPU Usage not match!");
        }
    }
}
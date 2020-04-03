/*
 *
 *  Copyright 2016 Robert Winkler
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package io.github.resilience4j.circuitbreaker;

import io.github.resilience4j.core.EventProcessor;
import io.github.resilience4j.core.Registry;
import io.github.resilience4j.core.registry.EntryAddedEvent;
import io.github.resilience4j.core.registry.EntryRemovedEvent;
import io.github.resilience4j.core.registry.EntryReplacedEvent;
import io.github.resilience4j.core.registry.InMemoryRegistryStore;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import io.vavr.Tuple;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.BDDAssertions.assertThat;


public class CircuitBreakerRegistryTest {

    private static Optional<EventProcessor<?>> getEventProcessor(
        Registry.EventPublisher<CircuitBreaker> eventPublisher) {
        if (eventPublisher instanceof EventProcessor<?>) {
            return Optional.of((EventProcessor<?>) eventPublisher);
        }

        return Optional.empty();
    }

    @Test
    public void shouldInitRegistryTags() {
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.ofDefaults();
        Map<String, CircuitBreakerConfig> circuitBreakerConfigs = Collections
            .singletonMap("default", circuitBreakerConfig);
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(circuitBreakerConfigs,new NoOpCircuitBreakerEventConsumer(),io.vavr.collection.HashMap.of("Tag1Key","Tag1Value"));
        assertThat(registry.getTags()).isNotEmpty();
        assertThat(registry.getTags()).containsOnly(Tuple.of("Tag1Key","Tag1Value"));
    }

    @Test
    public void shouldReturnTheCorrectName() {
        CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("testName");
        assertThat(circuitBreaker).isNotNull();
        assertThat(circuitBreaker.getName()).isEqualTo("testName");
    }

    @Test
    public void shouldBeTheSameCircuitBreaker() {
        CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("testName");
        CircuitBreaker circuitBreaker2 = circuitBreakerRegistry.circuitBreaker("testName");
        assertThat(circuitBreaker).isSameAs(circuitBreaker2);
        assertThat(circuitBreakerRegistry.getAllCircuitBreakers()).hasSize(1);
    }

    @Test
    public void shouldBeNotTheSameCircuitBreaker() {
        CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("testName");
        CircuitBreaker circuitBreaker2 = circuitBreakerRegistry.circuitBreaker("otherTestName");
        assertThat(circuitBreaker).isNotSameAs(circuitBreaker2);

        assertThat(circuitBreakerRegistry.getAllCircuitBreakers()).hasSize(2);
    }

    @Test
    public void noTagsByDefault() {
        CircuitBreaker circuitBreaker = CircuitBreakerRegistry.ofDefaults()
            .circuitBreaker("testName");
        assertThat(circuitBreaker.getTags()).hasSize(0);
    }

    @Test
    public void tagsOfRegistryAddedToInstance() {
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.ofDefaults();
        Map<String, CircuitBreakerConfig> circuitBreakerConfigs = Collections
            .singletonMap("default", circuitBreakerConfig);
        io.vavr.collection.Map<String, String> circuitBreakerTags = io.vavr.collection.HashMap
            .of("key1", "value1", "key2", "value2");
        CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry
            .of(circuitBreakerConfigs, circuitBreakerTags);
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("testName");

        assertThat(circuitBreaker.getTags()).containsOnlyElementsOf(circuitBreakerTags);
    }

    @Test
    public void tagsAddedToInstance() {
        CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();
        io.vavr.collection.Map<String, String> retryTags = io.vavr.collection.HashMap
            .of("key1", "value1", "key2", "value2");
        CircuitBreaker circuitBreaker = circuitBreakerRegistry
            .circuitBreaker("testName", retryTags);

        assertThat(circuitBreaker.getTags()).containsOnlyElementsOf(retryTags);
    }

    @Test
    public void tagsOfRetriesShouldNotBeMixed() {
        CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.ofDefaults();
        io.vavr.collection.Map<String, String> circuitBreakerTags = io.vavr.collection.HashMap
            .of("key1", "value1", "key2", "value2");
        CircuitBreaker circuitBreaker = circuitBreakerRegistry
            .circuitBreaker("testName", circuitBreakerConfig, circuitBreakerTags);
        io.vavr.collection.Map<String, String> circuitBreakerTags2 = io.vavr.collection.HashMap
            .of("key3", "value3", "key4", "value4");
        CircuitBreaker circuitBreaker2 = circuitBreakerRegistry
            .circuitBreaker("otherTestName", circuitBreakerConfig, circuitBreakerTags2);

        assertThat(circuitBreaker.getTags()).containsOnlyElementsOf(circuitBreakerTags);
        assertThat(circuitBreaker2.getTags()).containsOnlyElementsOf(circuitBreakerTags2);
    }

    @Test
    public void tagsOfInstanceTagsShouldOverrideRegistryTags() {
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.ofDefaults();
        Map<String, CircuitBreakerConfig> circuitBreakerConfigs = Collections
            .singletonMap("default", circuitBreakerConfig);
        io.vavr.collection.Map<String, String> circuitBreakerTags = io.vavr.collection.HashMap
            .of("key1", "value1", "key2", "value2");
        io.vavr.collection.Map<String, String> instanceTags = io.vavr.collection.HashMap
            .of("key1", "value3", "key4", "value4");
        CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry
            .of(circuitBreakerConfigs, circuitBreakerTags);
        CircuitBreaker circuitBreaker = circuitBreakerRegistry
            .circuitBreaker("testName", circuitBreakerConfig, instanceTags);

        io.vavr.collection.Map<String, String> expectedTags = io.vavr.collection.HashMap
            .of("key1", "value3", "key2", "value2", "key4", "value4");
        assertThat(circuitBreaker.getTags()).containsOnlyElementsOf(expectedTags);
    }

    @Test
    public void testCreateWithDefaultConfiguration() {
        CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry
            .of(CircuitBreakerConfig.ofDefaults());
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("testName");
        CircuitBreaker circuitBreaker2 = circuitBreakerRegistry.circuitBreaker("otherTestName");
        assertThat(circuitBreaker).isNotSameAs(circuitBreaker2);

        assertThat(circuitBreakerRegistry.getAllCircuitBreakers()).hasSize(2);
    }

    @Test
    public void testCreateWithCustomConfiguration() {
        float failureRate = 30f;
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
            .failureRateThreshold(failureRate).build();

        CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry
            .of(circuitBreakerConfig);
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("testName");

        assertThat(circuitBreaker.getCircuitBreakerConfig().getFailureRateThreshold())
            .isEqualTo(failureRate);
    }

    @Test
    public void testCreateWithConfigurationMap() {
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
            .failureRateThreshold(30f).build();
        CircuitBreakerConfig customCircuitBreakerConfig = CircuitBreakerConfig.custom()
            .failureRateThreshold(40f).build();
        Map<String, CircuitBreakerConfig> configs = new HashMap<>();
        configs.put("default", circuitBreakerConfig);
        configs.put("custom", customCircuitBreakerConfig);

        CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.of(configs);

        assertThat(circuitBreakerRegistry.getDefaultConfig()).isNotNull();
        assertThat(circuitBreakerRegistry.getDefaultConfig().getFailureRateThreshold())
            .isEqualTo(30f);
        assertThat(circuitBreakerRegistry.getConfiguration("custom")).isNotEmpty();
    }

    @Test
    public void testCreateWithSingleRegistryEventConsumer() {
        CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry
            .of(CircuitBreakerConfig.ofDefaults(), new NoOpCircuitBreakerEventConsumer());

        getEventProcessor(circuitBreakerRegistry.getEventPublisher())
            .ifPresent(eventProcessor -> assertThat(eventProcessor.hasConsumers()).isTrue());
    }

    @Test
    public void testCreateWithMultipleRegistryEventConsumer() {
        List<RegistryEventConsumer<CircuitBreaker>> registryEventConsumers = new ArrayList<>();
        registryEventConsumers.add(new NoOpCircuitBreakerEventConsumer());
        registryEventConsumers.add(new NoOpCircuitBreakerEventConsumer());

        CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry
            .of(CircuitBreakerConfig.ofDefaults(), registryEventConsumers);

        getEventProcessor(circuitBreakerRegistry.getEventPublisher())
            .ifPresent(eventProcessor -> assertThat(eventProcessor.hasConsumers()).isTrue());
    }

    @Test
    public void testCreateWithConfigurationMapWithSingleRegistryEventConsumer() {
        Map<String, CircuitBreakerConfig> configs = new HashMap<>();
        configs.put("custom", CircuitBreakerConfig.ofDefaults());

        CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry
            .of(configs, new NoOpCircuitBreakerEventConsumer());

        getEventProcessor(circuitBreakerRegistry.getEventPublisher())
            .ifPresent(eventProcessor -> assertThat(eventProcessor.hasConsumers()).isTrue());
    }

    @Test
    public void testCreateWithConfigurationMapWithMultiRegistryEventConsumer() {
        Map<String, CircuitBreakerConfig> configs = new HashMap<>();
        configs.put("custom", CircuitBreakerConfig.ofDefaults());

        List<RegistryEventConsumer<CircuitBreaker>> registryEventConsumers = new ArrayList<>();
        registryEventConsumers.add(new NoOpCircuitBreakerEventConsumer());
        registryEventConsumers.add(new NoOpCircuitBreakerEventConsumer());

        CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry
            .of(configs, registryEventConsumers);

        getEventProcessor(circuitBreakerRegistry.getEventPublisher())
            .ifPresent(eventProcessor -> assertThat(eventProcessor.hasConsumers()).isTrue());
    }

    @Test
    public void testAddConfiguration() {
        CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();
        float failureRate = 30f;
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
            .failureRateThreshold(failureRate).build();
        circuitBreakerRegistry.addConfiguration("someSharedConfig", circuitBreakerConfig);

        assertThat(circuitBreakerRegistry.getDefaultConfig()).isNotNull();
        assertThat(circuitBreakerRegistry.getDefaultConfig().getFailureRateThreshold())
            .isEqualTo(50f);
        assertThat(circuitBreakerRegistry.getConfiguration("someSharedConfig")).isNotEmpty();

        CircuitBreaker circuitBreaker = circuitBreakerRegistry
            .circuitBreaker("name", "someSharedConfig");
        assertThat(circuitBreaker.getCircuitBreakerConfig()).isEqualTo(circuitBreakerConfig);
        assertThat(circuitBreaker.getCircuitBreakerConfig().getFailureRateThreshold())
            .isEqualTo(failureRate);
    }

    @Test
    public void testCreateWithConfigurationMapWithoutDefaultConfig() {
        float failureRate = 30f;
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
            .failureRateThreshold(failureRate).build();

        CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry
            .of(Collections.singletonMap("someSharedConfig", circuitBreakerConfig));

        assertThat(circuitBreakerRegistry.getDefaultConfig()).isNotNull();
        assertThat(circuitBreakerRegistry.getDefaultConfig().getFailureRateThreshold())
            .isEqualTo(50f);
        assertThat(circuitBreakerRegistry.getConfiguration("someSharedConfig")).isNotEmpty();

        CircuitBreaker circuitBreaker = circuitBreakerRegistry
            .circuitBreaker("name", "someSharedConfig");

        assertThat(circuitBreaker.getCircuitBreakerConfig()).isEqualTo(circuitBreakerConfig);
        assertThat(circuitBreaker.getCircuitBreakerConfig().getFailureRateThreshold())
            .isEqualTo(failureRate);
    }

    @Test
    public void testCreateWithNullConfig() {
        assertThatThrownBy(() -> CircuitBreakerRegistry.of((CircuitBreakerConfig) null))
            .isInstanceOf(NullPointerException.class).hasMessage("Config must not be null");
    }

    @Test
    public void testCreateUsingBuilderWithDefaultConfig() {
        CircuitBreakerRegistry circuitBreakerRegistry =
            CircuitBreakerRegistry.custom().withCircuitBreakerConfig(CircuitBreakerConfig.ofDefaults()).build();
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("testName");
        CircuitBreaker circuitBreaker2 = circuitBreakerRegistry.circuitBreaker("otherTestName");
        assertThat(circuitBreaker).isNotSameAs(circuitBreaker2);

        assertThat(circuitBreakerRegistry.getAllCircuitBreakers()).hasSize(2);
    }

    @Test
    public void testCreateUsingBuilderWithCustomConfig() {
        float failureRate = 30f;
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
            .failureRateThreshold(failureRate).build();

        CircuitBreakerRegistry circuitBreakerRegistry =
            CircuitBreakerRegistry.custom().withCircuitBreakerConfig(circuitBreakerConfig).build();
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("testName");

        assertThat(circuitBreaker.getCircuitBreakerConfig().getFailureRateThreshold())
            .isEqualTo(failureRate);
    }

    @Test
    public void testCreateUsingBuilderWithoutDefaultConfig() {
        float failureRate = 30f;
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
            .failureRateThreshold(failureRate).build();

        CircuitBreakerRegistry circuitBreakerRegistry =
            CircuitBreakerRegistry.custom().addCircuitBreakerConfig("someSharedConfig", circuitBreakerConfig).build();

        assertThat(circuitBreakerRegistry.getDefaultConfig()).isNotNull();
        assertThat(circuitBreakerRegistry.getDefaultConfig().getFailureRateThreshold())
            .isEqualTo(50f);
        assertThat(circuitBreakerRegistry.getConfiguration("someSharedConfig")).isNotEmpty();

        CircuitBreaker circuitBreaker = circuitBreakerRegistry
            .circuitBreaker("name", "someSharedConfig");

        assertThat(circuitBreaker.getCircuitBreakerConfig()).isEqualTo(circuitBreakerConfig);
        assertThat(circuitBreaker.getCircuitBreakerConfig().getFailureRateThreshold())
            .isEqualTo(failureRate);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddMultipleDefaultConfigUsingBuilderShouldThrowException() {
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
            .failureRateThreshold(30f).build();
        CircuitBreakerRegistry.custom().addCircuitBreakerConfig("default", circuitBreakerConfig).build();
    }

    @Test
    public void testCreateUsingBuilderWithDefaultAndCustomConfig() {
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
            .failureRateThreshold(30f).build();
        CircuitBreakerConfig customCircuitBreakerConfig = CircuitBreakerConfig.custom()
            .failureRateThreshold(40f).build();

        CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.custom()
            .withCircuitBreakerConfig(circuitBreakerConfig)
            .addCircuitBreakerConfig("custom", customCircuitBreakerConfig)
            .build();

        assertThat(circuitBreakerRegistry.getDefaultConfig()).isNotNull();
        assertThat(circuitBreakerRegistry.getDefaultConfig().getFailureRateThreshold())
            .isEqualTo(30f);
        assertThat(circuitBreakerRegistry.getConfiguration("custom")).isNotEmpty();
    }

    @Test
    public void testCreateUsingBuilderWithNullConfig() {
        assertThatThrownBy(
            () -> CircuitBreakerRegistry.custom().withCircuitBreakerConfig(null).build())
            .isInstanceOf(NullPointerException.class).hasMessage("Config must not be null");
    }

    @Test
    public void testCreateUsingBuilderWithMultipleRegistryEventConsumer() {
        CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.custom()
            .withCircuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
            .addRegistryEventConsumer(new NoOpCircuitBreakerEventConsumer())
            .addRegistryEventConsumer(new NoOpCircuitBreakerEventConsumer())
            .build();

        getEventProcessor(circuitBreakerRegistry.getEventPublisher())
            .ifPresent(eventProcessor -> assertThat(eventProcessor.hasConsumers()).isTrue());
    }

    @Test
    public void testCreateUsingBuilderWithRegistryTags() {
        io.vavr.collection.Map<String, String> circuitBreakerTags = io.vavr.collection.HashMap
            .of("key1", "value1", "key2", "value2");
        CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.custom()
            .withCircuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
            .withTags(circuitBreakerTags)
            .build();
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("testName");

        assertThat(circuitBreaker.getTags()).containsOnlyElementsOf(circuitBreakerTags);
    }

    @Test
    public void testCreateUsingBuilderWithRegistryStore() {
        CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.custom()
            .withCircuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
            .withRegistryStore(new InMemoryRegistryStore())
            .build();
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("testName");
        CircuitBreaker circuitBreaker2 = circuitBreakerRegistry.circuitBreaker("otherTestName");

        assertThat(circuitBreaker).isNotSameAs(circuitBreaker2);
        assertThat(circuitBreakerRegistry.getAllCircuitBreakers()).hasSize(2);
    }

    private static class NoOpCircuitBreakerEventConsumer implements
        RegistryEventConsumer<CircuitBreaker> {

        @Override
        public void onEntryAddedEvent(EntryAddedEvent<CircuitBreaker> entryAddedEvent) {
        }

        @Override
        public void onEntryRemovedEvent(EntryRemovedEvent<CircuitBreaker> entryRemoveEvent) {
        }

        @Override
        public void onEntryReplacedEvent(EntryReplacedEvent<CircuitBreaker> entryReplacedEvent) {
        }
    }
}

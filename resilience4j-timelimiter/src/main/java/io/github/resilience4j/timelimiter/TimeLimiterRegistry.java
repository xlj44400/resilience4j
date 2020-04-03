/*
 *
 *  Copyright 2019 authors
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
package io.github.resilience4j.timelimiter;

import io.github.resilience4j.core.Registry;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import io.github.resilience4j.timelimiter.internal.InMemoryTimeLimiterRegistry;
import io.vavr.collection.Seq;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Manages all TimeLimiter instances.
 */
public interface TimeLimiterRegistry extends Registry<TimeLimiter, TimeLimiterConfig> {

    /**
     * Creates a TimeLimiterRegistry with a custom default TimeLimiter configuration.
     *
     * @param defaultTimeLimiterConfig a custom default TimeLimiter configuration
     * @return a TimeLimiterRegistry instance backed by a custom TimeLimiter configuration
     */
    static TimeLimiterRegistry of(TimeLimiterConfig defaultTimeLimiterConfig) {
        return new InMemoryTimeLimiterRegistry(defaultTimeLimiterConfig);
    }

    /**
     * Creates a TimeLimiterRegistry with a custom default TimeLimiter configuration and a
     * TimeLimiter registry event consumer.
     *
     * @param defaultTimeLimiterConfig a custom default TimeLimiter configuration.
     * @param registryEventConsumer    a TimeLimiter registry event consumer.
     * @return a TimeLimiterRegistry with a custom TimeLimiter configuration and a TimeLimiter
     * registry event consumer.
     */
    static TimeLimiterRegistry of(TimeLimiterConfig defaultTimeLimiterConfig,
        RegistryEventConsumer<TimeLimiter> registryEventConsumer) {
        return new InMemoryTimeLimiterRegistry(defaultTimeLimiterConfig, registryEventConsumer);
    }

    /**
     * Creates a TimeLimiterRegistry with a custom default TimeLimiter configuration and a list of
     * TimeLimiter registry event consumers.
     *
     * @param defaultTimeLimiterConfig a custom default TimeLimiter configuration.
     * @param registryEventConsumers   a list of TimeLimiter registry event consumers.
     * @return a TimeLimiterRegistry with a custom TimeLimiter configuration and list of TimeLimiter
     * registry event consumers.
     */
    static TimeLimiterRegistry of(TimeLimiterConfig defaultTimeLimiterConfig,
        List<RegistryEventConsumer<TimeLimiter>> registryEventConsumers) {
        return new InMemoryTimeLimiterRegistry(defaultTimeLimiterConfig, registryEventConsumers);
    }

    /**
     * Returns a managed {@link TimeLimiterConfig} or creates a new one with a default TimeLimiter
     * configuration.
     *
     * @return The {@link TimeLimiterConfig}
     */
    static TimeLimiterRegistry ofDefaults() {
        return new InMemoryTimeLimiterRegistry(TimeLimiterConfig.ofDefaults());
    }

    /**
     * Creates a TimeLimiterRegistry with a Map of shared TimeLimiter configurations.
     *
     * @param configs a Map of shared TimeLimiter configurations
     * @return a TimeLimiterRegistry with a Map of shared TimeLimiter configurations.
     */
    static TimeLimiterRegistry of(Map<String, TimeLimiterConfig> configs) {
        return new InMemoryTimeLimiterRegistry(configs);
    }

    /**
     * Creates a TimeLimiterRegistry with a Map of shared TimeLimiter configurations.
     * <p>
     * Tags added to the registry will be added to every instance created by this registry.
     *
     * @param configs a Map of shared TimeLimiter configurations
     * @param tags    default tags to add to the registry
     * @return a TimeLimiterRegistry with a Map of shared TimeLimiter configurations.
     */
    static TimeLimiterRegistry of(Map<String, TimeLimiterConfig> configs,
        io.vavr.collection.Map<String, String> tags) {
        return new InMemoryTimeLimiterRegistry(configs, tags);
    }

    /**
     * Creates a TimeLimiterRegistry with a Map of shared TimeLimiter configurations and a
     * TimeLimiter registry event consumer.
     *
     * @param configs               a Map of shared TimeLimiter configurations.
     * @param registryEventConsumer a TimeLimiter registry event consumer.
     * @return a TimeLimiterRegistry with a Map of shared TimeLimiter configurations and a
     * TimeLimiter registry event consumer.
     */
    static TimeLimiterRegistry of(Map<String, TimeLimiterConfig> configs,
        RegistryEventConsumer<TimeLimiter> registryEventConsumer) {
        return new InMemoryTimeLimiterRegistry(configs, registryEventConsumer);
    }

    /**
     * Creates a TimeLimiterRegistry with a Map of shared TimeLimiter configurations and a
     * TimeLimiter registry event consumer.
     *
     * @param configs               a Map of shared TimeLimiter configurations.
     * @param registryEventConsumer a TimeLimiter registry event consumer.
     * @param tags                  default tags to add to the registry
     * @return a TimeLimiterRegistry with a Map of shared TimeLimiter configurations and a
     * TimeLimiter registry event consumer.
     */
    static TimeLimiterRegistry of(Map<String, TimeLimiterConfig> configs,
        RegistryEventConsumer<TimeLimiter> registryEventConsumer,
        io.vavr.collection.Map<String, String> tags) {
        return new InMemoryTimeLimiterRegistry(configs, registryEventConsumer, tags);
    }

    /**
     * Creates a TimeLimiterRegistry with a Map of shared TimeLimiter configurations and a list of
     * TimeLimiter registry event consumers.
     *
     * @param configs                a Map of shared TimeLimiter configurations.
     * @param registryEventConsumers a list of TimeLimiter registry event consumers.
     * @return a TimeLimiterRegistry with a Map of shared TimeLimiter configurations and a list of
     * TimeLimiter registry event consumers.
     */
    static TimeLimiterRegistry of(Map<String, TimeLimiterConfig> configs,
        List<RegistryEventConsumer<TimeLimiter>> registryEventConsumers) {
        return new InMemoryTimeLimiterRegistry(configs, registryEventConsumers);
    }

    /**
     * Returns all managed {@link TimeLimiter} instances.
     *
     * @return all managed {@link TimeLimiter} instances.
     */
    Seq<TimeLimiter> getAllTimeLimiters();

    /**
     * Returns a managed {@link TimeLimiter} or creates a new one with the default TimeLimiter
     * configuration.
     *
     * @param name the name of the TimeLimiter
     * @return The {@link TimeLimiter}
     */
    TimeLimiter timeLimiter(String name);

    /**
     * Returns a managed {@link TimeLimiter} or creates a new one with the default TimeLimiter
     * configuration.
     * <p>
     * The {@code tags} passed will be appended to the tags already configured for the registry.
     * When tags (keys) of the two collide the tags passed with this method will override the tags
     * of the registry.
     *
     * @param name the name of the TimeLimiter
     * @param tags tags added to the TimeLimiter
     * @return The {@link TimeLimiter}
     */
    TimeLimiter timeLimiter(String name, io.vavr.collection.Map<String, String> tags);

    /**
     * Returns a managed {@link TimeLimiter} or creates a new one with a custom TimeLimiter
     * configuration.
     *
     * @param name              the name of the TimeLimiter
     * @param timeLimiterConfig a custom TimeLimiter configuration
     * @return The {@link TimeLimiter}
     */
    TimeLimiter timeLimiter(String name, TimeLimiterConfig timeLimiterConfig);

    /**
     * Returns a managed {@link TimeLimiter} or creates a new one with a custom TimeLimiter
     * configuration.
     * <p>
     * The {@code tags} passed will be appended to the tags already configured for the registry.
     * When tags (keys) of the two collide the tags passed with this method will override the tags
     * of the registry.
     *
     * @param name              the name of the TimeLimiter
     * @param timeLimiterConfig a custom TimeLimiter configuration
     * @param tags              tags added to the TimeLimiter
     * @return The {@link TimeLimiter}
     */
    TimeLimiter timeLimiter(String name, TimeLimiterConfig timeLimiterConfig,
        io.vavr.collection.Map<String, String> tags);

    /**
     * Returns a managed {@link TimeLimiterConfig} or creates a new one with a custom
     * TimeLimiterConfig configuration.
     *
     * @param name                      the name of the TimeLimiterConfig
     * @param timeLimiterConfigSupplier a supplier of a custom TimeLimiterConfig configuration
     * @return The {@link TimeLimiterConfig}
     */
    TimeLimiter timeLimiter(String name, Supplier<TimeLimiterConfig> timeLimiterConfigSupplier);

    /**
     * Returns a managed {@link TimeLimiter} or creates a new one with a custom TimeLimiter
     * configuration.
     * <p>
     * The {@code tags} passed will be appended to the tags already configured for the registry.
     * When tags (keys) of the two collide the tags passed with this method will override the tags
     * of the registry.
     *
     * @param name                      the name of the TimeLimiter
     * @param timeLimiterConfigSupplier a supplier of a custom TimeLimiter configuration
     * @param tags                      tags added to the TimeLimiter
     * @return The {@link TimeLimiter}
     */
    TimeLimiter timeLimiter(String name,
        Supplier<TimeLimiterConfig> timeLimiterConfigSupplier,
        io.vavr.collection.Map<String, String> tags);

    /**
     * Returns a managed {@link TimeLimiter} or creates a new one.
     * The configuration must have been added upfront via {@link #addConfiguration(String, Object)}.
     *
     * @param name       the name of the TimeLimiter
     * @param configName the name of the shared configuration
     * @return The {@link TimeLimiter}
     */
    TimeLimiter timeLimiter(String name, String configName);

    /**
     * Returns a managed {@link TimeLimiter} or creates a new one.
     * The configuration must have been added upfront via {@link #addConfiguration(String, Object)}.
     * <p>
     * The {@code tags} passed will be appended to the tags already configured for the registry.
     * When tags (keys) of the two collide the tags passed with this method will override the tags
     * of the registry.
     *
     * @param name       the name of the TimeLimiter
     * @param configName the name of the shared configuration
     * @param tags       tags added to the TimeLimiter
     * @return The {@link TimeLimiter}
     */
    TimeLimiter timeLimiter(String name, String configName,
        io.vavr.collection.Map<String, String> tags);

}

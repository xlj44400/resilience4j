/*
 *
 *  Copyright 2017 Robert Winkler, Lucas Lech, Mahmoud Romeh
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
package io.github.resilience4j.bulkhead.internal;

import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadConfig;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadRegistry;
import io.github.resilience4j.core.ConfigurationNotFoundException;
import io.github.resilience4j.core.RegistryStore;
import io.github.resilience4j.core.registry.AbstractRegistry;
import io.github.resilience4j.core.registry.InMemoryRegistryStore;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import io.vavr.collection.Array;
import io.vavr.collection.HashMap;
import io.vavr.collection.Seq;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Thread pool Bulkhead instance manager; Constructs/returns thread pool bulkhead instances.
 */
public final class InMemoryThreadPoolBulkheadRegistry extends
    AbstractRegistry<ThreadPoolBulkhead, ThreadPoolBulkheadConfig> implements
    ThreadPoolBulkheadRegistry {

    /**
     * The constructor with default default.
     */
    public InMemoryThreadPoolBulkheadRegistry() {
        this(HashMap.empty());
    }

    public InMemoryThreadPoolBulkheadRegistry(io.vavr.collection.Map<String, String> tags) {
        this(ThreadPoolBulkheadConfig.ofDefaults(), tags);
    }

    public InMemoryThreadPoolBulkheadRegistry(Map<String, ThreadPoolBulkheadConfig> configs) {
        this(configs, HashMap.empty());
    }

    public InMemoryThreadPoolBulkheadRegistry(Map<String, ThreadPoolBulkheadConfig> configs,
        io.vavr.collection.Map<String, String> tags) {
        this(configs.getOrDefault(DEFAULT_CONFIG, ThreadPoolBulkheadConfig.ofDefaults()), tags);
        this.configurations.putAll(configs);
    }

    public InMemoryThreadPoolBulkheadRegistry(
        Map<String, ThreadPoolBulkheadConfig> configs,
        RegistryEventConsumer<ThreadPoolBulkhead> registryEventConsumer) {
        this(configs, registryEventConsumer, HashMap.empty());
    }

    public InMemoryThreadPoolBulkheadRegistry(
        Map<String, ThreadPoolBulkheadConfig> configs,
        RegistryEventConsumer<ThreadPoolBulkhead> registryEventConsumer,
        io.vavr.collection.Map<String, String> tags) {
        this(configs.getOrDefault(DEFAULT_CONFIG, ThreadPoolBulkheadConfig.ofDefaults()),
            registryEventConsumer, tags);
        this.configurations.putAll(configs);
    }

    public InMemoryThreadPoolBulkheadRegistry(
        Map<String, ThreadPoolBulkheadConfig> configs,
        List<RegistryEventConsumer<ThreadPoolBulkhead>> registryEventConsumers) {
        this(configs, registryEventConsumers, HashMap.empty());
    }

    public InMemoryThreadPoolBulkheadRegistry(
        Map<String, ThreadPoolBulkheadConfig> configs,
        List<RegistryEventConsumer<ThreadPoolBulkhead>> registryEventConsumers,
        io.vavr.collection.Map<String, String> tags) {
        this(configs.getOrDefault(DEFAULT_CONFIG, ThreadPoolBulkheadConfig.ofDefaults()),
            registryEventConsumers, tags);
        this.configurations.putAll(configs);
    }

    /**
     * The constructor with custom default config.
     *
     * @param defaultConfig The default config.
     */
    public InMemoryThreadPoolBulkheadRegistry(ThreadPoolBulkheadConfig defaultConfig) {
        super(defaultConfig);
    }

    public InMemoryThreadPoolBulkheadRegistry(ThreadPoolBulkheadConfig defaultConfig,
        io.vavr.collection.Map<String, String> tags) {
        super(defaultConfig, tags);
    }

    public InMemoryThreadPoolBulkheadRegistry(
        ThreadPoolBulkheadConfig defaultConfig,
        RegistryEventConsumer<ThreadPoolBulkhead> registryEventConsumer) {
        super(defaultConfig, registryEventConsumer);
    }

    public InMemoryThreadPoolBulkheadRegistry(
        ThreadPoolBulkheadConfig defaultConfig,
        RegistryEventConsumer<ThreadPoolBulkhead> registryEventConsumer,
        io.vavr.collection.Map<String, String> tags) {
        super(defaultConfig, registryEventConsumer, tags);
    }

    public InMemoryThreadPoolBulkheadRegistry(
        ThreadPoolBulkheadConfig defaultConfig,
        List<RegistryEventConsumer<ThreadPoolBulkhead>> registryEventConsumers) {
        super(defaultConfig, registryEventConsumers);
    }

    public InMemoryThreadPoolBulkheadRegistry(
        ThreadPoolBulkheadConfig defaultConfig,
        List<RegistryEventConsumer<ThreadPoolBulkhead>> registryEventConsumers,
        io.vavr.collection.Map<String, String> tags) {
        super(defaultConfig, registryEventConsumers, tags);
    }

    public InMemoryThreadPoolBulkheadRegistry(Map<String, ThreadPoolBulkheadConfig> configs,
                                          List<RegistryEventConsumer<ThreadPoolBulkhead>> registryEventConsumers,
                                          io.vavr.collection.Map<String, String> tags, RegistryStore<ThreadPoolBulkhead> registryStore) {
        super(configs.getOrDefault(DEFAULT_CONFIG, ThreadPoolBulkheadConfig.ofDefaults()),
            registryEventConsumers, Optional.ofNullable(tags).orElse(HashMap.empty()),
            Optional.ofNullable(registryStore).orElse(new InMemoryRegistryStore<>()));
        this.configurations.putAll(configs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Seq<ThreadPoolBulkhead> getAllBulkheads() {
        return Array.ofAll(entryMap.values());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ThreadPoolBulkhead bulkhead(String name) {
        return bulkhead(name, HashMap.empty());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ThreadPoolBulkhead bulkhead(String name, io.vavr.collection.Map<String, String> tags) {
        return bulkhead(name, getDefaultConfig(), tags);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ThreadPoolBulkhead bulkhead(String name, ThreadPoolBulkheadConfig config) {
        return bulkhead(name, config, HashMap.empty());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ThreadPoolBulkhead bulkhead(String name, ThreadPoolBulkheadConfig config,
        io.vavr.collection.Map<String, String> tags) {
        return computeIfAbsent(name, () -> ThreadPoolBulkhead
            .of(name, Objects.requireNonNull(config, CONFIG_MUST_NOT_BE_NULL), getAllTags(tags)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ThreadPoolBulkhead bulkhead(String name,
        Supplier<ThreadPoolBulkheadConfig> bulkheadConfigSupplier) {
        return bulkhead(name, bulkheadConfigSupplier, HashMap.empty());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ThreadPoolBulkhead bulkhead(String name,
        Supplier<ThreadPoolBulkheadConfig> bulkheadConfigSupplier,
        io.vavr.collection.Map<String, String> tags) {
        return computeIfAbsent(name, () -> ThreadPoolBulkhead.of(name, Objects.requireNonNull(
            Objects.requireNonNull(bulkheadConfigSupplier, SUPPLIER_MUST_NOT_BE_NULL).get(),
            CONFIG_MUST_NOT_BE_NULL), getAllTags(tags)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ThreadPoolBulkhead bulkhead(String name, String configName) {
        return bulkhead(name, configName, HashMap.empty());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ThreadPoolBulkhead bulkhead(String name, String configName,
        io.vavr.collection.Map<String, String> tags) {
        return computeIfAbsent(name, () -> ThreadPoolBulkhead.of(name, getConfiguration(configName)
            .orElseThrow(() -> new ConfigurationNotFoundException(configName)), getAllTags(tags)));
    }

    @Override
    public void close() throws Exception {
        for (ThreadPoolBulkhead bulkhead : getAllBulkheads()) {
            bulkhead.close();
        }
    }
}

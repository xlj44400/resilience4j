/*
 *
 *  Copyright 2016 Robert Winkler and Bohdan Storozhuk
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
package io.github.resilience4j.ratelimiter.internal;

import io.github.resilience4j.core.registry.*;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.Mockito.*;

public class InMemoryRateLimiterRegistryTest {

    private static final int LIMIT = 50;
    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final Duration REFRESH_PERIOD = Duration.ofNanos(500);
    private static final String CONFIG_MUST_NOT_BE_NULL = "Config must not be null";
    private static final String NAME_MUST_NOT_BE_NULL = "Name must not be null";
    @Rule
    public ExpectedException exception = ExpectedException.none();
    private RateLimiterConfig config;


    @Before
    public void init() {
        config = RateLimiterConfig.custom()
            .timeoutDuration(TIMEOUT)
            .limitRefreshPeriod(REFRESH_PERIOD)
            .limitForPeriod(LIMIT)
            .build();
    }

    @Test
    public void rateLimiterPositive() throws Exception {
        RateLimiterRegistry registry = RateLimiterRegistry.of(config);
        RateLimiter firstRateLimiter = registry.rateLimiter("test");
        RateLimiter anotherLimit = registry.rateLimiter("test1");
        RateLimiter sameAsFirst = registry.rateLimiter("test");

        then(firstRateLimiter).isEqualTo(sameAsFirst);
        then(firstRateLimiter).isNotEqualTo(anotherLimit);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void rateLimiterPositiveWithSupplier() throws Exception {
        RateLimiterRegistry registry = new InMemoryRateLimiterRegistry(config);
        Supplier<RateLimiterConfig> rateLimiterConfigSupplier = mock(Supplier.class);
        when(rateLimiterConfigSupplier.get())
            .thenReturn(config);

        RateLimiter firstRateLimiter = registry.rateLimiter("test", rateLimiterConfigSupplier);
        verify(rateLimiterConfigSupplier, times(1)).get();
        RateLimiter sameAsFirst = registry.rateLimiter("test", rateLimiterConfigSupplier);
        verify(rateLimiterConfigSupplier, times(1)).get();
        RateLimiter anotherLimit = registry.rateLimiter("test1", rateLimiterConfigSupplier);
        verify(rateLimiterConfigSupplier, times(2)).get();

        then(firstRateLimiter).isEqualTo(sameAsFirst);
        then(firstRateLimiter).isNotEqualTo(anotherLimit);
    }

    @Test
    public void rateLimiterConfigIsNull() throws Exception {
        exception.expect(NullPointerException.class);
        exception.expectMessage(CONFIG_MUST_NOT_BE_NULL);
        new InMemoryRateLimiterRegistry((RateLimiterConfig) null);
    }

    @Test
    public void rateLimiterNewWithNullName() throws Exception {
        exception.expect(NullPointerException.class);
        exception.expectMessage(NAME_MUST_NOT_BE_NULL);
        RateLimiterRegistry registry = new InMemoryRateLimiterRegistry(config);
        registry.rateLimiter(null);
    }

    @Test
    public void rateLimiterNewWithNullNonDefaultConfig() throws Exception {
        exception.expect(NullPointerException.class);
        exception.expectMessage(CONFIG_MUST_NOT_BE_NULL);
        RateLimiterRegistry registry = new InMemoryRateLimiterRegistry(config);
        RateLimiterConfig rateLimiterConfig = null;
        registry.rateLimiter("name", rateLimiterConfig);
    }

    @Test
    public void rateLimiterNewWithNullNameAndNonDefaultConfig() throws Exception {
        exception.expect(NullPointerException.class);
        exception.expectMessage(NAME_MUST_NOT_BE_NULL);
        RateLimiterRegistry registry = new InMemoryRateLimiterRegistry(config);
        registry.rateLimiter(null, config);
    }

    @Test
    public void rateLimiterNewWithNullNameAndConfigSupplier() throws Exception {
        exception.expect(NullPointerException.class);
        exception.expectMessage(NAME_MUST_NOT_BE_NULL);
        RateLimiterRegistry registry = new InMemoryRateLimiterRegistry(config);
        registry.rateLimiter(null, () -> config);
    }

    @Test
    public void rateLimiterNewWithNullConfigSupplier() throws Exception {
        exception.expect(NullPointerException.class);
        exception.expectMessage("Supplier must not be null");
        RateLimiterRegistry registry = new InMemoryRateLimiterRegistry(config);
        Supplier<RateLimiterConfig> rateLimiterConfigSupplier = null;
        registry.rateLimiter("name", rateLimiterConfigSupplier);
    }

    @Test
    public void rateLimiterGetAllRateLimiters() {
        RateLimiterRegistry registry = new InMemoryRateLimiterRegistry(config);
        registry.rateLimiter("foo");

        assertThat(registry.getAllRateLimiters().size()).isEqualTo(1);
        assertThat(registry.getAllRateLimiters().get(0).getName()).isEqualTo("foo");
    }

    @Test
    public void shouldCreateRateLimiterRegistryWithRegistryStore() {
        RegistryEventConsumer<RateLimiter> registryEventConsumer = getNoOpsRegistryEventConsumer();
        List<RegistryEventConsumer<RateLimiter>> registryEventConsumers = new ArrayList<>();
        registryEventConsumers.add(registryEventConsumer);
        Map<String, RateLimiterConfig> configs = new HashMap<>();
        final RateLimiterConfig defaultConfig = RateLimiterConfig.ofDefaults();
        configs.put("default", defaultConfig);
        final InMemoryRateLimiterRegistry inMemoryRateLimiterRegistry =
            new InMemoryRateLimiterRegistry(configs, registryEventConsumers,
                io.vavr.collection.HashMap.of("Tag1", "Tag1Value"), new InMemoryRegistryStore());

        AssertionsForClassTypes.assertThat(inMemoryRateLimiterRegistry).isNotNull();
        AssertionsForClassTypes.assertThat(inMemoryRateLimiterRegistry.getDefaultConfig()).isEqualTo(defaultConfig);
        AssertionsForClassTypes.assertThat(inMemoryRateLimiterRegistry.getConfiguration("testNotFound")).isEmpty();
        inMemoryRateLimiterRegistry.addConfiguration("testConfig", defaultConfig);
        AssertionsForClassTypes.assertThat(inMemoryRateLimiterRegistry.getConfiguration("testConfig")).isNotNull();
    }

    private RegistryEventConsumer<RateLimiter> getNoOpsRegistryEventConsumer() {
        return new RegistryEventConsumer<RateLimiter>() {
            @Override
            public void onEntryAddedEvent(EntryAddedEvent<RateLimiter> entryAddedEvent) {
            }
            @Override
            public void onEntryRemovedEvent(EntryRemovedEvent<RateLimiter> entryRemoveEvent) {
            }
            @Override
            public void onEntryReplacedEvent(EntryReplacedEvent<RateLimiter> entryReplacedEvent) {
            }
        };
    }

}
/*
 * Copyright 2017 Jan Sykora
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.resilience4j.ratpack.bulkhead.monitoring.endpoint;

import io.github.resilience4j.bulkhead.event.BulkheadEvent;
import io.github.resilience4j.common.bulkhead.monitoring.endpoint.BulkheadEventDTOFactory;
import io.github.resilience4j.common.bulkhead.monitoring.endpoint.BulkheadEventsEndpointResponse;
import io.github.resilience4j.consumer.CircularEventConsumer;
import io.github.resilience4j.consumer.EventConsumerRegistry;
import io.github.resilience4j.ratpack.Resilience4jConfig;
import io.vavr.collection.Seq;
import ratpack.exec.Promise;
import ratpack.func.Action;
import ratpack.func.Function;
import ratpack.handling.Chain;
import ratpack.jackson.Jackson;
import ratpack.sse.ServerSentEvents;
import reactor.core.publisher.Flux;

import javax.inject.Inject;
import java.util.Comparator;

/**
 * Provides event and stream event endpoints for bulkhead events.
 */
public class BulkheadChain implements Action<Chain> {

    private final EventConsumerRegistry<BulkheadEvent> eventConsumerRegistry;

    @Inject
    public BulkheadChain(EventConsumerRegistry<BulkheadEvent> eventConsumerRegistry) {
        this.eventConsumerRegistry = eventConsumerRegistry;
    }

    @Override
    public void execute(Chain chain) throws Exception {
        String prefix = chain.getRegistry().get(Resilience4jConfig.class).getEndpoints()
            .getBulkhead().getPath();
        chain.prefix(prefix, chain1 -> {
            chain1.get("events", ctx ->
                Promise.<BulkheadEventsEndpointResponse>async(d -> {
                    BulkheadEventsEndpointResponse response = new BulkheadEventsEndpointResponse(
                        eventConsumerRegistry
                            .getAllEventConsumer()
                            .flatMap(CircularEventConsumer::getBufferedEvents)
                            .sorted(Comparator.comparing(BulkheadEvent::getCreationTime))
                            .map(BulkheadEventDTOFactory::createBulkheadEventDTO).toJavaList());
                    d.success(response);
                }).then(r -> ctx.render(Jackson.json(r)))
            );
            chain1.get("stream/events", ctx -> {
                Seq<Flux<BulkheadEvent>> eventStreams = eventConsumerRegistry
                    .getAllEventConsumer()
                    .flatMap(CircularEventConsumer::getBufferedEvents)
                    .map(Flux::just);
                ctx.render(serverSentEvents(chain1, eventStreams));
            });
            chain1.get("events/:name", ctx -> {
                    String bulkheadName = ctx.getPathTokens().get("name");
                    Promise.<BulkheadEventsEndpointResponse>async(d -> {
                        BulkheadEventsEndpointResponse response = new BulkheadEventsEndpointResponse(
                            eventConsumerRegistry
                                .getEventConsumer(bulkheadName)
                                .getBufferedEvents()
                                .map(BulkheadEventDTOFactory::createBulkheadEventDTO).toJavaList());
                        d.success(response);
                    }).then(r -> ctx.render(Jackson.json(r)));
                }
            );
            chain1.get("stream/events/:name", ctx -> {
                String bulkheadName = ctx.getPathTokens().get("name");
                Seq<Flux<BulkheadEvent>> eventStreams = eventConsumerRegistry
                    .getAllEventConsumer()
                    .flatMap(CircularEventConsumer::getBufferedEvents)
                    .filter(e -> e.getBulkheadName().equals(bulkheadName))
                    .map(Flux::just);
                ctx.render(serverSentEvents(chain1, eventStreams));
            });
            chain1.get("events/:name/:type", ctx -> {
                    String bulkheadName = ctx.getPathTokens().get("name");
                    String eventType = ctx.getPathTokens().get("type");
                    Promise.<BulkheadEventsEndpointResponse>async(d -> {
                        BulkheadEventsEndpointResponse response = new BulkheadEventsEndpointResponse(
                            eventConsumerRegistry
                                .getEventConsumer(bulkheadName)
                                .getBufferedEvents()
                                .filter(event -> event.getEventType() == BulkheadEvent.Type
                                    .valueOf(eventType.toUpperCase()))
                                .map(BulkheadEventDTOFactory::createBulkheadEventDTO).toJavaList());
                        d.success(response);
                    }).then(r -> ctx.render(Jackson.json(r)));
                }
            );
            chain1.get("stream/events/:name/:type", ctx -> {
                String bulkheadName = ctx.getPathTokens().get("name");
                String eventType = ctx.getPathTokens().get("type");
                Seq<Flux<BulkheadEvent>> eventStreams = eventConsumerRegistry
                    .getAllEventConsumer()
                    .flatMap(CircularEventConsumer::getBufferedEvents)
                    .filter(e -> e.getBulkheadName().equals(bulkheadName))
                    .filter(e -> e.getEventType().name().equals(eventType))
                    .map(Flux::just);
                ctx.render(serverSentEvents(chain1, eventStreams));
            });
        });
    }

    private ServerSentEvents serverSentEvents(Chain chain, Seq<Flux<BulkheadEvent>> eventStreams) {
        Function<BulkheadEvent, String> data = b -> Jackson.getObjectWriter(chain.getRegistry())
            .writeValueAsString(BulkheadEventDTOFactory.createBulkheadEventDTO(b));
        return ServerSentEvents.serverSentEvents(Flux.merge(eventStreams),
            e -> e.id(BulkheadEvent::getBulkheadName).event(c -> c.getEventType().name())
                .data(data));
    }
}

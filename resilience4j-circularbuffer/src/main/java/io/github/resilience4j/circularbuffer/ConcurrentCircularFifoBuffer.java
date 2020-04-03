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
package io.github.resilience4j.circularbuffer;

import io.vavr.collection.List;
import io.vavr.control.Option;

import java.util.Arrays;

/**
 * Thread safe implementation of {@link CircularFifoBuffer} on top of {@link
 * ConcurrentEvictingQueue}
 **/
public class ConcurrentCircularFifoBuffer<T> implements CircularFifoBuffer<T> {

    private final ConcurrentEvictingQueue<T> queue;
    private final int capacity;

    /**
     * Creates an {@code ConcurrentCircularFifoBuffer} with the given (fixed) capacity
     *
     * @param capacity the capacity of this {@code ConcurrentCircularFifoBuffer}
     * @throws IllegalArgumentException if {@code capacity < 1}
     */
    public ConcurrentCircularFifoBuffer(int capacity) {
        this.capacity = capacity;
        queue = new ConcurrentEvictingQueue<>(capacity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return queue.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFull() {
        return queue.size() == capacity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<T> toList() {
        T[] elementsArray = (T[]) queue.toArray();
        return List.ofAll(Arrays.asList(elementsArray));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(T element) {
        queue.offer(element);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Option<T> take() {
        return Option.of(queue.poll());
    }
}
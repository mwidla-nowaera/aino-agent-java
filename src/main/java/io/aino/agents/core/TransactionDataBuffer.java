/*
 *  Copyright 2016 Aino.io
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.aino.agents.core;



import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Buffer for holding {@link TransactionSerializable} objects to be sent.
 */
public class TransactionDataBuffer {
    private final ObjectMapper mapper = new ObjectMapper();
    private final List<TransactionDataObserver> observers = new ArrayList<TransactionDataObserver>();

    private final LinkedBlockingDeque<TransactionSerializable> transactions = new LinkedBlockingDeque<TransactionSerializable>();
    private final Lock lock = new ReentrantLock();

    private final int sizeThreshold;

    public TransactionDataBuffer(int sizeThreshold) {
        this.sizeThreshold = sizeThreshold;
    }

    /**
     * Adds observer for listening to size changes in buffer.
     * @param observer observer
     */
    public void addLogDataSizeObserver(TransactionDataObserver observer) {
        observers.add(observer);
    }

    /**
     * Adds serializable version log entry to the buffer.
     * @param entry serializable log entry
     */
    public void addTransaction(TransactionSerializable entry) {

        this.transactions.addFirst(entry);

        if(lock.tryLock()) {
            int currentSize = this.getSize();
            try {
                int observerCount = observers.size();

                for(int i = 0; i < observerCount; i++) {
                    TransactionDataObserver observer = observers.get(i);
                    if(observer != null) {
                        observer.logDataAdded(currentSize);
                    }
                }

            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * Returns the entries serialized as string and clears this buffer.
     *
     * @return serializable log entries
     * @throws IOException when json serialization fails
     */
    public String getDataToSend() throws IOException {
        final List<TransactionSerializable> entries = new ArrayList<TransactionSerializable>();
        this.transactions.drainTo(entries, elementsToDrain());
        return mapper.writeValueAsString(new Object() {
            private final List<TransactionSerializable> transactions = entries;

            public List<TransactionSerializable> getTransactions() { return transactions; } // Needed for mapper?
        });
    }

    public LinkedBlockingDeque<TransactionSerializable> getTransactions() { return transactions; }

    /**
     * Checks if this buffer is empty.
     *
     * @return true if empty
     */
    public boolean isEmpty() {
        return this.transactions.isEmpty();
    }

    /**
     * Checks if this buffer contains entries.
     *
     * @return true if contains data
     */
    public boolean containsData() {
        return !isEmpty();
    }

    /**
     * Gets the number of entries in this buffer.
     *
     * @return entry count
     */
    public int getSize() {
        return this.transactions.size();
    }

    private int elementsToDrain() {
        // ensure transactions get sent one at a time when size threshold is zero or one
        return sizeThreshold <= 1 ? 1 : Integer.MAX_VALUE;
    }

}

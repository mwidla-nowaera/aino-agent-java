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

package io.aino.agents.core.overloadchecker;

import io.aino.agents.core.TransactionDataBuffer;
import io.aino.agents.core.config.AgentConfig;

import java.util.TimerTask;

/**
 * Timer task to check if send queue is well above size threshold.
 * If buffer size is more than 30% bigger than the size threshold, increase sending threads.
 * This is necessary when system is generating lots of messages and connection to aino.io is slow:
 * send buffer size increases and transactions get bigger... which leads bigger and bigger HTTP post requests.
 */
public class SenderOverloadCheckerTask extends TimerTask {

    private final TransactionDataBuffer buffer;
    private final AgentConfig config;
    private ThreadAmountObserver observer;

    /**
     * Constructor.
     * @param obs observer to pass the info on queue state
     * @param buffer buffer to check
     * @param config agent configuration
     */
    public SenderOverloadCheckerTask(ThreadAmountObserver obs, TransactionDataBuffer buffer, AgentConfig config) {
        this.buffer = buffer;
        this.config = config;
        this.observer = obs;
    }

    @Override
    public void run() {
        if(buffer.isEmpty())
            return;

        if(config.getSizeThreshold() < buffer.getSize() * 1.3) {
            observer.increaseThreads();
        }
    }

}

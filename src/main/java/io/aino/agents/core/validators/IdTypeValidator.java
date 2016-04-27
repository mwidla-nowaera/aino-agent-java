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

package io.aino.agents.core.validators;

import io.aino.agents.core.Transaction;
import io.aino.agents.core.config.AgentConfig;
import io.aino.agents.core.AgentCoreException;

import java.util.List;
import java.util.Map;

/**
 * Validator for {@link Transaction} id types.
 * Checks that log entry's id types are configured to the agent.
 */
public class IdTypeValidator implements TransactionValidator {

    private AgentConfig config;

    /**
     * Constructor.
     *
     * @param agentConfig agent configuration
     */
    public IdTypeValidator(AgentConfig agentConfig) {
        this.config = agentConfig;
    }

    @Override
    public void validate(Transaction entry) {

        for(Map.Entry<String, List<String>> val : entry.getIds().entrySet()) {
            if(!config.getIdTypes().entryExists(val.getKey())) {
                throw new AgentCoreException("IdType not found: " + val.getKey());
            }
        }
    }
}

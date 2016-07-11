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

/**
 * Validator for {@link Transaction}'s operation.
 * Checks that operation is configured to the agent.
 */
public class OperationValidator implements TransactionValidator {

    private AgentConfig config;

    /**
     * Constructor.
     *
     * @param conf agent configuration
     */
    public OperationValidator(AgentConfig conf) {
        this.config = conf;
    }

    @Override
    public void validate(Transaction entry) {
        if(null != entry.getOperationKey() && !config.getOperations().entryExists(entry.getOperationKey()))
            throw new AgentCoreException("Operation does not exist: " + entry.getOperationKey());
    }
}

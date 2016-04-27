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
import io.aino.agents.core.AgentCoreException;
import io.aino.agents.core.config.AgentConfig;
import org.apache.commons.lang3.StringUtils;

/**
 * Validator for {@link Transaction}'s application.
 * Checks that log entry has 'from' and 'to' applications,
 * checks that both application keys are configured.
 */
public class ApplicationValidator implements TransactionValidator {

    private AgentConfig config;

    /**
     * Constructor.
     * @param conf agent configuration
     */
    public ApplicationValidator(AgentConfig conf) {
        this.config = conf;
    }

    @Override
    public void validate(Transaction entry) {
        validateFromToExists(entry);
        validateFromApplication(entry);
        validateToApplication(entry);
    }

    private void validateFromToExists(Transaction entry) {
        if(StringUtils.isBlank(entry.getFromKey())) {
            throw new AgentCoreException("from does not exist!");
        }
        if(StringUtils.isBlank(entry.getToKey())) {
            throw new AgentCoreException("to does not exist!");
        }
    }

    private void validateFromApplication(Transaction entry) {
        if(!config.getApplications().entryExists(entry.getFromKey())) {
            throw new AgentCoreException("from application does not exist: " + entry.getFromKey());
        }
    }

    private void validateToApplication(Transaction entry) {
        if(!config.getApplications().entryExists(entry.getToKey())) {
            throw new AgentCoreException("to application does not exist: " + entry.getToKey());
        }
    }
}

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
import io.aino.agents.core.Agent;
import io.aino.agents.core.config.ClasspathResourceConfigBuilder;
import io.aino.agents.core.AgentCoreException;
import org.junit.Before;
import org.junit.Test;

public class OperationValidatorTest {

    private Agent agent;

    @Before
    public void setUp() {
        agent = Agent
                .getFactory()
                .setConfigurationBuilder(new ClasspathResourceConfigBuilder("validConfig.xml"))
                .build();
    }

    @Test
    public void testDoesNotThrowWithMissingOperation() {
        Transaction tle = new Transaction(agent.getAgentConfig());
        tle.setFromKey("esb");
        tle.setToKey("app01");

        agent.addTransaction(tle);
    }

    @Test
    public void testDoesNotThrowWithValidOperation() {
        Transaction tle = new Transaction(agent.getAgentConfig());
        tle.setFromKey("esb");
        tle.setToKey("app01");
        tle.setOperationKey("create");

        agent.addTransaction(tle);
    }

    @Test(expected = AgentCoreException.class)
    public void testThrowsWithInvalidOperation() {
        Transaction tle = new Transaction(agent.getAgentConfig());
        tle.setFromKey("esb");
        tle.setToKey("app01");
        tle.setOperationKey("updaeet");

        agent.addTransaction(tle);
    }
}

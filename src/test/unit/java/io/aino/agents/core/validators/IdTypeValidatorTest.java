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

import io.aino.agents.core.Agent;
import io.aino.agents.core.Transaction;
import io.aino.agents.core.AgentCoreException;
import io.aino.agents.core.config.ClasspathResourceConfigBuilder;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class IdTypeValidatorTest {

    private Agent agent;
    private Transaction tle;

    @Before
    public void setUp() {
        agent = Agent
                .getFactory()
                .setConfigurationBuilder(new ClasspathResourceConfigBuilder("validConfig.xml"))
                .build();

        tle = new Transaction(agent.getAgentConfig());
        tle.setFromKey("esb");
        tle.setToKey("app01");
        tle.setOperationKey("create");
    }

    @Test
    public void testDoesNotThrowWithValidIdType() {
        tle.addIdsByTypeKey("dataType01", new ArrayList<String>() {{ this.add("441"); }});

        agent.addTransaction(tle);
    }

    @Test(expected = AgentCoreException.class)
    public void testThrowsWithInvalidIdType() {
        tle.addIdsByTypeKey("invalidDataType", new ArrayList<String>() {{ this.add("123"); this.add("6666"); }});

        agent.addTransaction(tle);
    }

    @Test
    public void testDoesNotThrowWithMultipleValidIdTypes() {
        List<String> idList1 = tle.addIdTypeKey("dataType01");
        List<String> idList2 = tle.addIdTypeKey("dataType02");

        idList1.add("111");
        idList2.add("555");

        agent.addTransaction(tle);
    }

    @Test(expected = AgentCoreException.class)
    public void testThrowsWithOneValidAndOneInvalidIdType() {
        List<String> idListValid = tle.addIdTypeKey("dataType01");
        List<String> idListInvalid = tle.addIdTypeKey("Invalid");

        idListInvalid.add("661");
        idListValid.add("11");

        agent.addTransaction(tle);
    }

}

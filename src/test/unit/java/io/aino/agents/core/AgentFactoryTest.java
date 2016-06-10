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

import io.aino.agents.core.config.FileConfigBuilder;
import io.aino.agents.core.config.InvalidAgentConfigException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Parameterized.class)
public class AgentFactoryTest {

    @Parameters(name= "{index}: ({0}) should be a valid configuration file: {1}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {"src/test/resources/validConfig.xml", true},
                {"src/test/resources/validConfigWithIntervalAndSize.xml", true},
                {"src/test/resources/validConfigWithProxy.xml", true},
                {"src/test/resources/invalidConfig.xml", false},
        });
    }

    @Parameter(0)
    public String fileName;

    @Parameter(1)
    public Boolean expectedValidity;

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testGetFactoryWorksWithValidConfigurationFile() throws Exception {
        Agent agent = null;

        try {
            Agent.LoggerFactory factory = Agent.getFactory();
            factory.setConfigurationBuilder(new FileConfigBuilder(new File(fileName)));
            agent = factory.build();
        } catch(Exception e){
            //it happens
        }

        assertEquals(expectedValidity, null != agent);
    }

}
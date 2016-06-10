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

package io.aino.agents.core.config;

import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ConfigBuilderTest {

    @Test
    public void testConfigBuilderDoesNotThrowWithValidConf() {
        AgentConfig conf = new ClasspathResourceConfigBuilder("validConfig.xml").build();

        assertNotNull("AgentConfig object should not be null", conf);
    }

    @Test(expected = InvalidAgentConfigException.class)
    public void testConfigBuilderThrowsWithInvalidConf() {
        new ClasspathResourceConfigBuilder("invalidConfig.xml").build();
    }

    @Test
    public void testFileConfigBuilderLoadsConfigWithValidConf() throws FileNotFoundException {
        File file = new File(this.getClass().getClassLoader().getResource("validConfig.xml").getPath());
        AgentConfig conf = new FileConfigBuilder(file).build();

        assertNotNull("AgentConfig object should not be null", conf);
    }

    @Test
    public void testConfigBuilderPopulatesServiceConfigs() {
        AgentConfig conf = new ClasspathResourceConfigBuilder("validConfig.xml").build();

        assertEquals("addressUri is correct", "http://localhost:8808/api/1.0/saveLogArray", conf.getLogServiceUri());
        assertEquals("apiKey is correct", "80D0710C-2EE6-481E-BA9E-9A21C2486EE7", conf.getApiKey());
        assertEquals("sendInterval is correct", 1000, conf.getSendInterval());
        assertEquals("sizeThreshold is correct", 0, conf.getSizeThreshold());
    }

    @Test
    public void testConfigBuilderPopulatesOperationConfigs() {
        AgentConfig conf = new ClasspathResourceConfigBuilder("validConfig.xml").build();

        assertEquals("create operation exists with name 'Create'", "Create", conf.getOperations().getEntry("create"));
        assertEquals("update operation exists with name 'Update'", "Update", conf.getOperations().getEntry("update"));
        assertEquals("delete operation exists with name 'Delete'", "Delete", conf.getOperations().getEntry("delete"));
    }

    @Test
    public void testConfigBuilderPopulatesIdTypeConfigs() {
        AgentConfig conf = new ClasspathResourceConfigBuilder("validConfig.xml").build();

        assertEquals("'dataType01' idType exists with name 'Data Type 1", "Data Type 1", conf.getIdTypes().getEntry("dataType01"));
        assertEquals("'dataType02' idType exists with name 'Data Type 5", "Data Type 5", conf.getIdTypes().getEntry("dataType02"));
    }

    @Test
    public void testConfigBuilderPopulatesApplicationConfigs() {
        AgentConfig conf = new ClasspathResourceConfigBuilder("validConfig.xml").build();

        assertEquals("application with name 'ESB' exists with key 'esb'", "ESB", conf.getApplications().getEntry("esb"));
        assertEquals("application with name 'TestApp 1' exists with key 'app01'", "TestApp 1", conf.getApplications().getEntry("app01"));

    }

    @Test
    public void testConfigBuilderWorksWithProxyDefinition() {
        AgentConfig confWithoutProxy = new ClasspathResourceConfigBuilder("validConfig.xml").build();
        assertEquals("proxy should not be defined", false, confWithoutProxy.isProxyDefined());

        AgentConfig confWithProxy = new ClasspathResourceConfigBuilder("validConfigWithProxy.xml").build();
        assertEquals("proxy should be defined", true, confWithProxy.isProxyDefined());
        assertEquals("proxy host should be set", "127.0.0.1", confWithProxy.getProxyHost());
        assertEquals("proxy port should be set", 8080, confWithProxy.getProxyPort());
    }

}

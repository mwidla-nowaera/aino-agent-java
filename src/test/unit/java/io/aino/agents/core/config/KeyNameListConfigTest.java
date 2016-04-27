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

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assume.assumeTrue;

public class KeyNameListConfigTest {

    private KeyNameListConfig object;

    @Before
    public void setUp() {
        object = new KeyNameListConfig();
    }

    @Test
    public void testAddEntryWorks() {
        object.addEntry("test1", "test2");
        assertEquals("element with key 'test1' exists with value 'test2", "test2", object.getEntry("test1"));
        assertEquals("element with key 'test3' does not exist", null, object.getEntry("test3"));
    }

    @Test
    public void testAddEntriesWorks() {
        HashMap<String, String> entries = new HashMap<String, String>();
        entries.put("test1", "test11");
        entries.put("test666", "test6661");

        object.addEntries(entries);

        assertEquals("element with key 'test1' exists with value 'test11'", "test11", object.getEntry("test1"));
        assertEquals("element with key 'test666' exists with value 'test6661'", "test6661", object.getEntry("test666"));
        assertEquals("element with key 'argh' does not exist", null, object.getEntry("argh"));
    }

    @Test
    public void testEntryExistsWorks() {
        object.addEntry("test9", "test991");
        assertTrue("entryExists() returns true", object.entryExists("test9"));
        assertFalse("entryExists() returns false", object.entryExists("test911"));
    }

    @Test(expected = InvalidAgentConfigException.class)
    public void testThrowsWithDuplicateEntry() {
        object.addEntry("hoi", ":D");
        object.addEntry("hoi", ":D");
    }
}

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

import java.util.HashMap;
import java.util.Map;

/**
 * Class for holding key-value pairs.
 */
public class KeyNameListConfig {
    private Map<String, String> entries = new HashMap<String, String>();

    /**
     * Returns value based on key.
     *
     * @param key key to search for
     * @return value corresponding to key
     */
    public String getEntry(String key){
        return this.entries.get(key);
    }

    /**
     * Adds key-value pair to this object.
     * @param key key
     * @param value value
     */
    public void addEntry(String key, String value){
        if(this.entryExists(key)) {
            throw new InvalidAgentConfigException("key " + key + "already exists.");
        }
        this.entries.put(key, value);
    }

    /**
     * Batch add multiple key-value pairs from Map.
     *
     * @param operationsMap Map containing key-value pairs to be added.
     */
    public void addEntries(Map<String, String> operationsMap){
        this.entries.putAll(operationsMap);
    }

    /**
     * Checks if key exists.
     *
     * @param key key to check
     * @return true if key was found
     */
    public boolean entryExists(String key){
        return this.entries.containsKey(key);
    }

    /**
     * Checks if value exists.
     *
     * @param name value to check
     * @return true if value was found
     */
    public boolean nameExists(String name) {
        return this.entries.containsValue(name);
    }

}

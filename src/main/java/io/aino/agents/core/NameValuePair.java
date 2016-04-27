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

import org.apache.commons.lang3.StringUtils;

/**
 * Class representing name-value pair.
 *
 */
public class NameValuePair {
    private final String name;
    private final String value;

    private final int hash;

    /**
     * Default constructor.
     *
     * @param name name of the name-value pair
     * @param value value of the name-value pair
     */
    public NameValuePair(String name, String value) {
        this.name = name;
        this.value = value;

        this.hash = (name + value).hashCode();
    }

    /**
     * Gets name part.
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets value part.
     *
     * @return value
     */
    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if(null == obj) {
            return false;
        }

        if(!(obj instanceof NameValuePair)) {
            return false;
        }

        NameValuePair other = (NameValuePair) obj;

        if(!StringUtils.equals(this.name, other.name)){
            return false;
        }

        if(!StringUtils.equals(this.value, other.value)){
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return hash;
    }

}

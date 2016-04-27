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

import java.io.InputStream;

/**
 * Class for reading configuration file from classpath resource.
 */
public class ClasspathResourceConfigBuilder extends InputStreamConfigBuilder {

    /**
     * Constructor.
     *
     * @param resourceName xml resource in classpath
     */
    public ClasspathResourceConfigBuilder(String resourceName) {
        super(readClassPathResource(resourceName));
    }

    private static InputStream readClassPathResource(String resourceName) {
        return ClasspathResourceConfigBuilder.class.getClassLoader().getResourceAsStream(resourceName);
    }
}

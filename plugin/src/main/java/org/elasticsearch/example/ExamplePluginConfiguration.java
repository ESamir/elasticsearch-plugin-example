/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.elasticsearch.example;

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;

import java.io.IOException;
import java.nio.file.Path;

/**
 *
 */
public class ExamplePluginConfiguration
{
    private static final String CONFIG_FILE = "elasticsearch-plugin-example/config.yml";

    private Settings settings;

    @Inject
    public ExamplePluginConfiguration(Environment environment) throws IOException {
        Path path = environment.configFile().resolve(CONFIG_FILE);
        settings = Settings.builder().loadFromPath(path).build();
    }

    public Settings getSettings() {
        return settings;
    }
}

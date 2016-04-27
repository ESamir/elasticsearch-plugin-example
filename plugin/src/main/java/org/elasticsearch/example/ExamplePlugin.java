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

import org.elasticsearch.common.component.LifecycleComponent;
import org.elasticsearch.common.inject.AbstractModule;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.example.rest.ExamplePluginRestAction;
import org.elasticsearch.example.service.ExampleService;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.rest.RestModule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Example elasticsearch plugin.
 */
public class ExamplePlugin extends Plugin
{

    @Override
    public String name() {
        return "elasticsearch-example-plugin";
    }

    @Override
    public String description() {
        return "A Simple Elasticsearch Plugin";
    }

    @Override
    public Collection<Module> nodeModules() {
        return Collections.<Module>singletonList(new ExamplePluginConfigurationModule());
    }

    @Override
    public Collection<Class<? extends LifecycleComponent>> nodeServices() {
        Collection<Class<? extends LifecycleComponent>> services = new ArrayList<>();
        services.add(ExampleService.class);
        return services;
    }

    public void onModule(RestModule module) {
        module.addRestAction(ExamplePluginRestAction.class);
    }

    public static class ExamplePluginConfigurationModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(ExamplePluginConfiguration.class).asEagerSingleton();
        }
    }
}

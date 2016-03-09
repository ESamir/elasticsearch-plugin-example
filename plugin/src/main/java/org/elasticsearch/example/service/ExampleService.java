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
package org.elasticsearch.example.service;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.example.ExamplePluginConfiguration;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchService;

/**
 * Simple example service to demonstrate injecting various internal core services.
 */
public class ExampleService extends AbstractLifecycleComponent<ExampleService>
{
    private final Settings pluginSettings;
    private final ClusterService clusterService;
    private final SearchService searchService;

    @Inject
    public ExampleService(Settings settings, ExamplePluginConfiguration config, ClusterService clusterService,
                          SearchService searchService) {
        super(settings);
        this.pluginSettings = config.getSettings();
        this.clusterService = clusterService;
        this.searchService = searchService;
    }

    public SearchResponse process(final SearchResponse response) {

        for (SearchHit hit : response.getHits()) {
            // XXX - Do something interesting here.
        }

        return response;
    }

    @Override
    protected void doStart() {
        logger.info("Starting example service");
    }

    @Override
    protected void doStop() {
        logger.info("Stopping example service");
    }

    @Override
    protected void doClose() {
        logger.info("Closing example service");
    }
}

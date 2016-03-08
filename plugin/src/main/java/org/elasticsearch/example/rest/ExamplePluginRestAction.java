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
package org.elasticsearch.example.rest;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.example.service.ExampleService;
import org.elasticsearch.rest.*;
import org.elasticsearch.common.inject.Inject;

import static org.elasticsearch.rest.RestRequest.Method.GET;

/**
 * REST endpoint for example plugin.
 */
public class ExamplePluginRestAction extends BaseRestHandler
{
    private final ExampleService service;

    @Inject
    public ExamplePluginRestAction(Settings settings, RestController controller, Client client, ExampleService service) {
        super(settings, controller, client);
        this.service = service;
        controller.registerHandler(GET, "/_example", this);
        controller.registerHandler(GET, "/_example/{query}", this);
    }

    @Override
    protected void handleRequest(RestRequest request, RestChannel channel, Client client) throws Exception {

        String query = request.param("query");

        if (query == null || query.isEmpty()) {
            channel.sendResponse(new BytesRestResponse(RestStatus.OK, "Hey there, you didn't ask me to do anything. You are silly.\n"));
            return;
        }

        logger.debug("Received request: [{}]", query);
        String s = service.execute(query);
        channel.sendResponse(new BytesRestResponse(RestStatus.OK, "Thank you for your service. I am: " + s));
    }
}

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

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.example.service.ExampleService;
import org.elasticsearch.rest.*;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.rest.action.search.RestSearchAction;

import java.io.IOException;

import static org.elasticsearch.rest.RestRequest.Method.GET;

/**
 * REST endpoint for example plugin.
 */
public class ExamplePluginRestAction extends BaseRestHandler
{
    private final ExampleService service;

    @Inject
    public ExamplePluginRestAction(Settings settings,
                                   RestController controller,
                                   Client client,
                                   ExampleService service) {
        super(settings, controller, client);
        this.service = service;
        controller.registerHandler(GET, "/{index}/{type}/_example", this);
        controller.registerHandler(GET, "/{index}/_example", this);
    }

    @Override
    protected void handleRequest(RestRequest request, RestChannel channel, Client client) throws Exception {

        SearchRequest searchRequest = RestSearchAction.parseSearchRequest(request, parseFieldMatcher);

        client.search(searchRequest, new ActionListener<SearchResponse>() {
            @Override
            public void onResponse(SearchResponse searchResponse) {

                SearchResponse modified = service.process(searchResponse);

                try {
                    channel.sendResponse(new BytesRestResponse(RestStatus.OK, json(modified, channel)));
                }
                catch (IOException e) {
                    logger.error("Failed to process search response", e);
                    onFailure(e);
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                channel.sendResponse(new BytesRestResponse(RestStatus.INTERNAL_SERVER_ERROR, "Go eat a lemon."));
            }
        });
    }

    private XContentBuilder json(SearchResponse response, RestChannel channel) throws IOException {
        XContentBuilder builder = channel.newBuilder();
        builder.prettyPrint();
        builder.startObject();
        response.toXContent(builder, channel.request());
        builder.endObject();
        return builder;
    }
}

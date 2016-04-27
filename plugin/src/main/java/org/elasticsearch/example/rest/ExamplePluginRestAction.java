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
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.example.service.ExampleService;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.elasticsearch.rest.RestRequest.Method.PUT;
import static org.elasticsearch.rest.RestRequest.Method.POST;

/**
 * REST endpoint for example plugin.
 */
public class ExamplePluginRestAction extends BaseRestHandler
{
    private final ExampleService service;

    @Inject
    public ExamplePluginRestAction(Settings settings, RestController controller, Client client,
                                   ExampleService service) {
        super(settings, controller, client);
        this.service = service;
        controller.registerHandler(PUT, "/_example", this);
        controller.registerHandler(POST, "/_example", this);
    }

    @Override
    protected void handleRequest(RestRequest request, RestChannel channel, Client client) throws Exception {

        BytesReference content = request.content();
        List<Tuple<String, String>> textToAnalyze = new ArrayList<>();

        try (XContentParser parser = XContentFactory.xContent(content).createParser(content))
        {
            String currentFieldName = null;
            XContentParser.Token token = parser.currentToken();

            while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
                if (token == XContentParser.Token.FIELD_NAME) {
                    currentFieldName = parser.currentName();
                }
                else if (token.isValue()) {
                    textToAnalyze.add(new Tuple<>(currentFieldName, parser.text()));
                }
            }
        }

        // Finished parsing the payload, now let's analyze the text.
        List<Tuple<String, String[]>> results = service.analyze(textToAnalyze);

        sendResponse(channel, results);
    }

    private void sendResponse(RestChannel channel, List<Tuple<String, String[]>> results) throws IOException {

        XContentBuilder builder = channel.newBuilder();
        builder.startObject();
        builder.startObject("results");

        for (Tuple<String, String[]> result : results)
        {
            builder.startObject("entities");
            builder.array(result.v1(), result.v2());
            builder.endObject();
        }

        builder.endObject();
        builder.endObject();

        channel.sendResponse(new BytesRestResponse(RestStatus.OK, builder));
    }

}

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

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.util.Span;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.SpecialPermission;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.example.ExamplePluginConfiguration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Simple example service to demonstrate injecting various internal core services.
 */
public class ExampleService extends AbstractLifecycleComponent<ExampleService>
{
    private final Settings pluginSettings;

    private static final String NLP_MODEL_PERSON       = "example.plugin.nlp.model.person";
    private static final String NLP_MODEL_ORGANIZATION = "example.plugin.nlp.model.organization";

    private static InputStream personStream;
    private static InputStream organizationStream;

    private static TokenNameFinderModel[] models = new TokenNameFinderModel[2];

    @Inject
    public ExampleService(Settings settings, ExamplePluginConfiguration config) {
        super(settings);
        this.pluginSettings = config.getSettings();
    }

    @Override
    protected void doStart() {

        logger.info("Initializing example service");

        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new SpecialPermission());
        }

        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Void>()
            {
                @Override
                public Void run() throws Exception
                {
                    personStream = new FileInputStream(pluginSettings.get(NLP_MODEL_PERSON));
                    models[0] = new TokenNameFinderModel(personStream);
                    return null;
                }
            });
        }
        catch (PrivilegedActionException e) {
            throw new ElasticsearchException("Unable to load NLP model: " + pluginSettings.get(NLP_MODEL_PERSON), e);
        }

        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Void>()
            {
                @Override
                public Void run() throws Exception
                {
                    organizationStream = new FileInputStream(pluginSettings.get(NLP_MODEL_ORGANIZATION));
                    models[1] = new TokenNameFinderModel(organizationStream);
                    return null;
                }
            });
        }
        catch (PrivilegedActionException e) {
            throw new ElasticsearchException("Unable to load NLP model: " + pluginSettings.get(NLP_MODEL_ORGANIZATION), e);
        }

        logger.info("Example service initialized and ready");
    }

    public List<Tuple<String, String[]>> analyze(List<Tuple<String, String>> pairs) {

        List<Tuple<String, String[]>> results = new ArrayList<>(pairs.size());

        Tokenizer tokenizer = SimpleTokenizer.INSTANCE;

        for (Tuple<String, String> pair : pairs)
        {
            String[] tokens = tokenizer.tokenize(pair.v2());
            List<String> entities = new ArrayList<>();

            for (TokenNameFinderModel model : models)
            {
                NameFinderME finder = new NameFinderME(model);
                Span[] spans = finder.find(tokens);
                entities.addAll(Arrays.asList(Span.spansToStrings(spans, tokens)));
            }

            results.add(new Tuple<>(pair.v1(), entities.toArray(new String[entities.size()])));
        }

        return results;
    }

    @Override
    protected void doStop() {
        logger.info("Stopping example service");
    }

    @Override
    protected void doClose() {
        logger.info("Closing example service");

        if (personStream != null) {
            try {
                personStream.close();
            }
            catch (IOException e) {
                logger.error("Unable to close person stream", e);
            }
        }

        if (organizationStream != null) {

            try {
                organizationStream.close();
            }
            catch (IOException e) {
                logger.error("Unable to close organization stream", e);
            }
        }
    }
}

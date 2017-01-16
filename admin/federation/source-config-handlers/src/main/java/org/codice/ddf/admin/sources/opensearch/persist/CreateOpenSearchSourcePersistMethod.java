/**
 * Copyright (c) Codice Foundation
 * <p>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.ddf.admin.sources.opensearch.persist;

import static org.codice.ddf.admin.api.config.federation.SourceConfiguration.ENDPOINT_URL;
import static org.codice.ddf.admin.api.config.federation.SourceConfiguration.ID;
import static org.codice.ddf.admin.api.config.federation.SourceConfiguration.PASSWORD;
import static org.codice.ddf.admin.api.config.federation.SourceConfiguration.USERNAME;
import static org.codice.ddf.admin.api.handler.ConfigurationMessage.MessageType.FAILURE;
import static org.codice.ddf.admin.api.handler.ConfigurationMessage.MessageType.SUCCESS;
import static org.codice.ddf.admin.api.handler.ConfigurationMessage.buildMessage;
import static org.codice.ddf.admin.api.handler.SourceConfigurationHandler.CREATE;

import java.util.List;
import java.util.Map;

import org.codice.ddf.admin.api.config.federation.sources.OpenSearchSourceConfiguration;
import org.codice.ddf.admin.api.handler.ConfigurationMessage;
import org.codice.ddf.admin.api.handler.method.PersistMethod;
import org.codice.ddf.admin.api.handler.report.TestReport;
import org.codice.ddf.admin.api.persist.ConfigReport;
import org.codice.ddf.admin.api.persist.Configurator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class CreateOpenSearchSourcePersistMethod
        extends PersistMethod<OpenSearchSourceConfiguration> {
    public static final String CREATE_OPENSEARCH_SOURCE_ID = CREATE;

    public static final String DESCRIPTION =
            "Attempts to create and persist a OpenSearch source given a configuration.";

    //Result types
    private static final String SOURCE_CREATED = "source-created";

    private static final String CREATION_FAILED = "creation-failed";

    // Field -> Description maps
    public static final List<String> REQUIRED_FIELDS = ImmutableList.of(ID, ENDPOINT_URL);

    private static final List<String> OPTIONAL_FIELDS = ImmutableList.of(USERNAME, PASSWORD);

    private static final Map<String, String> SUCCESS_TYPES = ImmutableMap.of(SOURCE_CREATED,
            "OpenSearch Source successfully created.");

    private static final Map<String, String> FAILURE_TYPES = ImmutableMap.of(CREATION_FAILED,
            "Failed to create OpenSearch Source.");

    public CreateOpenSearchSourcePersistMethod() {
        super(CREATE_OPENSEARCH_SOURCE_ID,
                DESCRIPTION,
                REQUIRED_FIELDS,
                OPTIONAL_FIELDS,
                SUCCESS_TYPES,
                FAILURE_TYPES,
                null);
    }

    @Override
    public TestReport persist(OpenSearchSourceConfiguration configuration) {
        List<ConfigurationMessage> results =
                configuration.validate(REQUIRED_FIELDS);
        if (!results.isEmpty()) {
            return new TestReport(results);
        }
        Configurator configurator = new Configurator();
        ConfigReport report;
        configurator.createManagedService(configuration.factoryPid(), configuration.configMap());
        report = configurator.commit();
        return report.containsFailedResults() ? new TestReport(buildMessage(FAILURE,
                CREATION_FAILED,
                "Failed to create OpenSearch Source")) : new TestReport(buildMessage(SUCCESS,
                SOURCE_CREATED,
                "OpenSearch Source created"));
    }

}

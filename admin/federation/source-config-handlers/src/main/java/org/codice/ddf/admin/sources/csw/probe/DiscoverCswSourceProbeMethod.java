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
package org.codice.ddf.admin.sources.csw.probe;

import static org.codice.ddf.admin.api.config.sources.SourceConfiguration.PASSWORD;
import static org.codice.ddf.admin.api.config.sources.SourceConfiguration.PORT;
import static org.codice.ddf.admin.api.config.sources.SourceConfiguration.SOURCE_HOSTNAME;
import static org.codice.ddf.admin.api.config.sources.SourceConfiguration.USERNAME;
import static org.codice.ddf.admin.api.handler.ConfigurationMessage.INTERNAL_ERROR;
import static org.codice.ddf.admin.api.handler.commons.SourceHandlerCommons.BAD_CONFIG;
import static org.codice.ddf.admin.api.handler.commons.SourceHandlerCommons.CERT_ERROR;
import static org.codice.ddf.admin.api.handler.commons.SourceHandlerCommons.DISCOVER_SOURCES_ID;
import static org.codice.ddf.admin.api.handler.commons.SourceHandlerCommons.ENDPOINT_DISCOVERED;
import static org.codice.ddf.admin.api.handler.commons.SourceHandlerCommons.NO_ENDPOINT;
import static org.codice.ddf.admin.api.handler.commons.SourceHandlerCommons.UNTRUSTED_CA;
import static org.codice.ddf.admin.api.handler.report.ProbeReport.createProbeReport;
import static org.codice.ddf.admin.sources.csw.CswSourceConfigurationHandler.CSW_SOURCE_CONFIGURATION_HANDLER_ID;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.codice.ddf.admin.api.config.sources.CswSourceConfiguration;
import org.codice.ddf.admin.api.handler.commons.UrlAvailability;
import org.codice.ddf.admin.api.handler.method.ProbeMethod;
import org.codice.ddf.admin.api.handler.report.ProbeReport;
import org.codice.ddf.admin.sources.csw.CswSourceUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class DiscoverCswSourceProbeMethod extends ProbeMethod<CswSourceConfiguration> {

    public static final String CSW_DISCOVER_SOURCES_ID = DISCOVER_SOURCES_ID;
    public static final String DESCRIPTION = "Attempts to discover a CSW endpoint based on a hostname and port using optional authentication information.";
    public static final List<String> REQUIRED_FIELDS = ImmutableList.of(SOURCE_HOSTNAME, PORT);
    public static final List<String> OPTIONAL_FIELDS = ImmutableList.of(USERNAME, PASSWORD);
    public static final Map<String, String> SUCCESS_TYPES = ImmutableMap.of(ENDPOINT_DISCOVERED, "Discovered CSW endpoint.");
    public static final Map<String, String> FAILURE_TYPES = ImmutableMap.of(
            CERT_ERROR, "The discovered source has incorrectly configured SSL certificates and is insecure.",
            NO_ENDPOINT, "No CSW endpoint found.",
            BAD_CONFIG, "Endpoint discovered, but could not create valid configuration.",
            INTERNAL_ERROR, "Failed to create configuration from valid request to valid endpoint.");
    public static final Map<String, String> WARNING_TYPES = ImmutableMap.of(UNTRUSTED_CA,
            "The discovered URL has incorrectly configured SSL certificates and is likely insecure.");

    public DiscoverCswSourceProbeMethod() {
        super(CSW_DISCOVER_SOURCES_ID,
                DESCRIPTION,
                REQUIRED_FIELDS,
                OPTIONAL_FIELDS,
                SUCCESS_TYPES,
                FAILURE_TYPES,
                null);
    }

    @Override
    public ProbeReport probe(CswSourceConfiguration configuration) {
        CswSourceConfiguration config = new CswSourceConfiguration(configuration);
        ProbeReport results = new ProbeReport(config.validate(REQUIRED_FIELDS));
        if (results.containsFailureMessages()) {
            return results;
        }

        String result;
        Map<String, Object> probeResult = new HashMap<>();
        Optional<UrlAvailability> availability = CswSourceUtils.confirmEndpointUrl(config);
        if (!availability.isPresent()) {
            result = NO_ENDPOINT;
        } else if (availability.get().isCertError()) {
            result = CERT_ERROR;
        } else if (availability.get().isAvailable()) {
            config.endpointUrl(availability.get().getUrl());
            Optional<CswSourceConfiguration> preferred = CswSourceUtils.getPreferredConfig(config);
            if (preferred.isPresent()) {
                result = availability.get().isTrustedCertAuthority() ? ENDPOINT_DISCOVERED : UNTRUSTED_CA;
                probeResult.put(CSW_DISCOVER_SOURCES_ID,
                        preferred.get().configurationHandlerId(CSW_SOURCE_CONFIGURATION_HANDLER_ID));
            } else {
                result = INTERNAL_ERROR;
            }
        } else {
            result = INTERNAL_ERROR;
        }
        return createProbeReport(SUCCESS_TYPES, FAILURE_TYPES, WARNING_TYPES, result).probeResults(probeResult);
    }

}

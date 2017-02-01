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
package org.codice.ddf.admin.sources.opensearch;

import static java.net.HttpURLConnection.HTTP_OK;
import static org.codice.ddf.admin.api.handler.commons.SourceHandlerCommons.PING_TIMEOUT;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.net.ssl.SSLPeerUnverifiedException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContexts;
import org.codice.ddf.admin.api.config.sources.OpenSearchSourceConfiguration;
import org.codice.ddf.admin.api.handler.commons.UrlAvailability;

import com.google.common.collect.ImmutableList;

public class OpenSearchSourceUtils {

    private static final List<String> OPENSEARCH_MIME_TYPES = ImmutableList.of("application/atom+xml");

    private static final List<String> URL_FORMATS = ImmutableList.of(
            "https://%s:%d/services/catalog/query",
            "https://%s:%d/catalog/query",
            "http://%s:%d/services/catalog/query",
            "http://%s:%d/catalog/query");

    private static HttpClient noTrustClient = HttpClientBuilder.create()
            .setDefaultRequestConfig(RequestConfig.custom()
                    .setConnectTimeout(PING_TIMEOUT)
                    .build())
            .build();

    private static HttpClient trustClient;
    static {
        try {
            trustClient = HttpClientBuilder.create()
                    .setDefaultRequestConfig(RequestConfig.custom()
                            .setConnectTimeout(PING_TIMEOUT)
                            .build())
                    .setSSLSocketFactory(new SSLConnectionSocketFactory(
                            SSLContexts.custom()
                                    .loadTrustMaterial(null, (chain, authType) -> true)
                                    .build()
                    ))
                    .build();
        } catch (Exception e) {
            trustClient = HttpClientBuilder.create().build();
        }
    }


    //Given a config, returns the correct URL format for the endpoint if one exists
    public static UrlAvailability confirmEndpointUrl(OpenSearchSourceConfiguration config) {
        Optional<UrlAvailability> result = URL_FORMATS.stream()
                .map(formatUrl -> String.format(formatUrl,
                        config.sourceHostName(),
                        config.sourcePort()))
                .map(OpenSearchSourceUtils::getUrlAvailability)
                .filter(avail -> avail.isAvailable() || avail.isCertError())
                .findFirst();
        return result.isPresent() ? result.get() : null;
    }

    // Given a configuration with and endpointUrl, determines if that URL is available as an OS source
    public static UrlAvailability getUrlAvailability(String url) {
        UrlAvailability result = new UrlAvailability(url);
        int status;
        String contentType;
        HttpGet request = new HttpGet(url);
        try {
            HttpResponse response = noTrustClient.execute(request);
            status = response.getStatusLine() .getStatusCode();
            contentType = response.getEntity().getContentType().getValue();
            if (status == HTTP_OK && OPENSEARCH_MIME_TYPES.contains(contentType)) {
                return result.trustedCertAuthority(true).certError(false).available(true);
            } else {
                return result.trustedCertAuthority(true).certError(false).available(false);
            }
        } catch (SSLPeerUnverifiedException e) {
            // This is the hostname != cert name case - if this occurs, the URL's SSL cert configuration
            // is incorrect, or a serious network security issue has occurred.
            return result.trustedCertAuthority(false).certError(true).available(false);
        } catch (IOException e) {
            try {
                HttpResponse response = trustClient.execute(request);
                status = response.getStatusLine().getStatusCode();
                contentType = response.getEntity().getContentType().getValue();
                if (status == HTTP_OK && OPENSEARCH_MIME_TYPES.contains(contentType)) {
                    return result.trustedCertAuthority(false).certError(false).available(true);
                }
            } catch (Exception e1) {
                return result.trustedCertAuthority(false).certError(false).available(false);
            }
        }
        return result;
    }

    protected static void setNoTrustClient(HttpClient client) {
        noTrustClient = client;
    }

    protected static void setTrustClient(HttpClient client) {
        trustClient = client;
    }
}

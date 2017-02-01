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
package org.codice.ddf.admin.sources.opensearch

import org.apache.http.Header
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.StatusLine
import org.apache.http.client.HttpClient
import org.codice.ddf.admin.api.config.sources.OpenSearchSourceConfiguration
import spock.lang.Specification

import javax.net.ssl.SSLPeerUnverifiedException

class OpenSearchSourceUtilsTest extends Specification {

    def client = Mock(HttpClient)
    def response = Mock(HttpResponse)
    def statusLine = Mock(StatusLine)
    def entity = Mock(HttpEntity)
    def cType = Mock(Header)
    def configuration = Mock(OpenSearchSourceConfiguration)

    // Tests for getUrlAvailability
    def 'test happy path trusted CA'() {
        setup:
        OpenSearchSourceUtils.setNoTrustClient(client)

        when:
        def urlAvail = OpenSearchSourceUtils.getUrlAvailability("testUrl")

        then:
        1 * client.execute(_) >> response
        1 * response.getStatusLine() >> statusLine
        1 * statusLine.getStatusCode() >> 200
        1 * response.getEntity() >> entity
        1 * entity.getContentType() >> cType
        1 * cType.getValue() >> "application/atom+xml"

        assert urlAvail.isAvailable()
        assert !urlAvail.isCertError()
        assert urlAvail.isTrustedCertAuthority()
    }

    def 'test bad return code noTrustClient' () {
        setup:
        OpenSearchSourceUtils.setNoTrustClient(client)

        when:
        def urlAvail = OpenSearchSourceUtils.getUrlAvailability("testUrl")

        then:
        1 * client.execute(_) >> response
        1 * response.getStatusLine() >> statusLine
        1 * statusLine.getStatusCode() >> 405
        1 * response.getEntity() >> entity
        1 * entity.getContentType() >> cType
        1 * cType.getValue() >> "application/atom+xml"
        assert !urlAvail.isAvailable()
        assert !urlAvail.isCertError()
        assert urlAvail.isTrustedCertAuthority()
    }

    def 'test bad mime type noTrustClient' () {
        setup:
        OpenSearchSourceUtils.setNoTrustClient(client)

        when:
        def urlAvail = OpenSearchSourceUtils.getUrlAvailability("testUrl")

        then:
        1 * client.execute(_) >> response
        1 * response.getStatusLine() >> statusLine
        1 * statusLine.getStatusCode() >> 200
        1 * response.getEntity() >> entity
        1 * entity.getContentType() >> cType
        1 * cType.getValue() >> "application/json"
        assert !urlAvail.isAvailable()
        assert !urlAvail.isCertError()
        assert urlAvail.isTrustedCertAuthority()
    }

    def 'test cert error with noTrustClient'() {
        setup:
        OpenSearchSourceUtils.setNoTrustClient(client)

        when:
        def urlAvail = OpenSearchSourceUtils.getUrlAvailability("testUrl")

        then:
        1 * client.execute(_) >> {throw new SSLPeerUnverifiedException("test")}
        assert !urlAvail.isAvailable()
        assert urlAvail.isCertError()
        assert !urlAvail.isTrustedCertAuthority()

    }

    def 'test good path trustClient'() {
        setup:
        OpenSearchSourceUtils.setNoTrustClient(client)
        OpenSearchSourceUtils.setTrustClient(client)

        when:
        def urlAvail = OpenSearchSourceUtils.getUrlAvailability("testUrl")

        then:
        2 * client.execute(_) >> {throw new IOException("exception")} >> response
        1 * response.getStatusLine() >> statusLine
        1 * statusLine.getStatusCode() >> 200
        1 * response.getEntity() >> entity
        1 * entity.getContentType() >> cType
        1 * cType.getValue() >> "application/atom+xml"

        assert urlAvail.isAvailable()
        assert !urlAvail.isCertError()
        assert !urlAvail.isTrustedCertAuthority()
    }

    def 'test bad return code trustClient'() {
        setup:
        OpenSearchSourceUtils.setNoTrustClient(client)
        OpenSearchSourceUtils.setTrustClient(client)

        when:
        def urlAvail = OpenSearchSourceUtils.getUrlAvailability("testUrl")

        then:
        2 * client.execute(_) >> {throw new IOException("exception")} >> response
        1 * response.getStatusLine() >> statusLine
        1 * statusLine.getStatusCode() >> 405
        1 * response.getEntity() >> entity
        1 * entity.getContentType() >> cType
        1 * cType.getValue() >> "application/atom+xml"
        assert !urlAvail.isAvailable()
        assert !urlAvail.isCertError()
        assert !urlAvail.isTrustedCertAuthority()
    }

    def 'test bad mime type trustClient'() {
        setup:
        OpenSearchSourceUtils.setNoTrustClient(client)
        OpenSearchSourceUtils.setTrustClient(client)

        when:
        def urlAvail = OpenSearchSourceUtils.getUrlAvailability("testUrl")

        then:
        2 * client.execute(_) >> {throw new IOException("exception")} >> response
        1 * response.getStatusLine() >> statusLine
        1 * statusLine.getStatusCode() >> 200
        1 * response.getEntity() >> entity
        1 * entity.getContentType() >> cType
        1 * cType.getValue() >> "application/json"
        assert !urlAvail.isAvailable()
        assert !urlAvail.isCertError()
        assert !urlAvail.isTrustedCertAuthority()
    }

    def 'test failure to connect'() {
        setup:
        OpenSearchSourceUtils.setNoTrustClient(client)
        OpenSearchSourceUtils.setTrustClient(client)

        when:
        def urlAvail = OpenSearchSourceUtils.getUrlAvailability("testUrl")

        then:
        2 * client.execute(_) >> {throw new IOException("exception")}
        assert !urlAvail.isAvailable()
        assert !urlAvail.isCertError()
        assert !urlAvail.isTrustedCertAuthority()
    }


    // Tests for confimEndpointUrl
    def 'test no URL selected with bad hostname/port'() {
        setup:
        OpenSearchSourceUtils.setNoTrustClient(client)
        OpenSearchSourceUtils.setTrustClient(client)

        when:
        def endpointUrl = OpenSearchSourceUtils.confirmEndpointUrl(configuration)

        then:
        _ * configuration.sourceHostName() >> "test"
        _ * configuration.sourcePort() >> 443
        _ * client.execute(_) >> {throw new IOException()}
        endpointUrl == Optional.empty()
    }

    def 'test URL created with no cert error'() {
        setup:
        OpenSearchSourceUtils.setNoTrustClient(client)

        when:
        def endpointUrl = OpenSearchSourceUtils.confirmEndpointUrl(configuration)

        then:
        _ * configuration.sourceHostName() >> "test"
        _ * configuration.sourcePort() >> 443
        1 * client.execute(_) >> response
        1 * response.getStatusLine() >> statusLine
        1 * statusLine.getStatusCode() >> 200
        1 * response.getEntity() >> entity
        1 * entity.getContentType() >> cType
        1 * cType.getValue() >> "application/atom+xml"

        endpointUrl.isPresent()
        endpointUrl.get().getUrl() == "https://test:443/services/catalog/query"
    }
}
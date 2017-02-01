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
package org.codice.ddf.admin.sources.csw

import org.apache.http.Header
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.StatusLine
import org.apache.http.client.HttpClient
import org.codice.ddf.admin.api.config.sources.CswSourceConfiguration
import spock.lang.Specification

import javax.net.ssl.SSLPeerUnverifiedException

import static org.codice.ddf.admin.api.services.CswServiceProperties.CSW_GMD_FACTORY_PID
import static org.codice.ddf.admin.api.services.CswServiceProperties.CSW_PROFILE_FACTORY_PID
import static org.codice.ddf.admin.api.services.CswServiceProperties.CSW_SPEC_FACTORY_PID

class CswSourceUtilsTest extends Specification {

    def client = Mock(HttpClient)
    def response = Mock(HttpResponse)
    def statusLine = Mock(StatusLine)
    def entity = Mock(HttpEntity)
    def cType = Mock(Header)
    def configuration = Mock(CswSourceConfiguration)

    def metacardXml = this.getClass().getClassLoader().getResourceAsStream('metacardGetCapabilities.xml')
    def gmdXml = this.getClass().getClassLoader().getResourceAsStream('gmdGetCapabilities.xml')
    def specXml = this.getClass().getClassLoader().getResourceAsStream('specGetCapabilities.xml')

    // Tests for getUrlAvailability
    def 'test happy path trusted CA'() {
        setup:
        CswSourceUtils.setNoTrustClient(client)

        when:
        def urlAvail = CswSourceUtils.getUrlAvailability("testUrl")

        then:
        1 * client.execute(_) >> response
        1 * response.getStatusLine() >> statusLine
        1 * statusLine.getStatusCode() >> 200
        1 * response.getEntity() >> entity
        1 * entity.getContentType() >> cType
        1 * cType.getValue() >> "text/xml"

        assert urlAvail.isAvailable()
        assert !urlAvail.isCertError()
        assert urlAvail.isTrustedCertAuthority()
    }

    def 'test bad return code noTrustClient' () {
        setup:
        CswSourceUtils.setNoTrustClient(client)

        when:
        def urlAvail = CswSourceUtils.getUrlAvailability("testUrl")

        then:
        1 * client.execute(_) >> response
        1 * response.getStatusLine() >> statusLine
        1 * statusLine.getStatusCode() >> 405
        1 * response.getEntity() >> entity
        1 * entity.getContentType() >> cType
        1 * cType.getValue() >> "application/xml"
        assert !urlAvail.isAvailable()
        assert !urlAvail.isCertError()
        assert urlAvail.isTrustedCertAuthority()
    }

    def 'test bad mime type noTrustClient' () {
        setup:
        CswSourceUtils.setNoTrustClient(client)

        when:
        def urlAvail = CswSourceUtils.getUrlAvailability("testUrl")

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
        CswSourceUtils.setNoTrustClient(client)

        when:
        def urlAvail = CswSourceUtils.getUrlAvailability("testUrl")

        then:
        1 * client.execute(_) >> {throw new SSLPeerUnverifiedException("test")}
        assert !urlAvail.isAvailable()
        assert urlAvail.isCertError()
        assert !urlAvail.isTrustedCertAuthority()

    }

    def 'test good path trustClient'() {
        setup:
        CswSourceUtils.setNoTrustClient(client)
        CswSourceUtils.setTrustClient(client)

        when:
        def urlAvail = CswSourceUtils.getUrlAvailability("testUrl")

        then:
        2 * client.execute(_) >> {throw new IOException("exception")} >> response
        1 * response.getStatusLine() >> statusLine
        1 * statusLine.getStatusCode() >> 200
        1 * response.getEntity() >> entity
        1 * entity.getContentType() >> cType
        1 * cType.getValue() >> "text/xml"

        assert urlAvail.isAvailable()
        assert !urlAvail.isCertError()
        assert !urlAvail.isTrustedCertAuthority()
    }

    def 'test bad return code trustClient'() {
        setup:
        CswSourceUtils.setNoTrustClient(client)
        CswSourceUtils.setTrustClient(client)

        when:
        def urlAvail = CswSourceUtils.getUrlAvailability("testUrl")

        then:
        2 * client.execute(_) >> {throw new IOException("exception")} >> response
        1 * response.getStatusLine() >> statusLine
        1 * statusLine.getStatusCode() >> 405
        1 * response.getEntity() >> entity
        1 * entity.getContentType() >> cType
        1 * cType.getValue() >> "application/xml"
        assert !urlAvail.isAvailable()
        assert !urlAvail.isCertError()
        assert !urlAvail.isTrustedCertAuthority()
    }

    def 'test bad mime type trustClient'() {
        setup:
        CswSourceUtils.setNoTrustClient(client)
        CswSourceUtils.setTrustClient(client)

        when:
        def urlAvail = CswSourceUtils.getUrlAvailability("testUrl")

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
        CswSourceUtils.setNoTrustClient(client)
        CswSourceUtils.setTrustClient(client)

        when:
        def urlAvail = CswSourceUtils.getUrlAvailability("testUrl")

        then:
        2 * client.execute(_) >> {throw new IOException("exception")}
        assert !urlAvail.isAvailable()
        assert !urlAvail.isCertError()
        assert !urlAvail.isTrustedCertAuthority()
    }

    // Tests for getPreferredConfig

    def 'test empty response'() {
        setup:
        CswSourceUtils.setTrustClient(client)

        when:
        def config = CswSourceUtils.getPreferredConfig(configuration)

        then:
        1 * client.execute(_) >> response
        1 * response.getEntity() >> entity
        1 * entity.getContent() >> ""

        assert config == Optional.empty()
    }

    def 'test parse metacard:uri config'() {
        setup:
        CswSourceUtils.setTrustClient(client)

        when:
        def config = CswSourceUtils.getPreferredConfig(configuration)

        then:
        1 * client.execute(_) >> response
        1 * response.getEntity() >> entity
        1 * entity.getContent() >> metacardXml

        assert config.isPresent()
        config.get().factoryPid() == CSW_PROFILE_FACTORY_PID
    }

    def 'test parse GMD config'() {
        setup:
        CswSourceUtils.setTrustClient(client)

        when:
        def config = CswSourceUtils.getPreferredConfig(configuration)

        then:
        1 * client.execute(_) >> response
        1 * response.getEntity() >> entity
        1 * entity.getContent() >> gmdXml

        assert config.isPresent()
        config.get().factoryPid() == CSW_GMD_FACTORY_PID
    }

    def 'test no metacard or GMD parse config'() {
        setup:
        CswSourceUtils.setTrustClient(client)

        when:
        def config = CswSourceUtils.getPreferredConfig(configuration)

        then:
        1 * client.execute(_) >> response
        1 * response.getEntity() >> entity
        1 * entity.getContent() >> specXml

        assert config.isPresent()
        config.get().factoryPid() == CSW_SPEC_FACTORY_PID
    }

    // Tests for discover URL
    def 'test no URL selected with bad hostname/port'() {
        setup:
        CswSourceUtils.setNoTrustClient(client)
        CswSourceUtils.setTrustClient(client)

        when:
        def endpointUrl = CswSourceUtils.confirmEndpointUrl(configuration)

        then:
        _ * configuration.sourceHostName() >> "test"
        _ * configuration.sourcePort() >> 443
        _ * client.execute(_) >> {throw new IOException()}
        endpointUrl == Optional.empty()
    }

    def 'test URL created with no cert error'() {
        setup:
        CswSourceUtils.setNoTrustClient(client)

        when:
        def endpointUrl = CswSourceUtils.confirmEndpointUrl(configuration)

        then:
        _ * configuration.sourceHostName() >> "test"
        _ * configuration.sourcePort() >> 443
        1 * client.execute(_) >> response
        1 * response.getStatusLine() >> statusLine
        1 * statusLine.getStatusCode() >> 200
        1 * response.getEntity() >> entity
        1 * entity.getContentType() >> cType
        1 * cType.getValue() >> "text/xml"

        endpointUrl.isPresent()
        endpointUrl.get().getUrl() == "https://test:443/services/csw"
    }
}
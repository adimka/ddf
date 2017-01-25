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

package org.codice.ddf.admin.security.ldap.test;

import static org.codice.ddf.admin.api.config.ldap.LdapConfiguration.BASE_GROUP_DN;
import static org.codice.ddf.admin.api.config.ldap.LdapConfiguration.BASE_USER_DN;
import static org.codice.ddf.admin.api.config.ldap.LdapConfiguration.BIND_KDC;
import static org.codice.ddf.admin.api.config.ldap.LdapConfiguration.BIND_METHOD;
import static org.codice.ddf.admin.api.config.ldap.LdapConfiguration.BIND_REALM;
import static org.codice.ddf.admin.api.config.ldap.LdapConfiguration.BIND_USER_DN;
import static org.codice.ddf.admin.api.config.ldap.LdapConfiguration.BIND_USER_PASSWORD;
import static org.codice.ddf.admin.api.config.ldap.LdapConfiguration.ENCRYPTION_METHOD;
import static org.codice.ddf.admin.api.config.ldap.LdapConfiguration.GROUP_OBJECT_CLASS;
import static org.codice.ddf.admin.api.config.ldap.LdapConfiguration.HOST_NAME;
import static org.codice.ddf.admin.api.config.ldap.LdapConfiguration.PORT;
import static org.codice.ddf.admin.api.config.ldap.LdapConfiguration.USER_NAME_ATTRIBUTE;
import static org.codice.ddf.admin.api.handler.report.Report.createReport;
import static org.codice.ddf.admin.security.ldap.LdapConnectionResult.BASE_GROUP_DN_NOT_FOUND;
import static org.codice.ddf.admin.security.ldap.LdapConnectionResult.BASE_USER_DN_NOT_FOUND;
import static org.codice.ddf.admin.security.ldap.LdapConnectionResult.CANNOT_BIND;
import static org.codice.ddf.admin.security.ldap.LdapConnectionResult.CANNOT_CONFIGURE;
import static org.codice.ddf.admin.security.ldap.LdapConnectionResult.CANNOT_CONNECT;
import static org.codice.ddf.admin.security.ldap.LdapConnectionResult.FOUND_BASE_GROUP_DN;
import static org.codice.ddf.admin.security.ldap.LdapConnectionResult.FOUND_BASE_USER_DN;
import static org.codice.ddf.admin.security.ldap.LdapConnectionResult.FOUND_USER_NAME_ATTRIBUTE;
import static org.codice.ddf.admin.security.ldap.LdapConnectionResult.NO_GROUPS_IN_BASE_GROUP_DN;
import static org.codice.ddf.admin.security.ldap.LdapConnectionResult.NO_USERS_IN_BASE_USER_DN;
import static org.codice.ddf.admin.security.ldap.LdapConnectionResult.SUCCESSFUL_BIND;
import static org.codice.ddf.admin.security.ldap.LdapConnectionResult.USER_NAME_ATTRIBUTE_NOT_FOUND;
import static org.codice.ddf.admin.security.ldap.LdapConnectionResult.toDescriptionMap;
import static org.codice.ddf.admin.security.ldap.test.LdapTestingCommons.LdapConnectionAttempt;
import static org.codice.ddf.admin.security.ldap.test.LdapTestingCommons.bindUserToLdapConnection;
import static org.codice.ddf.admin.security.ldap.test.LdapTestingCommons.getLdapQueryResults;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.codice.ddf.admin.api.config.ldap.LdapConfiguration;
import org.codice.ddf.admin.api.handler.ConfigurationMessage;
import org.codice.ddf.admin.api.handler.method.TestMethod;
import org.codice.ddf.admin.api.handler.report.ProbeReport;
import org.codice.ddf.admin.api.handler.report.Report;
import org.forgerock.opendj.ldap.Connection;
import org.forgerock.opendj.ldap.Filter;
import org.forgerock.opendj.ldap.SearchScope;
import org.forgerock.opendj.ldap.responses.SearchResultEntry;

import com.google.common.collect.ImmutableList;

public class DirectoryStructTestMethod extends TestMethod<LdapConfiguration> {
    private static final String LDAP_DIRECTORY_STRUCT_TEST_ID = "dir-struct";

    private static final String DESCRIPTION =
            "Verifies that the specified directory structure, entries and required attributes to configure LDAP exist.";

    private static final List<String> REQUIRED_FIELDS = ImmutableList.of(HOST_NAME,
            PORT,
            ENCRYPTION_METHOD,
            BIND_USER_DN,
            BIND_USER_PASSWORD,
            BIND_METHOD,
            BASE_USER_DN,
            // TODO: tbatie - 1/11/17 - validate membershipNameAttribute once implemented
            BASE_GROUP_DN,
            USER_NAME_ATTRIBUTE);

    private static final List<String> OPTIONAL_FIELDS = ImmutableList.of(BIND_REALM,
            BIND_KDC,
            GROUP_OBJECT_CLASS);

    private static final Map<String, String> SUCCESS_TYPES = toDescriptionMap(Arrays.asList(
            FOUND_BASE_USER_DN,
            FOUND_BASE_GROUP_DN,
            FOUND_USER_NAME_ATTRIBUTE));

    private static final Map<String, String> FAILURE_TYPES = toDescriptionMap(Arrays.asList(
            CANNOT_CONFIGURE,
            CANNOT_CONNECT,
            CANNOT_BIND,
            BASE_USER_DN_NOT_FOUND,
            BASE_GROUP_DN_NOT_FOUND,
            USER_NAME_ATTRIBUTE_NOT_FOUND));

    private static final Map<String, String> WARNING_TYPES = toDescriptionMap(Arrays.asList(
            NO_USERS_IN_BASE_USER_DN,
            NO_GROUPS_IN_BASE_GROUP_DN));

    public DirectoryStructTestMethod() {
        super(LDAP_DIRECTORY_STRUCT_TEST_ID, DESCRIPTION, REQUIRED_FIELDS, OPTIONAL_FIELDS,
                SUCCESS_TYPES,
                FAILURE_TYPES,
                WARNING_TYPES);
    }

    @Override
    public Report test(LdapConfiguration configuration) {
        LdapConnectionAttempt connectionAttempt = bindUserToLdapConnection(configuration);

        if (connectionAttempt.result() != SUCCESSFUL_BIND) {
            return createReport(SUCCESS_TYPES,
                    FAILURE_TYPES,
                    WARNING_TYPES,
                    Collections.singletonList(connectionAttempt.result()
                            .name()));
        }

        Map<String, String> resultsWithConfigIds = new HashMap<>();
        try (Connection ldapConnection = connectionAttempt.connection()) {
            List<SearchResultEntry> userDirExists = getLdapQueryResults(ldapConnection,
                    configuration.baseUserDn(),
                    Filter.present("objectClass")
                            .toString(),
                    SearchScope.BASE_OBJECT,
                    1);
            if (userDirExists.isEmpty()) {
                resultsWithConfigIds.put(BASE_USER_DN_NOT_FOUND.name(), BASE_USER_DN);
            } else {
                List<SearchResultEntry> baseUsersResults = getLdapQueryResults(ldapConnection,
                        configuration.baseUserDn(),
                        Filter.present(configuration.userNameAttribute())
                                .toString(),
                        SearchScope.SUBORDINATES,
                        1);
                if (baseUsersResults.isEmpty()) {
                    resultsWithConfigIds.put(NO_USERS_IN_BASE_USER_DN.name(), BASE_USER_DN);
                    resultsWithConfigIds.put(USER_NAME_ATTRIBUTE_NOT_FOUND.name(),
                            USER_NAME_ATTRIBUTE);
                } else {
                    resultsWithConfigIds.put(FOUND_USER_NAME_ATTRIBUTE.name(), USER_NAME_ATTRIBUTE);
                }
            }

            List<SearchResultEntry> groupDirExists = getLdapQueryResults(ldapConnection,
                    configuration.baseGroupDn(),
                    Filter.present("objectClass")
                            .toString(),
                    SearchScope.BASE_OBJECT,
                    1);
            if (groupDirExists.isEmpty()) {
                resultsWithConfigIds.put(BASE_GROUP_DN_NOT_FOUND.name(), BASE_GROUP_DN);
            } else {
                List<SearchResultEntry> baseGroupResults = getLdapQueryResults(ldapConnection,
                        configuration.baseGroupDn(),
                        Filter.equality("objectClass", configuration.groupObjectClass())
                                .toString(),
                        SearchScope.SUBORDINATES,
                        1,
                        "objectClass");
                if (baseGroupResults.isEmpty()) {
                    resultsWithConfigIds.put(NO_GROUPS_IN_BASE_GROUP_DN.name(), BASE_GROUP_DN);
                } else {
                    resultsWithConfigIds.put(FOUND_BASE_GROUP_DN.name(), BASE_GROUP_DN);
                }
            }

        }
        return createReport(SUCCESS_TYPES, FAILURE_TYPES, WARNING_TYPES, resultsWithConfigIds);
    }
}

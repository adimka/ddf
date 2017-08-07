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
package ddf.catalog.operation.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import ddf.catalog.operation.Query;

public class FacetedQueryRequest extends QueryRequestImpl {

    public static final String FACET_FIELDS_KEY = "facet-fields";

    public static final String FACET_RESULTS_KEY = "facet-results";

    /**
     * Instantiates a FacetedQueryRequest to facet on the provided fields.
     * @param query The query to be sent to the data source
     * @param facetFields A list of fields for which to return text faceting counts
     */
    public FacetedQueryRequest(Query query, List<String> facetFields) {
        this(query, false, null, null, facetFields);
    }

    /**
     * Instantiates a FacetedQueryRequest to facet on the provided fields.
     * @param query The query to be sent to the data source
     * @param isEnterprise Specifies if this FacetedQueryRequest is an enterprise query
     * @param sourceIds A list of sources to query
     * @param properties Properties supplied to this query for auth, transactions, etc
     * @param facetFields A list of fields for which to return text faceting counts
     */
    public FacetedQueryRequest(Query query, boolean isEnterprise, Collection<String> sourceIds,
            Map<String, Serializable> properties, List<String> facetFields) {
        super(query, isEnterprise, sourceIds, properties);
        if (facetFields == null) {
            facetFields = new ArrayList<>();
        }
        this.properties.put(FACET_FIELDS_KEY, (Serializable) facetFields);
    }

}

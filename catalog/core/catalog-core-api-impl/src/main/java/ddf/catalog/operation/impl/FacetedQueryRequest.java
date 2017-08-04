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

    public static final String FACET_FIELDS_KEY = "text-facets";

    /**
     * Instantiates a FacetedQueryRequest designed to facet on the provided fields.
     * @param query
     * @param facetFields
     */
    public FacetedQueryRequest(Query query, List<String> facetFields) {
        this(query, false, null, null, facetFields);
    }

    public FacetedQueryRequest(Query query, boolean isEnterprise, Collection<String> sourceIds,
            Map<String, Serializable> properties, List<String> facetFields) {
        super(query, isEnterprise, sourceIds, properties);
        if (facetFields == null) {
            facetFields = new ArrayList<>();
        }
        this.properties.put(FACET_FIELDS_KEY, (Serializable) facetFields);
    }

}

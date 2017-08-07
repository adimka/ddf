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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FacetedFieldResult {

    private String fieldName;

    private List<FacetValueCount> facetValues;

    public FacetedFieldResult(String fieldName, List<String> fieldValues, List<Long> valueCounts) {
        this.fieldName = fieldName;
        facetValues = new ArrayList<>();

        Iterator<String> valueItr = fieldValues.iterator();
        Iterator<Long> countItr = valueCounts.iterator();

        while (valueItr.hasNext() && countItr.hasNext()) {
            facetValues.add(new FacetValueCount(valueItr.next(), countItr.next()));
        }

    }

    public String getFieldName() {
        return fieldName;
    }

    public List<FacetValueCount> getFacetValues() {
        return facetValues;
    }

    private class FacetValueCount {
        private String value;
        private long count;

        FacetValueCount(String value, long count) {
            this.value = value;
            this.count = count;
        }

        long getCount() {
            return count;
        }

        String getValue() {
            return value;
        }
    }

}

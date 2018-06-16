/**
 * Copyright (c) Codice Foundation
 *
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 *
 **/
/*global require*/
var wreqr = require('wreqr');
var Marionette = require('marionette');
var _ = require('underscore');
var $ = require('jquery');
var template = require('./ingest.hbs');
var CustomElements = require('js/CustomElements');
var router = require('component/router/router');
var IngestDetails = require('component/ingest-details/ingest-details.view');
var properties = require('properties');

module.exports = Marionette.LayoutView.extend({
    template: template,
    tagName: CustomElements.register('ingest'),
    modelEvents: {},
    events: {},
    ui: {},
    regions: {
        ingestDetails: '.ingest-details'
    },
    initialize: function() {
        this.listenTo(router, 'change', this.handleRoute);
    },
    handleRoute: function() {
        if (router.toJSON().name === 'openIngest' && !properties.isUploadEnabled()) {
            router.notFound();
        }
    },
    onRender() {
        this.handleRoute();
    },
    onBeforeShow: function() {
        this.ingestDetails.show(new IngestDetails({url: '/services/catalog/'}));
    }
});
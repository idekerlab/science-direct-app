/*global $,cy */
$(function () {
    'use strict';

    var CYJS_ID = '#cy';
    var NETWORK_FILE_NAME = 'network.json';
    var STYLE_FILE_NAME = 'style.json';

    /**
     * Find first style in the list
     */
    function findPreferredStyle(styles) {
        var styleLen = styles.length;
        if(styleLen <= 0) {
            return null;
        }

        return styles[0];
    }


    $(CYJS_ID).cytoscape({
        // Since the network is always created from Cytoscape 3,
        // (x,y) locations are always available.
        layout: {
            name: 'preset',
            padding: 10
        },

        boxSelectionEnabled: false,
        minZoom: 0.3,
        maxZoom: 5,

        ready: function () {
            window.cy = this;
            $.getJSON(NETWORK_FILE_NAME).success(function(network) {
                $.getJSON(STYLE_FILE_NAME).success(function(styles) {

                    cy.load(network.elements);
                    console.log(network);
                    var vs = findPreferredStyle(styles);
                    cy.style().fromJson(vs.style).update();
                    cy.nodes().lock();

                    // Remove loading tag
                    $('#loading').remove();
                });
            });
        }
    });
});
/*
 * Copyright 2015-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 ONOS GUI -- Topology layer filtering Module.
 Provides functionality to visually differentiate between the packet and
 optical layers of the topology.
 */

(function () {
    'use strict';

    // injected refs
    var flash;

    // api to topoForce
    var api;
    /*
     node()     // get ref to D3 selection of nodes
     link()     // get ref to D3 selection of links
     */

    // function to be replaced by the localization bundle function
    var topoLion = function (x) {
        return '#tfltr#' + x + '#';
    };

    var smax = 'suppressedmax';

    // which "layer" a particular item "belongs to"
    var layerLookup = {
            host: {
                endstation: 'pkt', // default, if host event does not define type
                router: 'pkt',
                bgpSpeaker: 'pkt',
            },
            device: {
                switch: 'pkt',
                router: 'pkt',
                roadm: 'opt',
                otn: 'opt',
            },
            link: {
                hostLink: 'pkt',
                direct: 'pkt',
                indirect: '',
                tunnel: '',
                optical: 'opt',
            },
        },
        // order of layer cycling in button
        dispatch = [
            {
                type: 'all',
                action: function () { suppressLayers(false); },
            },
            {
                type: 'pkt',
                action: function () { showLayer('pkt'); },
            },
            {
                type: 'opt',
                action: function () { showLayer('opt'); },
            },
        ],
        layer = 0,
        layerType,
        lionKey;

    function clickAction() {
        layer = (layer + 1) % dispatch.length;
        dispatch[layer].action();
        layerType = dispatch[layer].type;
        lionKey = 'fl_layer_' + layerType;
        flash.flash(topoLion(lionKey));
    }

    function selected() {
        return dispatch[layer].type;
    }

    function inLayer(d, layer) {
        var type = d.class === 'link' ? d.type() : d.type,
            look = layerLookup[d.class],
            lyr = look && look[type];
        return lyr === layer;
    }

    function unsuppressLayer(which) {
        api.node().each(function (d) {
            var node = d.el;
            if (inLayer(d, which)) {
                node.classed(smax, false);
            }
        });

        api.link().each(function (d) {
            var link = d.el;
            if (inLayer(d, which)) {
                link.classed(smax, false);
            }
        });
    }

    function suppressLayers(b) {
        api.node().classed(smax, b);
        api.link().classed(smax, b);
    }

    function showLayer(which) {
        suppressLayers(true);
        unsuppressLayer(which);
    }

    // invoked after the localization bundle has been received from the server
    function setLionBundle(bundle) {
        topoLion = bundle;
    }

    // === -----------------------------------------------------
    // === MODULE DEFINITION ===

    angular.module('ovTopo')
        .factory('TopoFilterService', ['FlashService',
            function (_flash_) {
                flash = _flash_;

                function initFilter(_api_) {
                    api = _api_;
                }

                return {
                    initFilter: initFilter,

                    clickAction: clickAction,
                    selected: selected,
                    inLayer: inLayer,
                    setLionBundle: setLionBundle,
                };
            },
        ]);
}());

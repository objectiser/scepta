///
/// Copyright 2015 Red Hat, Inc. and/or its affiliates
/// and other contributors as indicated by the @author tags.
///
/// Licensed under the Apache License, Version 2.0 (the "License");
/// you may not use this file except in compliance with the License.
/// You may obtain a copy of the License at
///
///    http://www.apache.org/licenses/LICENSE-2.0
///
/// Unless required by applicable law or agreed to in writing, software
/// distributed under the License is distributed on an "AS IS" BASIS,
/// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
/// See the License for the specific language governing permissions and
/// limitations under the License.
///

/// <reference path="../../includes.ts"/>
/// <reference path="sceptaDesignGlobals.ts"/>
module SceptaDesign {

  export var _module = angular.module(SceptaDesign.pluginName, ["xeditable","ui.codemirror"]);

  var tab = undefined;

  _module.config(['$locationProvider', '$routeProvider', 'HawtioNavBuilderProvider', ($locationProvider, $routeProvider:ng.route.IRouteProvider, builder:HawtioMainNav.BuilderFactory) => {
    tab = builder.create()
      .id(SceptaDesign.pluginName)
      .title(() => "Policy Design")
      .href(() => "/design")
      .build();
    builder.configureRouting($routeProvider, tab);
    $locationProvider.html5Mode(true);
    //$locationProvider.hashPrefix('!');
    $routeProvider.
      when('/design', {
        templateUrl: 'plugins/scepta-design/html/organizations.html',
        controller: 'SceptaDesign.OrganizationsController'
      }).
      when('/design/:organization', {
        templateUrl: 'plugins/scepta-design/html/organization.html',
        controller: 'SceptaDesign.OrganizationController'
      }).
      when('/design/:organization/:policygroup', {
        templateUrl: 'plugins/scepta-design/html/policygroup.html',
        controller: 'SceptaDesign.PolicyGroupController'
      }).
      when('/design/:organization/:policygroup/endpoint/:endpoint', {
        templateUrl: 'plugins/scepta-design/html/endpoint.html',
        controller: 'SceptaDesign.EndpointController'
      }).
      when('/design/:organization/:policygroup/policy/:policy', {
        templateUrl: 'plugins/scepta-design/html/policy.html',
        controller: 'SceptaDesign.PolicyController'
      }).
      when('/design/:organization/:policygroup/policy/:policy/:resource', {
        templateUrl: 'plugins/scepta-design/html/resource.html',
        controller: 'SceptaDesign.ResourceController'
      });

  }]);

  _module.run(function(editableOptions) {
      editableOptions.theme = 'bs3';
  });

  _module.run(['HawtioNav', (HawtioNav:HawtioMainNav.Registry) => {
    HawtioNav.add(tab);
    log.debug("loaded");
  }]);

  hawtioPluginLoader.addModule(SceptaDesign.pluginName);
}

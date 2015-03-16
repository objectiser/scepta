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

/// <reference path="sceptaDesignPlugin.ts"/>
module SceptaDesign {

  export var PolicyGroupController = _module.controller("SceptaDesign.PolicyGroupController", ['$scope', '$routeParams', '$http', ($scope, $routeParams, $http) => {
    $scope.organizationName = $routeParams.organization;
    $scope.policyGroupName = $routeParams.policygroup;

    $http.get('/scepta-server/design/'+$scope.organizationName+'/group/'+$scope.policyGroupName).success(function(data) {
      $scope.policygroup = data;
    });

    $http.get('/scepta-server/design/'+$scope.organizationName+'/group/'+$scope.policyGroupName+'/policy').success(function(data) {
      $scope.policies = data;
    });

    $http.get('/scepta-server/design/'+$scope.organizationName+'/group/'+$scope.policyGroupName+'/tag').success(function(data) {
      $scope.tags = data;
    });

    $scope.updatePolicyGroup = function() {
      return $http.put('/scepta-server/design/'+$scope.organizationName+'/group/'+$scope.policyGroupName, $scope.policygroup);
    };

    $scope.exportPolicyGroup = function() {
      window.open('/scepta-server/design/'+$scope.organizationName+'/group/'+$scope.policyGroupName+'/export', 'export');
    };

    $scope.downloadDeployment = function() {
      window.open('/scepta-server/deployment/'+$scope.organizationName+'/'+$scope.policyGroupName+'/'+this.tag.name, 'download');
    };

    $scope.buildPolicyGroup = function() {
      return $http.post('/scepta-server/design/'+$scope.organizationName+'/group/'+$scope.policyGroupName+'/tag', "Tag description tbd", { "headers": { "Content-Type": "text/plain" } });
    };

    $scope.addPolicy = function() {
      $http.post('/scepta-server/design/'+$scope.organizationName+'/group/'+$scope.policyGroupName+'/policy', $scope.newPolicy)
        .success(function(data, status, headers, config) {
        $http.get('/scepta-server/design/'+$scope.organizationName+'/group/'+$scope.policyGroupName+'/policy').success(function(data) {
          $scope.policies = data;
          $scope.newPolicy = new Object();
        });
      });
    };

    $scope.nameOrderProp = 'name';

    $scope.addEndpoint = function() {
      $scope.policygroup.endpoints.push($scope.endpoint);
      $scope.updatePolicyGroup();
      $scope.endpoint = new Object();
    };

    $scope.removeEndpoint = function(event) {
      var c = confirm("Are you sure?");
      if (c == true) {
        var endpointName = event.currentTarget.attributes.getNamedItem('endpoint').value;
        for (var i = $scope.policygroup.endpoints.length - 1; i >= 0; i--) { 
          var ep=$scope.policygroup.endpoints[i];     
          if (ep.name === endpointName) {
            $scope.policygroup.endpoints.remove(ep);
            $scope.updatePolicyGroup();
          }
        }
      } 
    };

    $scope.addProcessor = function() {
      $scope.policygroup.processors.push($scope.processor);
      $scope.updatePolicyGroup();
      $scope.processor = new Object();
    };

    $scope.removeProcessor = function(event) {
      var c = confirm("Are you sure?");
      if (c == true) {
        var processorName = event.currentTarget.attributes.getNamedItem('processor').value;
        for (var i = $scope.policygroup.processors.length - 1; i >= 0; i--) { 
          var proc=$scope.policygroup.processors[i];     
          if (proc.name === processorName) {
            $scope.policygroup.processors.remove(proc);
            $scope.updatePolicyGroup();
          }
        }
      } 
    };

  }]);

}

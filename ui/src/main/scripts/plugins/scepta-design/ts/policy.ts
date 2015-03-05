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

  export var PolicyController = _module.controller("SceptaDesign.PolicyController", ['$scope', '$routeParams', '$http', ($scope, $routeParams, $http) => {
    $scope.organizationName = $routeParams.organization;
    $scope.policyGroupName = $routeParams.policygroup;
    $scope.policyName = $routeParams.policy;

    $http.get('/scepta-server/design/'+$scope.organizationName+'/group/'+$scope.policyGroupName+'/policy/'+$scope.policyName).success(function(data) {
      $scope.policy = data;
    });

    $http.get('/scepta-server/design/'+$scope.organizationName+'/group/'+$scope.policyGroupName+'/policy/'+$scope.policyName+'/definition').success(function(data) {
      $scope.policyDefinition = data;

      $scope.$watch("policyDefinition", function(newValue, oldValue) {
        return $http.put('/scepta-server/design/'+$scope.organizationName+'/group/'+$scope.policyGroupName+'/policy/'+$scope.policyName+'/definition', $scope.policyDefinition, { "headers": { "Content-Type": "text/plain" } });
      });
    });

    $scope.updatePolicy = function() {
      return $http.put('/scepta-server/design/'+$scope.organizationName+'/group/'+$scope.policyGroupName+'/policy/'+$scope.policyName, $scope.policy);
    };

    $scope.editorOptions = {
      lineWrapping : true,
      lineNumbers: true,
      mode: 'xml'
    };

    $scope.resourceOrderProp = 'name';
    $scope.dependencyOrderProp = 'artifactId';

    $scope.addResource = function() {
      $scope.policy.resources.push($scope.resource);
      $scope.updatePolicy();
      $scope.resource = new Object();
    };

    $scope.removeResource = function(event) {
      var c = confirm("Are you sure?");
      if (c == true) {
        var resourceName = event.currentTarget.attributes.getNamedItem('resource').value;
        for (var i = $scope.policy.resources.length - 1; i >= 0; i--) { 
          var r=$scope.policy.resources[i];     
          if (r.name === resourceName) {
            $scope.policy.resources.remove(r);
            $scope.updatePolicy();
          }
        }
      } 
    };

    $scope.addDependency = function() {
      $scope.policy.dependencies.push($scope.dependency);
      $scope.updatePolicy();
      $scope.dependency = new Object();
    };

    $scope.removeDependency = function(event) {
      var c = confirm("Are you sure?");
      if (c == true) {
        var dependency = JSON.parse(event.currentTarget.attributes.getNamedItem('dependency').value);
        for (var i = $scope.policy.dependencies.length - 1; i >= 0; i--) { 
          var d=$scope.policy.dependencies[i];     
          if (d.groupId === dependency.groupId && d.artifactId === dependency.artifactId) {
            $scope.policy.dependencies.remove(d);
            $scope.updatePolicy();
          }
        }
      }
    };

  }]);

}

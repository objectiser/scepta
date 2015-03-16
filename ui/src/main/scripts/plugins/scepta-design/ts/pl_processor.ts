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

  export var PLProcessorController = _module.controller("SceptaDesign.PLProcessorController", ['$scope', '$routeParams', '$http', ($scope, $routeParams, $http) => {
    $scope.organizationName = $routeParams.organization;
    $scope.policyGroupName = $routeParams.policygroup;
    $scope.policyName = $routeParams.policy;
    $scope.processorName = $routeParams.processor;

    $http.get('/scepta-server/design/'+$scope.organizationName+'/group/'+$scope.policyGroupName+'/policy/'+$scope.policyName).success(function(data) {
      $scope.policy = data;
      $scope.policy.processors.forEach(function(proc) {
        if (proc.name === $scope.processorName) {
          $scope.processor = proc;
          $scope.reset();
        }
      });
    });

    $scope.updatePolicy = function() {
      return $http.put('/scepta-server/design/'+$scope.organizationName+'/group/'+$scope.policyGroupName+'/policy/'+$scope.policyName, $scope.policy);
    };

    $scope.dependencyOrderProp = 'artifactId';

    $scope.addDependency = function() {
      $scope.editable.dependencies.push($scope.dependency);
      $scope.dependency = new Object();
    };

    $scope.removeDependency = function(event) {
      var c = confirm("Are you sure?");
      if (c == true) {
        var dependency = JSON.parse(event.currentTarget.attributes.getNamedItem('dependency').value);
        for (var i = $scope.editable.dependencies.length - 1; i >= 0; i--) { 
          var d=$scope.editable.dependencies[i];     
          if (d.groupId === dependency.groupId && d.artifactId === dependency.artifactId) {
            $scope.editable.dependencies.remove(d);
          }
        }
      }
    };

    $scope.update = function() {
      $scope.policy.processors.remove($scope.processor);
      $scope.processor = angular.copy($scope.editable);
      $scope.policy.processors.push($scope.processor);
      $scope.updatePolicy();
    };

    $scope.reset = function() {
      $scope.editable = angular.copy($scope.processor);
    };

  }]);

}

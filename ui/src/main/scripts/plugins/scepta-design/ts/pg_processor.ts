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

  export var PGProcessorController = _module.controller("SceptaDesign.PGProcessorController", ['$scope', '$routeParams', '$http', ($scope, $routeParams, $http) => {
    $scope.organizationName = $routeParams.organization;
    $scope.policyGroupName = $routeParams.policygroup;
    $scope.processorName = $routeParams.processor;

    $http.get('/scepta-server/design/'+$scope.organizationName+'/group/'+$scope.policyGroupName).success(function(data) {
      $scope.policyGroup = data;
      $scope.policyGroup.processors.forEach(function(proc) {
        if (proc.name === $scope.processorName) {
          $scope.processor = proc;
          $scope.reset();
        }
      });
    });

    $scope.updatePolicyGroup = function() {
      return $http.put('/scepta-server/design/'+$scope.organizationName+'/group/'+$scope.policyGroupName, $scope.policyGroup);
    };

    $scope.dependencyOrderProp = 'artifactId';

    $scope.addDependency = function() {
      $scope.processor.dependencies.push($scope.dependency);
      $scope.dependency = new Object();
    };

    $scope.removeDependency = function(event) {
      var c = confirm("Are you sure?");
      if (c == true) {
        var dependency = JSON.parse(event.currentTarget.attributes.getNamedItem('dependency').value);
        for (var i = $scope.processor.dependencies.length - 1; i >= 0; i--) { 
          var d=$scope.processor.dependencies[i];     
          if (d.groupId === dependency.groupId && d.artifactId === dependency.artifactId) {
            $scope.processor.dependencies.remove(d);
          }
        }
      }
    };

    $scope.update = function() {
      $scope.policyGroup.processors.remove($scope.processor);
      $scope.processor = angular.copy($scope.editable);
      $scope.policyGroup.processors.push($scope.processor);
      $scope.updatePolicyGroup();
    };

    $scope.reset = function() {
      $scope.editable = angular.copy($scope.processor);
    };

  }]);

}

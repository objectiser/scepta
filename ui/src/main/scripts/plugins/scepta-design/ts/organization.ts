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

  export var OrganizationController = _module.controller("SceptaDesign.OrganizationController", ['$scope', '$routeParams', '$http', ($scope, $routeParams, $http) => {
    $scope.organizationName = $routeParams.organization;

    $http.get('/scepta-server/design/'+$scope.organizationName).success(function(data) {
      $scope.organization = data;
    });

    $http.get('/scepta-server/design/'+$scope.organizationName+'/group').success(function(data) {
      $scope.policygroups = data;
    });

    $scope.updateOrganization = function() {
      return $http.put('/scepta-server/design/'+$scope.organizationName, $scope.organization);
    };

    $scope.importPolicyGroup = function() {
    };

    $scope.addPolicyGroup = function() {
      $http.post('/scepta-server/design/'+$scope.organizationName+'/group', $scope.newPolicyGroup)
        .success(function(data, status, headers, config) {
        $http.get('/scepta-server/design/'+$scope.organizationName+'/group').success(function(data) {
          $scope.policygroups = data;
          $scope.newPolicyGroup = new Object();
        });
      });
    };

    $scope.removePolicyGroup = function(event) {
      var c = confirm("Are you sure?");
      if (c == true) {
        var policyGroupName = event.currentTarget.attributes.getNamedItem('policyGroup').value;
        $http.delete('/scepta-server/design/'+$scope.organizationName+'/group/'+policyGroupName)
          .success(function(data, status, headers, config) {
          $http.get('/scepta-server/design/'+$scope.organizationName+'/group').success(function(data) {
            $scope.policygroups = data;
          });
        });
      } 
    };

    $scope.nameOrderProp = 'name';

    $scope.readSingleFile = function(evt) {
      var f = evt.target.files[0]; 

      if (f) {
        var r = new FileReader();
        r.onload = function(e) { 
          $http.post('/scepta-server/design/'+$scope.organizationName+'/import', r.result).
            success(function(data, status, headers, config) {
              $http.get('/scepta-server/design/'+$scope.organizationName+'/group').success(function(data) {
                $scope.policygroups = data;
              });
            });
        }
        r.readAsText(f);
      } else { 
        alert("Failed to load file");
      }
    }

    // TODO: Check if ok to add listener on document element from here???
    document.getElementById('policyGroupImportFile').addEventListener('change', $scope.readSingleFile, false);

  }]);

}

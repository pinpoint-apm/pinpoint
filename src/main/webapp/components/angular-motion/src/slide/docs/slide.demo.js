'use strict';

angular.module('mgcrea.ngStrapDocs')

.config(function($asideProvider, $modalProvider) {
  angular.extend($modalProvider.defaults, {
    container: 'body',
    html: true
  });
  angular.extend($asideProvider.defaults, {
    container: 'body',
    html: true
  });
})

.controller('SlideDemoCtrl', function($scope) {
  $scope.aside = {title: 'Title', content: 'Hello Aside<br />This is a multiline message!'};
  $scope.modal = {title: 'Title', content: 'Hello Modal<br />This is a multiline message!'};
});

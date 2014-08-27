var app = angular.module('myApp', ['angular-intro-plus']);

app.controller('MyController', function ($scope) {

    $scope.CompletedEvent = function () {
        console.log("Completed Event called");
    };

    $scope.ExitEvent = function () {
        console.log("Exit Event called");
    };

    $scope.ChangeEvent = function () {
        console.log("Change Event called");
    };

    $scope.BeforeChangeEvent = function () {
        console.log("Before Change Event called");
    };

    $scope.AfterChangeEvent = function () {
        console.log("After Change Event called");
    };

    $scope.BeforeOverlayCreation = function () {
        console.log('BeforeOverlayCreation');
    };

    $scope.AfterOverlayCreation = function () {
        console.log('AfterOverlayCreation');
    };

    $scope.BeforeOverlayRemoval = function () {
        console.log('BeforeOverlayRemoval');
    };

    $scope.AfterOverlayRemoval = function () {
        console.log('AfterOverlayRemoval');
    };

    $scope.IntroPlusOptions = {
        steps: [
            {
                element: document.querySelector('#step1'),
                intro: "This is the first tooltip."
            },
            {
                element: document.querySelectorAll('#step2')[0],
                intro: "<strong>You</strong> can also <em>include</em> HTML",
                position: 'right'
            },
            {
                element: '#step3',
                intro: 'More features, more fun.',
                position: 'left'
            },
            {
                element: '#step4',
                intro: "Another step.",
                position: 'bottom'
            },
            {
                element: '#step5',
                intro: 'Get it, use it.'
            },
            {
                element: '#hiddenText',
                intro: 'Hidden Text are shown.'
            }
        ],
        helpIcons: '<span class="glyphicon glyphicon-question-sign" style="position:absolute;color:#fff;font-size:24px;z-index:1000000;cursor:pointer;"></span>'
    };

    $scope.$watch('bShowHiddenText', function (newValue, oldValue) {
        if (angular.isDefined(newValue)) {
            $scope.refreshHelpIcons();
        }
    });

});


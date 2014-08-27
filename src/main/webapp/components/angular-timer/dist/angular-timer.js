/**
 * angular-timer - v1.0.12 - 2014-02-03 9:30 PM
 * https://github.com/siddii/angular-timer
 *
 * Copyright (c) 2014 Siddique Hameed
 * Licensed MIT <https://github.com/siddii/angular-timer/blob/master/LICENSE.txt>
 */
angular.module('timer', [])
  .directive('timer', ['$compile', function ($compile) {
    return  {
      restrict: 'E',
      replace: false,
      scope: {
        interval: '=interval',
        startTimeAttr: '=startTime',
        endTimeAttr: '=endTime',
        countdownattr: '=countdown',
        autoStart: '&autoStart'
      },
      controller: ['$scope', '$element', '$attrs', '$timeout', function ($scope, $element, $attrs, $timeout) {

        //angular 1.2 doesn't support attributes ending in "-start", so we're
        //supporting both "autostart" and "auto-start" as a solution for
        //backward and forward compatibility.
        $scope.autoStart = $attrs.autoStart || $attrs.autostart;

        if ($element.html().trim().length === 0) {
          $element.append($compile('<span>{{millis}}</span>')($scope));
        } else {
          $element.append($compile($element.contents())($scope));
        }

        $scope.startTime = null;
        $scope.endTime = null;
        $scope.timeoutId = null;
        $scope.countdown = $scope.countdownattr && parseInt($scope.countdownattr, 10) >= 0 ? parseInt($scope.countdownattr, 10) : undefined;
        $scope.isRunning = false;

        $scope.$on('timer-start', function () {
          $scope.start();
        });

        $scope.$on('timer-resume', function () {
          $scope.resume();
        });

        $scope.$on('timer-stop', function () {
          $scope.stop();
        });

        $scope.$on('timer-clear', function () {
          $scope.clear();
        });

        function resetTimeout() {
          if ($scope.timeoutId) {
            clearTimeout($scope.timeoutId);
          }
        }

        $scope.start = $element[0].start = function () {
          $scope.startTime = $scope.startTimeAttr ? new Date($scope.startTimeAttr) : new Date();
          $scope.endTime = $scope.endTimeAttr ? new Date($scope.endTimeAttr) : null;
          $scope.countdown = $scope.countdownattr && parseInt($scope.countdownattr, 10) > 0 ? parseInt($scope.countdownattr, 10) : undefined;
          resetTimeout();
          tick();
          $scope.isRunning = true;
        };

        $scope.resume = $element[0].resume = function () {
          resetTimeout();
          if ($scope.countdownattr) {
            $scope.countdown += 1;
          }
          $scope.startTime = new Date() - ($scope.stoppedTime - $scope.startTime);
          tick();
          $scope.isRunning = true;
        };

        $scope.stop = $scope.pause = $element[0].stop = $element[0].pause = function () {
          $scope.clear();
          $scope.$emit('timer-stopped', {millis: $scope.millis, seconds: $scope.seconds, minutes: $scope.minutes, hours: $scope.hours, days: $scope.days});
        };

        $scope.clear = $element[0].clear = function () {
          // same as stop but without the event being triggered
          $scope.stoppedTime = new Date();
          resetTimeout();
          $scope.timeoutId = null;
          $scope.isRunning = false;
        };

        $element.bind('$destroy', function () {
          resetTimeout();
          $scope.isRunning = false;
        });

        function calculateTimeUnits() {
          $scope.seconds = Math.floor(($scope.millis / 1000) % 60);
          $scope.minutes = Math.floor((($scope.millis / (60000)) % 60));
          $scope.hours = Math.floor((($scope.millis / (3600000)) % 24));
          $scope.days = Math.floor((($scope.millis / (3600000)) / 24));
        }

        //determine initial values of time units and add AddSeconds functionality
        if ($scope.countdownattr) {
          $scope.millis = $scope.countdownattr * 1000;

          $scope.addCDSeconds = $element[0].addCDSeconds = function(extraSeconds){
            $scope.countdown += extraSeconds;
            $scope.$digest();
          };

          $scope.$on('timer-add-cd-seconds', function (e, extraSeconds) {
            $timeout(function (){
              $scope.addCDSeconds(extraSeconds);
            });
          });
        } else {
          $scope.millis = 0;
        }
        calculateTimeUnits();

        var tick = function () {

          $scope.millis = new Date() - $scope.startTime;
          var adjustment = $scope.millis % 1000;

          if ($scope.endTimeAttr) {
            $scope.millis = $scope.endTime - new Date();
            adjustment = $scope.interval - $scope.millis % 1000;
          }


          if ($scope.countdownattr) {
            $scope.millis = $scope.countdown * 1000;
          }

          if ($scope.millis < 0) {
            $scope.stop();
            $scope.millis = 0;
            calculateTimeUnits();
            return;
          }
          calculateTimeUnits();
          if ($scope.countdown > 0) {
            $scope.countdown--;
          }
          else if ($scope.countdown <= 0) {
            $scope.stop();
            return;
          }

          //We are not using $timeout for a reason. Please read here - https://github.com/siddii/angular-timer/pull/5
          $scope.timeoutId = setTimeout(function () {
            tick();
            $scope.$digest();
          }, $scope.interval - adjustment);

          $scope.$emit('timer-tick', {timeoutId: $scope.timeoutId, millis: $scope.millis});
        };

        if ($scope.autoStart === undefined || $scope.autoStart === true) {
          $scope.start();
        }
      }]
    };
  }]);

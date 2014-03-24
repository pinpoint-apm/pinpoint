'use strict';

pinpointApp.controller('LoginFormCtrl', [ '$scope', '$http', '$cookies', '$base64', 'encodeURIComponentFilter',
    function ($scope, $http, $cookies, $base64, encodeURIComponentFilter) {

        // initialize variables

        // initialize variables of methods
        var setRememberId, getRememberId, tryLogin, parseUserDataForSSO;


        setRememberId = function (inputId) {
            $cookies['rememberedId'] = inputId;
        };

        getRememberId = function () {
            return $cookies['rememberedId'] || '';
        };

        tryLogin = function (user) {
            $http
                .post('https://alpha-enterprise-auth.nhncorp.com/imap/api/auth', parseUserDataForSSO(user))
//                .post('https://enterprise-auth.nhncorp.com/imap/api/auth', parseUserDataForSSO(user))
                .success(function(data, status){
                    console.log('success', arguments);
                    if (data && data.returnCode === '00') {
                        $scope.hide();
                    } else {
                        alert('Not existing user. Please try again.');
                    }
                })
                .error(function (data, status) {
                    console.log('error', arguments);
                    alert('Login failed. Please try again.');
                });
        };

        parseUserDataForSSO = function (user) {
            return {
                employeeId: encodeURIComponentFilter($base64.encode(user.id)),
                password: encodeURIComponentFilter($base64.encode(user.password))
            };
        };

        $scope.signIn = function (user) {
            console.log('signIn', user);
            setRememberId(user.rememberId ? user.employeeId : '');
            tryLogin(user);
        };

        // bootstrap
        $scope.user = {
            id: getRememberId(),
            password: '',
            rememberId: getRememberId() ? true : false
        };


    }
]);

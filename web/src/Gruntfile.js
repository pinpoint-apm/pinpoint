/**
 * http://gruntjs.com/configuring-tasks
 */
module.exports = function (grunt) {
    var path = require('path');
    var DOC_PATH = 'jsdoc/';

    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),

        clean: [ DOC_PATH ],
        jsdoc: {
            dist: {
                src: ['main/webapp/common/**/*.js', 'main/webapp/features/**/*.js', 'main/webapp/pages/**/*.js', 'main/webapp/scripts/**/*.js'],
                options: {
                    verbose: true,
                    destination: DOC_PATH,
                    'private': false
                }
            }
        }
    });

    // Load task libraries
    [
     	'grunt-contrib-clean',
     	'grunt-contrib-copy',
        'grunt-jsdoc',
    ].forEach(function (taskName) {
        grunt.loadNpmTasks(taskName);
    });

    grunt.registerTask('jsdocClean', 'Clear document folder', [ 'clean' ]);


    grunt.registerTask('jsdocBuild', 'Create documentations', [
        'clean',
        'jsdoc:dist'
    ]);
};

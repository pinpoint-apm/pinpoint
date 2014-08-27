module.exports = function(grunt) {

    // Project configuration.
    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),
        uglify: {
            options: {
                banner: '/*! <%= pkg.name %> <%= grunt.template.today("yyyy-mm-dd") %> */\n'
            },
            build: {
                src: 'src/angular-intro-plus.js',
                dest: 'build/angular-intro-plus.min.js'
            }
        },
        cssmin: {
            add_banner: {
                options: {
                    banner: '/*! <%= pkg.name %> <%= grunt.template.today("yyyy-mm-dd") %> */'
                },
                files: {
                    'build/angular-intro-plus.min.css': ['src/angular-intro-plus.css']
                }
            }
        },
        jshint: {
            lib: {
                options: {},
                src: ['lib/*.js']
            }
        },
        watch: {
            scripts: {
                files: 'lib/*.js',
                tasks: ['jshint', 'uglify', 'cssmin'],
                options: {
                    interrupt: true
                }
            },
            gruntfile: {
                files: 'Gruntfile.js'
            }
        }
    });

    // Load all grunt tasks
    require('load-grunt-tasks')(grunt);

    // Default task(s).
    grunt.registerTask('default', ['jshint', 'uglify', 'cssmin']);
};

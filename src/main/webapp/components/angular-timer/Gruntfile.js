module.exports = function (grunt) {

  grunt.loadNpmTasks('grunt-contrib-clean');
  grunt.loadNpmTasks('grunt-contrib-copy');
  grunt.loadNpmTasks('grunt-contrib-jshint');
  grunt.loadNpmTasks('grunt-contrib-concat');
  grunt.loadNpmTasks('grunt-contrib-watch');
  grunt.loadNpmTasks('grunt-contrib-uglify');
  grunt.loadNpmTasks('grunt-karma');
  grunt.loadNpmTasks('grunt-contrib-connect');

  var userConfig = {
    dist_dir: 'dist',

    app_files: {
      js: [ 'app/**/*.js', '!app/**/*.spec.js' ]
    }
  };

  var taskConfig = {
    pkg: grunt.file.readJSON("package.json"),

    meta: {
      banner: '/**\n' +
        ' * <%= pkg.name %> - v<%= pkg.version %> - <%= grunt.template.today("yyyy-mm-dd h:MM TT") %>\n' +
        ' * <%= pkg.homepage %>\n' +
        ' *\n' +
        ' * Copyright (c) <%= grunt.template.today("yyyy") %> <%= pkg.author %>\n' +
        ' * Licensed <%= pkg.licenses.type %> <<%= pkg.licenses.url %>>\n' +
        ' */\n'
    },

    concat: {
      compile_js: {
        options: {
          banner: '<%= meta.banner %>'
        },
        src: [
          'app/**/*.js'
        ],
        dest: '<%= dist_dir %>/<%= pkg.name %>.js'
      }
    },

    uglify: {
      options: {
        banner: '<%= meta.banner %>'
      },
      files: {
        src: ['<%= concat.compile_js.dest %>'],
        dest: '<%= dist_dir %>/<%= pkg.name %>.min.js'
      }
    },

    jshint: {
      src: [
        '<%= app_files.js %>'
      ],
      gruntfile: [
        'Gruntfile.js'
      ],
      options: {
        curly: true,
        immed: true,
        newcap: true,
        noarg: true,
        sub: true,
        boss: true,
        eqnull: true
      },
    },

    connect: {
      server: {
        options: {
          port: 3030,
          base: '.',
          keepalive: false,
          livereload:true,
          open: true
        }
      },
      testserver: {
        options: {
          port: 3030,
          base: '.'
        }
      }
    },

    karma: {
      unit: {
        configFile: 'config/karma.conf.js',
        singleRun: true,
        browsers: ['PhantomJS']
      },
      e2e: {
        configFile: 'config/karma-e2e.conf.js',
        singleRun: true,
        browsers: ['PhantomJS']
      }
    },

    watch: {
      scripts: {
        files: ['Gruntfile.js', '*.json', 'app/**/*.js','*.html'],
        tasks: ['build'],
        options: {
          livereload: true
        }
      }
    }
  };

  grunt.initConfig(grunt.util._.extend(taskConfig, userConfig));

  grunt.registerTask('default', [ 'connect:server', 'watch']);

  grunt.registerTask('tests', [ 'connect:testserver', 'build', 'karma:unit', 'karma:e2e']);

  grunt.registerTask('build', [
    'jshint', 'concat', 'uglify'
  ]);

};

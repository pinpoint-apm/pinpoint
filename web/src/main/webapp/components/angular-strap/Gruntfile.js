'use strict';

// # Globbing
// for performance reasons we're only matching one level down:
// 'test/spec/{,*/}*.js'
// use this if you want to recursively match all subfolders:
// 'test/spec/**/*.js'

module.exports = function (grunt) {

  // Load grunt tasks automatically
  require('load-grunt-tasks')(grunt);

  // Time how long tasks take. Can help when optimizing build times
  // require('time-grunt')(grunt);

  // Define the configuration for all the tasks
  grunt.initConfig({

    // Project meta
    pkg: require('./package.json'),
    bower: require('./bower.json'),
    meta: {
      banner: '/**\n' +
      ' * <%= pkg.name %>\n' +
      ' * @version v<%= pkg.version %> - <%= grunt.template.today("yyyy-mm-dd") %>\n' +
      ' * @link <%= pkg.homepage %>\n' +
      ' * @author <%= pkg.author.name %> (<%= pkg.author.email %>)\n' +
      ' * @license MIT License, http://www.opensource.org/licenses/MIT\n' +
      ' */\n'
    },

    // Project settings
    yo: {
      // Configurable paths
      src: require('./bower.json').appPath || 'src',
      dist: 'dist',
      docs: 'docs',
      pages: 'pages'
    },

    // Watches files for changes and runs tasks based on the changed files
    watch: {
      js: {
        files: ['{.tmp,<%= yo.src %>}/scripts/{,*/}*.js'],
        tasks: ['newer:jshint:all']
      },
      jsTest: {
        files: ['test/spec/{,*/}*.js'],
        tasks: ['newer:jshint:test', 'karma']
      },
      styles: {
        options: {
          spawn: false
        },
        files: ['{docs,<%= yo.src %>}/styles/{,*/}*.less'],
        tasks: ['less:dev', 'autoprefixer']
      },
      gruntfile: {
        files: ['Gruntfile.js']
      },
      livereload: {
        options: {
          livereload: '<%= connect.options.livereload %>'
        },
        files: [
          '{docs,.dev,<%= yo.src %>}/{,*/}{,docs/}*.html',
          '{docs,.tmp,<%= yo.src %>}/{,*/}*.css',
          '{docs,.dev,.tmp,<%= yo.src %>}/{,*/}{,docs/}*.js',
          '{docs,<%= yo.src %>}/images/{,*/}*.{png,jpg,jpeg,gif,webp,svg}'
        ]
      }
    },

    // The actual grunt server settings
    connect: {
      options: {
        port: 9000,
        // Change this to '0.0.0.0' to access the server from outside.
        hostname: '0.0.0.0',
        livereload: 35729
      },
      livereload: {
        options: {
          open: true,
          base: [
            '.tmp',
            '.dev',
            'docs',
            '<%= yo.src %>'
          ]
        }
      },
      test: {
        options: {
          port: 9001,
          base: [
            '.tmp',
            'test',
            '<%= yo.src %>'
          ]
        }
      },
      dist: {
        options: {
          base: '<%= yo.dist %>'
        }
      }
    },

    // Make sure code styles are up to par and there are no obvious mistakes
    jshint: {
      options: {
        jshintrc: '.jshintrc',
        reporter: require('jshint-stylish')
      },
      all: [
        'Gruntfile.js',
        '<%= yo.src %>/scripts/{,*/}*.js'
      ],
      test: {
        options: {
          jshintrc: 'test/.jshintrc'
        },
        src: ['test/spec/{,*/}*.js']
      }
    },

    // Empties folders to start fresh
    clean: {
      dist: {
        files: [{
          dot: true,
          src: [
            '.tmp',
            '<%= yo.dist %>/*',
            '!<%= yo.dist %>/.git*'
          ]
        }]
      },
      docs: {
        files: [{
          dot: true,
          src: [
            '.tmp',
            '<%= yo.pages %>/*',
            '!<%= yo.pages %>/.git*'
          ]
        }]
      },
      server: '.tmp'
    },

    // Compile less stylesheets
    less: {
      options: {
      },
      dev: {
        options: {
          dumpLineNumbers: 'comments',
        },
        files: [{
          expand: true,
          cwd: '<%= yo.docs %>/styles/',
          src: '*.less',
          dest: '.tmp/styles/',
          ext: '.css'
        }]
      },
      docs: {
        options: {
          cleancss: false
        },
        files: [{
          expand: true,
          cwd: '<%= yo.docs %>/styles/',
          src: '*.less',
          dest: '.tmp/styles/',
          ext: '.css'
        }]
      }
    },

    // Add vendor prefixed styles
    autoprefixer: {
      options: {
        browsers: ['last 2 versions']
      },
      dist: {
        files: [{
          expand: true,
          cwd: '.tmp/styles/',
          src: '{,*/}*.css',
          dest: '.tmp/styles/'
        }]
      }
    },

    // Reads HTML for usemin blocks to enable smart builds that automatically
    // concat, minify and revision files. Creates configurations in memory so
    // additional tasks can operate on them
    useminPrepare: {
      html: '<%= yo.docs %>/index.html',
      options: {
        dest: '<%= yo.pages %>'
      }
    },

    // Performs rewrites based on rev and the useminPrepare configuration
    usemin: {
      html: '<%= yo.pages %>/index.html',
      css: ['<%= yo.pages %>/styles/{,*/}*.css'],
      options: {
        assetsDirs: ['<%= yo.pages %>', '<%= yo.pages %>/images']
      }
    },

    // Embed static ngincludes
    nginclude: {
      docs: {
        files: [{
          src: '<%= yo.docs %>/index.html',
          dest: '<%= yo.pages %>/index.html'
        }],
        options: {
          assetsDirs: ['<%= yo.src %>', '<%= yo.docs %>']
        }
      }
    },

    // Minify html files
    htmlmin: {
      options: {
        collapseWhitespace: true,
        removeComments: false
      },
      docs: {
        files: [{
          expand: true,
          cwd: '<%= yo.pages %>',
          src: ['*.html'],//, 'views/{,*/}*.html'],
          dest: '<%= yo.pages %>'
        }]
      }
    },

    // Renames files for browser caching purposes
    rev: {
      dist: {
        files: {
          src: [
            '<%= yo.pages %>/scripts/{,*/}*.js',
            '<%= yo.pages %>/styles/{,*/}*.css',
            '<%= yo.pages %>/images/{,*/}*.{png,jpg,jpeg,gif,webp,svg}',
            '<%= yo.pages %>/styles/fonts/*'
          ]
        }
      }
    },

    // Copies remaining files to places other tasks can use
    copy: {
      static: {
        files: [{
          expand: true,
          cwd: '<%= yo.pages %>',
          dest: '<%= yo.pages %>/static',
          src: [
            'images/{,*/}*.png',
            'scripts/{,*/}*.js',
            'styles/{,*/}*.css'
          ]
        }]
      },
      docs: {
        files: [{
          expand: true,
          cwd: '<%= yo.docs %>/',
          dest: '<%= yo.pages %>',
          src: [
            'images/*',
            '1.0/**/*'
          ]
        }, {
          src: 'bower_components/angular-motion/dist/angular-motion.css',
          dest: '<%= yo.pages %>/styles/angular-motion.css'
        }, {
          src: '.tmp/styles/bootstrap-additions.css',
          dest: '<%= yo.pages %>/styles/bootstrap-additions.css'
        }, {
          expand: true,
          cwd: '<%= yo.dist %>',
          dest: '<%= yo.pages %>/dist',
          src: '{,*/}*.{js,map}'
        }]
      }
    },

    // Run some tasks in parallel to speed up the build process
    concurrent: {
      docs: [
        'less:docs',
        'uglify:generated',
        'cssmin:generated'
      ],
      server: [
        'less:dev'
      ],
      test: [
        'less:dev'
      ],
      dist: [
        'less:dist',
        'imagemin',
        'svgmin',
        'htmlmin'
      ]
    },

    concat: {
      // generated: {
      //   options: {
      //     banner: '(function(window, document, $, undefined) {\n\'use strict\';\n',
      //     footer: '\n})(window, document, window.jQuery);\n'
      //   }
      // },
      dist: {
        options: {
          // Replace all 'use strict' statements in the code with a single one at the top
          banner: '(function(window, document, undefined) {\n\'use strict\';\n',
          footer: '\n})(window, document);\n',
          process: function(src, filepath) {
            return '// Source: ' + filepath + '\n' +
              src.replace(/(^|\n)[ \t]*('use strict'|"use strict");?\s*/g, '$1');
          }
        },
        files: [{
          src: ['<%= yo.src %>/module.js', '<%= yo.src %>/{,*/}*.js'],
          dest: '<%= yo.dist %>/<%= pkg.name %>.js'
        }, {
          src: ['<%= yo.dist %>/modules/{,*/}*.tpl.js'],
          dest: '<%= yo.dist %>/<%= pkg.name %>.tpl.js'
        }]
      },
      banner: {
        options: {
          banner: '<%= meta.banner %>',
        },
        files: [{
          expand: true,
          cwd: '<%= yo.dist %>',
          src: '{,*/}*.js',
          dest: '<%= yo.dist %>'
        }]
      },
      docs: {
        options: {
          banner: '<%= meta.banner %>',
        },
        files: [{
          expand: true,
          cwd: '<%= yo.pages %>',
          src: ['scripts/{demo,docs,angular-strap}*', 'styles/{main}*'],
          dest: '<%= yo.pages %>'
        }]
      }
    },

    // Allow the use of non-minsafe AngularJS files. Automatically makes it
    // minsafe compatible so Uglify does not destroy the ng references
    ngmin: {
      dist: {
        files: [{
          src: '<%= yo.dist %>/<%= pkg.name %>.js',
          dest: '<%= yo.dist %>/<%= pkg.name %>.js'
        }]
      },
      modules: {
        files: [{
          expand: true,
          flatten: true,
          cwd: '<%= yo.src %>',
          src: '{,*/}*.js',
          dest: '<%= yo.dist %>/modules'
        }]
      },
      docs: {
        files: [{
          expand: true,
          cwd: '.tmp/concat/scripts',
          src: '*.js',
          dest: '.tmp/concat/scripts'
        }]
      }
    },

    ngtemplates:  {
      test: {
        options:  {
          module: function(src) { return 'mgcrea.ngStrap.' + src.match(/src\/(.+)\/.*/)[1]; },
          url: function(url) { return url.replace('src/', ''); },
          htmlmin: { collapseWhitespace: true },
          usemin: 'scripts/angular-strap.tpl.min.js' // docs
        },
        files: [{
          expand: true,
          flatten: true,
          cwd: '<%= yo.src %>',
          src: '{,*/}/*.tpl.html',
          dest: '.tmp/templates',
          ext: '.tpl.js'
        }]
      },
      dist: {
        options:  {
          module: function(src) { return 'mgcrea.ngStrap.' + src.match(/src\/(.+)\/.*/)[1]; },
          url: function(url) { return url.replace('src/', ''); },
          htmlmin: { collapseWhitespace: true },
        },
        files: [{
          expand: true,
          flatten: true,
          cwd: '<%= yo.src %>',
          src: '{,*/}/*.tpl.html',
          dest: '<%= yo.dist %>/modules',
          ext: '.tpl.js'
        }]
      },
      docs: {
        options:  {
          module: 'mgcrea.ngStrapDocs',
          usemin: 'scripts/docs.tpl.min.js'
        },
        files: [{
          cwd: '<%= yo.src %>',
          src: '{,*/}docs/*.html',
          dest: '.tmp/templates/scripts/src-docs.js'
        },
        {
          cwd: '<%= yo.docs %>',
          // src: 'views/{,*/}*.html',
          src: 'views/{aside,sidebar}.html',
          dest: '.tmp/templates/scripts/docs-views.js'
        }
        ]
      }
    },

    uglify: {
      dist: {
        options: {
          report: 'gzip',
          sourceMap: '<%= yo.dist %>/<%= pkg.name %>.min.map',
          sourceMappingURL: '<%= pkg.name %>.min.map'
        },
        files: [{
          expand: true,
          cwd: '<%= yo.dist %>',
          src: '{,*/}*.js',
          dest: '<%= yo.dist %>',
          ext: '.min.js'
        }, {
          expand: true,
          cwd: '<%= yo.dist %>',
          src: '{,*/}*.tpl.js',
          dest: '<%= yo.dist %>',
          ext: '.tpl.min.js'
        }]
      }
    },

    // Test settings
    karma: {
      options: {
        configFile: 'test/karma.conf.js',
        browsers: ['PhantomJS']
      },
      unit: {
        options: {
          reporters: ['dots', 'coverage']
        },
        singleRun: true,
      },
      server: {
        options: {
          reporters: ['progress']
        },
        autoWatch: true
      }
    },

    coveralls: {
      options: {
        /*jshint camelcase: false */
        coverage_dir: 'test/coverage/PhantomJS 1.9.7 (Linux)/',
        force: true
      }
    }

  });


  grunt.registerTask('serve', function (target) {
    if (target === 'dist') {
      return grunt.task.run(['build', 'connect:dist:keepalive']);
    }

    grunt.task.run([
      'clean:server',
      'ngtemplates:test',
      'concurrent:server',
      'autoprefixer',
      'connect:livereload',
      'watch'
    ]);
  });

  grunt.registerTask('test', [
    'clean:server',
    // 'concurrent:test',
    // 'autoprefixer',
    'ngtemplates:test',
    'connect:test',
    'karma:unit'
  ]);

  grunt.registerTask('build', [
    'clean:dist',
    'ngtemplates:dist',
    'concat:dist',
    'ngmin:dist',
    'ngmin:modules',
    'uglify:dist',
    'concat:banner'
  ]);

  grunt.registerTask('docs', [
    'clean:docs',
    'useminPrepare',
    // 'concurrent:docs',
    'less:docs',
    'autoprefixer',
    'nginclude:docs',
    'ngtemplates:test',
    'ngtemplates:docs',
    'concat:generated',
    'ngmin:docs',
    'copy:docs',
    'cssmin:generated',
    'uglify:generated',
    'concat:docs',
    'copy:static',
    'rev',
    'usemin',
    // 'htmlmin:docs' // breaks code preview
  ]);

  grunt.registerTask('default', [
    'newer:jshint',
    'test',
    'build'
  ]);

};

/*
Plugin Name: amCharts Responsive
Description: This plugin add responsive functionality to JavaScript Charts and Maps.
Author: Martynas Majeris, amCharts
Contributors: Ohad Schneider
Version: 1.0.3
Author URI: http://www.amcharts.com/

Copyright 2015 amCharts

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

	http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

Please note that the above license covers only this plugin. It by all means does
not apply to any other amCharts products that are covered by different licenses.
*/

/*global AmCharts*/

AmCharts.addInitHandler( function( chart ) {
  "use strict";

  if ( chart.responsive === undefined || chart.responsive.ready === true || chart.responsive.enabled !== true )
    return;
  var version = chart.version.split( "." );
  if ( ( version.length < 2 ) || Number( version[ 0 ] ) < 3 || ( Number( version[ 0 ] ) === 3 && Number( version[ 1 ] ) < 13 ) )
    return;

  // a short variable for easy reference
  var r = chart.responsive;

  r.ready = true;
  r.currentRules = {};
  r.overridden = [];

  // defaults per chart type
  var defaults = {

    /**
     * AmPie
     */
    "pie": [

      /**
       * Disable legend in certain cases
       */
      {
        "maxWidth": 550,
        "legendPosition": "left",
        "overrides": {
          "legend": {
            "enabled": false
          }
        }
      }, {
        "maxWidth": 550,
        "legendPosition": "right",
        "overrides": {
          "legend": {
            "enabled": false
          }
        }
      }, {
        "maxWidth": 150,
        "overrides": {
          "legend": {
            "enabled": false
          }
        }
      }, {
        "maxHeight": 350,
        "legendPosition": "top",
        "overrides": {
          "legend": {
            "enabled": false
          }
        }
      }, {
        "maxHeight": 350,
        "legendPosition": "bottom",
        "overrides": {
          "legend": {
            "enabled": false
          }
        }
      }, {
        "maxHeight": 150,
        "overrides": {
          "legend": {
            "enabled": false
          }
        }
      },

      /**
       * Narrow chart
       */
      {
        "maxWidth": 400,
        "overrides": {
          "labelsEnabled": false
        }
      }, {
        "maxWidth": 100,
        "overrides": {
          "legend": {
            "enabled": false
          }
        }
      },

      /**
       * Short chart
       */
      {
        "maxHeight": 350,
        "overrides": {
          "pullOutRadius": 0
        }
      }, {
        "maxHeight": 200,
        "overrides": {
          "titles": {
            "enabled": false
          },
          "labelsEnabled": false
        }
      },

      /**
       * Supersmall
       */
      {
        "maxWidth": 60,
        "overrides": {
          "autoMargins": false,
          "marginTop": 0,
          "marginBottom": 0,
          "marginLeft": 0,
          "marginRight": 0,
          "radius": "50%",
          "innerRadius": 0,
          "balloon": {
            "enabled": false
          },
          "legend": {
            "enabled": false
          }
        }
      }, {
        "maxHeight": 60,
        "overrides": {
          "marginTop": 0,
          "marginBottom": 0,
          "marginLeft": 0,
          "marginRight": 0,
          "radius": "50%",
          "innerRadius": 0,
          "balloon": {
            "enabled": false
          },
          "legend": {
            "enabled": false
          }
        }
      }
    ],

    /**
     * AmFunnel
     */
    "funnel": [ {
      "maxWidth": 550,
      "legendPosition": "left",
      "overrides": {
        "legend": {
          "enabled": false
        }
      }
    }, {
      "maxWidth": 550,
      "legendPosition": "right",
      "overrides": {
        "legend": {
          "enabled": false
        }
      }
    }, {
      "maxWidth": 150,
      "overrides": {
        "legend": {
          "enabled": false
        }
      }
    }, {
      "maxHeight": 500,
      "legendPosition": "top",
      "overrides": {
        "legend": {
          "enabled": false
        }
      }
    }, {
      "maxHeight": 500,
      "legendPosition": "bottom",
      "overrides": {
        "legend": {
          "enabled": false
        }
      }
    }, {
      "maxHeight": 150,
      "overrides": {
        "legend": {
          "enabled": false
        }
      }
    }, {
      "maxWidth": 400,
      "overrides": {
        "labelsEnabled": false,
        "marginLeft": 10,
        "marginRight": 10,
        "legend": {
          "enabled": false
        }
      }
    }, {
      "maxHeight": 350,
      "overrides": {
        "pullOutRadius": 0,
        "legend": {
          "enabled": false
        }
      }
    }, {
      "maxHeight": 300,
      "overrides": {
        "titles": {
          "enabled": false
        }
      }
    } ],

    /**
     * AmRadar
     */
    "radar": [ {
      "maxWidth": 550,
      "legendPosition": "left",
      "overrides": {
        "legend": {
          "enabled": false
        }
      }
    }, {
      "maxWidth": 550,
      "legendPosition": "right",
      "overrides": {
        "legend": {
          "enabled": false
        }
      }
    }, {
      "maxWidth": 150,
      "overrides": {
        "legend": {
          "enabled": false
        }
      }
    }, {
      "maxHeight": 350,
      "legendPosition": "top",
      "overrides": {
        "legend": {
          "enabled": false
        }
      }
    }, {
      "maxHeight": 350,
      "legendPosition": "bottom",
      "overrides": {
        "legend": {
          "enabled": false
        }
      }
    }, {
      "maxHeight": 150,
      "overrides": {
        "legend": {
          "enabled": false
        }
      }
    }, {
      "maxWidth": 300,
      "overrides": {
        "labelsEnabled": false
      }
    }, {
      "maxWidth": 200,
      "overrides": {
        "autoMargins": false,
        "marginTop": 0,
        "marginBottom": 0,
        "marginLeft": 0,
        "marginRight": 0,
        "radius": "50%",
        "titles": {
          "enabled": false
        },
        "valueAxes": {
          "labelsEnabled": false,
          "radarCategoriesEnabled": false
        }
      }
    }, {
      "maxHeight": 300,
      "overrides": {
        "labelsEnabled": false
      }
    }, {
      "maxHeight": 200,
      "overrides": {
        "autoMargins": false,
        "marginTop": 0,
        "marginBottom": 0,
        "marginLeft": 0,
        "marginRight": 0,
        "radius": "50%",
        "titles": {
          "enabled": false
        },
        "valueAxes": {
          "radarCategoriesEnabled": false
        }
      }
    }, {
      "maxHeight": 100,
      "overrides": {
        "valueAxes": {
          "labelsEnabled": false
        }
      }
    } ],

    /**
     * AmGauge
     */
    "gauge": [ {
      "maxWidth": 550,
      "legendPosition": "left",
      "overrides": {
        "legend": {
          "enabled": false
        }
      }
    }, {
      "maxWidth": 550,
      "legendPosition": "right",
      "overrides": {
        "legend": {
          "enabled": false
        }
      }
    }, {
      "maxWidth": 150,
      "overrides": {
        "legend": {
          "enabled": false
        }
      }
    }, {
      "maxHeight": 500,
      "legendPosition": "top",
      "overrides": {
        "legend": {
          "enabled": false
        }
      }
    }, {
      "maxHeight": 500,
      "legendPosition": "bottom",
      "overrides": {
        "legend": {
          "enabled": false
        }
      }
    }, {
      "maxHeight": 150,
      "overrides": {
        "legend": {
          "enabled": false
        }
      }
    }, {
      "maxWidth": 200,
      "overrides": {
        "titles": {
          "enabled": false
        },
        "allLabels": {
          "enabled": false
        },
        "axes": {
          "labelsEnabled": false
        }
      }
    }, {
      "maxHeight": 200,
      "overrides": {
        "titles": {
          "enabled": false
        },
        "allLabels": {
          "enabled": false
        },
        "axes": {
          "labelsEnabled": false
        }
      }
    } ],

    /**
     * AmSerial
     */
    "serial": [

      /**
       * Disable legend in certain cases
       */
      {
        "maxWidth": 550,
        "legendPosition": "left",
        "overrides": {
          "legend": {
            "enabled": false
          }
        }
      }, {
        "maxWidth": 550,
        "legendPosition": "right",
        "overrides": {
          "legend": {
            "enabled": false
          }
        }
      }, {
        "maxWidth": 100,
        "overrides": {
          "legend": {
            "enabled": false
          }
        }
      }, {
        "maxHeight": 350,
        "legendPosition": "top",
        "overrides": {
          "legend": {
            "enabled": false
          }
        }
      }, {
        "maxHeight": 350,
        "legendPosition": "bottom",
        "overrides": {
          "legend": {
            "enabled": false
          }
        }
      }, {
        "maxHeight": 100,
        "overrides": {
          "legend": {
            "enabled": false
          }
        }
      },


      /**
       * Narrow chart
       */
      {
        "maxWidth": 350,
        "overrides": {
          "autoMarginOffset": 0,
          "graphs": {
            "hideBulletsCount": 10
          }
        }
      }, {
        "maxWidth": 350,
        "rotate": false,
        "overrides": {
          "marginLeft": 10,
          "marginRight": 10,
          "valueAxes": {
            "ignoreAxisWidth": true,
            "inside": true,
            "title": "",
            "showFirstLabel": false,
            "showLastLabel": false
          },
          "graphs": {
            "bullet": "none"
          }
        }
      }, {
        "maxWidth": 350,
        "rotate": true,
        "overrides": {
          "marginLeft": 10,
          "marginRight": 10,
          "categoryAxis": {
            "ignoreAxisWidth": true,
            "inside": true,
            "title": ""
          }
        }
      }, {
        "maxWidth": 200,
        "rotate": false,
        "overrides": {
          "marginLeft": 10,
          "marginRight": 10,
          "marginTop": 10,
          "marginBottom": 10,
          "categoryAxis": {
            "ignoreAxisWidth": true,
            "labelsEnabled": false,
            "inside": true,
            "title": "",
            "guides": {
              "inside": true
            }
          },
          "valueAxes": {
            "ignoreAxisWidth": true,
            "labelsEnabled": false,
            "axisAlpha": 0,
            "guides": {
              "label": ""
            }
          },
          "legend": {
            "enabled": false
          }
        }
      }, {
        "maxWidth": 200,
        "rotate": true,
        "overrides": {
          "chartScrollbar": {
            "scrollbarHeight": 4,
            "graph": "",
            "resizeEnabled": false
          },
          "categoryAxis": {
            "labelsEnabled": false,
            "axisAlpha": 0,
            "guides": {
              "label": ""
            }
          },
          "legend": {
            "enabled": false
          }
        }
      }, {
        "maxWidth": 100,
        "rotate": false,
        "overrides": {
          "valueAxes": {
            "gridAlpha": 0
          }
        }
      }, {
        "maxWidth": 100,
        "rotate": true,
        "overrides": {
          "categoryAxis": {
            "gridAlpha": 0
          }
        }
      },

      /**
       * Short chart
       */
      {
        "maxHeight": 300,
        "overrides": {
          "autoMarginOffset": 0,
          "graphs": {
            "hideBulletsCount": 10
          }
        }
      }, {
        "maxHeight": 200,
        "rotate": false,
        "overrides": {
          "marginTop": 10,
          "marginBottom": 10,
          "categoryAxis": {
            "ignoreAxisWidth": true,
            "inside": true,
            "title": "",
            "showFirstLabel": false,
            "showLastLabel": false
          }
        }
      }, {
        "maxHeight": 200,
        "rotate": true,
        "overrides": {
          "marginTop": 10,
          "marginBottom": 10,
          "valueAxes": {
            "ignoreAxisWidth": true,
            "inside": true,
            "title": "",
            "showFirstLabel": false,
            "showLastLabel": false
          },
          "graphs": {
            "bullet": "none"
          }
        }
      }, {
        "maxHeight": 150,
        "rotate": false,
        "overrides": {
          "titles": {
            "enabled": false
          },
          "chartScrollbar": {
            "scrollbarHeight": 4,
            "graph": "",
            "resizeEnabled": false
          },
          "categoryAxis": {
            "labelsEnabled": false,
            "ignoreAxisWidth": true,
            "axisAlpha": 0,
            "guides": {
              "label": ""
            }
          }
        }
      }, {
        "maxHeight": 150,
        "rotate": true,
        "overrides": {
          "titles": {
            "enabled": false
          },
          "valueAxes": {
            "labelsEnabled": false,
            "ignoreAxisWidth": true,
            "axisAlpha": 0,
            "guides": {
              "label": ""
            }
          }
        }
      }, {
        "maxHeight": 100,
        "rotate": false,
        "overrides": {
          "valueAxes": {
            "labelsEnabled": false,
            "ignoreAxisWidth": true,
            "axisAlpha": 0,
            "gridAlpha": 0,
            "guides": {
              "label": ""
            }
          }
        }
      }, {
        "maxHeight": 100,
        "rotate": true,
        "overrides": {
          "categoryAxis": {
            "labelsEnabled": false,
            "ignoreAxisWidth": true,
            "axisAlpha": 0,
            "gridAlpha": 0,
            "guides": {
              "label": ""
            }
          }
        }
      },

      /**
       * Really small charts: microcharts and sparklines
       */
      {
        "maxWidth": 100,
        "overrides": {
          "autoMargins": false,
          "marginTop": 0,
          "marginBottom": 0,
          "marginLeft": 0,
          "marginRight": 0,
          "categoryAxis": {
            "labelsEnabled": false
          },
          "valueAxes": {
            "labelsEnabled": false
          }
        }
      }, {
        "maxHeight": 100,
        "overrides": {
          "autoMargins": false,
          "marginTop": 0,
          "marginBottom": 0,
          "marginLeft": 0,
          "marginRight": 0,
          "categoryAxis": {
            "labelsEnabled": false
          },
          "valueAxes": {
            "labelsEnabled": false
          }
        }
      }
    ],

    /**
     * AmXY
     */
    "xy": [

      /**
       * Disable legend in certain cases
       */
      {
        "maxWidth": 550,
        "legendPosition": "left",
        "overrides": {
          "legend": {
            "enabled": false
          }
        }
      }, {
        "maxWidth": 550,
        "legendPosition": "right",
        "overrides": {
          "legend": {
            "enabled": false
          }
        }
      }, {
        "maxWidth": 100,
        "overrides": {
          "legend": {
            "enabled": false
          }
        }
      }, {
        "maxHeight": 350,
        "legendPosition": "top",
        "overrides": {
          "legend": {
            "enabled": false
          }
        }
      }, {
        "maxHeight": 350,
        "legendPosition": "bottom",
        "overrides": {
          "legend": {
            "enabled": false
          }
        }
      }, {
        "maxHeight": 100,
        "overrides": {
          "legend": {
            "enabled": false
          }
        }
      },

      /**
       * Narrow chart
       */
      {
        "maxWidth": 250,
        "overrides": {
          "autoMarginOffset": 0,
          "autoMargins": false,
          "marginTop": 0,
          "marginBottom": 0,
          "marginLeft": 0,
          "marginRight": 0,
          "valueAxes": {
            "inside": true,
            "title": "",
            "showFirstLabel": false,
            "showLastLabel": false
          },
          "legend": {
            "enabled": false
          }
        }
      }, {
        "maxWidth": 150,
        "overrides": {
          "valueyAxes": {
            "labelsEnabled": false,
            "axisAlpha": 0,
            "gridAlpha": 0,
            "guides": {
              "label": ""
            }
          }
        }
      },

      /**
       * Short chart
       */
      {
        "maxHeight": 250,
        "overrides": {
          "autoMarginOffset": 0,
          "autoMargins": false,
          "marginTop": 0,
          "marginBottom": 0,
          "marginLeft": 0,
          "marginRight": 0,
          "valueAxes": {
            "inside": true,
            "title": "",
            "showFirstLabel": false,
            "showLastLabel": false
          },
          "legend": {
            "enabled": false
          }
        }
      }, {
        "maxWidth": 150,
        "overrides": {
          "valueyAxes": {
            "labelsEnabled": false,
            "axisAlpha": 0,
            "gridAlpha": 0,
            "guides": {
              "label": ""
            }
          }
        }
      }
    ],

    /**
     * AmStock
     */
    "stock": [ {
      "maxWidth": 500,
      "overrides": {
        "dataSetSelector": {
          "position": "top"
        },
        "periodSelector": {
          "position": "bottom"
        }
      }
    }, {
      "maxWidth": 400,
      "overrides": {
        "dataSetSelector": {
          "selectText": "",
          "compareText": ""
        },
        "periodSelector": {
          "periodsText": "",
          "inputFieldsEnabled": false
        }
      }
    } ],

    /**
     * AmMap
     */
    "map": [ {
      "maxWidth": 200,
      "overrides": {
        "zoomControl": {
          "zoomControlEnabled": false
        },
        "smallMap": {
          "enabled": false
        },
        "valueLegend": {
          "enabled": false
        },
        "dataProvider": {
          "areas": {
            "descriptionWindowWidth": 160,
            "descriptionWindowRight": 10,
            "descriptionWindowTop": 10
          },
          "images": {
            "descriptionWindowWidth": 160,
            "descriptionWindowRight": 10,
            "descriptionWindowTop": 10
          },
          "lines": {
            "descriptionWindowWidth": 160,
            "descriptionWindowRight": 10,
            "descriptionWindowTop": 10
          }
        }
      }
    }, {
      "maxWidth": 150,
      "overrides": {
        "dataProvider": {
          "areas": {
            "descriptionWindowWidth": 110,
            "descriptionWindowRight": 10,
            "descriptionWindowTop": 10
          },
          "images": {
            "descriptionWindowWidth": 110,
            "descriptionWindowRight": 10,
            "descriptionWindowTop": 10
          },
          "lines": {
            "descriptionWindowWidth": 110,
            "descriptionWindowLeft": 10,
            "descriptionWindowRight": 10
          }
        }
      }
    }, {
      "maxHeight": 200,
      "overrides": {
        "zoomControl": {
          "zoomControlEnabled": false
        },
        "smallMap": {
          "enabled": false
        },
        "valueLegend": {
          "enabled": false
        },
        "dataProvider": {
          "areas": {
            "descriptionWindowHeight": 160,
            "descriptionWindowRight": 10,
            "descriptionWindowTop": 10
          },
          "images": {
            "descriptionWindowHeight": 160,
            "descriptionWindowRight": 10,
            "descriptionWindowTop": 10
          },
          "lines": {
            "descriptionWindowHeight": 160,
            "descriptionWindowRight": 10,
            "descriptionWindowTop": 10
          }
        }
      }
    }, {
      "maxHeight": 150,
      "overrides": {
        "dataProvider": {
          "areas": {
            "descriptionWindowHeight": 110,
            "descriptionWindowRight": 10,
            "descriptionWindowTop": 10
          },
          "images": {
            "descriptionWindowHeight": 110,
            "descriptionWindowRight": 10,
            "descriptionWindowTop": 10
          },
          "lines": {
            "descriptionWindowHeight": 110,
            "descriptionWindowLeft": 10,
            "descriptionWindowRight": 10
          }
        }
      }
    } ]
  };

  var isNullOrUndefined = function( obj ) {
    return ( obj === null ) || ( obj === undefined );
  };

  var isArray = function( obj ) {
    return ( !isNullOrUndefined( obj ) && Object.prototype.toString.call( obj ) === "[object Array]" );
  };

  var isObject = function( obj ) {
    return ( obj !== null && typeof obj === "object" ); //the null check is necessary - recall that typeof null === "object" !
  };

  var findArrayObjectById = function( arr, id ) {
    for ( var i = 0; i < arr.length; i++ ) {
      if ( isObject( arr[ i ] ) && arr[ i ].id === id )
        return arr[ i ];
    }
    return undefined; //we can use undefined as it doesn't have an Id property and so will never be the desired object from the array
  };

  var cloneWithoutPrototypes = function( obj ) {
    if ( !isObject( obj ) ) {
      return obj;
    }

    if ( isArray( obj ) ) {
      return obj.slice(); //effectively clones the array
    }

    var clone = {}; //here is where we lose the prototype
    for ( var property in obj ) {
      if ( Object.prototype.hasOwnProperty.call( obj, property ) ) {
        clone[ property ] = cloneWithoutPrototypes( obj[ property ] );
      }
    }
    return clone;
  };

  var originalValueRetainerPrefix = "{F0578839-A214-4E2D-8D1B-44941ECE8332}_";
  var noOriginalPropertyStub = {};

  var overrideProperty = function( object, property, overrideValue ) {

    var originalValueRetainerProperty = originalValueRetainerPrefix + property;
    if ( !( originalValueRetainerProperty in object ) ) {
      object[ originalValueRetainerProperty ] = ( property in object ) ? object[ property ] : noOriginalPropertyStub;
    }

    object[ property ] = cloneWithoutPrototypes( overrideValue );

    r.overridden.push( {
      object: object,
      property: property
    } );
  };

  var restoreOriginalProperty = function( object, property ) {
    var originalValue = object[ originalValueRetainerPrefix + property ];
    if ( originalValue === noOriginalPropertyStub ) {
      delete object[ property ];
    } else {
      object[ property ] = originalValue;
    }
  };

  var restoreOriginals = function() {
    while ( r.overridden.length > 0 ) {
      var override = r.overridden.pop();
      restoreOriginalProperty( override.object, override.property );
    }
  };

  var redrawChart = function() {
    chart.dataChanged = true;
    if ( chart.type !== "xy" ) {
      chart.marginsUpdated = false;
    }
    chart.zoomOutOnDataUpdate = false;
    chart.validateNow( true );
    restoreOriginalProperty( chart, "zoomOutOnDataUpdate" );
  };

  var applyConfig = function( current, override ) {

    if ( isNullOrUndefined( override ) ) {
      return;
    }

    for ( var property in override ) {
      if ( !Object.prototype.hasOwnProperty.call( override, property ) ) {
        continue;
      }

      var currentValue = current[ property ];
      var overrideValue = override[ property ];

      //property doesn't exist on current object or it exists as null/undefined => completely override it
      if ( isNullOrUndefined( currentValue ) ) {
        if ( property !== "periodSelector" && property !== "dataSetSelector" ) // do not attempt to override non-existing selectors
          overrideProperty( current, property, overrideValue );
        continue;
      }

      //current value is an array => override method depends on override form
      if ( isArray( currentValue ) ) {

        //override value is an array => override method depends on array elements
        if ( isArray( overrideValue ) ) {

          //current value is an array of non-objects => override the entire array
          //we assume a uniformly-typed array, so checking the first value should suffice
          if ( ( currentValue.length > 0 && !isObject( currentValue[ 0 ] ) ) || ( overrideValue.length > 0 && !isObject( overrideValue[ 0 ] ) ) ) {
            overrideProperty( current, property, overrideValue );
            continue;
          }

          var idPresentOnAllOverrideElements = true;
          for ( var k = 0; k < overrideValue.length; k++ ) {
            if ( isNullOrUndefined( overrideValue[ k ] ) || isNullOrUndefined( overrideValue[ k ].id ) ) {
              idPresentOnAllOverrideElements = false;
              break;
            }
          }

          //Id property is present on all override elements => override elements by ID
          if ( idPresentOnAllOverrideElements ) {
            for ( var i = 0; i < overrideValue.length; i++ ) {
              var correspondingCurrentElement = findArrayObjectById( currentValue, overrideValue[ i ].id );
              if ( correspondingCurrentElement === undefined ) {
                throw ( 'could not find element to override in "' + property + '" with ID: ' + overrideValue[ i ].id );
              }
              applyConfig( correspondingCurrentElement, overrideValue[ i ] );
            }
            continue;
          }

          //Id property is not set on all override elements and there aren't too many overrides => override objects by their index
          if ( overrideValue.length <= currentValue.length ) {
            for ( var l = 0; l < overrideValue.length; l++ ) {
              applyConfig( currentValue[ l ], overrideValue[ l ] );
            }
            continue;
          }

          throw "too many index-based overrides specified for object array property: " + property;
        }

        // override value is a single object => override all current array objects with that object
        if ( isObject( overrideValue ) ) {
          for ( var j = 0; j < currentValue.length; j++ ) {
            applyConfig( currentValue[ j ], overrideValue );
          }
          continue;
        }

        throw ( "non-object override detected for array property: " + property );
      }

      if ( isObject( currentValue ) ) {
        applyConfig( currentValue, overrideValue );
        continue;
      }

      //if we reached this point, the property is defined on the current object but is not an object => override it
      overrideProperty( current, property, overrideValue );
    }
  };

  var checkRules = function() {

    var width = chart.divRealWidth;
    var height = chart.divRealHeight;

    // do nothing if the container is hidden (has no size)
    if ( width === 0 || height === 0 )
      return;

    // update current rules
    var rulesChanged = false;
    for ( var i = 0; i < r.rules.length; i++ ) {
      var rule = r.rules[ i ];

      var ruleMatches =
        ( rule.minWidth === undefined || ( rule.minWidth <= width ) ) && ( rule.maxWidth === undefined || ( rule.maxWidth >= width ) ) &&
        ( rule.minHeight === undefined || ( rule.minHeight <= height ) ) && ( rule.maxHeight === undefined || ( rule.maxHeight >= height ) ) &&
        ( rule.rotate === undefined || ( rule.rotate === true && chart.rotate === true ) || ( rule.rotate === false && ( chart.rotate === undefined || chart.rotate === false ) ) ) &&
        ( rule.legendPosition === undefined || ( chart.legend !== undefined && chart.legend.position !== undefined && chart.legend.position === rule.legendPosition ) );

      if ( ruleMatches ) {
        if ( r.currentRules[ i ] === undefined ) {
          r.currentRules[ i ] = true;
          rulesChanged = true;
        }
      } else if ( r.currentRules[ i ] !== undefined ) {
        r.currentRules[ i ] = undefined;
        rulesChanged = true;
      }
    }

    if ( !rulesChanged )
      return;

    restoreOriginals();

    for ( var key in r.currentRules ) {
      if ( !Object.prototype.hasOwnProperty.call( r.currentRules, key ) ) {
        continue;
      }

      if ( r.currentRules[ key ] !== undefined ) {
        if ( isNullOrUndefined( r.rules[ key ] ) ) {
          throw "null or undefined rule in index: " + key;
        }
        applyConfig( chart, r.rules[ key ].overrides );
      }
    }

    // TODO - re-apply zooms/slices as necessary

    redrawChart();
  };

  defaults.gantt = defaults.serial;

  if ( !isArray( r.rules ) ) {
    r.rules = defaults[ chart.type ];
  } else if ( r.addDefaultRules !== false ) {
    r.rules = defaults[ chart.type ].concat( r.rules );
  }

  //retain original zoomOutOnDataUpdate value
  overrideProperty( chart, "zoomOutOnDataUpdate", chart.zoomOutOnDataUpdate );

  chart.addListener( "resized", checkRules );
  chart.addListener( "init", checkRules );

}, [ "pie", "serial", "xy", "funnel", "radar", "gauge", "gantt", "stock", "map" ] );
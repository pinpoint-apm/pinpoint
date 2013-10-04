(function (chaiJquery) {
  // Module systems magic dance.
  if (typeof require === "function" && typeof exports === "object" && typeof module === "object") {
    // NodeJS
    module.exports = chaiJquery;
  } else if (typeof define === "function" && define.amd) {
    // AMD
    define(['jquery'], function ($) {
      return function (chai, utils) {
        return chaiJquery(chai, utils, $);
      };
    });
  } else {
    // Other environment (usually <script> tag): plug in to global chai instance directly.
    chai.use(function (chai, utils) {
      return chaiJquery(chai, utils, jQuery);
    });
  }
}(function (chai, utils, $) {
  var inspect = utils.inspect,
      flag = utils.flag;
  $ = $ || jQuery;

  $.fn.inspect = function (depth) {
    var el = $('<div />').append(this.clone());
    if (depth !== undefined) {
      var children = el.children();
      while (depth-- > 0)
        children = children.children();
      children.html('...');
    }
    return el.html();
  };

  var props = {attr: 'attribute', css: 'CSS property'};
  for (var prop in props) {
    (function (prop, description) {
      chai.Assertion.addMethod(prop, function (name, val) {
        var actual = flag(this, 'object')[prop](name);

        if (!flag(this, 'negate') || undefined === val) {
          this.assert(
              undefined !== actual
            , 'expected #{this} to have a #{exp} ' + description
            , 'expected #{this} not to have a #{exp} ' + description
            , name
          );
        }

        if (undefined !== val) {
          this.assert(
              val === actual
            , 'expected #{this} to have a ' + inspect(name) + ' ' + description + ' with the value #{exp}, but the value was #{act}'
            , 'expected #{this} not to have a ' + inspect(name) + ' ' + description + ' with the value #{act}'
            , val
            , actual
          );
        }

        flag(this, 'object', actual);
      });
    })(prop, props[prop]);
  }

  chai.Assertion.addMethod('data', function (name, val) {
    // Work around a chai bug (https://github.com/logicalparadox/chai/issues/16)
    if (flag(this, 'negate') && undefined !== val && undefined === flag(this, 'object').data(name)) {
      return;
    }

    var assertion = new chai.Assertion(flag(this, 'object').data());
    if (flag(this, 'negate'))
      assertion = assertion.not;
    return assertion.property(name, val);
  });

  chai.Assertion.addMethod('class', function (className) {
    this.assert(
        flag(this, 'object').hasClass(className)
      , 'expected #{this} to have class #{exp}'
      , 'expected #{this} not to have class #{exp}'
      , className
    );
  });

  chai.Assertion.addMethod('id', function (id) {
    this.assert(
        flag(this, 'object').attr('id') === id
      , 'expected #{this} to have id #{exp}'
      , 'expected #{this} not to have id #{exp}'
      , id
    );
  });

  chai.Assertion.addMethod('html', function (html) {
    this.assert(
        flag(this, 'object').html() === html
      , 'expected #{this} to have HTML #{exp}'
      , 'expected #{this} not to have HTML #{exp}'
      , html
    );
  });

  chai.Assertion.addMethod('text', function (text) {
    this.assert(
        flag(this, 'object').text() === text
      , 'expected #{this} to have text #{exp}'
      , 'expected #{this} not to have text #{exp}'
      , text
    );
  });

  chai.Assertion.addMethod('value', function (value) {
    this.assert(
        flag(this, 'object').val() === value
      , 'expected #{this} to have value #{exp}'
      , 'expected #{this} not to have value #{exp}'
      , value
    );
  });

  $.each(['visible', 'hidden', 'selected', 'checked', 'disabled'], function (i, attr) {
    chai.Assertion.addProperty(attr, function () {
      this.assert(
          flag(this, 'object').is(':' + attr)
        , 'expected #{this} to be ' + attr
        , 'expected #{this} not to be ' + attr);
    });
  });

  chai.Assertion.overwriteProperty('exist', function (_super) {
    return function () {
      var obj = flag(this, 'object');
      if (obj instanceof $) {
        this.assert(
            obj.length > 0
          , 'expected ' + inspect(obj.selector) + ' to exist'
          , 'expected ' + inspect(obj.selector) + ' not to exist');
      } else {
        _super.apply(this, arguments);
      }
    };
  });

  chai.Assertion.overwriteProperty('empty', function (_super) {
    return function () {
      var obj = flag(this, 'object');
      if (obj instanceof $) {
        this.assert(
          obj.is(':empty')
          , 'expected #{this} to be empty'
          , 'expected #{this} not to be empty');
      } else {
        _super.apply(this, arguments);
      }
    };
  });

  chai.Assertion.overwriteProperty('be', function (_super) {
    return function () {
      var be = function (selector) {
        var obj = flag(this, 'object');
        if (obj instanceof $) {
          this.assert(
              obj.is(selector)
            , 'expected #{this} to be #{exp}'
            , 'expected #{this} not to be #{exp}'
            , selector
          );
        } else {
          _super.apply(this, arguments);
        }
      };
      be.__proto__ = this;
      return be;
    }
  });

  chai.Assertion.overwriteMethod('match', function (_super) {
    return function (selector) {
      var obj = flag(this, 'object');
      if (obj instanceof $) {
        this.assert(
            obj.is(selector)
          , 'expected #{this} to match #{exp}'
          , 'expected #{this} not to match #{exp}'
          , selector
        );
      } else {
        _super.apply(this, arguments);
      }
    }
  });

  chai.Assertion.overwriteProperty('contain', function (_super) {
    return function () {
      _super.call(this);
      var contain = function (text) {
        var obj = flag(this, 'object');
        if (obj instanceof $) {
          this.assert(
              obj.is(':contains(\'' + text + '\')')
            , 'expected #{this} to contain #{exp}'
            , 'expected #{this} not to contain #{exp}'
            , text
          );
        } else {
          Function.prototype.apply.call(_super.call(this), this, arguments);
        }
      };
      contain.__proto__ = this;
      return contain;
    }
  });

  chai.Assertion.overwriteProperty('have', function (_super) {
    return function () {
      var obj = flag(this, 'object');
      if (obj instanceof $) {
        var have = function (selector) {
          this.assert(
              // Using find() rather than has() to work around a jQuery bug:
              //   http://bugs.jquery.com/ticket/11706
              obj.find(selector).length > 0
            , 'expected #{this} to have #{exp}'
            , 'expected #{this} not to have #{exp}'
            , selector
          );
        };
        have.__proto__ = this;
        return have;
      } else {
        _super.call(this);
      }
    }
  });
}));

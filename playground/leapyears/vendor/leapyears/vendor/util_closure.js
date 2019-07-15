goog.provide("leapyears.vendor.util_closure");

goog.scope(function() {
  var module = leapyears.vendor.util_closure;

  module.isLeap = function(val) {
    return (0 === (val % 400)) || (((val % 100) > 0) && (0 === (val % 4)));
  };
});

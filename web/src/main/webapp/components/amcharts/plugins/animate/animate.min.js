!function(){"use strict"
function t(t,n,i){return t*(i-n)+n}function n(t){var n=1-t
return n*=n*n*n,1-n}function i(t,n,i,e){this._object=t,this._key=n,this._from=i,this._to=e}function e(t,i,e,r,a){this._finished=!1,this._startTime=null,this._duration=t,this._easing=null==i?n:i,this._onComplete=e,this._tweens=r,this._chart=a}function r(){this._animating=!1,this._animations=[],this._onBeforeFrames=[],this._onAfterFrames=[]
var t=this
this._raf=function(n){t._onFrame(n)}}function a(t){return t!==t}function o(t,n){for(var i=0;i<t.length;++i)n(t[i])}function u(t,n){for(var i=0;i<t.length;++i)if(t[i]===n)return
t.push(n)}function l(t,n,i){n[i]||(n[i]=!0,t.push(i))}function s(t,n,i,e){o(e,function(e){var r=i[e]
null!=r&&l(t,n,r)})}function h(t,n,i){s(n,i,t,["alphaField","valueField"])}function f(t,n,i){h(t,n,i)}function m(t,n,i){h(t,n,i),s(n,i,t,["labelRadiusField"])}function c(t,n,i){s(n,i,t,["alphaField","bulletSizeField","closeField","dashLengthField","errorField","highField","lowField","openField","valueField"])}function p(t,n,i){c(t,n,i),s(n,i,t,["xField","yField"])}function _(t,n,i,e){o(t,function(t){e(t,n,i)})}function d(t,n,i){s(n,i,t,["widthField"])}function v(t){var n=[],i={}
return"funnel"===t.type?f(t,n,i):"pie"===t.type?m(t,n,i):"serial"===t.type?(d(t.categoryAxis,n,i),_(t.graphs,n,i,c)):"radar"===t.type?_(t.graphs,n,i,c):"xy"===t.type&&_(t.graphs,n,i,p),n}function y(t){var n={}
return("serial"===t.type||"radar"===t.type||"xy"===t.type)&&o(t.valueAxes,function(t){if(null==n[t.id]){n[t.id]={minimum:t.minimum,maximum:t.maximum}
var i,e=t.minRR,r=t.maxRR,a=r-e
i=0===a?Math.pow(10,Math.floor(Math.log(Math.abs(r))*Math.LOG10E))/10:Math.pow(10,Math.floor(Math.log(Math.abs(a))*Math.LOG10E))/10,null==t.minimum&&(t.minimum=Math.floor(e/i)*i-i),null==t.maximum&&(t.maximum=Math.ceil(r/i)*i+i)}}),n}function F(t,n){("serial"===t.type||"radar"===t.type||"xy"===t.type)&&o(t.valueAxes,function(t){var i=n[t.id]
null!=i&&(null==i.minimum&&delete t.minimum,null==i.maximum&&delete t.maximum)})}function g(t){return"funnel"===t.type||"pie"===t.type?t.titleField:"serial"===t.type||"radar"===t.type?t.categoryField:void 0}function x(t,n){var i=t[n]
return null==i?null:(i=+i,a(i)?null:i)}function M(t,n){var i=t[n]
return null==i?null:""+i}function w(t,n){var i={}
return o(t,function(t){var e=M(t,n)
null!=e&&(i[e]=t)}),i}function A(t,n,e,r){var a=[]
return o(t,function(t){var u=M(t,n)
if(null!=u&&u in e){var l=e[u]
o(r,function(n){var e=x(l,n),r=x(t,n)
null!=e&&null!=r&&a.push(new i(t,n,e,r))})}}),a}function T(t,n,e){for(var r=[],a=Math.min(t.length,n.length),u=0;a>u;++u){var l=t[u],s=n[u]
o(e,function(t){var n=x(l,t),e=x(s,t)
null!=n&&null!=e&&r.push(new i(s,t,n,e))})}return r}function b(t,n){if("xy"===t.type){var i=v(t)
return T(t.dataProvider,n,i)}var e=g(t),i=v(t),r=w(t.dataProvider,e)
return A(n,e,r,i)}function k(t,n){function i(){F(r,o),null!=n.complete&&n.complete()}var r=this,a=b(r,t),o=y(r)
r.dataProvider=t
var u=new e(n.duration,n.easing,i,a,r)
return C.animate(u),u}if("undefined"==typeof requestAnimationFrame)var R=1e3/60,B=function(t){setTimeout(function(){t((new Date).getTime())},R)}
else var B=requestAnimationFrame
i.prototype.interpolate=function(n){this._object[this._key]=t(n,this._from,this._to)},e.prototype.cancel=function(){this._finished=!0,this._startTime=null,this._duration=null,this._easing=null,this._onComplete=null,this._tweens=null,this._chart=null},e.prototype._onFrame=function(t){if(this._finished)return!0
if(null===this._startTime)return this._startTime=t,!1
var n=t-this._startTime
return n<this._duration?(this._tick(n/this._duration),!1):(this._end(1),this.cancel(),!0)},e.prototype._tick=function(t){t=this._easing(t)
for(var n=this._tweens,i=0;i<n.length;++i)n[i].interpolate(t)
u(D,this._chart)},e.prototype._end=function(t){this._tick(t),this._onComplete()},r.prototype.animate=function(t){this._animations.push(t),this._animating||(this._animating=!0,B(this._raf))},r.prototype.onBeforeFrame=function(t){this._onBeforeFrames.push(t)},r.prototype.onAfterFrame=function(t){this._onAfterFrames.push(t)},r.prototype._onFrame=function(t){for(var n=this._onBeforeFrames,i=0;i<n.length;++i)n[i](t)
for(var e=this._animations,i=0;i<e.length;++i){var r=e[i]
r._onFrame(t)&&(e.splice(i,1),--i)}for(var a=this._onAfterFrames,i=0;i<a.length;++i)a[i](t)
0===e.length?this._animating=!1:B(this._raf)}
var C=new r,D=[]
C.onAfterFrame(function(){for(var t=0;t<D.length;++t)D[t].validateData()
D.length=0}),AmCharts.addInitHandler(function(t){t.animateData=k},["funnel","pie","serial","radar","xy"])}()

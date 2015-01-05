;(function($, window, document, undefined){
    // plugin Name for Class creation
	var sPluginName = 'Class',
		// extend handle
		oExtendHandle = {
			// inherit prototype
	        // property not implemented - unnecessary
			extend : function(SuperClass){
				this.prototype = this.copySuperPrototype(SuperClass);
				this.prototype.$super = SuperClass;

				return this;
			},
			// copy super Class prototype member
			copySuperPrototype : function(SuperClass){
				var oExtendedPrototype = {};

				$.extend(true,
					oExtendedPrototype,
					SuperClass.prototype,
					// properties not inherited from super
					{
						"$init" : null,
						"$super" : null,
						"constructor" : null
					},
					// replace with this.prototype
					this.prototype
				);

				return oExtendedPrototype;
			}
		},
		// super class handle
		oSuperClassHandle = {
			// redefine super class member
			redefineSuperMember : function(oInstance, aClassHasSuper){
				var ClassHasSuper = oInstance;

				while(ClassHasSuper.$super !== undefined){
					ClassHasSuper.$super = this.getNewSuperMember(
						ClassHasSuper.$super);
					
					// all method executions in the inheritance chain are reflected in 'this'
					// 'this' = current instance
					ClassHasSuper.$super.$this = oInstance;

					aClassHasSuper.push(ClassHasSuper);

					// repeatedly loop super Class
					ClassHasSuper = ClassHasSuper.$super;
				}
			},
			// get redefined super member
			getNewSuperMember : function(SuperClass){
				var oNewSuper = {},
					oSuperPrototype = SuperClass.prototype,
					varMember = null;

				for(var sKey in oSuperPrototype){
					varMember = oSuperPrototype[sKey];
					
					if(!oSuperPrototype.hasOwnProperty(sKey)){
						continue;
					}

					if(this.isPassRedefine(sKey)){
						oNewSuper[sKey] = varMember;
						continue;
					}

					if(typeof varMember === "function"){
						oNewSuper[sKey] = this.redefineSuperMethod(
							sKey, varMember);
						continue;
					}

					oNewSuper[sKey] = varMember;
				}

				return oNewSuper;
			},
			isPassRedefine : function(sKey){
				var htPassKey = {
					"constructor" : 1,
					"$super" : 1
				};

				if(htPassKey[sKey]){
					return true;
				}

				return false;
			},
			// redefine super method
			// chnage context to child instance
			redefineSuperMethod : function(sSuperMethod, fSuperMethod){
				// this = super class
				return function(){
					var oChild = this.$this,
						oChildBackup = {
							"super" : oChild.$super,
							"method" : oChild[sSuperMethod]
						},
						result = "";

					oSuperClassHandle.changeChildMethod({
						"oSuper" : this,
						"oChild" : oChild,
						"sSuperMethod" : sSuperMethod,
						"fSuperMethod" : fSuperMethod
					});

					// invoke super method with child's context 
					result = fSuperMethod.apply(oChild, arguments);

					oSuperClassHandle.restoreChildMethod({
						"oChild" : oChild,
						"sSuperMethod" : sSuperMethod,
						"oChildBackup" : oChildBackup
					});

					return result;
				};
			},
			// change child method to super method
			changeChildMethod : function(htParam){
				var oSuper = htParam.oSuper,
					oChild = htParam.oChild,
					sSuperMethod = htParam.sSuperMethod,
					fSuperMethod = htParam.fSuperMethod;

				// replace child method with parent's method
				// takes care of recursive method calls
				oChild[sSuperMethod] = fSuperMethod;
				
				// replace child's $super
				// super invoked by the executed method is the direct parent of the class inherited by the super class.
				oChild.$super = oSuper.$super;
			},
			// restore child method
			restoreChildMethod : function(htParam){
				var oChild = htParam.oChild,
					sSuperMethod = htParam.sSuperMethod,
					oChildBackup = htParam.oChildBackup;

				oChild[sSuperMethod] = oChildBackup["method"];
				oChild.$super = oChildBackup["super"];
			},
			// run all super class $init()
			runSuperInit : function(aClassHasSuper, oArguments){
				var nLength = aClassHasSuper.length,
					SuperClass = null;
				
				while(nLength--){
					SuperClass = aClassHasSuper[nLength].$super;

					if (typeof SuperClass.$init !== "function"){
						continue;
					}

					SuperClass.$init.apply(SuperClass, oArguments);
				}
			}
		},
		oAttachToPrototype = {
			/**
			 * invoke user function on the context
			 *
			 * @param {Function}	fnMethod	function to be invoked
			 * @param {HashTable}	htParam		parameter to pass to the function
			 */
			_trigger : function(fnMethod, htParam){
				if(!fnMethod){
					return false;
				}

				fnMethod.apply(this, [htParam]);
			},
			// option controll (get, set, expend)
			option : function(key, value){
				// all option
				if("undefined" === key){
					return this.htOption || {};
				}
				// set
				if(typeof value !== "undefined"){
					return this.htOption[key] = value;
				}
				// get
				if(typeof key === "string" && typeof this.htOption[key] !== 'undefined'){
					return this.htOption[key];
				}
				// extend
				$.extend(this.htOption, key);
			}
		};

	$[sPluginName] = function(htMember){
		// The actual Class constructor
		function Class(){
			var aClassHasSuper = [];

			this.htOption = {};

			// redefine super class members
			oSuperClassHandle.redefineSuperMember(this, aClassHasSuper);

			// run all super class inits 
			oSuperClassHandle.runSuperInit(aClassHasSuper, arguments);

			if(typeof this.$init === "function"){
				this.$init.apply(this, arguments);
			}
		}

		$.extend(Class, oExtendHandle);

		Class.prototype = $.extend(htMember, oAttachToPrototype);
		Class.prototype.constructor = Class;

		return Class;
	};
})(jQuery, window, document);
// */

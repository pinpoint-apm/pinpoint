;(function($, window, document, undefined){
	// Class 생성 plugin Name
	var sPluginName = 'Class',
		// extend handle
		oExtendHandle = {
			// 상속
			// prototype 만 상속
			// property 는 불 필요한 것 같아 구현 안했다
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
					// super 로 부터 상속 하지 않을 속성
					{
						"$init" : null,
						"$super" : null,
						"constructor" : null
					},
					// this prototype 으로 overwrite
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
					
					// 모든 상속 chain 의 method 실행 결과는 this 에 반영된다.
					// this = 현재 instance
					ClassHasSuper.$super.$this = oInstance;

					aClassHasSuper.push(ClassHasSuper);

					// 상위 Class 를 계속 loop
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

					// child context 로 super method 실행
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

				// child 의 method 를 실행 될 super method 로 교체
				// 재귀 함수 호출을 위한 처리
				oChild[sSuperMethod] = fSuperMethod;
				
				// child 의 $super 를 교체
				// 실행된 method 에서 호출한 super 는 super Class 가 상속한
				// 직속 상위 Class 가 된다.
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
			 * context 를 instance 로 사용자 함수 실행
			 *
			 * @param {Function}	fnMethod	실행 할 함수
			 * @param {HashTable}	htParam		함수에 전달 될 parameter
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

			// super class 의 member 재정의
			oSuperClassHandle.redefineSuperMember(this, aClassHasSuper);

			// super class 의 모든 init 을 실행 한다.
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

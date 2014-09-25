/*
Class make plugin like NHN jindo
parameter 로 넘겨진 JSON 을 member 로 가지는 Class 를 생성

@author 최승학
@version 1.0
@since version 1.0

@requires jQuery

@example

// 상속
;(function($){
	// var Mother = jindo.$Class({
	var Mother = $.Class({
		$init : function(){
			this.name = "mother";
		},
		methodA : function(){
			console.log("mother's methodA");
			console.log("mother name = " + this.name);
			this.methodB();
		},
		methodB : function(){
			console.log("mother's methodB");
		}
	});

	// var Son = jindo.$Class({
	var Son = $.Class({
		$init : function(){
			// this.name = "son"
		},
		methodA : function(){
			console.log("son's methodA");
			console.log("son name = " + this.name);
			this.$super.methodA();
		}
	}).extend(Mother);
	
	var Son2 = $.Class({
		$init : function(){
			// this.name = "son2";
		},
		methodA : function(){
			console.log("son2's methodA");
			console.log("son2 name = " + this.name);
			this.$super.methodA();
		},
		methodB : function(){
			console.log("son2's methodB");
		}
	}).extend(Son);
	
	var oSon = new Son2();
	oSon.methodA();
	// ※ jQuery Class result
	// son2's methodA
	// son2 name = mother
	// son's methodA
	// son name = mother
	// mother's methodA
	// mother name = mother
	// son2's methodB


	// ※ jindo result
	// son2's methodA
	// son2 name = mother
	// son's methodA
	// son name = undefined
	// mother's methodA
	// mother name = mother
	// son2's methodB


	// ※ java Class result
	// son2's methodA
	// son2 name = mother
	// son's methodA
	// son name = mother
	// mother's methodA
	// mother name = mother
	// son2's methodB
})(jQuery);

// java 상속 결과 test source 참고
class Mother{
	String name = "";

	public Mother(){
		this.name = "mother";
	}

	public void methodA(){
		System.out.println("mother's methodA");
		System.out.println("mother name = " + this.name);
		methodB();
	}

	public void methodB(){
		System.out.println("mother's methodB");
	}
}

class Son extends Mother{
	public Son(){
		// init
	}

	public void methodA(){
		System.out.println("son's methodA");
		System.out.println("son name = " + this.name);
		super.methodA();
	}
}

class Son2 extends Son{
	public Son2(){
		// init
	}

	public void methodA(){
		System.out.println("son2's methodA");
		System.out.println("son2 name = " + this.name);
		super.methodA();
	}

	public void methodB(){
		System.out.println("son2's methodB");
	}
}

class Inheritance{
	public static void main(String[] args){
		Son2 oSon = new Son2();
		oSon.methodA();
	}
}

// trigger custom function
;(function($){
	var TriggerTest = $.Class({
		$init : function(htParam){
			this.option = htParam;
		},
		methodA : function(){
			this._trigger(this.option.printAge);
		},
	});

	var oTriggerTest = new TriggerTest({
		"nAge" : 18,
		"printAge" : function(){
			console.log(this.option.nAge);
		}
	});
	
	// 18
	oTriggerTest.methodA();
})(jQuery);
*/
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
				if(typeof key === "string"){
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

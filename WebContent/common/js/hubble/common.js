/**
//==============================================================
// 이 파일을 변경시 이 부분에 변경내용을 기록해 주시기 바랍니다.
//================== HISTORY ===================================
// 2006.07.18, ??? 추가.
   - 팝업 공통 : WinPop(aUrl, aName, aWidth, aHeight, aScroll)
   - UTF-8 --> 한글  디코딩 : utf8(wide) [include: toHex(n), encodeURIComponentNew(s) ]
// 2006.07.19, sjmoon@brainz.co.kr
   - input 포맷 체크 : isValidFormat(input,format)
   - input 빈값 체크 : isEmpty(input)
   - input email 체크 : isValidEmail(input)
   - input 전화번호 체크 : isValidPhone(input)
   - input 콤마 제거 : removeComma(input)
   - input 콤마 찍기 : Comma(input)
   - data 문자열 크기 계산 : gfn_getStrLen(str)
   - data 문자열로 변환 : gfn_getStr(arg)
   - data 숫자인지 체크 : gfn_chkNumber(arg)
// 2006.07.19, jaulim@brainz.co.kr
   - IE 패치에 따른 object 코드 출력 : JS_viewObj(objhtml) ==> 2006.07.20 삭제함.
// 2006.07.20, sjmoon@brainz.co.kr
   - select id에 코드값 셋팅 : f_setSelect(select_id, op_array, opname_array, default_op, empty_opname)
   - input id에 기본값 셋팅 : f_setInputText(input_id, default_value)
//2006.07.23 redmiki@brainz.co.kr
   - isGo() : 두번 입력 방지
   - ifram 호출 : function iframe_call(frame_name , call_path)
      예) frame_call('info', '"+webPath+"/ZItsmOmAccountUserCONT.ksms?cmd=getAccountUserView2')
// 2006.07.23, sjmoon@brainz.co.kr
   - select 박스에서 해당 값을 선택한 것으로 셋팅하기 : gfn_setSelectBox(obj, value)
   - select 박스에서 선택된 항목의 값을 가져오기 : gfn_getSelectBoxText(obj)
// 2006.07.25 redmiki@brainz.co.kr
   - 조직 목록 리스트 제공 : gfn_getSelectBoxText();
      document.frm.userorgnm 에 조직 명 전달
      document.frm.chk_userorg 에 조직 코드 전달
// 2006.08.01 redmiki@brainz.co.kr
   - 사용자 목록 리스트 제공 : gfn_userList_call(form, name)
        form : 폼 이름 , name : 필드 이름
      userList 에 사용자  myid, 사용자 이름 구분
        예) 258*홍길동:247*ㅎㅎㅎ
// 2006.08.01 sjmoon@brainz.co.kr
   - f_setSelect() : 버그 수정.
// 2006.08.02 sjmoon@brainz.co.kr
    - 자산 정보 요약 조회 popup : gfn_assetInfo_view( asset_id )
    - 자산 성능 요약 조회 popup : gfn_assetPerform_view( asset_id )
// 2006.08.08 sjmoon@brainz.co.kr
    - gfn_setSelectBoxText 추가 : selectbox의 text 값을 비교해서 기본값 설정을 한다.

// 2006.09.25 jaulim@brainz.co.kr
    - chgPrssILtoOn 추가 : Process 활성, 비활성
// 2006.09.29 lkg1024@brainz.co.kr
    - initialize 추가 : webPath 설정
// 2006.10.22 lkg1024@brainz.co.kr
    - pWinPop 추가 : prototype의 Form.serialize를 사용할 경우 utf-8로 인코딩이 자동으로 이루어짐
                            WinPop을 사용할 경우 utf-8로 인코딩 된 내용을 한번더 utf-8로 바꾸기 때문에
                            원하는 문자열이 리턴이 안됨
// 2006.11.02 원종혁 (jhwon@brainz.co.kr)
    - fromDate 추가. 일시(날짜+시간) 또는 일자 데이타를 숫자스트링으로 변경한다. fromDate( '2006-11-02 11:13:11' )  ==> '20061102111311'
    - toDate( floatValue, flagDT ) 추가. 일시(날짜+시간) 또는 일자가 숫자스트링일때 일시 형식으로 변경한다. toDate( '20061102111311', 'T' ) ==> '2006-11-02 11:13:11'
// 2006.11.03 lkg1024@brainz.co.kr
    - fromDate, toDate 변경 :
    - new Date().toDate("20061102195910", "YYYY-MM-DD hh:mm:ss"),
    - new Date().fromDate("2006-11-02 20:37:12")
// 2006.11.07 sjmoon@brainz.co.kr
    - 버튼에 스타일 만드는 함수  btn_SetStyle( 1, btn_submit, '등록', 80, "fn_save()");
      DIV id='btn_submit'><!-- 등록 fn_save() --></DIV
// 2006.11.23 sjmoon@brainz.co.kr
    - 버튼에 스타일 만드는 함수 변경..  btn_SetStyle( "A", btn_submit, "등록", 80, "fn_save()", "padding-top:3px; padding-left:4px");
      DIV id='btn_submit'><!-- 등록 fn_save() --></DIV
// 2006.11.27 sjmoon@brainz.co.kr
    - z_getMsg(msgId, [title] ) : message.js 에서 사용. @ 문자를 치환한 메세지를 생성한다.
    - z_alertMsgFocus(msgId , focusObj) : alert(msgId)를 띄우고 focusObj 에 focus 를 준다.
// 2006.11.29 sjmoon@brainz.co.kr
    - 풍선도움말 기능 추가. z_offTooltip(), z_onTooltip( msg );
      사용법 : 기본은 아래처럼 쓰시구.. focus 후에도 나타낼려면 onfocus, onblur 도 추가해야 합니다.
      <INPUT type=text name="title" onMouseOut="z_offTooltip();" onMouseOver="z_onTooltip('<B>주의</B><BR>제목을 똑바로 쓰세요!!')">
// 2006.12.07 sjmoon@brainz.co.kr
    - z_chkValueFocus 추가 : obj 의 값이 있는지 체크해서 값이 없으면 메세지를 뿌리고 포커스를 옮긴다.
//2006.12.18 redmiki@brainz.co.kr
        - z_ItsmUserSearchPop 추가 : ITSM 담당자별 사용자 선택 팝업
        - 사용법 ㅣ //param1 : 사용자 코드, param2 : 사용자명 , param3 : 권한코드(예)서비스데스크담당자 , 인시던트담당자)
            function z_ItsmUserSearchPop(param1, param2, param3)
            opener.f_printItsmUser(frm.parm1.value, frm.parm2.value, val[0], val[1])}
// 2007.01.11 sjmoon@brainz.co.kr
    - z_divToggle   추가 : div 보여주기 토글.
    - z_btnStyle( "B", btn_submit, "등록", 80, "fn_save()", "", false); 사용법은 함수 설명 참조.
// 2007.01.15 sjmoon@brainz.co.kr
    - z_tabDisplay 추가 : 새로운 tab ui 를 그려준다.
    var taFun = new Array("sumview('cino')", "commview('cino')", "detview()", "relview()", "histview()", "reldoc()");
    var taName= new Array("요약","공통","상세","연관","변경이력","관련문서");
    z_tabDisplay( 'tab_top', taFun, taName, 1);
          <DIV id='tab_top'><!-- tab --></DIV>
// 2007.01.16 sjmoon@brainz.co.kr
    z_tabDisplay 폼안에서 오류나는 버그 해결.
// 2007.01.25 sjmoon@brainz.co.kr
    - z_divToggle 에 프레임 리사이즈 코드 추가. body 프레임에서만 불러야 한다.
// 2007.01.26 hgpark@brainz.co.kr
    - z_ServiceSearchPop2 수정 추가
            기존의 서비스 팝업에서 param5 운영중 가져올수 있게 수정 추가
// 2007.02.01 sjmoon@brainz.co.kr
    - stripTag 추가. html의 <> 와 DB에서 오류날 수 있는 ' 를 치환한다.obj.value = (obj.value).stripTag();
// 2007.02.02 jhwon@brainz.co.kr
    - check_length 추가. 입력 필드의 길이를 계산하여 길이가 넘으면 짤르고 alert() 경고한다.
// 2007.02.08 sjmoon@brainz.co.kr
    - z_btnStyle 수정 : width 값 무시하고 자동계산하도록 변경함.
//================== //HISTORY ===================================
 */

/**
 * Name : commonInit Description : 스크립트에서 사용할 webPath 변수를 셋팅한다. Parameters :
 * webPath : 프레임에서 쓰는 webPath Revision : 2006.10.22 lkg1024@brainz.co.kr initial
 * release
 */
var webPath = "/static";
function commonInit(webPath) {
	this.webPath = webPath;
}

/**
 * Name : pWinPop Description : Window Open 공통 모듈(화면의 중앙에 위치시키며 Focus를 준다)
 * prototype의 Form.serialize를 사용할 경우 utf-8로 인코딩이 자동으로 이루어짐 WinPop을 사용할 경우 utf-8로
 * 인코딩 된 내용을 한번더 utf-8로 바꾸기 때문에 원하는 문자열이 리턴이 안됨 Parameters : aUrl : 띄울 윈도우의 url
 * aName: 윈도우 이름. aWidth : 넓이 aHeight : 높이 aScroll : 스크롤여부 (true, false) Use :
 * pWinPop('/zenius/index.jsp','pop', 300, 200, 'Y') Revision : 2006.10.22
 * lkg1024@brainz.co.kr initial release
 */
Xoffset = 20;
Yoffset = 20;
var scrWidth = screen.width;
var scrHeight = screen.height;
function pWinPop(aUrl, aName, aWidth, aHeight, aScroll) {
	var popScroll;
	var popWidth = aWidth;
	var popHeight = aHeight;
	var popLeft = (scrWidth - popWidth) / 2;
	var popTop = (scrHeight - popHeight) / 2;
	var popStyle = "";

	if (aName == 'webdbapp')
		popStyle = "status=no,resizable=yes,menubar=no,toolbar=no,location=no";
	else
		popStyle = "status=yes,resizable=yes,menubar=no,toolbar=no,location=no";

	if (aScroll) {
		popScroll = "yes";
	} else {
		popScroll = "no";
	}

	popStyle += ",scrollbars=" + popScroll;
	popStyle += ",width=" + popWidth;
	popStyle += ",height=" + popHeight;
	popStyle += ",left=" + popLeft;
	popStyle += ",top=" + popTop;
	// popStyle += ",status=yes";

	if (aUrl.indexOf("?") == -1) {
		returnURL = aUrl;
	} else {
		var URI = aUrl.substring(0, aUrl.indexOf("?"));
		var Param = aUrl.substring(aUrl.indexOf("?") + 1);
		var resultParam = "";

		Param = Param.split("&");

		for ( var i = 0; i < Param.length; i++) {
			var tmpValue = Param[i];

			resultParam += tmpValue.substring(0, tmpValue.indexOf("=")) + "=";
			resultParam += tmpValue.substring(tmpValue.indexOf("=") + 1,
					tmpValue.length)
					+ "&";
		}

		returnURL = URI
				+ (resultParam == "" ? "" : "?"
						+ resultParam.substring(0, resultParam.length - 1));
	}

	// var winOpen = window.open(returnURL, aName, popStyle);
	// 2007-05-08 jhwon nhn??? ?? ???? popup? ??? _blank? ??.
	var winOpen = window.open(returnURL, "_blank", popStyle);
	winOpen.focus();
	// return winOpen;
}

/**
 * Window Open 공통 모듈(화면의 중앙에 위치시키며 Focus를 준다) ex)
 * WinPop('/zenius/index.jsp','pop', 300, 200, true, true)
 * 
 * aScroll, aResize 파라미터는 사용하지 않으면 기본값으로 'no'
 */
function WinPop(aUrl, aName, aWidth, aHeight, aScroll, aResize) {

	var popScroll;
	var popResize;
	var popWidth = aWidth;
	var popHeight = aHeight;
	var popLeft = (scrWidth - popWidth) / 2;
	var popTop = (scrHeight - popHeight) / 2;
	var popStyle = "status=yes,menubar=no,toolbar=no,location=no";

	if (aScroll)
		popScroll = "yes";
	else
		popScroll = "no";

	if (aResize)
		popResize = "yes";
	else
		popResize = "no";

	popStyle += ",scrollbars=" + popScroll;
	popStyle += ",resizable=" + popResize;
	popStyle += ",width=" + popWidth;
	popStyle += ",height=" + popHeight;
	popStyle += ",left=" + popLeft;
	popStyle += ",top=" + popTop;

	if (aUrl.indexOf("?") == -1) {
		returnURL = aUrl;
	} else {
		var URI = aUrl.substring(0, aUrl.indexOf("?"));
		var Param = aUrl.substring(aUrl.indexOf("?") + 1);
		var resultParam = "";

		Param = Param.split("&");

		for (i = 0; i < Param.length; i++) {
			var tmpValue = Param[i];

			resultParam += tmpValue.substring(0, tmpValue.indexOf("=")) + "=";
			resultParam += encodeURIComponentNew(tmpValue.substring(tmpValue
					.indexOf("=") + 1, tmpValue.length))
					+ "&";
		}

		returnURL = URI
				+ (resultParam == "" ? "" : "?"
						+ resultParam.substring(0, resultParam.length - 1));
	}

	var winOpen = window.open(returnURL, aName, popStyle);
	winOpen.focus();

}

function utf8(wide) {
	var c, s;
	var enc = "";
	var i = 0;
	while (i < wide.length) {
		c = wide.charCodeAt(i++);
		// handle UTF-16 surrogates
		if (c >= 0xDC00 && c < 0xE000)
			continue;
		if (c >= 0xD800 && c < 0xDC00) {
			if (i >= wide.length)
				continue;
			s = wide.charCodeAt(i++);
			if (s < 0xDC00 || c >= 0xDE00)
				continue;
			c = ((c - 0xD800) << 10) + (s - 0xDC00) + 0x10000;
		}
		// output value
		if (c < 0x80)
			enc += String.fromCharCode(c);
		else if (c < 0x800)
			enc += String.fromCharCode(0xC0 + (c >> 6), 0x80 + (c & 0x3F));
		else if (c < 0x10000)
			enc += String.fromCharCode(0xE0 + (c >> 12),
					0x80 + (c >> 6 & 0x3F), 0x80 + (c & 0x3F));
		else
			enc += String.fromCharCode(0xF0 + (c >> 18),
					0x80 + (c >> 12 & 0x3F), 0x80 + (c >> 6 & 0x3F),
					0x80 + (c & 0x3F));
	}
	return enc;
}

var hexchars = "0123456789ABCDEF";

function toHex(n) {
	return hexchars.charAt(n >> 4) + hexchars.charAt(n & 0xF);
}

var okURIchars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-";

function encodeURIComponentNew(s) {
	var ss = utf8(s);
	var c;
	var enc = "";
	for ( var i = 0; i < ss.length; i++) {
		if (okURIchars.indexOf(ss.charAt(i)) == -1)
			enc += "%" + toHex(ss.charCodeAt(i));
		else
			enc += ss.charAt(i);
	}
	return enc;
}

/**
 * 입력값이 사용자가 정의한 포맷 형식인지 체크 자세한 format 형식은 자바스크립트의 'regular expression'을 참조
 */
function isValidFormat(input, format) {
	if (input.value.search(format) != -1) {
		return true; // 올바른 포맷 형식
	}
	return false;
}

/**
 * 입력값에 스페이스 이외의 의미있는 값이 있는지 체크 ex) if (isEmpty(form.keyword)) { alert("검색조건을
 * 입력하세요."); }
 */
function isEmpty(input) {
	if (input.value == null || input.value.replace(/ /gi, "") == "") {
		return true;
	}

	return false;
}

/**
 * 변수값에 스페이스 이외의 의미있는 값이 있는지 체크 ex) if (isNull(value)) { alert("변수값이 null
 * 입니다."); }
 */
function isNull(value) {
	if (value == null || (typeof (value) == "string" && value.trim() == "")) {
		return true;
	}

	return false;
}

/**
 * 입력값이 이메일 형식인지 체크 ex) if (!isValidEmail(form.email)) { alert("올바른 이메일 주소가
 * 아닙니다."); }
 */
function isValidEmail(input) {
	// var format = /^(\S+)@(\S+)\.([A-Za-z]+)$/;
	var format = /^((\w|[\-\.])+)@((\w|[\-\.])+)\.([A-Za-z]+)$/;
	return isValidFormat(input, format);
}

/**
 * 입력값이 전화번호 형식(숫자-숫자-숫자)인지 체크
 */
function isValidPhone(input) {
	var format = /^(\d+)-(\d+)-(\d+)$/;
	return isValidFormat(input, format);
}

/**
 * 입력값에서 콤마를 없앤다.
 */
function removeComma(input) {
	return input.value.replace(/,/gi, "");
}

/**
 * 3자리마다 콤마 찍기.
 */

function Comma(input) {
	var inputString = new String;
	var outputString = new String;
	var counter = 0;
	var decimalPoint = 0;
	var end = 0;

	inputString = input.toString();
	outputString = '';
	decimalPoint = inputString.indexOf('.', 1);

	if (decimalPoint == -1) {
		end = inputString.length - (inputString.charAt(0) == '-' ? 1 : 0);
		for (counter = 1; counter <= inputString.length; counter++) {
			outputString = (counter % 3 == 0 && counter < end ? ',' : '')
					+ inputString.charAt(inputString.length - counter)
					+ outputString;
		}
	} else {
		end = decimalPoint - (inputString.charAt(0) == '-' ? 1 : 0);
		for (counter = 1; counter <= decimalPoint; counter++) {
			outputString = (counter % 3 == 0 && counter < end ? ',' : '')
					+ inputString.charAt(decimalPoint - counter) + outputString;
		}
		for (counter = decimalPoint; counter < inputString.length; counter++) {
			outputString += inputString.charAt(counter);
		}
	}

	return (outputString);
}

// 문자열 크기 계산 - 바이트단위 : UTF-8 용 DB는 한글을 3byte로 인식한다.
function gfn_getStrLen(str) {
	var iCount = 0; // 메시지의 바이트를 저장하는 변수
	for ( var gi = 0; gi < str.length; gi++) { // 0-127 1byte, 128~ 3byte
		if (str.charCodeAt(gi) > 127) {
			iCount += 3;
		} else {
			iCount++;
		}
	}
	return iCount;
}

// 파라미터를 판단해서 String을 돌려줌
function gfn_getStr(arg) {
	if (arg) {
		if (arg.value == undefined) {
			return arg;
		} else {
			return arg.value;
		}
	} else {
		return arg;
	}
}

// 숫자 체크 - 숫자이면 true
function gfn_chkNumber(arg) {
	var num = "0123456789";
	var tmpStr = gfn_getStr(arg);

	if (tmpStr.length <= 0) {
		return false;
	}
	for ( var gi = 0; gi < tmpStr.length; gi++) {
		if (num.indexOf(tmpStr.substring(gi, gi + 1)) < 0) {
			// alert("숫자만 입력 하세요") ;
			return false;
		}
	}
	return true;
}

// object name 이나 id 를 object 로 돌려준다.
function f_getObject(objNameOrId) {
	var obj = document.getElementById(objNameOrId);
	if (!obj) {
		obj = eval('document.all.' + objNameOrId);
		if (!obj) {
			obj = eval(objNameOrId);
			if (!obj) {
				alert(objNameOrId + " 개체를 찾을수 없습니다.");
				obj = null;
			}
		}
	}
	return obj;
}

// 두번 서브밋 방지 코드
// submit 할 경우 사용 : onmousedown="isGo()"
// from으로 넘길때 사용 :set_submit();"
var isSubmit = true;
// document.onclick = isGo;
function set_submit() {
	isSubmit = true;
	// document.body.style.cursor = "wait";
}
function isGo() {
	if (isSubmit) {
		alert("처리중입니다.");
		return;
	}
}

// iframe 호출 공통으로
// ???) frame_call('info',
// '"+webPath+"/ZItsmOmAccountUserCONT.ksms?cmd=getAccountUserView2')

function iframe_call(frame_name, call_path) {
	eval("document.all." + frame_name + ".src = '" + call_path + "';");
	location.href = "#iframe";
}

// Select Box에서 지정된 옵션값으로 set하기
// use : gfn_setSelectBox(document.form.selectname, value)
function gfn_setSelectBox(obj, value) {
	for ( var gi = 0; gi < obj.length; gi++) {
		if (obj[gi].value == value) {
			obj[gi].selected = true;
			break;
		}
	}
}

function gfn_setRadioBox(obj, svc_cd, value) {

	obj.value = svc_cd;
	if (value == 'Y')
		obj.checked = true;

}

// Select Box에서 현재 select되어 있는 값의 옵션값 얻기
function gfn_getSelectBox(obj) {
	try {
		for ( var i = 0; i < obj.length; i++) {
			if (obj[i].selected == true) {
				return obj[i].value;
			}
		}
	} catch (x) {
	}
	return "";
}

// Select Box에서 현재 select되어 있는 값의 옵션값 얻기
function getRadioValueObj(obj) {
	// alert(obj);
	var selectedItem = '';
	var radioObj = obj;

	if (radioObj.length == null) { // 라디오버튼이 같은 name 으로 하나밖에 없다면
		radioObj.checked = true;
		selectedItem = radioObj.value;
	}

	else { // 라디오 버튼이 같은 name 으로 여러개 있다면
		for (i = 0; i < radioObj.length; i++) {
			if (radioObj[i].checked) {
				selectedItem = radioObj[i].value;
				break;
			}
		}
	}
	return selectedItem;
}

// Select Box에서 해당 text와 일치하는 부분을 선택시킨다
// use : gfn_setSelectBoxText(document.form.selectname, text)
function gfn_setSelectBoxText(obj, value) {
	for ( var gi = 0; gi < obj.length; gi++) {
		if (obj[gi].text == value) {
			obj[gi].selected = true;
			break;
		}
	}
}

// Select Box에서 현재 select되어 있는 값의 text얻기
function gfn_getSelectBoxText(obj) {
	try {
		for ( var i = 0; i < obj.length; i++) {
			if (obj[i].selected == true) {
				return obj[i].text;
			}
		}
	} catch (x) {
	}
	return "";
}

// 일시(날짜+시간) 또는 일자가 숫자스트링일때 일시 형식으로 변경한다.
// use : new Date().toDate("20061102195910", "YYYY-MM-DD hh:mm:ss") ==>
// '2006-11-02'
Date.prototype.toDate = function(dateValue, pattern) {
	var GLB_MONTH_IN_YEAR = new Array("1월", "2월", "3월", "4월", "5월", "6월", "7월",
			"8월", "9월", "10월", "11월", "12월");
	var GLB_DAY_IN_WEEK = new Array("일", "월", "화", "수", "목", "금", "토");

	this.setFullYear(dateValue.substring(0, 4));
	this.setMonth(dateValue.substring(4, 6) - 1);
	this.setDate(dateValue.substring(6, 8));
	this.setHours(dateValue.substring(8, 10));
	this.setMinutes(dateValue.substring(10, 12));
	this.setSeconds(dateValue.substring(12));

	var year = this.getFullYear();
	var month = this.getMonth() + 1;
	var day = this.getDate();
	var dayInWeek = this.getDay();
	var hour24 = this.getHours();
	var ampm = (hour24 < 12) ? 0 : 1;
	var hour12 = (hour24 > 12) ? (hour24 - 12) : hour24;
	var min = this.getMinutes();
	var sec = this.getSeconds();
	var YYYY = "" + year;
	var YY = YYYY.substr(2);
	var MM = (("" + month).length == 1) ? "0" + month : "" + month;
	var MON = GLB_MONTH_IN_YEAR[month - 1];
	var DD = (("" + day).length == 1) ? "0" + day : "" + day;
	var DAY = GLB_DAY_IN_WEEK[dayInWeek];
	var HH = (("" + hour24).length == 1) ? "0" + hour24 : "" + hour24;
	var hh = (("" + hour12).length == 1) ? "0" + hour12 : "" + hour12;
	var mm = (("" + min).length == 1) ? "0" + min : "" + min;
	var ss = (("" + sec).length == 1) ? "0" + sec : "" + sec;
	var SS = "" + this.getMilliseconds();
	var a = (a == 0) ? "AM" : "PM";

	var dateStr;
	var index = -1;

	if (typeof (pattern) == "undefined") {
		dateStr = "YYYYMMDD";
	} else {
		dateStr = pattern;
	}

	dateStr = dateStr.replace(/a/g, a);
	dateStr = dateStr.replace(/YYYY/g, YYYY);
	dateStr = dateStr.replace(/YY/g, YY);
	dateStr = dateStr.replace(/MM/g, MM);
	dateStr = dateStr.replace(/MON/g, MON);
	dateStr = dateStr.replace(/DD/g, DD);
	dateStr = dateStr.replace(/DAY/g, DAY);
	dateStr = dateStr.replace(/hh/g, hh);
	dateStr = dateStr.replace(/HH/g, HH);
	dateStr = dateStr.replace(/mm/g, mm);
	dateStr = dateStr.replace(/ss/g, ss);

	return dateStr;
}

// 일시(날짜+시간) 또는 일자 데이타를 숫자스트링으로 변경한다.
// use : new Date().fromDate("2006-11-02 20:37:12") ==> '20061102111311'
Date.prototype.fromDate = function(dateValue) {
	var pnumresult = "";

	for ( var ic = 0; ic < dateValue.length; ic++) {
		charpnum = dateValue.substr(ic, 1);
		if (charpnum.charCodeAt(0) > 47 && charpnum.charCodeAt(0) < 58) {
			pnumresult += charpnum;
		}
	}

	return pnumresult;
}

function setCipher2(number) {
	var ret = (("" + number).length == 1) ? "0" + number : "" + number;
	return ret;
}

function z_CreateForm(nm, mt, at, tg) {
	var f = document.createElement("form");
	f.name = nm;
	f.method = mt;
	f.action = at;
	f.target = tg;
	return f;
}
function z_AddHidden(f, n, v) {
	var i = document.createElement("input");
	i.type = "hidden";
	i.name = n;
	i.value = v;
	f.insertBefore(i);
	return f;
}

function getCommaVal(srcNumber) {
	var txtNumber = '' + srcNumber;
	var rxSplit = new RegExp('([0-9])([0-9][0-9][0-9][,.])');
	var arrNumber = txtNumber.split('.');
	arrNumber[0] += '.';
	do {
		arrNumber[0] = arrNumber[0].replace(rxSplit, '$1,$2');
	} while (rxSplit.test(arrNumber[0]));
	if (arrNumber.length > 1) {
		return arrNumber.join('');
	} else {
		return arrNumber[0].split('.')[0];
	}
}

function getFilename(f_name) {
	var n_name = "";
	if (f_name != "") {
		var lst_in = f_name.lastIndexOf('\\');
		n_name = f_name.substring(lst_in + 1);
	}
	return n_name;
}

function getFileSize(filePath) {
	var result;
	var img = new Image();
	img.dynsrc = filePath;
	var fileSize = parseInt(img.fileSize);

	if (fileSize < 1024) {
		result = getCommaVal(fileSize) + " byte";
	} else if (fileSize < 1000 * 1024) {
		result = getCommaVal(Math.round(fileSize * 100 / 1024) / 100) + " KB";
	} else {
		result = getCommaVal(Math.round(fileSize * 100 / (1024 * 1000)) / 100)
				+ " MB";
	}
	return result;
}

/**
 * last modified by kkumooli 2009.02.20
 */
function resizeIframe(curFrame) {
	try {
		if (typeof curFrame != "undefined") {

			curFrame.style.display = "block";

			if (curFrame.contentDocument
					&& curFrame.contentDocument.body.offsetHeight) { // ns6
																		// syntax
				if (curFrame.Document.body.scrollHeight > curFrame.scrollHeight) {
					curFrame.height = curFrame.contentDocument.body.scrollHeight;
				}
			} else if (curFrame.Document && curFrame.Document.body.scrollHeight) { // ie5+
																					// syntax
				if (curFrame.Document.body.scrollHeight > curFrame.scrollHeight) {
					curFrame.height = curFrame.Document.body.scrollHeight;
				}
			}

			try {
				// parent.resizeIframe_(
				// parent.document.getElementById("iframe_main"));
			} catch (e) {
				;
			}
		}
	} catch (e) {
		;
	}
}

function getRadioValue(objName) {
	var selectedItem = '';
	var radioObj = document.all(objName);

	if (radioObj.length == null) { // 라디오버튼이 같은 name 으로 하나밖에 없다면
		radioObj.checked = true;
		selectedItem = radioObj.value;
	} else {// 라디오 버튼이 같은 name 으로 여러개 있다면
		for (i = 0; i < radioObj.length; i++) {
			if (radioObj[i].checked) {
				selectedItem = radioObj[i].value;
				break;
			}
		}
	}
	return selectedItem;
}

String.prototype.trim = function() {
	return this.replace(/(^\s*)|(\s*$)/gi, "");
}

// 원하는 문자열을 전부 replace
String.prototype.replaceAll = function(str1, str2) {
	var temp_str = "";
	if (this.trim() != "" && str1 != str2) {
		temp_str = this.trim();
		while (temp_str.indexOf(str1) > -1) {
			temp_str = temp_str.replace(str1, str2);
		}
	}
	return temp_str;
}
/**
 * Name : z_divToggle Description : div 의 내용 보여주는 것을 토글한다. Parameters : divID :
 * div id 값. Use : z_divToggle( divTab1 ); <DIV id='divTab1'> </DIV> Revision :
 * 2007.01.11 sjmoon@brainz.co.kr initial release
 */

function z_divToggle(divID, h4Object, isExpand) {
	if (isExpand != undefined) {
		if (isExpand) {
			divID.style.display = "";
			h4Object.className = "expand";
		} else {
			divID.style.display = "none";
			h4Object.className = "collapse";
		}
	} else {
		if (divID.style.display == "none") {
			divID.style.display = "";
			if (h4Object != undefined) {
				h4Object.className = "expand";

			}
		} else {
			divID.style.display = "none";
			if (h4Object != undefined) {
				h4Object.className = "collapse";
			}
		}
	}
}

/**
 * 값 value1 이 param1 변수에 존재하는지 확인한다.
 */
function isExist(param1, value1) {
	var obj = document.getElementsByName(param1);
	for (i = 0; i < obj.length; i++) {
		if (obj[i].value == value1) {
			return false;
		}
	}
	return true;
}

/**
 * 필드의 길이가 bytelength이하인지를 검사.
 */
function check_length(src, bytelength) {
	var tmpStr, nStrLen, reserve, frm, bytelength;
	var content = document.all[src];
	// bytelength = 1410; // Textarea의 길이 200bytpe로 제한

	sInputStr = content.value;
	nStrLen = calculate_byte(sInputStr);

	if (nStrLen > bytelength) {
		tmpStr = Cut_Str(sInputStr, bytelength);
		reserve = nStrLen - bytelength;

		// alert("작성하신 글은 " + reserve + "바이트가 초과되었습니다.");
		alert("입력할수 있는 글의 길이를 초과하였습니다.");

		// byte 길이에 맞게 입력내용 수정
		content.value = tmpStr;
		nStrLen = calculate_byte(tmpStr);
		// frm.cbyte.value = nStrLen;
	} else {
		// frm.cbyte.value = nStrLen;
	}

	return;
}
/**
 * check_length() 함수에서 사용하는 서브 함수. 입력 필드의 바이트 수 계산.
 */
function calculate_byte(sTargetStr) {
	var sTmpStr, sTmpChar;
	var nOriginLen = 0;
	var nStrLength = 0;

	sTmpStr = new String(sTargetStr);
	nOriginLen = sTmpStr.length;

	for ( var i = 0; i < nOriginLen; i++) {
		sTmpChar = sTmpStr.charAt(i);

		if (escape(sTmpChar).length > 4) {
			nStrLength += 2;
		} else if (sTmpChar != '\r') {
			nStrLength++;
		}
	}

	return nStrLength;

}
/**
 * check_length() 함수에서 사용하는 서브 함수. 입력 필드에서 길이를 넘었을 때 길이를 넘는 문자 짜르기.
 */
function Cut_Str(sTargetStr, nMaxLen) {
	var sTmpStr, sTmpChar, sDestStr;
	var nOriginLen = 0;
	var nStrLength = 0;
	var sDestStr = "";
	sTmpStr = new String(sTargetStr);
	nOriginLen = sTmpStr.length;

	for ( var i = 0; i < nOriginLen; i++) {
		sTmpChar = sTmpStr.charAt(i);

		if (escape(sTmpChar).length > 4) {
			nStrLength = nStrLength + 2;

			// } else if ((sTmpChar != '\r')&&(sTmpChar != '\n')) {

			// 위에 else if문을 막고 아래라인 주석을 풀면 엔터값도 1byte로 포함한다.
			// check_length()에도 동일한 라인이 있으니 똑같이 주석츨 풀고 막아주어야함.

		} else if (sTmpChar != '\r') {

			nStrLength++;
		}

		if (nStrLength <= nMaxLen) {
			sDestStr = sDestStr + sTmpChar;
		} else {
			break;
		}

	}

	return sDestStr;

}

/**
 * Name : stripTag Description : <>' 를 치환한다. Parameters : String Use : obj.value =
 * (obj.value).stripTag(); Revision : 2007.02.1. sjmoon@brainz.co.kr initial
 * release
 */
String.prototype.stripTag = function() {
	var str1 = this;
	if (str1 != "") {
		str1 = str1.replace(/&/g, "&amp;");
		str1 = str1.replace(/</g, "&lt;");
		str1 = str1.replace(/>/g, "&gt;");
		str1 = str1.replace(/\'/g, "`");

	}

	return str1;
}

function z_cutString(str, limitBytes) {
	var byteLen = 0;
	var str_return = "";
	for (i = 0; i < str.length; i++) {
		if (str.charCodeAt(i) > 255)
			byteLen += 2;
		else
			byteLen++;

		if (byteLen > limitBytes) {
			str_return = "<font title='" + str + "'>" + str.substring(0, i)
					+ "..";
			return str_return;
		}
	}
	return str;
}

// 2007-02-06 nhn 제공하는 스크립트로써 사용자 정보를 보여준다.
function g_searchUserPop(NotesID, notusefield) {
	var url = "http://mynext-nbp.nhncorp.com/portal/server.pt/gateway/PTARGS_0_0_261_0_80405_43/http%3B/portlet.nhncorp.com/PS/Common/Search.jsp";
	if (NotesID.substr(0, 2) == "KR") {
		url = "http://mynext.nhncorp.com/portal/server.pt/gateway/PTARGS_0_0_261_0_80405_43/http%3B/portlet.nhncorp.com/PS/Common/Search.jsp";
	}
	url += "?UserID=" + NotesID;

	var subwin = openSubwin(url, 920, 300, "no", "subwin", "no");
	subwin.focus();
}

// 2009-05-11 itsm ci 정보 조회
function g_getCiPop(ciNo) {

	var url = "http://itsm.nhncorp.com/itsm/ZCfgCiManagerController.itsm?cmd=getCiViewPop&ci_no="
			+ ciNo;
	WinPop(url, '_blank', 805, 800, true, true);
}

// 2009-06-17 itsm ci 정보 조회
function g_getCiListPop(param) {

	var url = "http://itsm.nhncorp.com/itsm/ZCfgCiManagerController.itsm?cmd=getListPop&notSelected=true"
			+ param;
	WinPop(url, '_blank', 830, 840, true, true);
}

// 상면도 조회Pop 화면으로 이동.
function g_getIdcPop(idc_id) {
	// 위치없음.
	if (idc_id == 'IDC20071016000007')
		return;
	url = "http://itsm.nhncorp.com/itsm/infratool.itsm?cmd=getIDCDrawing&idc_id="
			+ idc_id;

	WinPop(url, 'sangMyeonPop', 800, 860, true);
}

// Rack 열 조회 화면으로 이동
function g_getRackRowPop(idc_id, rrow_id) {

	url = "http://itsm.nhncorp.com/itsm/infratool.itsm?cmd=getRackRowConfig&idc_id="
			+ idc_id + "&rack_row_id=" + rrow_id;

	WinPop(url, 'sangMyeonPop', 800, 860, true);
}

// Rack 조회 화면으로 이동
function g_getRackPop(idc_id, rrow_id, rack_id) {

	url = "http://itsm.nhncorp.com/itsm/infratool.itsm?cmd=getRackConfig&idc_id="
			+ idc_id + "&rack_row_id=" + rrow_id + "&rack_id=" + rack_id;

	WinPop(url, 'sangMyeonPop', 800, 860, true);
}

// 2007-02-06 nhn 제공하는 스크립트로써 사용자 정보를 보여준다.
function openSubwin(url, width, height, scrollbars, win_name, resizable) {
	var opt_scrollbars = (scrollbars == null) ? "auto" : scrollbars;
	var window_name = (win_name == null) ? "subwin" : win_name;
	var winFeature = setCenter(width, parseInt(height, 10) + 20)
			+ ",status=yes,menubar=no,resizable="
			+ (resizable == null ? "no" : resizable) + ",scrollbars="
			+ opt_scrollbars;
	var subwin = window.open(url, window_name, winFeature);
	return subwin;
}
// 2007-02-06 nhn 제공하는 스크립트로써 사용자 정보를 보여준다.
function setCenter(winwidth, winheight) {
	winx = Math.ceil((screen.availWidth - winwidth) / 2);
	winy = Math.ceil((screen.availHeight - winheight) / 2);
	if (winwidth == screen.availWidth)
		winwidth = screen.availWidth - 10;
	if (winheight == screen.availHeight)
		winheight = screen.availHeight - 30;
	return "left=" + winx + ",top=" + winy + ",width=" + winwidth + ",height="
			+ winheight;
}

// 숫자만 입력되도록 강제
function f_numberForce(_object) {
	var arg = _object.value;
	var num = "0123456789.";
	var tmpStr = gfn_getStr(arg);

	if (tmpStr.length <= 0) {
		return;
	}
	for ( var gi = 0; gi < tmpStr.length; gi++) {
		if (num.indexOf(tmpStr.substring(gi, gi + 1)) < 0) {
			alert("숫자만 입력 하세요");
			return _object.value = 0;
		}
	}
	return true;
}

function f_parseInt(_value) {
	var number = parseInt(_value);
	if (isNaN(number))
		number = 0;
	return number;
}

function tr_over(e) {
	e.style.backgroundColor = '#F5F8FD';
}
function tr_out(e) {
	e.style.backgroundColor = '';
}

function createHelperMsgBox() {
	var obj = document.createElement("DIV");
	obj.id = "helperMsgBox";
	obj.className = "help_msg";
	obj.style.display = "none";
	obj.style.position = "absolute";

	document.body.appendChild(obj);
	return obj;
}

// show MsgBox
function showHelper(msg) {
	if (msg == '')
		return;
	var helperObj = document.getElementById("helperMsgBox");
	if (!helperObj) {
		helperObj = createHelperMsgBox();
	}

	with (helperObj) {
		helperObj.innerHTML = "<table class='show_helper' border='0' cellspacing='0' cellpadding='0'><tr><td>"
				+ msg + "</td></tr></table>";

		if (document.body.clientWidth - (event.x + 200) > 0) {
			style.left = event.x;
			style.right = "";
		} else {
			style.right = document.body.clientWidth - event.x;
			style.left = "";
		}

		// style.left = event.x + 0 + document.body.scrollLeft;
		style.top = event.y + 10 + document.body.scrollTop;
		style.display = "";
	}

}

// hide MsgBox
function hideHelper() {
	var helperObj = document.getElementById("helperMsgBox");
	if (!helperObj)
		return;
	with (helperObj) {
		style.display = "none";
	}

}
function removeChar(str, flag) {
	// console.info("remove:"+flag);
	var regStr = "/\\" + flag + "/gi";
	var reg = eval(regStr);
	return str.replace(reg, "");
}

/**
 * 텍스트 필드에 yyyy-MM-dd 형식으로 입력되도록 강제. 사용법 : onKeyUp="f_forceDateFormat(this);"
 */
function f_forceDateFormat(event, obj) {
	if (event.keyCode == 8 || event.keyCode == 9 || event.keyCode == 13
			|| (event.keyCode >= 37 && event.keyCode <= 40))
		return false;
	// console.info(obj.value);
	// console.info("befor remove . str:"+obj.value+"
	// length:"+obj.value.length);
	var str = removeChar(obj.value, '-');
	// console.info("str:"+str+" length:"+str.length);
	if (str.length == 4) {
		obj.value = str + "-";
	} else if (str.length == 6) {
		var month = str.substring(4, 6);
		// console.info("month is "+month);
		if (parseInt(month, 10) > 12) {
			month = "12";
		} else if (parseInt(month, 10) == 0) {
			month = "01";
		}
		obj.value = str.substring(0, 4) + "-" + month + "-";
	} else if (str.length >= 8) {

		obj.value = str.substring(0, 4) + "-" + str.substring(4, 6) + "-"
				+ str.substring(6, 8);
	}

}
/**
 * 텍스트 필드에 HH:mm 형식으로 입력되도록 강제. 사용법 : onKeyUp="f_forceTimeFormat(this);"
 */
function f_forceTimeFormat(e, obj) {
	if (window.event) {
		var ieKey = window.event.keyCode;
	} else {
		var ieKey = e.chartCode;
	}
	// alert(ieKey);
	// 백스페이스키 무시
	if (ieKey == 8 || ieKey == 9 || ieKey == 13 || (ieKey >= 37 && ieKey <= 40))
		return false;
	var str = f_removeChar(obj.value, '.');
	str = f_removeChar(str, ':');
	if (str.length == 2) {
		obj.value = str + ":";
	} else if (str.length >= 4) {
		obj.value = str.substring(0, 2) + ":" + str.substring(2, 4);
	}

}

// 특정문자를 삭제한 값을 리턴
function f_removeChar(srcString, strchar) {
	var convString = '';
	for (z = 0; z < srcString.length; z++) {
		if (srcString.charAt(z) != strchar)
			convString = convString + srcString.charAt(z);
	}
	return convString;
}

/**
 * 텍스트 필드에서 yyyy-MM-dd 형식으로 입력되었는지 여부 확인. 사용법:
 * onblur="f_dateFormatConfirm(this);"
 */
function f_dateFormatConfirm(oo) {
	var alertMsg = "잘못된 날짜 형식입니다. [YYYY-MM-DD] 형식으로 입력해 주십시요.";
	if (oo.value == "") {
		alert(alertMsg);
		oo.focus();
		return false;
	}
	var dateValue = oo.value;
	/*
	 * var dateValue=new String(oo.value); var filter =
	 * /^\d{4}\-(0[1-9]|1[0-2])\-(0[1-9]|[12][0-9]|3[01])$/ig;
	 * if(!filter.test(dateValue)) { alert("잘못된 날짜 형식입니다. [YYYY-MM-DD] 형식으로 입력해
	 * 주십시요."); oo.value=""; oo.focus(); return false; }
	 */
	try {
		var year = parseInt(dateValue.substring(0, 4), 10);
		var isValidate = true;
		if (dateValue.length != 10)
			isValidate = false;
		if (isNaN(year) || year < 1900 || year > 3000)
			isValidate = false;
		var month = parseInt(dateValue.substring(5, 7), 10);
		if (isNaN(month) || month < 1 || month > 12)
			isValidate = false;
		var day = parseInt(dateValue.substring(8, 10), 10);
		if (isNaN(day) || day < 1 || day > 31)
			isValidate = false;

		if (!isValidate) {
			alert(alertMsg);
			oo.value = "";
			oo.focus();
		}

		return isValidate;
	} catch (x) {
		alert(x);
		alert(alertMsg);
		oo.value = "";
		oo.focus();
		return false;
	}
}

/**
 * 텍스트 필드에서 yyyy-MM-dd 형식으로 입력되었는지 여부 확인. 사용법:
 * onblur="f_timeFormatConfirm(this);"
 */
function f_timeFormatConfirm(oo) {
	var alertMsg = "잘못된 시간 형식입니다. [HH:mm] 형식으로 입력해 주십시요.";
	if (oo.value == "") {
		alert(alertMsg);
		return false;
	}
	var dateValue = new String(oo.value);
	/*
	 * var dateValue=new String(oo.value); var filter = /^\d{2}\:\d{2}$/ig;
	 * if(!filter.test(dateValue)) { alert("잘못된 시간 형식입니다. [HH:mm] 형식으로 입력해
	 * 주십시요."); oo.value=""; oo.focus(); return false; }
	 */

	try {
		var hour = parseInt(dateValue.substring(0, 2), 10);
		var isValidate = true;
		if (dateValue.length != 5)
			isValidate = false;
		if (isNaN(hour) || hour < 0 || hour > 23)
			isValidate = false;
		var min = parseInt(dateValue.substring(3, 5), 10);
		if (isNaN(min) || min < 0 || min > 59)
			isValidate = false;

		if (!isValidate) {
			alert(alertMsg);
			oo.value = "";
			oo.focus();
		}

		return isValidate;
	} catch (x) {
		alert(x);
		alert(alertMsg);
		oo.value = "";
		oo.focus();
		return false;
	}
	return true;
}

/**
 * 입력값에 특정 문자(chars)가 있는지 체크 특정 문자를 허용하지 않으려 할 때 사용 ex) if
 * (containsChars(form.name,"!,*&^%$#@~;")) { alert("이름 필드에는 특수 문자를 사용할 수
 * 없습니다."); }
 */
function containsChars(input, chars) {
	for ( var inx = 0; inx < input.value.length; inx++) {
		if (chars.indexOf(input.value.charAt(inx)) != -1)
			return true;
	}
	return false;
}

/**
 * 입력값이 특정 문자(chars)만으로 되어있는지 체크 특정 문자만 허용하려 할 때 사용 ex) if
 * (!containsCharsOnly(form.blood,"ABO")) { alert("혈액형 필드에는 A,B,O 문자만 사용할 수
 * 있습니다."); }
 */
function containsCharsOnly(input, chars) {
	for ( var inx = 0; inx < input.value.length; inx++) {
		if (chars.indexOf(input.value.charAt(inx)) == -1)
			return false;
	}
	return true;
}

/**
 * 입력값이 알파벳인지 체크 아래 isAlphabet() 부터 isNumComma()까지의 메소드가 자주 쓰이는 경우에는 var chars
 * 변수를 global 변수로 선언하고 사용하도록 한다. ex) var uppercase =
 * "ABCDEFGHIJKLMNOPQRSTUVWXYZ"; var lowercase = "abcdefghijklmnopqrstuvwxyz";
 * var number = "0123456789"; function isAlphaNum(input) { var chars = uppercase +
 * lowercase + number; return containsCharsOnly(input,chars); }
 */
function isAlphabet(input) {
	var chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	return containsCharsOnly(input, chars);
}

/**
 * 입력값이 알파벳 대문자인지 체크
 */
function isUpperCase(input) {
	var chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	return containsCharsOnly(input, chars);
}

/**
 * 입력값이 알파벳 소문자인지 체크
 */
function isLowerCase(input) {
	var chars = "abcdefghijklmnopqrstuvwxyz";
	return containsCharsOnly(input, chars);
}

/**
 * 입력값에 숫자만 있는지 체크
 */
function isNumber(input) {
	var chars = "0123456789";
	return containsCharsOnly(input, chars);
}

/**
 * 입력값에 IP만 있는지 체크
 */
function isIP(input) {
	var chars = "0123456789.";
	return containsCharsOnly(input, chars);
}

/**
 * 입력값이 알파벳,숫자로 되어있는지 체크
 */
function isAlphaNum(input) {
	var chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	return containsCharsOnly(input, chars);
}

/**
 * 입력값이 숫자,대시(-)로 되어있는지 체크
 */
function isNumDash(input) {
	var chars = "-0123456789";
	return containsCharsOnly(input, chars);
}

/**
 * 입력값이 숫자,콤마(,)로 되어있는지 체크
 */
function isNumComma(input) {
	var chars = ",0123456789";
	return containsCharsOnly(input, chars);
}

/**
 * 입력값에 숫자,소숫점(.),단위(K,M,G) 있는지 체크
 */
function isNumber2(input) {
	var chars = ".0123456789KkMmGg";
	return containsCharsOnly(input, chars);
}

/**
 * 입력값에 숫자,소숫점(.) 있는지 체크
 */
function isNumber3(input) {
	var chars = ".0123456789";
	return containsCharsOnly(input, chars);
}

/**
 * 입력값의 바이트 길이를 리턴 ex) if (getByteLength(form.title) > 100) { alert("제목은 한글
 * 50자(영문 100자) 이상 입력할 수 없습니다."); } Author : Wonyoung Lee
 */
function getByteLength(input) {
	var byteLength = 0;
	for ( var inx = 0; inx < input.value.length; inx++) {
		var oneChar = escape(input.value.charAt(inx));
		if (oneChar.length == 1) {
			byteLength++;
		} else if (oneChar.indexOf("%u") != -1) {
			byteLength += 2;
		} else if (oneChar.indexOf("%") != -1) {
			byteLength += oneChar.length / 3;
		}
	}
	return byteLength;
}

/**
 * 선택된 라디오버튼이 있는지 체크
 */
function hasCheckedRadio(input) {
	if (input.length > 1) {
		for ( var inx = 0; inx < input.length; inx++) {
			if (input[inx].checked)
				return true;
		}
	} else {
		if (input.checked)
			return true;
	}
	return false;
}

/**
 * 선택된 체크박스가 있는지 체크
 */
function hasCheckedBox(input) {
	return hasCheckedRadio(input);
}

/**
 * 입력받은 값의 IP유효성 체크 ex) if (verifyIP('10.2.1.123')) document.frm.submit(); else
 * return;
 */
function verifyIP(IPvalue) {
	errorString = "";
	theName = "IP 주소";

	var ipPattern = /^(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3})$/;
	var ipArray = IPvalue.match(ipPattern);

	if (IPvalue == "0.0.0.0")
		errorString = errorString + theName + ': ' + IPvalue
				+ '는 특별한 목적으로 사용되는 예약된 주소라 사용할수 없습니다.\n\n다시 입력하십시오. ';
	else if (IPvalue == "255.255.255.255")
		errorString = errorString + theName + ': ' + IPvalue
				+ '는 특별한 목적으로 사용되는 예약된 주소라 사용할수 없습니다.\n\n다시 입력하십시오. ';
	if (ipArray == null)
		errorString = errorString + theName + ': ' + IPvalue
				+ '는 입력이 안되었거나 올바른 주소가 아닙니다.\n\n다시 입력하십시오. ';
	else {
		for (i = 0; i < 4; i++) {
			thisSegment = ipArray[i];
			if (thisSegment > 255) {
				errorString = errorString + theName + ': ' + IPvalue
						+ '는 입력이 안되었거나 올바른 주소가 아닙니다.\n\n다시 입력하십시오. ';
				i = 4;
			}
			if ((i == 0) && (thisSegment > 255)) {
				errorString = errorString + theName + ': ' + IPvalue
						+ '는 특별한 목적으로 사용되는 예약된 주소라 사용할수 없습니다.';
				i = 4;
			}
		}
	}

	if (errorString == "") {
		return true;
	} else {
		alert(errorString);
		return false;
	}
}

String.prototype.trim = function() {
	return this.replace(/(^\s*)|(\s*$)/g, "");
}

// 체크박스전체선택
function selectAll() {
	for (i = 0; i < document.frm.elements.length; i++) {
		if (document.frm.elements[i].type == 'checkbox')
			document.frm.elements[i].checked = true;
	}
}

// 체크박스전체해제
function selectNone() {
	for (i = 0; i < document.frm.elements.length; i++) {
		if (document.frm.elements[i].type == 'checkbox')
			document.frm.elements[i].checked = false;
	}
}
function addOption(selectObj, text, value) {
	var no = new Option();
	no.value = value;
	no.text = text;
	selectObj.options[selectObj.options.length] = no;
}

/**
 * //2007-05-31 field 를 사용가능하게 or 사용불가능하게 toggle
 */
function toggleFieldEnable(f_field) {
	try {
		if (f_field != undefined) {
			if (f_field.disabled) {
				f_field.disabled = false;

				// 색상 변경은 input text 일 경우만..
				if (f_field.isTextEdit)
					f_field.style.backgroundColor = 'white';
			} else {
				f_field.disabled = true;
				if (f_field.isTextEdit)
					f_field.style.backgroundColor = '#dcdcdc';
			}
		}
	} catch (x) {
	}

}
function fieldEnable(f_field, isEnable) {
	try {
		if (f_field != undefined) {
			if (isEnable) {
				if (f_field.disabled) {
					f_field.disabled = false;
				}
				if (f_field.type == "text") {
					f_field.readOnly = false;
				}
				f_field.style.backgroundColor = 'white';
			} else {
				if (f_field.type == "text") {
					f_field.readOnly = true;
				}
				f_field.style.backgroundColor = '#dcdcdc';
			}
		}
	} catch (x) {
		alert(x);
	}

}

;(function($){
/**
 * jqGrid Thai Translation
 * Kittituch Manakul m.kittituch@Gmail.com
 * http://trirand.com/blog/ 
 * Dual licensed under the MIT and GPL licenses:
 * http://www.opensource.org/licenses/mit-license.php
 * http://www.gnu.org/licenses/gpl.html
**/
$.jgrid = $.jgrid || {};
$.extend($.jgrid,{
	defaults : {
		recordtext: "แสดง {0} - {1} จาก {2}",
		emptyrecords: "ไม่พบข้อมูล",
		loadtext: "กำลังร้องขอข้อมูล...",
		pgtext : "หน้า {0} จาก {1}"
	},
	search : {
		caption: "กำลังค้นหา...",
		Find: "ค้นหา",
		Reset: "คืนค่ากลับ",
		odata : ['เท่ากับ', 'ไม่เท่ากับ', 'น้อยกว่า', 'ไม่มากกว่า','มากกกว่า','ไม่น้อยกว่า', 'ขึ้นต้นด้วย','ไม่ขึ้นต้นด้วย','มีคำใดคำหนึ่งใน','ไม่มีคำใดคำหนึ่งใน','ลงท้ายด้วย','ไม่ลงท้ายด้วย','มีคำว่า','ไม่มีคำว่า'],
		groupOps: [	{ op: "และ", text: "ทั้งหมด" },	{ op: "หรือ",  text: "ใดๆ" }	],
		matchText: " ตรงกันกับ",
		rulesText: " ตามกฏ"
	},
	edit : {
		addCaption: "เพิ่มข้อมูล",
		editCaption: "แก้ไขข้อมูล",
		bSubmit: "บันทึก",
		bCancel: "ยกเลิก",
		bClose: "ปิด",
		saveData: "คุณต้องการบันทึการแก้ไข ใช่หรือไม่?",
		bYes : "บันทึก",
		bNo : "ละทิ้งการแก้ไข",
		bExit : "ยกเลิก",
		msg: {
			required:"ข้อมูลนี้จำเป็น",
			number:"กรุณากรอกหมายเลขให้ถูกต้อง",
			minValue:"ค่าของข้อมูลนี้ต้องไม่น้อยกว่า",
			maxValue:"ค่าของข้อมูลนี้ต้องไม่มากกว่า",
			email: "อีเมลล์นี้ไม่ถูกต้อง",
			integer: "กรุณากรอกเป็นจำนวนเต็ม",
			date: "กรุณากรอกวันที่ให้ถูกต้อง",
			url: "URL ไม่ถูกต้อง URL จำเป็นต้องขึ้นต้นด้วย 'http://' หรือ 'https://'",
			nodefined : "ไม่ได้ถูกกำหนดค่า!",
			novalue : "ต้องการการคืนค่า!",
			customarray : "ฟังก์ชันที่สร้างขึ้นต้องส่งค่ากลับเป็นแบบแอเรย์",
			customfcheck : "ระบบต้องการฟังก์ชันที่สร้างขึ้นสำหรับการตรวจสอบ!"
			
		}
	},
	view : {
		caption: "เรียกดูข้อมูล",
		bClose: "ปิด"
	},
	del : {
		caption: "ลบข้อมูล",
		msg: "คุณต้องการลบข้อมูลที่ถูกเลือก ใช่หรือไม่?",
		bSubmit: "ต้องการลบ",
		bCancel: "ยกเลิก"
	},
	nav : {
		edittext: "",
		edittitle: "แก้ไขข้อมูล",
		addtext:"",
		addtitle: "เพิ่มข้อมูล",
		deltext: "",
		deltitle: "ลบข้อมูล",
		searchtext: "",
		searchtitle: "ค้นหาข้อมูล",
		refreshtext: "",
		refreshtitle: "รีเฟรช",
		alertcap: "คำเตือน",
		alerttext: "กรุณาเลือกข้อมูล",
		viewtext: "",
		viewtitle: "ดูรายละเอียดข้อมูล"
	},
	col : {
		caption: "กรุณาเลือกคอลัมน์",
		bSubmit: "ตกลง",
		bCancel: "ยกเลิก"
	},
	errors : {
		errcap : "เกิดความผิดพลาด",
		nourl : "ไม่ได้กำหนด URL",
		norecords: "ไม่มีข้อมูลให้ดำเนินการ",
		model : "จำนวนคอลัมน์ไม่เท่ากับจำนวนคอลัมน์โมเดล!"
	},
	formatter : {
		integer : {thousandsSeparator: " ", defaultValue: '0'},
		number : {decimalSeparator:".", thousandsSeparator: " ", decimalPlaces: 2, defaultValue: '0.00'},
		currency : {decimalSeparator:".", thousandsSeparator: " ", decimalPlaces: 2, prefix: "", suffix:"", defaultValue: '0.00'},
		date : {
			dayNames:   [
				"อา", "จ", "อ", "พ", "พฤ", "ศ", "ส",
				"อาทิตย์", "จันทร์", "อังคาร", "พุธ", "พฤหัสบดี", "ศูกร์", "เสาร์"
			],
			monthNames: [
				"ม.ค.", "ก.พ.", "มี.ค.", "เม.ย.", "พ.ค.", "มิ.ย.", "ก.ค.", "ส.ค.", "ก.ย.", "ต.ค.", "พ.ย.", "ธ.ค.",
				"มกราคม", "กุมภาพันธ์", "มีนาคม", "เมษายน", "พฤษภาคม", "มิถุนายน", "กรกฏาคม", "สิงหาคม", "กันยายน", "ตุลาคม", "พฤศจิกายน", "ธันวาคม"
			],
			AmPm : ["am","pm","AM","PM"],
			S: function (j) {return ''},
			srcformat: 'Y-m-d',
			newformat: 'd/m/Y',
			masks : {
				ISO8601Long:"Y-m-d H:i:s",
				ISO8601Short:"Y-m-d",
				ShortDate: "n/j/Y",
				LongDate: "l, F d, Y",
				FullDateTime: "l, F d, Y g:i:s A",
				MonthDay: "F d",
				ShortTime: "g:i A",
				LongTime: "g:i:s A",
				SortableDateTime: "Y-m-d\\TH:i:s",
				UniversalSortableDateTime: "Y-m-d H:i:sO",
				YearMonth: "F, Y"
			},
			reformatAfterEdit : false
		},
		baseLinkUrl: '',
		showAction: '',
		target: '',
		checkbox : {disabled:true},
		idName : 'id'
	}
});
})(jQuery);

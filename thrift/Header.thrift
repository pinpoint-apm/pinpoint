namespace java com.profiler.dto

const byte SIGNATURE = 0xef;

struct Header {
	1: required  byte	signature = SIGNATURE,
	2: required  byte 	version = 0x10,
	3: required  i16    type
}

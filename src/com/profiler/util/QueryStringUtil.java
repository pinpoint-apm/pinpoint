package com.profiler.util;

public class QueryStringUtil {
//	public static void main(String args[]) {
//		String a="a\nb\rc\n";
//		
//		System.out.println(QueryStringUtil.removeCarriageReturn(a));
//	}
	public static String removeCarriageReturn(String query) {
//		query.replaceAll(regex, replacement)
		String result= query.replaceAll("[\r\n]", " "); 
		return result;
	}
	public static String removeAllMultiSpace(String query) {
		String after = query.trim().replaceAll(" +", " ");
		return after;
	}
}

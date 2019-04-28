package com.google.play.ngrok;

public class Log {
	public static Boolean isdebug=true;
	public static void print(String str)
	{
		if(isdebug)
		{
			System.out.println(str);
		}
	}
}

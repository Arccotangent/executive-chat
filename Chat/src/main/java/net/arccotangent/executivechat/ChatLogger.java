package net.arccotangent.executivechat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ChatLogger {

	public ChatLogger()
	{
		System.out.println(getTime() + " [INFO] [CORE] Logging interface initialized.");
	}
	
	private String getTime()
	{
		Calendar c = Calendar.getInstance();
		Date d = c.getTime();
		DateFormat df = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss.SSS aa z");
		String formatted = df.format(d);
		return formatted;
	}
	
	public void dbg(final String msg)
	{
		if (Main.debug)
			System.out.println(getTime() + " [DEBUG] " + msg);
	}
	
	public void info_nln(final String msg)
	{
		System.out.print(getTime() + " [INFO] " + msg);
	}
	
	public void info_nln2(final String msg)
	{
		System.out.print(getTime() + " [INFO] " + msg);
	}
	
	public void info_ln(final String msg)
	{
		System.out.println(msg);
	}
	
	public void info(final String msg)
	{
		System.out.println(getTime() + " [INFO] " + msg);
	}
	
	public void warn(final String msg)
	{
		System.out.println(getTime() + " [WARNING] " + msg);
	}
	
	public void err(final String msg)
	{
		System.out.println(getTime() + " [ERROR] " + msg);
	}
	
	@SuppressWarnings("unused")
	private void stdout_raw(final String msg)
	{
		System.out.println(msg);
	}
	
	@SuppressWarnings("unused")
	private void stderr_raw(final String msg)
	{
		System.err.println(msg);
	}
	
}

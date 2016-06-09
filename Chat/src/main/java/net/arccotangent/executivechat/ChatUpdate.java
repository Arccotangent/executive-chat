package net.arccotangent.executivechat;

import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class ChatUpdate {
	
	private static final int CONN_TIMEOUT = 1000; //milliseconds
	private static final int READ_TIMEOUT = 1000; //milliseconds
	
	public static void checkUpdates() throws IOException
	{
		Main.log.info("[UPDATE] Fetching server version number.");
		URL version_URL = new URL("https://s3.amazonaws.com/arcchat/version.txt");
		String version = IOUtils.toString(version_URL);
		Main.log.info("[UPDATE] Update server has Executive Chat " + version);
		Object[] versionMsg = new Object[]
				{
				"You have Executive Chat version " + Main.VERSION + "\nServer has Executive Chat version " + version, "\n\nYou may wish to review the Release Notes before downloading this update."
				};
		String[] versionOpt = new String[]
				{
				"Download Executive Chat " + version, "View Release Notes", "Done"
				};
		int option = JOptionPane.showOptionDialog(null, versionMsg, "Executive Chat Update Checker", 0, JOptionPane.INFORMATION_MESSAGE, null, versionOpt, versionOpt[0]);
		if (option == 0)
		{
			Main.log.info("[UPDATE] Downloading update. (version " + version + ")");
			URL chatServerUpdateURL = new URL("https://s3.amazonaws.com/arcchat/Executive-Chat.jar");
			File UPDATE_FILE = new File(Main.getHomeDir() + "/Executive-Chat-" + version + ".jar");
			JOptionPane.showMessageDialog(null, "Executive Chat download is commencing now!\n\nIf you have a slow internet connection, Executive Chat may seem unresponsive while downloading the update.\nDo not exit Executive Chat until the download is complete!", "Download Commencing", JOptionPane.WARNING_MESSAGE);
			FileUtils.copyURLToFile(chatServerUpdateURL, UPDATE_FILE, CONN_TIMEOUT, READ_TIMEOUT);
			JOptionPane.showMessageDialog(null, "Download is complete!\n\nFind the downloaded Executive Chat at the following location in your hard drive: " + UPDATE_FILE.getAbsolutePath() + "", "Executive Chat Download Complete!", JOptionPane.INFORMATION_MESSAGE);
			Main.log.info("[UPDATE] Update download complete.");
		}
		else if (option == 1)
		{
			Main.log.info("[UPDATE] Fetching release notes.");
			checkReleaseNotes();
		}
		else
		{
			Main.log.info("[UPDATE] Not downloading update.");
			JOptionPane.showMessageDialog(null, "No update is being downloaded.", "No Update Downloaded", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	public static void checkReleaseNotes() throws IOException
	{
		URL rn_URL = new URL("https://s3.amazonaws.com/arcchat/release-notes.txt");
		String rn = IOUtils.toString(rn_URL, "UTF-8");
		final JFrame reportDisplayFrame = new JFrame("Executive Chat Release Notes");
		  reportDisplayFrame.setSize(900, 700);
		  reportDisplayFrame.getContentPane().setLayout(new FlowLayout());
		  JTextArea reportDisplayText = new JTextArea(25, 80);
		  reportDisplayText.setEditable(false);
		  reportDisplayText.setLineWrap(true);
		  reportDisplayText.setWrapStyleWord(true);
		  reportDisplayText.setFont(Main.DIALOG_FONT);
		  int horizontalPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS;
		  int verticalPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS;
		  reportDisplayText.setText(rn);
		  JScrollPane finalNumberScroll = new JScrollPane(reportDisplayText, verticalPolicy, horizontalPolicy);
		  reportDisplayFrame.getContentPane().add(finalNumberScroll);
		  reportDisplayFrame.setVisible(true);
	}
}

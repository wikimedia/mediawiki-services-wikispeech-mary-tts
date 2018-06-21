package marytts.server.http;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Scanner;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;

class BuildInfoReader {

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
    private String startedAt;

    BuildInfoReader() {
	dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	startedAt = dateFormat.format(new Date());
    }

    private String runExternalCmd(String cmd) throws Exception {
	Process p = Runtime.getRuntime().exec(cmd);
	BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
	BufferedReader stderr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
	Scanner sce = new Scanner(stderr).useDelimiter("\\Z");
	if (sce.hasNext()) {
	    String msg = "[VersionInfo] Couldn't run external command: " + cmd + " : " + sce.next().trim();
	    System.err.println(msg);
	    //logger.info(msg);
	    return null;
	} else {
	    Scanner sc = new Scanner(stdout).useDelimiter("\\Z");
	    if (sc.hasNext()) {
		return sc.next().trim();
	    } else {
		String msg = "[VersionInfo] Couldn't read output from external command: " + cmd;
		System.err.println(msg);
		//logger.info(msg);
		return null;
	    }
	}
    
    }

    // STTS addition, November 2017
    protected String getBuildInfo() throws Exception {
	ArrayList<String> res = new ArrayList<String>();
	String buildInfoFile = "/wikispeech/marytts/build_info.txt";
	if (new File(buildInfoFile).exists()) {
	    Scanner sc = new Scanner(new BufferedReader(new FileReader(buildInfoFile)));
	    while (sc.hasNextLine()) {
		String l = sc.nextLine().trim();
		if (l.trim().length()>0) {
		    res.add(l);
		}
	    }
	    sc.close();
	} else {
	    //logger.info("[VersionInfo] No build info file found: " + buildInfoFile);
	    System.err.println("[VersionInfo] No build info file found: " + buildInfoFile);
	    res.add("Application name: marytts");
	    res.add("Build timestamp: n/a");
	    res.add("Built by: user");
	    try {
		String tag = runExternalCmd("git describe --tags");
		String branch = runExternalCmd("git rev-parse --abbrev-ref HEAD");
		if (tag != null && branch != null) {
		    res.add("Release: " + tag + " on branch " + branch);
		} else {
		    res.add("Release: unknown");
		}
	    } catch (Exception e) {
		System.err.println("[VersionInfo] Couldn't retrieve git release info: " + e.getMessage());
		//logger.info("[VersionInfo] Couldn't retrieve git release info: " + e.getMessage());
		res.add("Release: unknown");
	    }
	}

	res.add("Started: " + startedAt);
	String resString = "";
	for (String s : res)
	    resString += s + "\n";
	return resString.trim();
    }

}

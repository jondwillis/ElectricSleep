package com.androsz.electricsleepbeta.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class DeviceUtil {
	public static float getCpuClockSpeed() {
		float cpuclock = 0;
		try {
			final StringBuffer s = new StringBuffer();
			final Process p = Runtime.getRuntime().exec("cat /proc/cpuinfo");
			final BufferedReader input = new BufferedReader(
					new InputStreamReader(p.getInputStream()));
			String line = "";
			while ((line = input.readLine()) != null
					&& s.toString().length() == 0) {
				if (line.startsWith("BogoMIPS")) {
					s.append(line + "\n");
				}
			}
			final String cpuclockstr = s.substring(s.indexOf(":") + 2,
					s.length());
			cpuclock = Float.parseFloat(cpuclockstr);
		} catch (final Exception err) {
			// if ANYTHING goes wrong, just report 0 since this is only used for
			// performance appraisal.
		}
		return cpuclock;
	}

	public static String getHardwareName() {
		String hardwarenamestr = "{}";
		try {
			final StringBuffer s = new StringBuffer();
			final Process p = Runtime.getRuntime().exec("cat /proc/cpuinfo");
			final BufferedReader input = new BufferedReader(
					new InputStreamReader(p.getInputStream()));
			String line = "";
			while ((line = input.readLine()) != null
					&& s.toString().length() == 0) {
				if (line.startsWith("Hardware")) {
					s.append(line + "\n");
				}
			}
			hardwarenamestr = s.substring(s.indexOf(":") + 2, s.length());
		} catch (final Exception err) {
			// if ANYTHING goes wrong, just report 0 since this is only used for
			// performance appraisal.
		}
		return hardwarenamestr;
	}
}

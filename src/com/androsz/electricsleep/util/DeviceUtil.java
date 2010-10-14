package com.androsz.electricsleep.util;

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
			if (input.readLine() != null) {
				// System.out.println(line);
				s.append(input.readLine() + "\n");
			}
			final String cpuclockstr = s.substring(s.indexOf(":") + 1,
					s.length());
			cpuclock = Float.parseFloat(cpuclockstr);
		} catch (final Exception err) {
			// if ANYTHING goes wrong, just report 0 since this is only used for
			// performance appraisal.
		}
		return cpuclock;
	}
}

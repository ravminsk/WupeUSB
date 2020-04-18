package rav;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class DeviceUSB {

	private int count;
	private String devSdx = " ";// [/dev/sdx]
	private String devMediaFolder = " ";// [media/user]
	private String devSize = " ";// SIZE]
	private boolean devShred = false;
	private boolean devFormat = false;

	public boolean isDevShred() {
		return devShred;
	}

	public void setDevShred(boolean isDevShred) {
		this.devShred = isDevShred;
	}

	public boolean isDevFormat() {
		return devFormat;
	}

	public void setDevFormat(boolean isDevFormat) {
		this.devFormat = isDevFormat;
	}

	public String getDevSda() {
		return devSdx;
	}

	public String getDevMedia() {
		return devMediaFolder;
	}

	public String getDevSize() {
		return devSize;
	}

	public boolean isDevMounted() {
		boolean isDevMounted = false;
		try {
			String line;
			Process pLsblk;
			String[] words;

			devSdx = "";
			devMediaFolder = "";
			devSize = "";

			ProcessBuilder pbLsblk = new ProcessBuilder("lsblk", "-oRM,KNAME,SIZE,MOUNTPOINT");
			pLsblk = pbLsblk.start();
			BufferedReader brLsblk = new BufferedReader(new InputStreamReader(pLsblk.getInputStream()));
			while ((line = brLsblk.readLine()) != null) {
				if (line.charAt(1) == '1') {
					words = line.split("[^A-Za-zА-Яа-я0-9/.,-]+");
					if (words.length > 4) {
						devSdx = "/dev/" + words[2];
						devSize = words[3];
						devMediaFolder = line.substring(devSdx.length() + devSize.length() + 7);// 7 Spaces
						isDevMounted = true;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return isDevMounted;
	}

	// ___________________________________________________________________________________________________________________

	public int devShred() {
		count = 0;
		ProcessBuilder pbShred = new ProcessBuilder();
		try {
			Files.walk(Path.of(devMediaFolder)).forEach(fileName -> {
				try {
					Process pShred = pbShred.command("shred", "-fvzu", "-n1", fileName.toString()).start();
					BufferedReader br = new BufferedReader(new InputStreamReader(pShred.getErrorStream()));
					String line;
					while ((line = br.readLine()) != null) {
						if (line.contains("removed")) {
							count++;
							System.out.println(line);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			devShred = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return count;
	}

	// ___________________________________________________________________________________________________________________

	public boolean devFormat(String sizeFormat) {
		devFormat = false;
		String devRoot = (String) devSdx.subSequence(0, 8);
		ProcessBuilder pb = new ProcessBuilder();
		pb.redirectErrorStream(true);

		try {
			myOut(pb.command("umount", devMediaFolder).start());
			myOut(pb.command("dd", "if=/dev/zero", "of=" + devRoot, "bs=2M", "count=1", "status=progress").start());
			myOut(pb.command("parted", "-s", "-a", "minimal", devRoot, "mklabel", "msdos", "mkpart", "primary", "fat32",
					"32256b", sizeFormat).start());
			myOut(pb.command("sudo", "mkfs.fat", "-a", "-I", devRoot + "1").start());

			int mnt = 1;
			while (mnt != 0) {
				Process pr = pb.command("udisksctl", "mount", "-b", devRoot + "1").start();
				pr.waitFor();
				mnt = pr.exitValue();
			}
			devFormat = true;// must check!!!!!!!!!!!!!!!!!!!!!!

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			return devFormat;
		}
		return devFormat;
	}

	// must read InputStream and ErrorStream of ProcessBuilder for Process finishing
	void myOut(Process process) {
		BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
		try {
			while ((br.readLine()) != null) {
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

//end
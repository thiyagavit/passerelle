/**
 * 
 */
package fr.soleil.passerelle.util;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Date;

/**
 * @author Administrateur
 *
 */
public class FileSaver {
	private static FileSaver instance;
	private static boolean startSave;
	private static FileWriter file;
	private static int idx;
	private static String fullFileName;
	private String start;

	private FileSaver(String filePath, String fileName, String fileExtension) {
		startSave = false;
		// check current index
		File dir = new File(filePath);
		// find all files that starts with "fileName"
		start = fileName;
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File file, String name) {
				return name.startsWith(start);
			}
		};
		File[] fileList = dir.listFiles(filter);
		if (fileList == null || fileList.length == 0) {
			// no files have been created with that name
			idx = 1;
		} else {
			//check what is the current idx
			int tempIdx;
			idx = 0;
			for (int i = 0; i < fileList.length; i++) {
				String temp = fileList[i].getName().substring(
						fileList[i].getName().indexOf("_", fileName.length()-1) + 1,
						fileList[i].getName().indexOf("."));
				tempIdx = Integer.valueOf(temp).intValue();
				if (idx <= tempIdx) {
					idx = tempIdx + 1;
				}
			}

		}
		fullFileName = filePath + File.separator + fileName + "_" + idx + "."
				+ fileExtension;
	}

	public static synchronized FileSaver getInstance(String filePath,
			String fileName, String fileExtension) {
		if (instance == null || startSave == true)
			instance = new FileSaver(filePath, fileName, fileExtension);
		return instance;
	}

	public synchronized void endSave() {
		startSave = true;
	}

	public synchronized void save(String data) throws IOException {
		// write to current file
		file = new FileWriter(fullFileName, true);
		// timestamp the saved data
		Date date = new Date();
		String sDate = date.toString() + "\t";
		file.write(sDate);
		// save data
		file.write(data+"\n");
		file.close();

	}

	public String getFullFileName() {
		return fullFileName;
	}
}

package fr.soleil.bossanova.configuration;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import org.apache.commons.io.filefilter.WildcardFileFilter;
// Bug 18567
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SequenceRepository extends Observable {
	private final Map<String, File> sequences = new HashMap<String, File>();
	private static List<String> enabledSequences = new ArrayList<String>();


            // -----------------------------------------------------------------------------------
    // CONSTRUCTOR
    // -----------------------------------------------------------------------------------
    public SequenceRepository() {
        super();
        loadRepository();
    }

	public List<String> getSequenceNames() {

               loadRepository();

		ArrayList<String> seqList = new ArrayList<String>(sequences.keySet());

		Collections.sort(seqList);
		return seqList;
	}

	public void loadRepository() {
            	// Refresh repository first
		sequences.clear();
		
		String sequenceDirectoryName = Configuration.getSequencesDirectory();
		File sequenceDirectory = null;
		try {
			sequenceDirectory = new File(sequenceDirectoryName);
		} catch (NullPointerException e) {
            // Bug 18567
			LoggerFactory.getLogger(this.getClass()).error(
					"User sequence directory is not set");
		}
		if (sequenceDirectory != null) {
			try {
				List<File> seqFiles = getFileListing(sequenceDirectory);
				if (seqFiles != null) {
					for (Iterator<File> iterator = seqFiles.iterator(); iterator
							.hasNext();) {
						File file = iterator.next();
						String name = file.getName();
						if (name.endsWith(".moml")) {
							name = name.substring(0, name.indexOf(".moml"));
							try {
								name += " (" + file.getParentFile().getName()
										+ ")";
								sequences.put(name, file);
							} catch (Exception e) {
								//Fixed Findbugs bug JC Pret
								// Added exception processing
			                    // Bug 18567
								LoggerFactory.getLogger(this.getClass()).error(
								"Sequence file and file name could not be inserted into map");
								// End JC Pret
								// SILENT CATCH
							}
						}
					}
				}
			} catch (FileNotFoundException e) {
                // Bug 18567
				LoggerFactory.getLogger(this.getClass()).error(
						"Cannot load one of the user's sequence");
			}
		}
	}

	static public List<File> getFileListing(File aStartingDir)
			throws FileNotFoundException {
		validateDirectory(aStartingDir);
		List<File> result = new ArrayList<File>();

		FileFilter fileFilter = null;
		if (!enabledSequences.isEmpty()) {
			fileFilter = new WildcardFileFilter(enabledSequences);
		}

		File[] filesAndDirs = aStartingDir.listFiles(fileFilter);

		List<File> filesDirs = Arrays.asList(filesAndDirs);
		for (File file : filesDirs) {
			result.add(file); // always add, even if directory
			if (!file.isFile()) {
				// must be a directory
				// recursive call!
				List<File> deeperList = getFileListing(file);
				result.addAll(deeperList);
			}

		}
		Collections.sort(result);
		return result;
	}

	/**
	 * Directory is valid if it exists, does not represent a file, and can be
	 * read.
	 */
	static private void validateDirectory(File aDirectory)
			throws FileNotFoundException {
		if (aDirectory == null) {
			throw new IllegalArgumentException("Directory should not be null.");
		}
		if (!aDirectory.exists()) {
			throw new FileNotFoundException("Directory does not exist: "
					+ aDirectory);
		}
		if (!aDirectory.isDirectory()) {
			throw new IllegalArgumentException("Is not a directory: "
					+ aDirectory);
		}
		if (!aDirectory.canRead()) {
			throw new IllegalArgumentException("Directory cannot be read: "
					+ aDirectory);
		}
	}

	public File getSequenceForName(String name) {
		return sequences.get(name);
	}

	// Fixed Findbugs alert JC Pret Jan 2011
	// Split setEnabledElements in two methods: a static one dealing with static data update and a non-static
	// one for the rest of the processing
	/**
	public void setEnabledElements(List<String> sequences) {
		enabledSequences = sequences;
		setChanged();
		notifyObservers();
	}
	***/
	
	   public static void setEnabledElements(List<String> sequences) {
	        enabledSequences = sequences;
	   }
	   
	   public void notifyEnabledElementsChanged()
	   {
	        setChanged();
	        notifyObservers();
	   }
	   // End Fixed Findbugs alert

}
/**
 *
 */
package fr.soleil.bossanova.controller;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.Action;

// Bug 18567
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.enums.EnumConverter;

import fr.soleil.bossanova.gui.ScreenManager;
import fr.soleil.bossanova.model.Batch;
import fr.soleil.bossanova.model.DirectorType;

/**
 * @author VIGUIER
 * 
 */
public class BatchManager {

	private static File currentBatchFile = null;
	private final static XStream xmlStreamer = new XStream();
	static {
		xmlStreamer.registerConverter(new EnumConverter());
	}
	private static BossaNovaSequencerImpl sequencer;

	private BatchManager() {
		// DO NOT USE
	}

	public static void setSequencer(BossaNovaSequencerImpl sequencerToLink) {
		sequencer = sequencerToLink;
	}

	public static Batch saveBatchAs(File destination) throws IOException {	    
		currentBatchFile = destination;
		Batch result = sequencer.getBatch();
		result.setAllConfiguredParameters();
		FileWriter writer = null;
		try {
			writer = new FileWriter(destination);
			xmlStreamer.toXML(result, writer);
	        // Bug 17628
			// Setting saved batch indicator
			BossaNovaSequencerImpl.setCurrentBatchSaved(true);

		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (Exception e) {
                    // Bug 17628
					LoggerFactory.getLogger("BatchManager").error(
							"Cannot save Batch !", e);

				}
			}
		}
		return result;
	}

	public static void loadBatch(File file) throws IOException {
		currentBatchFile = file;
		FileReader reader = null;
		try {
			reader = new FileReader(file);
			Batch batch = (Batch) xmlStreamer.fromXML(reader);
			sequencer.setBatch(batch);
			// DBA Need to create director when loading a batch
			sequencer.checkBatchSteps();
//			sequencer.createBatchDirector();
			BossaNovaSequencerImpl.setCurrentBatchSaved(true);
		} catch (IOException ioe) {
            // Bug 18567
			LoggerFactory.getLogger("BatchManager").error(ioe.getMessage(), ioe);
			throw ioe;
		} catch (Exception e) {
            // Bug 18567
			LoggerFactory.getLogger("BatchManager").error(e.getMessage(), e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (Exception e) {
                    // Bug 18567
					LoggerFactory.getLogger("BatchManager").error(
							"Cannot load Batch !", e);
				}
			}
		}
	}

	public static Batch readBatchFile(File file) throws IOException {
		FileReader reader = null;

		try {
			reader = new FileReader(file);
			Batch batch = (Batch) xmlStreamer.fromXML(reader);
			return batch;
		} catch (IOException e) {
            // Bug 18567
			LoggerFactory.getLogger("BatchManager").error(e.getMessage(), e);
			throw e;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (Exception e) {
                    // Bug 18567
					LoggerFactory.getLogger("BatchManager").error(
							"Cannot read Batch !", e);
				}
			}
		}
	}

	public static void createNewBatch() {
		currentBatchFile = null;
		sequencer.cleanAll();
		Action addStepAction = ScreenManager.getAddStepAction(sequencer);
		if (addStepAction != null) {
			addStepAction.actionPerformed(null);
		}
	}

	public static void saveBatch() throws IOException {
		if (currentBatchFile != null) {
			saveBatchAs(currentBatchFile);
		}
	}

	public static String getCurrentBatchFileName() {
		String result = "";
		if (currentBatchFile != null) {
			result = currentBatchFile.getName();
		}
		return result;
	}

	public static BossaNovaSequencerImpl getSequencer() {
		return sequencer;
	}

	public static File getCurrentBatchFile() {
		return currentBatchFile;
	}

	public static DirectorType[] getDirectorTypes() {
		return DirectorType.values();
	}
}

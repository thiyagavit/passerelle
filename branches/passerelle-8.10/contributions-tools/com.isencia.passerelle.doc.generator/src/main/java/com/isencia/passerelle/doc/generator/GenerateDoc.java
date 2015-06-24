package com.isencia.passerelle.doc.generator;

import java.io.IOException;
import java.util.StringTokenizer;
import com.isencia.passerelle.actor.general.Const;
import com.sun.tools.javadoc.Main;
import doc.doclets.PtDoclet;

public class GenerateDoc {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String cmd = "javadoc "
//				+"-classpath \""
//				+ StringUtilities.getProperty("java.class.path")
//				+ ";./lib/tools.jar\""
				+ " -d doc/codeDoc "
				+ "-doclet "+PtDoclet.class.getName()
				+ " -docletpath E:/iSencia/Soleil/workspaces/workspace-passerelle-v4/com.isencia.passerelle.doc.generator/target/classes "
				+ " -sourcepath E:/iSencia/Soleil/workspaces/workspace-passerelle-v4/com.isencia.passerelle.actor/src "
//				;
				+Const.class.getPackage().getName();
		
		System.out.println("Executing cmd\n"+cmd);
		StringTokenizer st = new StringTokenizer(cmd);
		String[] cmdarray = new String[st.countTokens()];
	 	for (int i = 0; st.hasMoreTokens(); i++)
		    cmdarray[i] = st.nextToken();
	 	Main.execute(cmdarray);
	 	
//		Runtime runtime = Runtime.getRuntime();
//		Process _process = runtime.exec(cmd);
//
//        // Set up a Thread to read in any error messages
//        _StreamReaderThread errorGobbler = new _StreamReaderThread(
//                _process.getErrorStream());
//
//        // Set up a Thread to read in any output messages
//        _StreamReaderThread outputGobbler = new _StreamReaderThread(
//                _process.getInputStream());
//
//        // Start up the Threads
//        errorGobbler.start();
//        outputGobbler.start();
	 	
}

//    private static class _StreamReaderThread extends Thread {
//        _StreamReaderThread(InputStream inputStream) {
//            _inputStream = inputStream;
//        }
//
//        public void run() {
//            try {
//                InputStreamReader inputStreamReader = new InputStreamReader(
//                        _inputStream);
//                BufferedReader bufferedReader = new BufferedReader(
//                        inputStreamReader);
//                String line = null;
//
//                while ((line = bufferedReader.readLine()) != null) {
//                    System.out.println(line);
//                }
//            } catch (IOException ioe) {
//                System.err.println("IOException: " + ioe);
//            }
//        }
//
//        // Stream to read from.
//        private InputStream _inputStream;
//
//    }
}

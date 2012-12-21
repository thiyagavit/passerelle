package fr.soleil.bossanova.gui.view.batchViewer;


import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

public class BatchLoadSaveMonitor extends JFrame {

  JProgressBar pbar;

  static final int MY_MINIMUM = 0;
  static final int MY_MAXIMUM = 100;
  static final int WIDTH = 200;
  static final int HEIGHT = 50;

  public BatchLoadSaveMonitor( String title) {
      super(title);
      pbar = new JProgressBar();
      pbar.setMinimum(MY_MINIMUM);
      pbar.setMaximum(MY_MAXIMUM);
      pbar.setStringPainted(true);
      add(pbar);
    
      setSize(WIDTH, HEIGHT);
  }

  public void updateBar(int newValue) {
    pbar.setValue(newValue);
  }
  
  public void setString( String progressString )
  {
      pbar.setString(progressString);
  }

}

package com.isencia.message.ftp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import junit.framework.TestCase;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.WindowsFakeFileSystem;
import com.isencia.message.ChannelException;
import com.isencia.message.generator.MessageTextLineGenerator;

public class FtpSenderChannelTest extends TestCase {

  FakeFtpServer ftpServer;
  FtpSenderChannel ftpSndChannel;

  protected void setUp() throws Exception {
    ftpServer = new FakeFtpServer();

    FileSystem fileSystem = new WindowsFakeFileSystem();
    fileSystem.add(new FileEntry("c:\\data\\file1.txt", "abcdef\r\n1234567890"));
    ftpServer.setFileSystem(fileSystem);

    UserAccount userAccount = new UserAccount("pol", "pingo", "c:\\data");
    ftpServer.addUserAccount(userAccount);
    ftpServer.start();

    ftpSndChannel = new FtpSenderChannel("c:\\data\\file2.txt", "localhost", "pol", "pingo", false, true, new MessageTextLineGenerator());
  }

  protected void tearDown() throws Exception {
    try {
      ftpSndChannel.close();
    } catch (Exception e) {
      // ignore; can give errors when channel was not open...
    }
    ftpServer.stop();
  }

  public void testClose() throws ChannelException {
    ftpSndChannel.open();
    ftpSndChannel.close();
    assertFalse("Channel should not be open", ftpSndChannel.isOpen());
  }

  public void testOpen() throws ChannelException {
    ftpSndChannel.open();
    assertTrue("Channel should be open", ftpSndChannel.isOpen());
  }

  public void testSendMessages() {
    try {
      ftpSndChannel.open();
      ftpSndChannel.sendMessage("hello");
      ftpSndChannel.sendMessage("world");
      ftpSndChannel.close();
    } catch (Exception e) {
      fail("Msg send failed " + e.getMessage());
    }
    try {
      assertTrue(ftpServer.getFileSystem().exists("c:\\data\\file2.txt"));
      FileEntry fileEntry = (FileEntry) ftpServer.getFileSystem().getEntry("c:\\data\\file2.txt");
      BufferedReader reader = new BufferedReader(new InputStreamReader(fileEntry.createInputStream()));
      assertEquals("hello", reader.readLine());
      assertEquals("world", reader.readLine());
      reader.close();
    } catch (Exception e) {
      fail("Assertion failed " + e.getMessage());
    }
  }

}

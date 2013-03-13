package com.isencia.passerelle.process.model.impl.factory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import com.isencia.passerelle.process.model.Attribute;
import com.isencia.passerelle.process.model.Request;
import com.isencia.passerelle.process.model.ResultBlock;
import com.isencia.passerelle.process.model.ResultItem;

public class RequestExportTask {

  private static final long serialVersionUID = 1L;
  public static final String NEWLINE = System.getProperty("line.separator");
  private File outputFile;

  public RequestExportTask(File outputFile) {
    this.outputFile = outputFile;
  }

  public void execute(Request request) throws IOException {
    Map<String, ResultBlock> rbMap = new HashMap<String, ResultBlock>();

    for (com.isencia.passerelle.process.model.Task task : request.getProcessingContext().getTasks()) {
      Collection<ResultBlock> resultBlocks = task.getResultBlocks();
      for (ResultBlock block : resultBlocks) {
        ResultBlock temp = rbMap.get(block.getType());
        if (temp != null) {
          if (temp.getCreationTS().before(block.getCreationTS())) {
            rbMap.put(block.getType(), block);
          }
        } else {
          rbMap.put(block.getType(), block);
        }
      }
    }
    HashMap<String, File> files = createFiles(rbMap.values());
    files.put("requestAttributes", writeFile("requestAttributes", buildRequestAttributes(request)));
    ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outputFile));
    try {
      for (Map.Entry<String, File> file : files.entrySet()) {
        createZipEntry(out, file.getKey() + ".properties", getBytesFromFile(file.getValue()));
      }
    } finally {
      try {
        out.close();
      } catch (IOException e) {
      }
    }
  }

  private HashMap<String, File> createFiles(Collection<ResultBlock> resultBlocks) {
    HashMap<String, File> files = new HashMap<String, File>();
    for (ResultBlock currentBlock : resultBlocks) {
      File file = null;
      try {
        file = writeFile(currentBlock.getType(), buildResultBlock(currentBlock));
      } catch (Exception e) {

      }

      if (file != null)
        files.put(currentBlock.getType(), file);
    }
    return files;
  }

  public static final String TIMEZONE = "Europe/Brussels";
  public static final String DATEFORMAT = "dd/MM/yyyy HH:mm:ss";
  public static final String CREATION_DATE_ATTRIBUTE = "bgc.dar.test.creationdate";

  public static String generateDate(Date date) {
    if (date == null) {
      return null;
    }
    try {
      TimeZone time = TimeZone.getTimeZone(TIMEZONE);
      SimpleDateFormat formatter = new SimpleDateFormat(DATEFORMAT);
      formatter.setTimeZone(time);
      return formatter.format(date);
    } catch (Exception e) {
      return null;
    }
  }

  protected String buildRequestAttributes(Request request) {
    StringBuilder builder = new StringBuilder();
    for (Attribute attribute : request.getAttributes()) {
      builder.append(attribute.getName() + "=");
      builder.append(attribute.getValue() == null ? "" : attribute.getValue());
      builder.append(NEWLINE);
    }
    builder.append(CREATION_DATE_ATTRIBUTE + "=");
    builder.append(generateDate(request.getProcessingContext().getCreationTS()));
    builder.append(NEWLINE);
    return builder.toString();
  }

  protected String buildResultBlock(ResultBlock currentBlock) {
    StringBuilder builder = new StringBuilder();
    builder.append("task.type=" + currentBlock.getTask().getType() + NEWLINE);
    builder.append("task.class=" + currentBlock.getTask().getClass().getName() + NEWLINE);
    if (currentBlock.getColour() != null) {
      builder.append("analysis=" + currentBlock.getColour() + NEWLINE);
    }
    List<ResultItem> list = new ArrayList<ResultItem>();
    list.addAll(currentBlock.getAllItems());
    Comparator<ResultItem> comparator = new Comparator<ResultItem>() {

      public int compare(ResultItem arg0, ResultItem arg1) {
        if (arg0.getName() == null) {
          return 0;
        }
        return arg0.getName().compareTo(arg1.getName());
      }
    };
    Collections.sort(list, comparator);
    for (ResultItem item : list) {
      builder.append(item.getName().replace(" ", "\\ ").replace("=", "\\=") + "=" + getStringValue(item) + NEWLINE);
      if (item.getColour() != null) {
        builder.append(item.getName().replace(" ", "\\ ").replace("=", "\\=") + "|analysis=" + item.getColour() + NEWLINE);
      }
    }
    return builder.toString();
  }

  protected File writeFile(String fileName, String contents) {
    File file;
    FileWriter writer = null;

    try {
      try {
        file = File.createTempFile(fileName, ".properties");

        writer = new FileWriter(file);
        writer.write(contents);
      } finally {
        if (writer != null)
          try {
            writer.close();
          } catch (IOException ioe) {
          }
      }
      return file;
    } catch (IOException e) {
      return null;
    }
  }

  protected String getItemValue(ResultItem item) {
    if (item == null)
      return null;
    return item.getValueAsString();
  }

  protected String getStringValue(ResultItem item) {
    if (item == null || item.getValueAsString() == null || item.getValueAsString().trim().length() == 0)
      return "";
    return item.getValueAsString();
  }

  private String increaseInt(String value) {
    try {
      return String.valueOf(Integer.parseInt(value) + 1);
    } catch (Exception e) {
      return "0";
    }
  }

  private static void createZipEntry(ZipOutputStream out, String fileName, byte[] bytes) throws IOException {

    out.putNextEntry(new ZipEntry(fileName));
    out.write(bytes);
  }

  public static byte[] getBytesFromFile(File file) throws IOException {
    InputStream is = new FileInputStream(file);

    // Get the size of the file
    long length = file.length();

    // You cannot create an array using a long type.
    // It needs to be an int type.
    // Before converting to an int type, check
    // to ensure that file is not larger than Integer.MAX_VALUE.
    if (length > Integer.MAX_VALUE) {
      // File is too large
    }

    // Create the byte array to hold the data
    byte[] bytes = new byte[(int) length];

    // Read in the bytes
    int offset = 0;
    int numRead = 0;
    while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
      offset += numRead;
    }

    // Ensure all the bytes have been read in
    if (offset < bytes.length) {
      throw new IOException("Could not completely read file " + file.getName());
    }

    // Close the input stream and return bytes
    is.close();
    return bytes;
  }
}

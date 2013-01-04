/**
 * 
 */
package com.isencia.passerelle.project.repository.impl.filesystem;

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.compiler.PackageBuilderConfiguration;
import org.drools.io.ResourceFactory;

import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;
import com.isencia.passerelle.project.repository.api.MetaData;
import com.isencia.passerelle.project.repository.api.Project;

/**
 * The filesystem-based project structure follows the structure used by project asset import/export. I.e. a subfolder
 * "packages" contains further subfolders per KB package. A subfolder "sequences" contains all sequences of the project.
 * 
 * @author delerw
 * 
 */
public class FileSystemBasedProject implements Project {

  public final static String PACKAGES_SUBFOLDER = "packages";
  public final static String SEQUENCES_SUBFOLDER = "sequences";

  private File kbPackagesFolder;
  private String projectCode;
  private File sequencesFolder;

  public FileSystemBasedProject(File repoRootFolder, String projectCode) {
    this.kbPackagesFolder = new File(repoRootFolder, projectCode + "/" + PACKAGES_SUBFOLDER);
    this.sequencesFolder = new File(repoRootFolder, projectCode + "/" + SEQUENCES_SUBFOLDER);
    this.projectCode = projectCode;
  }

  /**
   * 
   * @return the project's code
   */
  public String getCode() {
    return projectCode;
  }

  public String[] getAllKnowledgeBaseCodes() {
    List<String> results = new ArrayList<String>();
    File[] files = kbPackagesFolder.listFiles((FileFilter) null);
    for (File file : files) {
      if (file.isDirectory()) {
        results.add(file.getName());
      }
    }
    return results.toArray(new String[results.size()]);
  }

  public KnowledgeBase getKnowledgeBase(String kbCode) throws Exception {
    File kbFolder = new File(kbPackagesFolder, kbCode);
    KnowledgeBase result = null;
    if (kbFolder.exists() && kbFolder.isDirectory()) {
      Properties packageBuilderProps = new Properties();
      packageBuilderProps.setProperty("drools.dialect.java.compiler", "JANINO");
      packageBuilderProps.setProperty("drools.dialect.default", "java");
      PackageBuilderConfiguration packageBuilderCfg = new PackageBuilderConfiguration(packageBuilderProps);

      KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder(packageBuilderCfg);
      File[] rulesFiles = kbFolder.listFiles((FileFilter) new SuffixFileFilter(new String[] { "drl" }));
      for (File rulesFile : rulesFiles) {
        try {
          kbuilder.add(ResourceFactory.newUrlResource(rulesFile.toURL()), ResourceType.DRL);
        } catch (MalformedURLException e) {
          throw new Exception("Url file not ok : " + rulesFile.toURL(), e);
        }
      }
      File[] rulesFlowFiles = kbFolder.listFiles((FileFilter) new SuffixFileFilter(new String[] { "rf" }));
      for (File rulesFlowFile : rulesFlowFiles) {
        try {
          kbuilder.add(ResourceFactory.newUrlResource(rulesFlowFile.toURL()), ResourceType.DRF);
        } catch (MalformedURLException e) {
          throw new Exception("Url file not ok : " + rulesFlowFile.toURL(), e);
        }
      }

      if (kbuilder.hasErrors()) {
        StringBuffer errorMessage = new StringBuffer("Compilation errors during building package: ");
        for (KnowledgeBuilderError error : kbuilder.getErrors()) {
          errorMessage.append(error.getMessage());
          errorMessage.append(" ");
        }
        throw new Exception(errorMessage.toString());

      } else {
        KnowledgeBaseConfiguration kbConfig = KnowledgeBaseFactory.newKnowledgeBaseConfiguration(null);
        result = KnowledgeBaseFactory.newKnowledgeBase(kbConfig);
        result.addKnowledgePackages(kbuilder.getKnowledgePackages());
      }
    }
    return result;
  }

  public Long[] getAllFlowIds() {
    return new Long[] {};
  }

  public String[] getAllFlows() {
    List<String> results = new ArrayList<String>();
    File[] files = sequencesFolder.listFiles((FileFilter) new SuffixFileFilter(new String[] { "moml", "xml" }));
    for (File file : files) {
      try {
        results.add(FlowManager.readMoml(file.toURL()).getName());
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    return results.toArray(new String[results.size()]);
  }

  public Flow getFlow(String arg0) {
    File flowFile = new File(sequencesFolder, arg0);
    Flow result = null;
    if (flowFile.exists() && flowFile.isFile()) {
      try {
        result = FlowManager.readMoml(flowFile.toURL());
      } catch (Exception e) {
      }
    }
    return result;
  }

  public Long getFlowId(String arg0) {

    return 0L;
  }

  public MetaData getMetaData() {
    return new MetaData("Project", null, null, projectCode, null, null);

  }

  public MetaData getFlowMetaData(String flowCode) {
    return new MetaData("Flow", null, null, flowCode, null, null);
  }

  public Long getId() {

    return 0L;
  }
}

/**
 * 
 */
package com.isencia.passerelle.project.repository.impl.filesystem;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;

import com.isencia.passerelle.core.IEventLog;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.project.repository.api.MetaData;
import com.isencia.passerelle.project.repository.api.Project;
import com.isencia.passerelle.project.repository.api.RepositoryService;

/**
 * @author delerw
 * 
 */
public class FileSystemBasedRepositoryService implements RepositoryService {

  public String getDefaultDsl(String name) {
    return null;
  }

  public String getDefaultDslr(String name) {
    return null;
  }

  public boolean existNewPackage(String packageCode) {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean existNewProject(String projectCode) {
    // TODO Auto-generated method stub
    return false;
  }

  private File rootFolder;

  /**
   * Instantiates a service instance with the default root folder (C:/temp/passerelle-repository)
   */
  public FileSystemBasedRepositoryService() {
    this(new File("C:/temp/passerelle-repository"));
  }

  /**
   * Instantiates a service instance with the given root folder
   * 
   * @param rootFolder
   */
  public FileSystemBasedRepositoryService(File rootFolder) {
    this.rootFolder = rootFolder;
  }

  public String[] getAllProjectCodes() {
    List<String> results = new ArrayList<String>();
    File[] files = rootFolder.listFiles((FileFilter) null);
    for (File file : files) {
      if (file.isDirectory()) {
        results.add(file.getName());
      }
    }
    return results.toArray(new String[results.size()]);
  }

  public Flow getFlow(String sequenceCode) {
    Flow result = null;
    File[] projectFolders = rootFolder.listFiles((FileFilter) null);
    for (File projectFolder : projectFolders) {
      if (projectFolder.isDirectory()) {
        Project p = getProject(projectFolder.getName());
        result = p.getFlow(sequenceCode);
        if (result != null) {
          break;
        }
      }
    }
    return result;
  }

  public Project getProject(String projectCode) {
    File projectFolder = new File(rootFolder, projectCode);
    Project result = null;
    if (projectFolder.exists() && projectFolder.isDirectory()) {
      result = new FileSystemBasedProject(rootFolder, projectCode);
    }
    return result;
  }

  /**
   * We assume that the 1st level of folders are all Projects, and they all contain just 1 level of packages.
   */
  public String[] getAllPackageCodes() {
    List<String> results = new ArrayList<String>();
    File[] projectFolders = rootFolder.listFiles((FileFilter) null);
    for (File projectFolder : projectFolders) {
      if (projectFolder.isDirectory()) {
        Project p = getProject(projectFolder.getName());
        String[] projectKBCodes = p.getAllKnowledgeBaseCodes();
        for (String kbCode : projectKBCodes) {
          results.add(kbCode);
        }
      }
    }
    return results.toArray(new String[results.size()]);
  }

  /**
   * Performs a very inefficient traversal of the Projects to find the right KB...
   * 
   */
  public KnowledgeBase getKnowledgeBase(String packageCode) throws Exception {
    KnowledgeBase result = null;
    File[] projectFolders = rootFolder.listFiles((FileFilter) null);
    for (File projectFolder : projectFolders) {
      if (projectFolder.isDirectory()) {
        Project p = getProject(projectFolder.getName());
        result = (KnowledgeBase)p.getKnowledgeBase(packageCode);
        if (result != null)
          break;
      }
    }
    return result;
  }

  public Flow getSubmodel(String arg0) {
    return getFlow(arg0);
  }

  public String getFlowCode(Long id) {

    return null;
  }

  public Project getProject(Long projectId) {

    return null;
  }

  public MetaData getFlowMetaData(String flowCode) {
    return new MetaData("Flow", null, null, flowCode, null, null);
  }

  KnowledgeBaseConfiguration kbConfig;

  public KnowledgeBaseConfiguration getKnowledgeBaseConfiguration() {
    if (kbConfig == null) {
      kbConfig = KnowledgeBaseFactory.newKnowledgeBaseConfiguration(null, KnowledgeBaseFactory.class.getClassLoader());
    }
    return kbConfig;
  }

  public void commitFlow(Flow flow, String comment) throws Exception {
  }

  public String[] getAllSubmodels() {

    return null;
  }

  public List<IEventLog> getLogs(String name, Integer maxResult) {
    return new ArrayList<IEventLog>();
  }

}

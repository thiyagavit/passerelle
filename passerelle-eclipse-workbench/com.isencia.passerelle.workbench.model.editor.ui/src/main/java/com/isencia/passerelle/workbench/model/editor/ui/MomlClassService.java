package com.isencia.passerelle.workbench.model.editor.ui;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.isencia.passerelle.editor.common.model.IMomlClassService;
import com.isencia.passerelle.project.repository.api.RepositoryService;

public class MomlClassService implements IMomlClassService {

  public MomlClassService() {

  }

  @Override
  public List<String> getAllActorClasses() {
    RepositoryService repoService = Activator.getDefault().getRepositoryService();
    if (repoService == null) {
      return Collections.EMPTY_LIST;
    }
    return Arrays.asList(repoService.getAllSubmodels());
  }

}

package fr.soleil.bossanova.configuration;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.prefs.Preferences;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;
import fr.soleil.bossanova.bossaNovaData.BossaNovaData;

public class ActorRepository extends Observable {
  
  private final static Logger LOGGER = LoggerFactory.getLogger(ActorRepository.class);

    private Preferences preferences;
    private Map<String, Class<? extends TypedAtomicActor>> actors = new HashMap<String, Class<? extends TypedAtomicActor>>();
    private Map<String, TypedAtomicActor> instances = new HashMap<String, TypedAtomicActor>();
    private List<String> enabledActors = new ArrayList<String>();

    // -----------------------------------------------------------------------------------
    // CONSTRUCTOR
    // -----------------------------------------------------------------------------------
    public ActorRepository() {
        super();
        preferences = Preferences.userNodeForPackage(BossaNovaData.getSingleton().getApplication().getClass());
        loadRepository();
    }

    private void loadRepository() {
        try {
            List<File> actorFiles = null;
            actors.clear();
            enabledActors.clear();

            String commonActorsXMLDirectory = Configuration.getCommonActorsDirectory();
            String specificActorsXMLDirectory = Configuration.getSpecificActorsDirectory();
            actorFiles = getActorFileList(commonActorsXMLDirectory);
            actorFiles.addAll(getActorFileList(specificActorsXMLDirectory));      

            for (Iterator<File> iterator = actorFiles.iterator(); iterator.hasNext();) {
                File xmlFile = (File) iterator.next();
                MoMLParser parser = new MoMLParser();
                MoMLParser.purgeModelRecord(xmlFile.toURL());
                NamedObj namedObject = null;
                try {
                    namedObject = (NamedObj) parser.parse(null, xmlFile.toURL());
                } catch (Exception e) {
                    // Bug 18567
                    LoggerFactory.getLogger(this.getClass()).error(
                            "Error while loading Actor in file " + xmlFile.toURL(), e);
                }
                if (namedObject != null) {
                    for (Iterator<NamedObj> iterator2 = namedObject.containedObjectsIterator(); iterator2.hasNext();) {
                        NamedObj obj = (NamedObj) iterator2.next();
                        String name = obj.getName();
                        List classes = ClassUtils.getAllSuperclasses(obj.getClass());
                        if (classes.contains(TypedAtomicActor.class)) {
                            Class<? extends TypedAtomicActor> actorClass = ((TypedAtomicActor) obj).getClass();
                            actors.put(name, actorClass);
                            instances.put(name, (TypedAtomicActor) obj);
                            if (!preferences.getBoolean(name, false)) {
                                enabledActors.add(name);
                            }
                        }
                    }
                }
            }
            Collections.sort(enabledActors);
//            System.out.println("==============> EnabledActors = " + enabledActors.toString());
        } catch (Exception e) {
            e.printStackTrace();
            // Bug 18567
            LoggerFactory.getLogger(this.getClass()).error("Error while loading Actor repository");
        }
    }

    private List<File> getActorFileList(String actorsXMLDirectory) {
        List<File> actorFiles = new ArrayList<File>();
        if (!StringUtils.isEmpty(actorsXMLDirectory)) {
            try {
                File commonActorsXMLDir = new File(actorsXMLDirectory);
                if (commonActorsXMLDir != null) {
                    File[] xmlFiles = commonActorsXMLDir.listFiles(new FilenameFilter() {

                        @Override
                        public boolean accept(File dir, String name) {
                            return name.endsWith(".xml");
                        }
                    });
                    if (xmlFiles != null) {
                        for (int i = 0; i < xmlFiles.length; i++) {
                            File file = xmlFiles[i];
                            actorFiles.add(file);
                        }
                    }

                }
            } catch (Exception e) {
                LoggerFactory.getLogger(this.getClass()).error(
                        "Error loading actors from directory " + actorsXMLDirectory, e);
            }
        }
        return actorFiles;
    }

    public List<String> getEnabledActorNames() {
        return enabledActors;
    }

    public List<String> getAllActorNames() {
        List<String> result = null;
        result = new ArrayList<String>(actors.keySet());
        return result;
    }

    public Class<? extends TypedAtomicActor> getActorClassForName(String name) {
        return actors.get(name);
    }

    public TypedAtomicActor getActorForName(String name) {
        return instances.get(name);
    }

    public boolean isElementEnabled(String elementName) {
        boolean result = false;
        result = enabledActors.contains(elementName);
        return result;
    }

    public void addEnabledElement(String elementName) {
        if (enabledActors.contains(elementName) == false) {
            enabledActors.add(elementName);
        }
        preferences.remove(elementName);
        setChanged();
        notifyObservers();
    }

    public void removeEnabledElement(String elementName) {
        enabledActors.remove(elementName);
        preferences.putBoolean(elementName, true);
        setChanged();
        notifyObservers();
    }
}

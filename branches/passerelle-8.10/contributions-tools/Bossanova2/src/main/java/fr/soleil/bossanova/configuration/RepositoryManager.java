package fr.soleil.bossanova.configuration;

import java.util.ArrayList;
import java.util.List;
//Fixed FindBugs bug JC Pret
//For Serializable interface implementation
import java.io.Serializable;

import fr.soleil.bossanova.model.StepType;
import java.util.Collections;
import java.util.Comparator;

public final class RepositoryManager {

    protected static MyComparator comparator = new MyComparator();
    private static ActorRepository actorRepository;
    private static SequenceRepository sequenceRepository;

    static {
        actorRepository = new ActorRepository();
        sequenceRepository = new SequenceRepository();
    }

    private RepositoryManager() {
        // DO NOT USE
    }

    public static ActorRepository getActorRepository() {
        return actorRepository;
    }

    public static SequenceRepository getSequenceRepository() {
        return sequenceRepository;
    }

    public static StepType getStepTypeFor(String elementName) {
        StepType result = null;
        if (actorRepository.getActorClassForName(elementName) != null) {
            result = StepType.ACTOR;
        }
        if (sequenceRepository.getSequenceForName(elementName) != null) {
            result = StepType.SEQUENCE;
        }
        return result;
    }

    public static List<String> getElementNames() {
        List<String> result = new ArrayList<String>();
        List<String> sequences = sequenceRepository.getSequenceNames();
        Collections.sort(sequences, comparator);
        List<String> actors = actorRepository.getEnabledActorNames();
        result.addAll(sequences);
        result.addAll(actors);
        return result;
    }
}


// Fixed FindBugs bug JC Pret
// Added Serializable interface
class MyComparator implements Comparator<String>, Serializable {

    @Override
    public int compare(String strA, String strB) {
        return strA.compareToIgnoreCase(strB);
    }
}

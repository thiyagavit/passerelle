package fr.soleil.passerelle;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import junit.framework.Assert;

import org.junit.rules.ExternalResource;

import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;

import fr.soleil.passerelle.domain.BasicDirector;

public class MomlRule extends ExternalResource {

    public FlowManager flowMgr;
    public Flow topLevel;
    public Reader in;
    public String sequenceName;

    public MomlRule(final String sequenceName) {
        this.sequenceName = sequenceName;
    }

    @Override
    protected void before() throws Throwable {
        in = new InputStreamReader(getClass().getResourceAsStream(sequenceName));
        flowMgr = new FlowManager();
        topLevel = FlowManager.readMoml(in);

        final BasicDirector dir = new BasicDirector(topLevel, "Dir");
        topLevel.setDirector(dir);
    }

    @Override
    protected void after() {
        if (in != null) {
            try {
                in.close();
            }
            catch (final IOException e) {
                Assert.fail("cant close file");
            }
        }
    }

}

Getting Passerelle to work with CDMA
=====================================

- install Nexus and make sure PATH and CLASSPATH are set (for a windows env)

- install HDFView to be able to visually check NXS (HDF) files

- make sure to launch your eclipse after the above Nexus installation has finished.
Otherwise you risk that the Nexis DLLs will not be found in your test launches from eclipse.

- ensure following bundles (or compatible more recent versions) are in your target platform 
(or classpath ico plain Java app) :
-- nu.xom_1.1.0.jar
-- org.jdom2_2.0.4.jar
-- org.nexusformat_1.0.0.jar
-- org.cdma.core_3.x.x.x.jar
-- org.cdma.engine.nexus_3.x.x.x.jar
-- org.cdma.plugin.soleil_3.x.x.x.jar
-- org.cdma.utilities_3.x.x.x.jar

Remark that if you have the org.cdma.... projects in your workspace i.o. as jars, a non-OSGi run may not work. The org.cdma.plugin.soleil should be packaged as a jar it seems, for its usage of Java ServiceLoader to work. (cfr META-INF/services/...)

Checkout fr.soleil.passerelle.cdma.actor & fr.soleil.passerelle.cdma.actor.test.

In the test project there is a models folder with two sample models to open a .nxs file and :
- read one item based on its physical location in the file
- find an images/spectrum array item based on a logical name, iterate over slices according to a configurable rank, modify values and dump the result to System.out.

You may need to change the URIs to the nxs file, depending on your setup.
The nxs files are in fr.soleil.passerelle.cdma.actor.test/CDMA_samples.
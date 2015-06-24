/* Javadoc Doclet that generates PtDoc XML

 Copyright (c) 2006-2007 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */

package doc.doclets;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import ptolemy.util.StringUtilities;
import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.SeeTag;
import com.sun.javadoc.Tag;

/**
 * Generate PtDoc output. See ptolemy/vergil/basic/DocML_1.dtd for the dtd.
 * <p>
 * If javadoc is called with -d <i>directoryName</i>, then documentation will be generated in <i>directoryName</i>. If the KEPLER property is set, then for a
 * class named <code>foo.bar.Baz</code>, the generated file is named <code>Baz.doc.xml</code>. If the KEPLER property is not set, then the generated file is
 * named <code>foo/bar/Baz.xml</code>.
 * <p>
 * This doclet writes the names of all the classes for which documentation was generated in a file called allNamedObjs.txt
 * 
 * @author Christopher Brooks, Edward A. Lee, Contributors: Nandita Mangal, Ian Brown
 * @version $Id: PtDoclet.java,v 1.40 2008/01/22 06:55:14 cxh Exp $
 * @since Ptolemy II 5.2
 */
public class WorkbenchHelpDoclet {
  private static VelocityEngine velocity = new VelocityEngine();

  /**
   * Given a command line option, return the number of command line arguments needed by that option.
   * 
   * @param option The command line option
   * @return If the option is "-d", return 2; otherwise, return 0.
   */
  public static int optionLength(final String option) {
    if (option.equals("-d")) {
      return 2;
    }
    return 0;
  }

  /**
   * Process the java files and generate PtDoc XML. Only classes that extend ptolemy.actor.TypedAtomicActor are processed, all other classes are ignored.
   * 
   * @param root The root of the java doc tree.
   * @return Always return true;
   * @exception IOException If there is a problem writing the documentation.
   * @exception ClassNotFoundException If there is a problem finding the class of one of the fields.
   */
  public static boolean start(final RootDoc root) throws IOException, ClassNotFoundException {
    System.out.println("Isencia version of PtDoc, with Kepler extensions");

    Context velocityContext = new VelocityContext();

    final ClassLoader classLoader = WorkbenchHelpDoclet.class.getClassLoader();
    System.out.println("Using classloader " + classLoader.getClass());
    if (URLClassLoader.class.isInstance(classLoader)) {
      final URL[] classPathURLs = ((URLClassLoader) classLoader).getURLs();
      for (final URL url : classPathURLs) {
        System.out.println("\n classPathURL " + url);
      }
    }

    _outputDirectory = _getOutputDirectory(root.options());
    final File outputDirectoryFile = new File(_outputDirectory);
    if (!outputDirectoryFile.isDirectory()) {
      if (!outputDirectoryFile.mkdirs()) {
        throw new IOException("Failed to create \"" + _outputDirectory + "\"");
      }
    }

    final ClassDoc baseActorDoc = root.classNamed("com.isencia.passerelle.actor.Actor");

    final ClassDoc[] classes = root.classes();
    final Collection<ClassDoc> actorClasses = filterClasses(baseActorDoc, classes);

    generateActorHelpFiles(root, actorClasses);

    generateActorHelpIndex(root, actorClasses);
    generateActorHelpTOC(root, actorClasses);
    generateActorHelpHtmlTOC(root, actorClasses);

    return true;
  }

  private static Collection<ClassDoc> filterClasses(ClassDoc baseClassDoc, ClassDoc[] classes) {
    List<ClassDoc> filteredClasses = new ArrayList<ClassDoc>();

    for (ClassDoc classDoc : classes) {
      if (!classDoc.isAbstract() && classDoc.subclassOf(baseClassDoc)) {
        filteredClasses.add(classDoc);
      }
    }
    return filteredClasses;
  }

  protected static void generateActorHelpFiles(final RootDoc root, final Collection<ClassDoc> classes) throws IOException, ClassNotFoundException {
    // final Class typedIOPortClass = Class.forName("ptolemy.actor.TypedIOPort");
    // final Class stringAttributeClass = Class.forName("ptolemy.kernel.util.StringAttribute");

    // InputStream templateAsStream = WorkbenchHelpDoclet.class.getResourceAsStream("/ActorHelpTemplate.html");
    // System.out.println("found stream "+templateAsStream);
    // Reader templateAsReader = new InputStreamReader(templateAsStream);
    String actorTemplate = FileUtils.readFileToString(new File(
        "C:\\data\\development\\workspaces\\isencia-workspaces\\passerelle-V7\\com.isencia.passerelle.doc.generator\\src\\main\\java\\ActorHelpTemplate.vm"));
    String attrTemplate = FileUtils.readFileToString(new File(
        "C:\\data\\development\\workspaces\\isencia-workspaces\\passerelle-V7\\com.isencia.passerelle.doc.generator\\src\\main\\java\\ActorAttributesHelpTemplate.vm"));
    // String template = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">" + "<html>" + "<head>"
    // + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">" + "<title>$actorName</title>" + "</head>"
    // + "<BODY LANG=\"en-US\" DIR=\"LTR\">" + "<H2>$actorName</H2>" + "<H4>$actorClassName</H4>" + "#foreach( $key in $allDocElements.keySet() )"
    // + "    <li>$key : $allDocElements.get($key)</li>" + "#end" + "</BODY></HTML>";
    for (final ClassDoc classe : classes) {
      final String className = classe.toString();
      try {
        _writeDoc(className, _generateClassLevelDocumentation(classe, actorTemplate));
        _writeAttrDoc(className, _generateFieldDocumentation(classe, attrTemplate));
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      // + _generateFieldDocumentation(classe, typedIOPortClass, "port")
      // + _generateFieldDocumentation(classe, parameterClass, "property") + _generateFieldDocumentation(classe, stringAttributeClass, "property")
      // + "</doc>\n");
      // templateAsReader.close();
    }
  }

  protected static void generateActorHelpIndex(final RootDoc root, final Collection<ClassDoc> classes) throws IOException, ClassNotFoundException {
    String template = FileUtils.readFileToString(new File(
        "C:\\data\\development\\workspaces\\isencia-workspaces\\passerelle-V7\\com.isencia.passerelle.doc.generator\\src\\main\\java\\indexTemplate.vm"));
    Map<String, String> actorClassNameMap = new TreeMap<String, String>();
    for (ClassDoc classDoc : classes) {
      actorClassNameMap.put(_generateShortClassName(classDoc), _generateClassName(classDoc));
    }
    Writer resultWriter = new StringWriter();
    try {
      Context velocityContext = new VelocityContext();
      velocityContext.put("allActorClasses", actorClassNameMap);

      velocity.evaluate(velocityContext, resultWriter, "index template", template);
      String res = resultWriter.toString();
      _writeIndex(res);
    } finally {
      try {
        resultWriter.close();
      } catch (Exception e) {
      }
    }
  }

  protected static void generateActorHelpTOC(final RootDoc root, final Collection<ClassDoc> classes) throws IOException, ClassNotFoundException {
    String template = FileUtils.readFileToString(new File(
        "C:\\data\\development\\workspaces\\isencia-workspaces\\passerelle-V7\\com.isencia.passerelle.doc.generator\\src\\main\\java\\tocTemplate.vm"));
    Map<String, String> actorClassNameMap = new TreeMap<String, String>();
    for (ClassDoc classDoc : classes) {
      actorClassNameMap.put(_generateShortClassName(classDoc), _generateClassName(classDoc));
    }
    Writer resultWriter = new StringWriter();
    try {
      Context velocityContext = new VelocityContext();
      velocityContext.put("allActorClasses", actorClassNameMap);

      velocity.evaluate(velocityContext, resultWriter, "TOC template", template);
      String res = resultWriter.toString();
      _writeTOC(res);
    } finally {
      try {
        resultWriter.close();
      } catch (Exception e) {
      }
    }
  }

  protected static void generateActorHelpHtmlTOC(final RootDoc root, final Collection<ClassDoc> classes) throws IOException, ClassNotFoundException {
    String template = FileUtils.readFileToString(new File(
        "C:\\data\\development\\workspaces\\isencia-workspaces\\passerelle-V7\\com.isencia.passerelle.doc.generator\\src\\main\\java\\htmlTocTemplate.vm"));
    Map<String, String> actorClassNameMap = new TreeMap<String, String>();
    for (ClassDoc classDoc : classes) {
      actorClassNameMap.put(_generateShortClassName(classDoc), _generateClassName(classDoc));
    }
    Writer resultWriter = new StringWriter();
    try {
      Context velocityContext = new VelocityContext();
      velocityContext.put("allActorClasses", actorClassNameMap);

      velocity.evaluate(velocityContext, resultWriter, "TOC template", template);
      String res = resultWriter.toString();
      _writeHtmlTOC(res);
    } finally {
      try {
        resultWriter.close();
      } catch (Exception e) {
      }
    }
  }

  // /////////////////////////////////////////////////////////////////
  // // private methods ////

  private static String _generateClassLevelDocumentation(ClassDoc classDoc, String actorHelpTemplate) throws Exception {
    Writer resultWriter = new StringWriter();
    try {
      Context velocityContext = new VelocityContext();
      velocityContext.put("actorName", _generateShortClassName(classDoc));
      velocityContext.put("actorClassName", _generateClassName(classDoc));
      velocityContext.put("allDocElements", _generateClassLevelDocumentationElements(classDoc));
      velocityContext.put("actorClassDoc", _inlineTagCommentText(classDoc));
      velocityContext.put("allActorAttributes", _generateActorAttributeElements(classDoc, Class.forName("ptolemy.data.expr.Parameter")));

      velocity.evaluate(velocityContext, resultWriter, "actor help template", actorHelpTemplate);
      return resultWriter.toString();
    } finally {
      try {
        resultWriter.close();
      } catch (Exception e) {
      }
    }
  }

  /**
   * Process customTags and return text that contains links to the javadoc output.
   * 
   * @param programElementDoc The class for which we are generating documentation.
   */
  private static String _customTagCommentText(final ProgramElementDoc programElementDoc) {

    // Process the comment as an array of tags. Doc.commentText()
    // should do this, but it does not.
    String documentation = "";

    final Tag tag[] = programElementDoc.tags("UserLevelDocumentation");
    final StringBuffer textTag = new StringBuffer();
    for (final Tag element : tag) {
      textTag.append(element.text());
    }

    if (textTag.toString().length() > 0) {
      documentation = "<UserLevelDocumentation>" + StringUtilities.escapeForXML(textTag.toString()) + "</UserLevelDocumentation>";
    }

    return documentation;
  }

  /**
   * Process inlineTags and return text that contains links to the javadoc output.
   * 
   * @param programElementDoc The class for which we are generating documentation.
   */
  private static String _inlineTagCommentText(final ProgramElementDoc programElementDoc) {
    // Process the comment as an array of tags. Doc.commentText()
    // should do this, but it does not.
    final StringBuffer documentation = new StringBuffer();
    final Tag tag[] = programElementDoc.inlineTags();
    for (final Tag element : tag) {
      if (element instanceof SeeTag) {
        final SeeTag seeTag = (SeeTag) element;
        documentation.append("<a href=\"");
        // The dot separated class or package name, if any.
        String classOrPackageName = null;
        boolean isIncluded = false;
        if (seeTag.referencedPackage() != null) {
          classOrPackageName = seeTag.referencedPackage().toString();
          isIncluded = seeTag.referencedPackage().isIncluded();
        }
        if (seeTag.referencedClass() != null) {
          classOrPackageName = seeTag.referencedClass().qualifiedName();
          isIncluded = seeTag.referencedClass().isIncluded();
        }

        // {@link ...} tags usually have a null label.
        String target = seeTag.label();
        if (target == null || target.length() == 0) {
          target = seeTag.referencedMemberName();
          if (target == null || target.length() == 0) {
            target = seeTag.referencedClassName();
          }
        }
        if (classOrPackageName != null) {
          if (target != null && target.indexOf("(") != -1) {
            // The target has a paren, so can't be a port or
            // parameter, so link to the html instead of the .xml.

            isIncluded = false;
          }

          // If the .xml file is not included in the output,
          // then link to the .html file
          documentation.append(_relativizePath(_outputDirectory, classOrPackageName, programElementDoc, isIncluded));
        }
        if (seeTag.referencedMember() != null) {
          documentation.append("#" + seeTag.referencedMember().name());
        }
        documentation.append("\">" + target + "</a>");
      } else {
        documentation.append(element.text());
      }
    }
    String docStr = documentation.toString();
    if(StringUtils.isEmpty(docStr))
      docStr = "no documentation found";
    return docStr;
  }

  private static String _generateClassName(final ClassDoc classDoc) {
    return classDoc.toString();
  }

  private static String _generateShortClassName(final ClassDoc classDoc) {
    final String className = classDoc.toString();
    String shortClassName = null;
    if (className.lastIndexOf(".") == -1) {
      shortClassName = className;
    } else {
      shortClassName = className.substring(className.lastIndexOf(".") + 1);
    }
    return shortClassName;
  }

  /**
   * Generate the classLevel documentation for a class
   * 
   * @param classDoc The class for which we are generating documentation.
   */
  private static Map<String, String> _generateClassLevelDocumentationElements(final ClassDoc classDoc) {
    Map<String, String> result = new TreeMap<String, String>();

    Tag[] tags = null;
    // Handle other class tags.
    final String[] classTags = { "version", "since", "Pt.ProposedRating", "Pt.AcceptedRating", "UserLevelDocumentation" };
    for (final String classTag : classTags) {
      tags = classDoc.tags(classTag);
      if (tags.length > 0) {
        final StringBuffer textTag = new StringBuffer();
        for (final Tag tag : tags) {
          textTag.append(tag.text());
        }
        result.put(classTag, StringUtilities.escapeForXML(textTag.toString()));
      }
    }
    return result;
  }

  /**
   * Generate documentation for all parameter fields of an actor.
   * 
   * @param classDoc The ClassDoc for the class we are documenting.
   * @param fieldBaseClass The base class for the field we are documenting.
   * @param attrTemplate 
   * @return The documentation for all fields that are derived from the fieldBaseClass parameter.
   */
  private static String _generateFieldDocumentation(final ClassDoc classDoc, String attrTemplate) throws Exception {
    Writer resultWriter = new StringWriter();
    try {
      Context velocityContext = new VelocityContext();
      velocityContext.put("actorName", _generateShortClassName(classDoc));
      velocityContext.put("allActorAttributes", _generateActorAttributeElements(classDoc, Class.forName("ptolemy.data.expr.Parameter")));

      velocity.evaluate(velocityContext, resultWriter, "actor help template", attrTemplate);
      return resultWriter.toString();
    } finally {
      try {
        resultWriter.close();
      } catch (Exception e) {
      }
    }
  }

  protected static Map<String, String> _generateActorAttributeElements(final ClassDoc classDoc, final Class fieldBaseClass) {
    final FieldDoc[] fields = classDoc.fields();
    Map<String, String> paramDocs = new TreeMap<String, String>();
    
    // FIXME: get fields from superclasses?
    for (final FieldDoc field : fields) {
      final String className = field.type().toString();
      try {
        if (className.equals("javax.media.j3d.Canvas3D") || className.equals("com.sun.j3d.utils.universe.SimpleUniverse")) {
          throw new Exception("Skipping " + className + ",it starts up X11 and interferes with the " + "nightly build");
        }
        final Class type = Class.forName(className);
        if (fieldBaseClass.isAssignableFrom(type)) {
          String value = field.name();
          String userFriendlyFieldNameTag = "docName";
          Tag[] tags = field.tags(userFriendlyFieldNameTag);
          if (tags.length > 0) {
            final StringBuffer textTag = new StringBuffer();
            for (final Tag tag : tags) {
              textTag.append(tag.text());
            }
            value = StringUtilities.escapeForXML(textTag.toString());
          } else {
            final AnnotationDesc[] an = field.annotations();
            for (final AnnotationDesc element2 : an) {
              if (element2.annotationType().toString().equals("com.isencia.passerelle.doc.generator.ParameterName")) {
                final AnnotationDesc.ElementValuePair[] pairs = element2.elementValues();
                if (pairs.length >= 1) {
                  value = pairs[0].value().toString();
                }
              }
            }
          }
          String name = null;
          if (value.startsWith("\"") && value.endsWith("\"")) {
            name = value.substring(1,value.length()-1);
          } else {
            name = value;
          }
          paramDocs.put(name, StringUtilities.escapeForXML(_inlineTagCommentText(field)));
        }
      } catch (final ClassNotFoundException ex) {
        // Ignored, we probably have a primitive type like boolean.
        // Java 1.5 Type.isPrimitive() would help here.
      } catch (final Throwable throwable) {
        // Ignore, probably a loader error for Java3D
        System.out.println("Failed to find class " + className);
        throwable.printStackTrace();
      }
    }
    return paramDocs;
  }

  /**
   * Process the doclet command line arguments and return the value of the -d parameter, if any.
   * 
   * @param options The command line options.
   * @return the value of the -d parameter, if any, otherwise return null.
   */
  private static String _getOutputDirectory(final String[][] options) {
    for (final String[] option : options) {
      if (option[0].equals("-d")) {
        return option[1];
      }
    }
    return null;
  }

  /**
   * Given two dot separated classpath names, return a relative path to the corresponding doc file. This method is used to create relative paths
   * 
   * @param directory The top level directory where the classes are written.
   * @param destinationClassName
   * @param programElementDoc The documentation for the base class.
   * @param isIncluded True if the destination class is included in the set of classes we are documenting. If isIncluded is true, we create a link to the .xml
   *          file. If isIncluded is false, we create a linke to the javadoc .html file.
   * @return a relative path from the base class to the destination class.
   */
  private static String _relativizePath(final String baseDirectory, final String destinationClassName, final ProgramElementDoc programElementDoc,
      final boolean isIncluded) {
    // Use / here because these will be used in URLS
    // String baseFileName = baseClassName.replace('.', "/");
    final String baseClassName = programElementDoc.qualifiedName();
    String destinationFileName = destinationClassName.replace('.', '/');
    if (baseDirectory != null) {
      // FIXME: will this work if baseDirectory is null?
      // baseFileName = baseDirectory + "/" + baseFileName;
      destinationFileName = baseDirectory + "/" + destinationFileName;
    }
    // URI baseURI = new File(baseFileName).toURI();
    final URI destinationURI = new File(destinationFileName).toURI();
    final URI baseDirectoryURI = new File(baseDirectory).toURI();
    final URI relativeURI = baseDirectoryURI.relativize(destinationURI);

    // Determine offsite from baseClassName to baseDirectory
    final String baseClassParts[] = baseClassName.split("\\.");
    final StringBuffer relativePath = new StringBuffer();

    int offset = 1;
    if (programElementDoc instanceof FieldDoc) {
      // Fields have names like foo.bar.bif, where bif is the method
      offset = 2;
    }
    for (int i = 0; i < baseClassParts.length - offset; i++) {
      relativePath.append("../");
    }

    // If the target is not in the list of actors we are creating
    // documentation for, then link to the .html file that
    // presumably was generated by javadoc; otherwise, link to the
    // .xml file
    final String extension = isIncluded ? ".xml" : ".html";

    System.out.println("PtDoclet: relativize: " + baseDirectory + " " + baseClassName + " " + baseClassParts.length + " " + offset + " " + relativePath
        + relativeURI.getPath() + extension);

    return relativePath + relativeURI.getPath() + extension;
  }

  /**
   * Write the output to a file.
   * 
   * @param className The dot separated fully qualified classname, which is used to specify the directory and filename to which the documentation is written.
   * @param documentation The documentation that is written.
   * @exception IOException If there is a problem writing the documentation.
   */
  private static void _writeDoc(final String className, final String documentation) throws IOException {
    String fileBaseName = className + ".html";

    String fileName = null;
    if (_outputDirectory != null) {
      fileName = _outputDirectory + File.separator + "html" + File.separator + fileBaseName;
    } else {
      fileName = fileBaseName;
    }
    // If necessary, create the directory.
    final File directoryFile = new File(fileName).getParentFile();
    if (!directoryFile.exists()) {
      if (!directoryFile.mkdirs()) {
        throw new IOException("Directory \"" + directoryFile + "\" does not exist and cannot be created.");
      }
    }
    System.out.println("Creating " + fileName);

    final FileWriter writer = new FileWriter(fileName);
    try {
      writer.write(documentation);
    } finally {
      writer.close();
    }
  }

  private static void _writeAttrDoc(final String className, final String documentation) throws IOException {
    String fileBaseName = className + "_attributes.html";

    String fileName = null;
    if (_outputDirectory != null) {
      fileName = _outputDirectory + File.separator + "html" + File.separator + fileBaseName;
    } else {
      fileName = fileBaseName;
    }
    // If necessary, create the directory.
    final File directoryFile = new File(fileName).getParentFile();
    if (!directoryFile.exists()) {
      if (!directoryFile.mkdirs()) {
        throw new IOException("Directory \"" + directoryFile + "\" does not exist and cannot be created.");
      }
    }
    System.out.println("Creating " + fileName);

    final FileWriter writer = new FileWriter(fileName);
    try {
      writer.write(documentation);
    } finally {
      writer.close();
    }
  }

  private static void _writeIndex(final String indexText) throws IOException {
    String fileBaseName = "index.xml";

    String fileName = null;
    if (_outputDirectory != null) {
      fileName = _outputDirectory + File.separator + fileBaseName;
    } else {
      fileName = fileBaseName;
    }
    // If necessary, create the directory.
    final File directoryFile = new File(fileName).getParentFile();
    if (!directoryFile.exists()) {
      if (!directoryFile.mkdirs()) {
        throw new IOException("Directory \"" + directoryFile + "\" does not exist and cannot be created.");
      }
    }
    System.out.println("Creating " + fileName);

    final FileWriter writer = new FileWriter(fileName);
    try {
      writer.write(indexText);
    } finally {
      writer.close();
    }
  }

  private static void _writeTOC(final String indexText) throws IOException {
    String fileBaseName = "passerelle.xml";

    String fileName = null;
    if (_outputDirectory != null) {
      fileName = _outputDirectory + File.separator + fileBaseName;
    } else {
      fileName = fileBaseName;
    }
    // If necessary, create the directory.
    final File directoryFile = new File(fileName).getParentFile();
    if (!directoryFile.exists()) {
      if (!directoryFile.mkdirs()) {
        throw new IOException("Directory \"" + directoryFile + "\" does not exist and cannot be created.");
      }
    }
    System.out.println("Creating " + fileName);

    final FileWriter writer = new FileWriter(fileName);
    try {
      writer.write(indexText);
    } finally {
      writer.close();
    }
  }

  private static void _writeHtmlTOC(final String indexText) throws IOException {
    String fileBaseName = "passerelle.html";

    String fileName = null;
    if (_outputDirectory != null) {
      fileName = _outputDirectory + File.separator + "html" + File.separator + fileBaseName;
    } else {
      fileName = fileBaseName;
    }
    // If necessary, create the directory.
    final File directoryFile = new File(fileName).getParentFile();
    if (!directoryFile.exists()) {
      if (!directoryFile.mkdirs()) {
        throw new IOException("Directory \"" + directoryFile + "\" does not exist and cannot be created.");
      }
    }
    System.out.println("Creating " + fileName);

    final FileWriter writer = new FileWriter(fileName);
    try {
      writer.write(indexText);
    } finally {
      writer.close();
    }
  }

  // /////////////////////////////////////////////////////////////////
  // // private variables ////

  /** Directory to which the output is to be written. */
  private static String _outputDirectory;
}

/** this file originally downloaded from 
 *http://www.javaworld.com/javaworld/jw-12-2000/junit/jw-1221-junit.zip
 *
 */

package org.greenstone.testing;

import java.util.*;
import java.io.*;
/*
 * This used to use JCF. However, JCF seems to have dissapeared from the web.
 * import lti.java.jcf.*;
 */


/**
 * This class is responsible for searching a directory for class files. It builds
 * a list of fully qualified class names from the class files in the directory tree.
 */
public class ClassFinder {
    final private Vector classNameList = new Vector ();
    final private int startPackageName;

    /**
     * Construct the class finder and locate all the classes in the directory structured
     * pointed to by <code>classPathRoot</code>. Only classes in the package <code>packageRoot</code>
     * are considered.
     */
    public ClassFinder(final File classPathRoot, final String packageRoot) throws IOException {
        startPackageName = classPathRoot.getAbsolutePath().length() + 1;
        String directoryOffset = packageRoot.replace ('.', File.separatorChar);
        findAndStoreTestClasses (new File (classPathRoot, directoryOffset));
    }
    
    /**
     * Given a file name, guess the fully qualified class name.
     */
    private String computeClassName (final File file) {
        String absPath = file.getAbsolutePath();
        String packageBase = absPath.substring (startPackageName, absPath.length () - 6);
        String className;
        className = packageBase.replace(File.separatorChar, '.');
        return className;
    }

    /**
     * This method does all the work. It runs down the directory structure looking
     * for java classes.
     */
    private void findAndStoreTestClasses (final File currentDirectory) throws IOException {
        String files[] = currentDirectory.list();
        for(int i = 0;i < files.length;i++) {
            File file = new File(currentDirectory, files[i]);
            String fileBase = file.getName ();
            int idx = fileBase.indexOf(".class");
            final int CLASS_EXTENSION_LENGTH = 6;
            
            if(idx != -1 && (fileBase.length() - idx) == CLASS_EXTENSION_LENGTH) {
/* 
 * This used to use JCF. However, JCF seems to have dissapeared from the web so we fallback
 * to a less elegant method. We compute the class name from the file name :-(
 *               JcfClassInputStream inputStream = new JcfClassInputStream(new FileInputStream (file));
 *               JcfClassFile classFile = new JcfClassFile (inputStream);
 *               System.out.println ("Processing: " + classFile.getFullName ().replace ('/','.'));
 *               classNameList.add (classFile.getFullName ().replace ('/','.'));
 */
                String className = computeClassName (file);
                classNameList.add (className);
            } else {
                if(file.isDirectory()) {
                    findAndStoreTestClasses (file);
                }
            }
        }
    }

    /**
     * Return the found classes.
     */
    public Iterator getClasses () {
        return classNameList.iterator ();
    }
}

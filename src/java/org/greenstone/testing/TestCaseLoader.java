package org.greenstone.testing;
import java.util.*;
import java.lang.reflect.*;
import junit.framework.*;
/**
 * Responsible for loading classes representing valid test cases.
 */
public class TestCaseLoader {
    final private Vector classList = new Vector ();
    final private String requiredType;
    
    /** 
     * Adds <code>testCaseClass</code> to the list of classdes
     * if the class is a test case we wish to load. Calls
     * <code>shouldLoadTestCase ()</code> to determine that.
     */
    private void addClassIfTestCase (final Class testCaseClass) {
        if (shouldAddTestCase (testCaseClass)) {
            classList.add (testCaseClass);
        }
    }

    /**
     * Determine if we should load this test case. 
     */
    private boolean shouldAddTestCase (final Class testCaseClass) {
        boolean isOfTheCorrectType = false;
        if (TestCase.class.isAssignableFrom(testCaseClass)) {
            try {
                Field testAllIgnoreThisField = testCaseClass.getDeclaredField("TEST_ALL_TEST_TYPE");
                final int EXPECTED_MODIFIERS = Modifier.STATIC | Modifier.PUBLIC | Modifier.FINAL;
                if (((testAllIgnoreThisField.getModifiers() & EXPECTED_MODIFIERS) != EXPECTED_MODIFIERS) ||
                    (testAllIgnoreThisField.getType() != String.class)) {
                    throw new IllegalArgumentException ("TEST_ALL_TEST_TYPE should be static public final String");
                }
                String testType = (String)testAllIgnoreThisField.get(testCaseClass);
                isOfTheCorrectType = requiredType.equals (testType);
            } catch (NoSuchFieldException e) {
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException ("The field " + testCaseClass.getName () + ".TEST_ALL_TEST_TYPE is not accessible.");
            }
        }
        return isOfTheCorrectType;
    }
    
       
    
    /**
     * Load the classes that represent test cases we are interested. 
     * @param classNamesIterator An iterator over a collection of fully qualified class names
     */
    public void loadTestCases (final Iterator classNamesIterator) {
        while (classNamesIterator.hasNext ()) {
            String className = (String)classNamesIterator.next ();
	    System.out.println("trying class "+className);
            try {
                Class candidateClass = Class.forName (className);
                addClassIfTestCase (candidateClass);
            } catch (ClassNotFoundException e) {
                System.err.println ("Cannot load class: " + className + " " + e.getMessage ());
            } catch (NoClassDefFoundError e) {
                System.err.println ("Cannot load class that " + className + " is dependant on");
            }
        }
    }
        
    /**
     * Construct this instance. Load all the test cases possible that derive
     * from <code>baseClass</code> and cannot be ignored. 
     * @param classNamesIterator An iterator over a collection of fully qualified class names
     */
    public TestCaseLoader(final String requiredType) {
        if (requiredType == null) throw new IllegalArgumentException ("requiredType is null");
        this.requiredType = requiredType;
    }
    
    /**
     * Obtain an iterator over the collection of test case classes loaded by <code>loadTestCases</code>
     */
    public Iterator getClasses () {
        return classList.iterator ();
    }
}

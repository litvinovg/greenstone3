/** XMLTagInfo
 * contains any methods that are specific to what kind of XML you are working on
 */

public class XMLTagInfo {

    public static boolean isIndexable(String tag) {
	if (tag.equals("chapter") || tag.equals("part") || tag.equals("frontmatter") || tag.equals("appendix") || tag.equals("index") || tag.equals("glossary") || tag.equals("biblio")) {
	    return true;
	} 
	return false;
    }

    public static boolean isScopable(String tag) {

	if (tag.equals("bookbody") || tag.equals("frontmatter") || tag.equals("backmatter")) {
	    return true;
	} 
	return false;
    }

    
}

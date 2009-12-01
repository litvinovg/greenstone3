/**
 *
 * @author  kjdon@cs.waikato.ac.nz
 * @version 
 */


// gsdl3 classes
import org.greenstone.gsdl3.util.XMLConverter;
import org.greenstone.gsdl3.util.GSFile;
// XML classes
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.DocumentTraversal;
import org.apache.xml.serialize.XMLSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.EntityResolver;
// java classes
import java.io.File;
import java.io.FileOutputStream;

public class ImportXML
    implements EntityResolver {
    File out_dir = null;
    XMLConverter converter = null;

    String base_path = null;
    public ImportXML() {
	converter = new XMLConverter();
	converter.setEntityResolver(this);
	
    }
    public void setOutDir(File out_dir) {
	this.out_dir = out_dir;
    }
    public void init() {

    }
    public void importFile(File file) throws Exception {
	importFile(file, "");
    }
    protected void importFile(File file, String local_path) throws Exception {

	if (file.isDirectory()) {
	    File files []  = file.listFiles();
	    for (int i=0; i<files.length; i++) {
		//if (files[i].getName().endsWith(".xml")) {
		importFile(files[i], local_path+File.separator+files[i].getName());
		    //}
	    }
	    return;
	}

	base_path = file.getPath();
	base_path = base_path.substring(0, base_path.lastIndexOf(File.separatorChar));
	System.out.println("base path = "+base_path);
	// now we have an actual file
	System.out.println("processing file "+file.getPath());
	File out_file = new File (out_dir, local_path);
	String name = file.getName();
	if (name.endsWith(".dtd")) {
	    if (!GSFile.copyFile(file, out_file)) {
		System.err.println("couldn't copy dtd file "+file.getPath()+" to "+out_file.getPath()+"- please do the copy yourself");
	    }
	    //copy the file
	    return;
	}
	if (!name.endsWith(".xml")) {
	    // now we ignore any that don't end in .xml
	    return;
	}
	// now do the importing
	Document doc = converter.getDOM(file);
	
        String gs3NS = "http://www.greenstone.org/gs3";
	
        Element rootNode = doc.getDocumentElement();
        
        rootNode.setAttribute("xmlns:gs3", gs3NS);
       
        DocumentTraversal traversal = (DocumentTraversal)doc;
        NodeIterator i = traversal.createNodeIterator(doc, NodeFilter.SHOW_ELEMENT, null, true);
        
        Element element = null;
        Node node = null;
        int id = 0;
        while ((node = i.nextNode()) != null) {
            element = (Element)node;
	    if (XMLTagInfo.isIndexable(element.getNodeName())) {
		element.setAttribute("gs3:id", Integer.toString(id++));
	    }
        }
        
        XMLSerializer gs3Serializer = new XMLSerializer(new FileOutputStream(out_file), null);
        gs3Serializer.asDOMSerializer().serialize(doc);
        
    }

    public void finish() {
    }
    
    public InputSource resolveEntity (String public_id, String system_id) {
	
	if (system_id.startsWith("file://")) {
	    return new InputSource(system_id);
	}
	if (!system_id.startsWith(File.separator)) {
	    system_id = base_path+File.separatorChar+system_id;
	}
	return new InputSource("file://"+system_id);
    }

}

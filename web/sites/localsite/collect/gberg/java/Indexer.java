import org.greenstone.gsdl3.util.GSEntityResolver;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
//import org.xml.sax.SAXParseException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
//import org.apache.lucene.document.DateField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
//import org.apache.lucene.analysis.SimpleAnalyzer;

import java.util.Stack;
import java.io.FileInputStream;
import java.io.File;
import java.net.URL;



public class Indexer extends DefaultHandler {
    IndexWriter writer = null;
    SAXParser sax_parser = null;
    Stack stack = null;
    String path = "";
    String current_node = "";
    String current_contents = "";
    Document current_doc = null;
    String scope = "";
    protected String file_id = null;
    private String base_path = null;
    /** pass in true if want to create a new index, false if want to use the existing one */
    public Indexer (File index_dir, boolean create) {
	try {
	    stack = new Stack();
	    SAXParserFactory sax_factory = SAXParserFactory.newInstance();
	    sax_parser = sax_factory.newSAXParser();
	    writer = new IndexWriter(index_dir.getPath(), new StandardAnalyzer(), create);
	    if (create) {
		writer.optimize();
	    }

	} catch (Exception e) {

	}
    }

    /** index one document */
    public void index (String file_id, File file) {
	this.file_id = file_id;
	this.path = "";
	this.base_path = file.getPath();
	this.base_path = this.base_path.substring(0, this.base_path.lastIndexOf(File.separatorChar));
	try {
            sax_parser.parse(new InputSource(new FileInputStream(file)), this);
        }
        catch (Exception e) {
	    println("parse error:");
            e.printStackTrace();
	}
    }
    
    /** optimise the index */
    public void finish() {
	try {
	    writer.optimize();
	    writer.close();
	} catch (Exception e) {}
    }

    protected void println(String s) { System.out.println(s); }

    public void startDocument() throws SAXException {
        println("Starting to index " + file_id);
    }
    public void endDocument() throws SAXException {
        println("... indexing finished.");
    }
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
	path = appendPathLink(path, qName, atts);
	if (XMLTagInfo.isScopable(qName)) {
	    scope = qName;
	}
	if (XMLTagInfo.isIndexable(qName)) {
	    pushOnStack();
	    current_node = qName;	
	    System.out.println("going to index "+qName );
	    String node_id = "";
	    String id = "<"+qName;
	    for (int i=0; i<atts.getLength(); i++) {
		String name = atts.getQName(i);
		String value = atts.getValue(i);
		if (name!=null && value != null) {
		    id += " "+name+"="+value;
		}
		if (name.equals("gs3:id")) {
		    node_id = value;
		}
	    }
	    id += "/>";

	    if (scope.equals(qName)) {

		    current_doc.add(new Field("nodeID", this.file_id+"."+qName,
					  Field.Store.YES,Field.Index.NO));
	    } else {
		current_doc.add(new Field("nodeID", this.file_id+"."+scope+"."+qName+"."+node_id,
					  Field.Store.YES,Field.Index.NO));
	    }
	}
    }
    public void endElement(String uri, String localName, String qName) throws SAXException {
	if (XMLTagInfo.isIndexable(qName) && qName.equals(current_node)) {
	    current_doc.add(new Field("content", current_contents,
				      Field.Store.NO,Field.Index.TOKENIZED));
	    try {
		writer.addDocument(current_doc);
	    } catch (java.io.IOException e) {
		e.printStackTrace();
	    }
	    popOffStack();
	}
	
	path = removePathLink(path);
    }

    public void characters(char ch[], int start, int length) throws SAXException {
	String data = new String(ch, start, length).trim();
	if (data.length() > 0 ) {
	    current_contents += data;
	}
    }
    
    protected String appendPathLink(String path, String qName, Attributes atts) {

	path = path + "/"+qName;
	if (atts.getLength()>0) {
	    String id = atts.getValue("gs3:id");
	    if (id != null) {
		path +=  "[@gs3:id='"+id+"']";
	    }
	}
	return path;
    }
    protected String  removePathLink(String path) {

	int i=path.lastIndexOf('/');
	if (i==-1) {
	    path="";
	} else {
	    path = path.substring(0, i);
	}
	return path;
    }
    /** these are what we save on the stack */
    private class MyDocument {

	public Document doc = null;
	public String contents = null;
	public String tagname = "";
	
    }
    protected void pushOnStack() {
	if (current_doc != null) {
	    MyDocument save = new MyDocument();
	    save.doc = current_doc;
	    save.contents = current_contents;
	    save.tagname = current_node;
	    stack.push(save);
	}
	current_doc = new Document();
	current_contents = "";
	current_node = "";
    }

    protected void popOffStack() {
	if (!stack.empty()) {
	    MyDocument saved = (MyDocument)stack.pop();
	    current_doc = saved.doc;
	    current_contents = saved.contents;
	    current_node = saved.tagname;
	} else {
	    current_doc = new Document();
	    current_contents = "";
	    current_node = "";
	}
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



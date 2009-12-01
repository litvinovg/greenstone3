
/**
 *
 * @author  kjdon@cs.waikato.ac.nz
 * @version 
 */

import java.io.File;

public class IndexXML extends Object {

    File out_dir = null;
    Indexer indexer = null;
    
    public void setOutDir(File out_dir) {
	this.out_dir = out_dir;
    }

    public void init() {
	indexer = new Indexer(out_dir, true);
    }
    public void finish() {
	indexer.finish();
    }
    public void indexFile(File file) {
	
	if (file.isDirectory()) {
	    File[] files = file.listFiles();
	    for (int i=0; i<files.length; i++) {
		if (files[i].isDirectory() || files[i].getName().endsWith(".xml")) {
		    indexFile(files[i]);
		}
	    }
	    
	} else {
	    String name = file.getName();
	    name = name.substring(0, name.lastIndexOf('.'));
	    System.out.println("Indexing "+file.getPath()+" with id "+name);
	    this.indexer.index(name, file);
	}
    }
        
}

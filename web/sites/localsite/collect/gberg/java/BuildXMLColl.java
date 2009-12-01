/**
 *
 * class for building a simple  XML collection 
 * @author  kjdon@cs.waikato.ac.nz
 * @version 
 */


import org.greenstone.gsdl3.util.GSFile;

import java.io.File;

public class BuildXMLColl {

    public static void main (String args[]) throws Exception { 
	if (args.length != 2) {
	    System.out.println("Usage: java BuildXMLColl site-home coll-name");
	    return;
	}
 
	String site_home = args[0];
	String coll_name = args[1];

	File import_dir = new File(GSFile.collectionImportDir(site_home, coll_name));
	File building_dir = new File(GSFile.collectionBuildDir(site_home, coll_name));

	if (!import_dir.exists()) {
	    System.out.println("Couldn't find import dir for collection "+coll_name);
	    return;
	}

	if (building_dir.exists()) {
	    // get rid of it
	    GSFile.deleteFile(building_dir);
	}
	building_dir.mkdir();
	
	File idx_dir = new File(building_dir.getPath()+File.separator+"idx"+File.separator+"temp.txt");
	idx_dir = idx_dir.getParentFile();
	File text_dir = new File(building_dir.getPath()+File.separator+"text"+File.separator+"temp.txt");
	text_dir = text_dir.getParentFile();
	idx_dir.mkdir();
	text_dir.mkdir();
	
	// first we import the coll
	ImportXML importer = new ImportXML();
	importer.setOutDir(text_dir);
	importer.init();
	importer.importFile(import_dir);
	importer.finish();
	
	// then we index it
	IndexXML indexer = new IndexXML();
	indexer.setOutDir(idx_dir);
	indexer.init();
	indexer.indexFile(text_dir);
	indexer.finish();
    }
}

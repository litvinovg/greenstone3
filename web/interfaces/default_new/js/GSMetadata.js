function GSMetadata(collection, site, documentID, name, indexPosition, archivesPosition, value)
{
	var _collection = collection;
	var _site = site;
	var _documentID = documentID;
	var _name = name;
	var _value = value;
	var _indexPosition = indexPosition;
	var _archivesPosition = archivesPosition;


	//The setImport, setArchives and setIndex parameters are optional flags that specify the places you want the metadata to be set
	this.setMetadata = function(newValue, setImport, setArchives, setIndex)
	{
		if((setImport && setArchives && setIndex) || (!setImport && !setArchives && !setIndex))
		{
			gs.functions.setMetadata(_collection, _site, _documentID, _name, _value);
		}
		else
		{
			if(setImport)
			{
				gs.functions.setImportMetadata(_collection, _site, _documentID, _name, _value);
			}
			else if(setArchives)
			{
				if(_archivesPosition != null)
				{
					gs.functions.setArchivesMetadata(_collection, _site, _documentID, _name, _archivesPosition);
				}
				else
				{
					gs.functions.setArchivesMetadata(_collection, _site, _documentID, _name, null, _value);
				}
			}
			else if(setIndex)
			{
				if(_indexPosition != null)
				{
					gs.functions.setIndexMetadata(_collection, _site, _documentID, _name, _indexPosition);
				}
				else
				{
					gs.functions.setIndexMetadata(_collection, _site, _documentID, _name, null, _value);
				}
			}
		}
	}
	
	//The removeImport, removeArchives and removeIndex parameters are optional flags that specify the places you want the metadata to be removed from
	this.removeMetadata = function(removeImport, removeArchives, removeIndex)
	{
		if((removeImport && removeArchives && removeIndex) || (!removeImport && !removeArchives && !removeIndex))
		{
			gs.functions.removeMetadata(_collection, _site, _documentID, _name, _value);
		}
		else
		{
			if(removeImport)
			{
				gs.functions.removeImportMetadata(_collection, _site, _documentID, _name, _value);
			}
			else if(removeArchives)
			{
				if(_archivesPosition != null)
				{
					gs.functions.removeArchivesMetadata(_collection, _site, _documentID, _name, _archivesPosition);
				}
				else
				{
					gs.functions.removeArchivesMetadata(_collection, _site, _documentID, _name, null, _value);
				}
			}
			else if(removeIndex)
			{
				if(_indexPosition != null)
				{
					gs.functions.removeIndexMetadata(_collection, _site, _documentID, _name, _indexPosition);
				}
				else
				{
					gs.functions.removeIndexMetadata(_collection, _site, _documentID, _name, null, _value);
				}
			}
		}
	}

        this.getValue = function()
        {
            return _value;
	}
	
}
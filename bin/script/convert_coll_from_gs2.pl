#!/usr/bin/perl -w

BEGIN {
    die "GSDLHOME not set\n" unless defined $ENV{'GSDLHOME'};
    die "GSDL3HOME not set\n" unless defined $ENV{'GSDL3HOME'};
    die "GSDL3SRCHOME not set\n" unless defined $ENV{'GSDL3SRCHOME'};
    die "GSDLOS not set\n" unless defined $ENV{'GSDLOS'};
    unshift (@INC, "$ENV{'GSDLHOME'}/perllib");
    unshift (@INC, "$ENV{'GSDL3SRCHOME'}/lib/perl/cpan");
}

use colcfg;
use docprint; # for sub escape_text
use util;
use parsargv;
use FileHandle;
use XML::Writer;
#can't get this to work on windows
#use GDBM_File;

use strict;

my $convert_format_stmts = 0;

&main();
sub print_usage() {
    print STDOUT "Usage: convert_coll_from_gs2.pl [options] coll-name\n";
    print STDOUT "options:\n";
    
    print STDOUT "   -collectdir             Directory where collection lives.\n";
    print STDOUT "   -verbosity              Controls the amount of output.\n";
    print STDOUT "   -defaultlang            The language that is considered the default (for display text etc). defaults to 'en'\n";
    print STDOUT "   -convert_format_stmts   (Deprecated.) Switch this on if you want the old behaviour of this script, which is \n";
    print STDOUT "                               to process format statements using perl regular expressions.\n";
    print STDOUT "                               This option is deprecated in favour of using 'formatconverter' which interprets \n";
    print STDOUT "                               format statements directly using the same C++ parsing code as in GS2 runtime.\n\n";
}


sub main {

    my ($defaultlang, $verbosity, $collectdir);
    # note that no defaults are passed for most options as they're set
    # later (after we check the collect.cfg file)
    if (!&parsargv::parse(\@ARGV,
			  'verbosity/\d+/', \$verbosity,
			  'collectdir/.*/', \$collectdir,
			  'defaultlang/.*/', \$defaultlang,
			  'convert_format_stmts', \$convert_format_stmts)) {
	&print_usage();
	die "\n";
    }

    # get and check the collection name
    my ($collection) = @ARGV;
    if (!defined($collection) || $collection eq "") {
	die "No collection specified\n";
    }
    if ($collection eq "gs2model") {
	die "You cant convert the model collection\n";
    }
    
    if (!defined $collectdir || $collectdir eq "") {
	$collectdir = &util::filename_cat ($ENV{'GSDLHOME'}, "collect");
    }

    if (!defined $defaultlang || $defaultlang eq "") {
	$defaultlang = 'en';
    }
    # add on the coll name
    $collectdir = &util::filename_cat ($collectdir, $collection);
    
    my $collconfigfilename = &util::filename_cat ($collectdir, "etc", "collect.cfg");
    print STDOUT "coll config=$collconfigfilename\n";
    my $collectcfg;
    if (-e $collconfigfilename) {
	$collectcfg = &colcfg::read_collect_cfg ($collconfigfilename);

    } else {
	print STDERR "collect.cfg not found!!";
	die "\n";
    }
    

    my $buildconfigfilename = &util::filename_cat ($collectdir, "index", "build.cfg");
    my $buildcfg;
    if (-e $buildconfigfilename) {
	$buildcfg = &colcfg::read_build_cfg ($buildconfigfilename);

    } else {
	print STDERR "build.cfg not found!!";
	die "\n";
    }
    

     
    my $colloutfile = &util::filename_cat ($collectdir, "etc", "collectionConfig.xml");
    if (-e $colloutfile) {
	print STDOUT "collectionConfig file already exists! overwriting it!\n";
	
    }
   
    my $buildoutfile = &util::filename_cat ($collectdir, "index", "buildConfig.xml");
    if (-e $buildoutfile) {
	print STDOUT "buildConfig file already exists! overwriting it!\n";
	
    }

#    my $db_file = &util::filename_cat ($collectdir, "index", "text", "$collection.ldb");
    my $database;
#    if (-e $db_file) {
#	$database = &open_database($db_file);
#    } else {
#	print STDERR "gdbm database file $db_file not found!!";
#	die "\n";
#    }
    
    my $buildtype;
    if (defined $buildcfg->{'buildtype'}) {
	$buildtype = $buildcfg->{'buildtype'};
    } else {
	$buildtype = 'mg';
    }
    
    my $indexstem = undef;
    if (defined $buildcfg->{'indexstem'}) {
	$indexstem = $buildcfg->{'indexstem'};
    }
    #my $indexstem = $buildcfg->{'indexstem'} || undef;
    my $infodbtype = $buildcfg->{'infodbtype'} || "gdbm";
    my $earliestDatestamp = $buildcfg->{'earliestdatestamp'} || undef;

    my $buildoutput = new IO::File(">$buildoutfile");
    binmode($buildoutput,":utf8");
    my $buildwriter = new XML::Writer(OUTPUT => $buildoutput, NEWLINES => 1);
    
    $buildwriter->xmlDecl("UTF-8");
    $buildwriter->startTag('buildConfig', 'xmlns:gsf'=>"http://www.greenstone.org/greenstone3/schema/ConfigFormat");
    
    my $colloutput = new IO::File(">$colloutfile");
    binmode($colloutput,":utf8");
    my $collwriter = new XML::Writer(OUTPUT => $colloutput, NEWLINES => 1);
    
    $collwriter->xmlDecl("UTF-8");
    $collwriter->startTag('CollectionConfig', 'xmlns:gsf'=>"http://www.greenstone.org/greenstone3/schema/ConfigFormat", 'xmlns:xsl'=>'http://www.w3.org/1999/XSL/Transform');
    
    #output the collection metadata to the collectionConfig file
    $collwriter->startTag('metadataList');
    
    my $creator = $collectcfg->{'creator'};
    &output_metadata($collwriter,'default', 'creator', $creator);
    my $public =$collectcfg->{'public'};
    &output_metadata($collwriter,'default', 'public', $public);
    
    $collwriter->endTag('metadataList');

    #output the display collectionmeta to collectionConfig.xml
    
    my $collectionmeta = $collectcfg->{'collectionmeta'};
    if (defined $collectionmeta) {
	my %name_map = ('collectionname', 'name',
		     'collectionextra', 'description',
		     'iconcollection', 'icon',
		     'iconcollectionsmall', 'smallicon');
	
	$collwriter->startTag('displayItemList');
	foreach my $entry ( keys %$collectionmeta) {
	     # some metadata names need to be specially mapped to other names
	     # most of them however, can retain their original names
	     my $name = (defined $name_map{$entry}) ? $name_map{$entry} : $entry;
	     foreach my $lang (keys %{$collectionmeta->{$entry}}) {
		 my $value = $collectionmeta->{$entry}->{$lang};
		 if ($entry =~ /^icon/) {
		     $value = format_icon_value($value);
		 } else {
		     $value = tidy_up_display_item($value);
		 }
		 &output_display($collwriter, $name, $lang, $value);
	     }
	}
	$collwriter->endTag('displayItemList');
    } 
    
    # output building metadata to build config file 
    my $numdocs = $buildcfg->{'numdocs'};
    $buildwriter->startTag('metadataList');
    &output_metadata($buildwriter,'', 'numDocs', $numdocs);
    &output_metadata($buildwriter,'', 'buildType', $buildtype);
    &output_metadata($buildwriter,'', 'indexStem', $indexstem) if(defined $indexstem);
    &output_metadata($buildwriter,'', 'infodbType', $infodbtype);
    &output_metadata($buildwriter,'', 'earliestDatestamp', $earliestDatestamp) if(defined $earliestDatestamp);
    $buildwriter->endTag('metadataList');
    
    
    #indexes
    # maps index name to shortname
    my $indexmap = {};
    # keeps the order for indexes
    my @indexlist = ();
    my $defaultindex = "";
    my $first = 1;
    my $maptype = "indexfieldmap";
    if ($buildtype eq "mg") {
	$maptype = "indexmap";
    }
    if (defined $buildcfg->{$maptype}) {
	my $indexmap_t = $buildcfg->{$maptype};
	foreach my $i (@$indexmap_t) {
	    my ($k, $v) = $i =~ /^(.*)\-\>(.*)$/;
	    $indexmap->{$k} = $v;
	    push @indexlist, $k;
	    if ($first == 1) {
		$defaultindex = $k;
		$first = 0;
	    }	
	}
    } else {
	print STDERR "$maptype not defined\n";
    }
    # we use the shortname for default index
    if (defined $collectcfg->{'defaultindex'}) {
	$defaultindex = $collectcfg->{'defaultindex'};
	#$defaultindex = $indexmap->{$defaultindex};
    }
    
    #  levels
    my $levelmap = {};
    my @levellist = ();
    my $default_search_level = "";
    my $default_search_level_shortname = "";
    my $default_retrieve_level = "Sec";
    $first = 1;
    if ($buildtype eq "mgpp" || $buildtype eq "lucene") {
	if (defined $buildcfg->{'levelmap'}) {
	    my $levelmap_t = $buildcfg->{'levelmap'};
	    foreach my $l (@$levelmap_t) {
		my ($k, $v) = $l =~ /^(.*)\-\>(.*)$/;
		$levelmap->{$k} = $v;
		push @levellist, $k;
		if ($first) {
		    $default_search_level = $k;
		    $default_search_level_shortname = $v;
		    $first = 0;
		}
	    }
	}

	if (defined $collectcfg->{'defaultlevel'}) {
	    $default_search_level = $collectcfg->{'defaultlevel'};
	    #$default_search_level = $levelmap->{$default_search_level};
	    $default_search_level_shortname = $levelmap->{$default_search_level};
	}
	if (defined $buildcfg->{'textlevel'}) {
	    $default_retrieve_level = $buildcfg->{'textlevel'};
	}
    }
    # format stuff
    my $format = $collectcfg->{'format'};

    #output the search stuff to coll cfg
    $collwriter->startTag('search','type'=>$buildtype);
    foreach my $i (keys %$indexmap) {
	$collwriter->startTag('index', 'name'=>$i);
	#find the coll meta stuff
	my $indexdisplay = ".$i";
	foreach my $lang (keys %{$collectionmeta->{$indexdisplay}}) {
	    my $value = $collectionmeta->{$indexdisplay}->{$lang};
	    output_display($collwriter, 'name', $lang, $i);
	}
	$collwriter->endTag('index');
    }

    #output the defaultIndex to coll cfg
    $collwriter->emptyTag('defaultIndex','name'=>$defaultindex);

    # indexOptions
    if (defined $collectcfg->{'indexoptions'}) {
	foreach my $i (@{$collectcfg->{'indexoptions'}}) {
	    $collwriter->emptyTag('indexOption', 'name'=>$i);
	}
    }

    #indexSubcollection
    my $indexsubcollections = $collectcfg->{'indexsubcollections'};
   
    if (defined $indexsubcollections) {
	my $indexsubcollections_t= $collectcfg->{'indexsubcollections'};
	foreach my $i ( @$indexsubcollections_t) {
	    $collwriter->startTag('indexSubcollection', 'name'=>$i);
	    &output_display($collwriter, 'name', $defaultlang, $i);
	    $collwriter->endTag('indexSubcollection');
	}
    } 

    #subcollection
    my $subcollection = $collectcfg->{'subcollection'};
    if (defined $subcollection){
	foreach my $entry (keys %$subcollection){
	    my $value = $subcollection->{$entry};
	    $collwriter->emptyTag('subcollection','filter'=>$value,'name'=>$entry);
	}
    }

    #indexlanguage
    my $languages = $collectcfg->{'languages'};
    if (defined $languages){
	my $languages_t = $collectcfg->{'languages'};
	foreach my $i (@$languages_t){
	    $collwriter->startTag('indexLanguage','name'=>$i);
	     &output_display($collwriter, 'name', $defaultlang, $i);
	    $collwriter->endTag('indexLanguage');
	}
    }

    #  level stuff for mgpp/lucene
    if ($buildtype eq 'mgpp' || $buildtype eq 'lucene'){
	foreach my $l (keys %$levelmap) {
	    $collwriter->startTag('level', 'name'=>$l);
	    #find the coll meta stuff
	    my $leveldisplay = ".$l";
	    foreach my $lang (keys %{$collectionmeta->{$leveldisplay}}) {
		my $value = $collectionmeta->{$leveldisplay}->{$lang};
		output_display($collwriter, 'name', $lang, $value);
	    }
	    $collwriter->endTag('level');
	}
	$collwriter->emptyTag('defaultLevel', 'name'=>$default_search_level);
    }
    
    # add in the search type 
    if (defined $format->{'SearchTypes'}){
	$collwriter->startTag('format', 'name'=>"searchType");
	$collwriter->charactersXML($format->{'SearchTypes'});
	$collwriter->endTag('format');
    }

    # add in the format stuff
    if (defined $format->{'SearchVList'}) {
	
	$collwriter->startTag('format');
	write_format($collwriter, $format->{'SearchVList'}, "document");
	$collwriter->endTag('format');
    }
    elsif (defined $format->{'VList'}) {
	$collwriter->startTag('format');
	write_format($collwriter, $format->{'VList'}, "document");
	$collwriter->endTag('format');
    }	
    
    $collwriter->endTag('search');

    # import plugins
    # if ImagePlugin is added, then need to add in a replaceListRef element for gs2-image
    my $contains_image_plugin = 0; 

    my $plugins = $collectcfg->{'plugin'};
    
    if (defined $plugins){
	$collwriter->startTag('import');
	$collwriter->startTag('pluginList');
	foreach my $pl (@$plugins) {
	    my ($pluginname) = @$pl[0];
	    if ($pluginname =~ m/^(ImagePlugin|ImagePlug|PagedImagePlugin)$/) {
		$contains_image_plugin = 1;
	    }
	    $collwriter->startTag('plugin','name'=>$pluginname);

	    for (my $i=1; $i<scalar(@$pl); $i++) {
		my $arg =@$pl[$i];
		if ($arg =~ /^-/){
		    my $option_name=@$pl[$i];
		    my $option_value=@$pl[$i+1];
		    if (defined $option_value){
			if ($option_value =~ /^-/){
			    $collwriter->startTag('option','name'=>$option_name); 
			    $collwriter->endTag('option');
			}else{
			    $collwriter->startTag('option','name'=>$option_name,'value'=>$option_value); 
			    $collwriter->endTag('option');
			}
		    }
		}
	    }
	    $collwriter->endTag('plugin');
	}
	$collwriter->endTag('pluginList');
	$collwriter->endTag('import');
    }

    $buildwriter->startTag('serviceRackList');
    
    my @levels = ();
    my $defaultlevel;
    my $service_type = "MG";
    if ($buildtype eq 'mgpp') {
	$service_type = "MGPP";
    } elsif ($buildtype eq "lucene") {
	$service_type = "Lucene";
    }

 #indexSubcollectionList

    my $subcollectionmap = $buildcfg->{'subcollectionmap'};
    my $firstsubcollection = 1;
    my $defaultsubcollection = "";
    my @subcollist;
    my $subcolmap = {};

    if (defined $buildcfg->{'subcollectionmap'}) {
	my $subcolmap_t = $buildcfg->{'subcollectionmap'};

	foreach my $l (@$subcolmap_t) {
	    my @pair = split(/->/, $l);
	    $subcolmap->{$pair[0]} = $pair[1];
	    push @subcollist, $pair[0];
	    if ($firstsubcollection==1) {
		$defaultsubcollection = $pair[1];
		$firstsubcollection = 0;
	    }	
	}

    }


    #do the retrieve service
    $buildwriter->startTag('serviceRack', 'name'=>"GS2".$service_type."Retrieve");
    if ($buildtype eq 'mgpp' || $buildtype eq 'lucene') {
	$buildwriter->emptyTag('defaultLevel', 'shortname'=>$default_retrieve_level);
    } elsif ($buildtype eq "mg") {
	$buildwriter->emptyTag('defaultIndex', 'shortname'=>$defaultindex);
    }
    
    if ((defined $defaultsubcollection) && ($defaultsubcollection ne "")) {
	$buildwriter->emptyTag('defaultIndexSubcollection', 'shortname'=>$defaultsubcollection);
    }

    if (defined $indexstem) {
	$buildwriter->emptyTag('indexStem', 'name'=>$indexstem);
    }
    $buildwriter->emptyTag('databaseType', 'name'=>$infodbtype) if (defined $infodbtype);
    
    # close off the Retrieve service
    $buildwriter->endTag('serviceRack');

    # add in the classifiers if needed

    my $count = 1;
    my $phind = 0;
    my $started_classifiers = 0;
    if (defined $collectcfg->{'classify'}) { 
	$collwriter->startTag('browse');
	# add in default format if necessary
	if (defined $format->{"VList"} || defined $format->{"HList"}) {
	    # global formats
	    $collwriter->startTag('format');
	    if (defined $format->{"VList"}) {
		# VLIst applies to both classifier and doc nodes
		write_format($collwriter, $format->{"VList"}, "document");
		write_format($collwriter, $format->{"VList"}, "classifier");
	    }
	    if (defined $format->{"HList"}) {
		# hlist is only for classifier nodes
		write_format($collwriter, $format->{"HList"}, "horizontal");
	    }
	    $collwriter->endTag('format');
	}
	my $classifiers = $collectcfg->{'classify'};
	foreach my $cl (@$classifiers) {
	    my $name = "CL$count";
	    $count++;
	    my ($classname) = @$cl[0];
	    if ($classname =~ /^phind$/i) {
		$phind=1;
		#should add it into coll config classifiers
		next;
	    }
	    
	    my $horizontalAtTop = &isHorizontalClassifier($database, $name);
	    if (not $started_classifiers) {
		$buildwriter->startTag('serviceRack', 'name'=>'GS2Browse');
		if (defined $indexstem) {
		    $buildwriter->emptyTag('indexStem', 'name'=>$indexstem);
		}
		$buildwriter->emptyTag('databaseType', 'name'=>$infodbtype) if (defined $infodbtype);

		$buildwriter->startTag('classifierList');		
		$started_classifiers = 1;
	    }
	    my $content = ''; #use buttonname first, then metadata
	    if ($classname eq "DateList") {
		$content = "Date";
	    } else {
		for (my $i=0; $i<scalar(@$cl); $i++) {
		    my $arg = @$cl[$i];
		    if ($arg eq "-buttonname"){
			$content = @$cl[$i+1];
			last;
		    } elsif ($arg eq "-metadata") {
			$content = @$cl[$i+1];
		    }

		    # remove "ex." prefix from "ex.metaname" but not from "ex.namespace.metaname"
		    $content =~ s@ex\.([^.]+)(,|;|$)@$1$2@g; #$content =~ s@ex\.([A-Z])@$1@g;
		}
	    }
	    if ($horizontalAtTop) {
		$buildwriter->emptyTag('classifier', 'name'=>$name, 'content'=>$content, 'horizontalAtTop'=>'true');

	    } else {
		$buildwriter->emptyTag('classifier', 'name'=>$name, 'content'=>$content);
	    } 
	    
	    
	   # $collwriter->startTag('classifier', 'name'=>$name);
	    $collwriter->startTag('classifier', 'name'=>$classname);
	    for (my $i=1; $i<scalar(@$cl); $i++) {
		my $arg =@$cl[$i];
		if ($arg =~ /^-/){
		    my $option_name=@$cl[$i];
		    my $option_value=@$cl[$i+1];
		    if (defined $option_value){
			if ($option_value=~ /^-/){
			    $collwriter->startTag('option','name'=>$option_name); 
			    $collwriter->endTag('option');
			}else{
			    $collwriter->startTag('option','name'=>$option_name,'value'=>$option_value); 
			    $collwriter->endTag('option');
			}
		    }
		}
	    }

	    my $vlist = $name."VList";
	    my $hlist = $name."HList";
	    my $dlist = "";
	    if ($classname eq "DateList") {
		$dlist = "DateList";
	    }
	    # need to work out how to split into classifier and document
	    if (defined $format->{$vlist} || defined $format->{$hlist} || defined $format->{$dlist}) {
		$collwriter->startTag('format');
		if (defined $format->{$vlist}) {
		    write_format($collwriter, $format->{$vlist}, "document");
		    write_format($collwriter, $format->{$vlist}, "classifier");
		} 
		if (defined $format->{$hlist}) {
		    write_format($collwriter, $format->{$hlist}, "horizontal");
		} 
		
		if (defined $format->{$dlist}) {
		    write_format($collwriter, $format->{$dlist}, "document");
		}
		$collwriter->endTag('format');
	    }
	    $collwriter->endTag('classifier');
	} #foreach classifier
	if ($started_classifiers) {
	    # end the classifiers
	    $buildwriter->endTag('classifierList');
	    # close off the Browse service
	    $buildwriter->endTag('serviceRack');
	}
	
	$collwriter->endTag('browse');
    }
    
    
    # the phind classifier is a separate service
    if ($phind) {
	# if phind classifier
	$buildwriter->emptyTag('serviceRack', 'name'=>'PhindPhraseBrowse');
    }
   
    # do the search service
    $buildwriter->startTag('serviceRack', 'name'=>'GS2'.$service_type.'Search');
    #$buildwriter->emptyTag('defaultIndex', 'shortname'=>$defaultindex);
    $buildwriter->emptyTag('defaultIndex', 'shortname'=>$indexmap->{$defaultindex});
    $buildwriter->startTag('indexList');
    #for each index
    foreach my $i (@indexlist) {
	my $index = $indexmap->{$i};
	$buildwriter->emptyTag('index', 'name'=>$i, 'shortname'=>$index);
    }	
    $buildwriter->endTag('indexList');
    if (defined $indexstem) {
	$buildwriter->emptyTag('indexStem', 'name'=>$indexstem);
    }
    $buildwriter->emptyTag('databaseType', 'name'=>$infodbtype) if (defined $infodbtype);
    
    # index options
    if ($buildtype eq 'mg' || $buildtype eq 'mgpp') {
	$buildwriter->startTag('indexOptionList');
	my $stemindexes = 3; # default is stem and casefold
	if (defined $buildcfg->{'stemindexes'} && $buildcfg->{'stemindexes'} =~ /^\d+$/ ) {
	    $stemindexes = $buildcfg->{'stemindexes'};
	}
	$buildwriter->emptyTag('indexOption', 'name'=>'stemIndexes', 'value'=>$stemindexes);
	
	my $maxnumeric = 4; # default
	if (defined $buildcfg->{'maxnumeric'} && $buildcfg->{'maxnumeric'} =~ /^\d+$/) {
	    $maxnumeric = $buildcfg->{'maxnumeric'};
	}
	$buildwriter->emptyTag('indexOption', 'name'=>'maxnumeric', 'value'=>$maxnumeric);
	
	$buildwriter->endTag('indexOptionList');
    }

    if ($buildtype eq 'mgpp' || $buildtype eq 'lucene') {
		
	# level info
	$buildwriter->emptyTag('defaultLevel', 'shortname'=>$default_search_level_shortname);
	$buildwriter->emptyTag('defaultDBLevel', 'shortname'=>$default_retrieve_level);
	$buildwriter->startTag('levelList');
	foreach my $l (@levellist) {
	    my $level = $levelmap->{$l};
	    $buildwriter->emptyTag('level', 'name'=>$l, 'shortname'=>$level);
	}	
	$buildwriter->endTag('levelList');
	
	# do the search types if there
	if (defined $collectcfg->{'searchtype'}) {
	    $buildwriter->startTag('searchTypeList');
	    foreach my $st (@{$collectcfg->{'searchtype'}}) {
		$buildwriter->emptyTag('searchType', 'name'=>$st);
	    }
	    $buildwriter->endTag('searchTypeList');
	} elsif (defined $format->{'SearchTypes'}) {
	    #check format statement 
	    my $searchtype = $format->{'SearchTypes'};
	    $buildwriter->startTag('searchTypeList');
	    if ($searchtype =~ /form/) {
		$buildwriter->emptyTag('searchType', 'name'=>'form');
	    }
	    if ($searchtype =~ /plain/) {
		$buildwriter->emptyTag('searchType', 'name'=>'plain');
	    }
	    $buildwriter->endTag('searchTypeList');
	}
    } 
    
    #indexLanguageList
    my $indexlanguages = $collectcfg->{'languages'};
    my $firstindexlanguage = 1;
    my $defaultindexlanguage_shortname;
    if (defined $indexlanguages){
	$buildwriter->startTag('indexLanguageList');
	my $languages_t = $collectcfg->{'languages'};
	foreach my $i (@$languages_t){
	    $buildwriter->startTag('indexLanguage','name'=>$i);
	     &output_display($buildwriter, 'name', $i, $i);
	    $buildwriter->endTag('indexLanguage');
	    if ($firstindexlanguage==1){
		$defaultindexlanguage_shortname = $i;
		$firstindexlanguage=0;
	    }
	}
    $buildwriter->endTag('indexLanguageList');
    $buildwriter->startTag('defaultIndexLanguage', 'name'=>$defaultindexlanguage_shortname,'shortname'=>$defaultindexlanguage_shortname);
    $buildwriter->endTag('defaultIndexLanguage');
    }

  #  my $defaultsubcollection = "";
   # my @subcollist;

    if (scalar(@subcollist)>0){

	$buildwriter->startTag('indexSubcollectionList');
	foreach my $i (keys %$subcolmap){
	    my $short_name = $subcolmap->{$i};
	    $buildwriter->emptyTag('indexSubcollection', 'name'=>$i, 'shortname'=>$short_name);
	}

	$buildwriter->endTag('indexSubcollectionList');
	$buildwriter->emptyTag('defaultIndexSubcollection', 'shortname'=>$defaultsubcollection);
    }

   
    $buildwriter->endTag('serviceRack');
    
    $buildwriter->endTag('serviceRackList');
    $buildwriter->endTag('buildConfig');

    # we add in the default replace list just in case we have macros in the 
    # collection
    $collwriter->emptyTag('replaceListRef', 'id'=>'gs2-standard');
    $collwriter->emptyTag('replaceListRef', 'id'=>'gs2-image') if  $contains_image_plugin;
    $collwriter->endTag('CollectionConfig');
    $collwriter->end();
    $buildwriter->end();
    $buildoutput->close();
    $colloutput->close();
    &close_database($database);
}


sub output_metadata {
    my ($writer, $lang, $metaname,  $metavalue) = @_;
    $lang = 'en' if $lang eq 'default';
    if ($lang ne ""){
	$writer->startTag('metadata', 'lang'=>$lang, 'name'=>$metaname);
    }else{
	$writer->startTag('metadata', 'name'=>$metaname);
    }
    $writer->characters($metavalue) if(defined $metavalue);
    $writer->endTag('metadata');
}

sub output_display {
    my ($writer, $name, $lang, $value) = @_;
    $lang = 'en' if $lang eq 'default';
    if ($lang =~ /^\[/) {
	($lang) = $lang =~ /\[l=(.*)\]/;
    }

    $writer->startTag('displayItem', 'name'=>$name, 'lang'=>$lang);
    $writer->characters($value);
    $writer->endTag('displayItem');
}
sub format_icon_value {
    my ($value) = @_;
    if ($value =~ /^_/) {
	my ($newvalue) = $value =~ /images\/(.*)$/;
	if ($newvalue) {
	    return $newvalue;
	}
    }
    return $value;
}

sub tidy_up_display_item {
    my ($value) = @_;
    # remove \n
    $value =~ s/\\n//g;
    # replace \' with '
    $value =~ s/\\\'/\'/g;
    # replace \" with "
    $value =~ s/\\\"/\"/g;
    # replace _httpprefix_ with _httpsite_
    $value =~ s/_httpprefix_/_httpsite_/g;
    $value =~ s/_gwcgi_//g;
    $value =~ s/[a-z][a-z]?=_cgiarg[a-z][a-z]?_&?//g;
    $value =~ s/&p=/&sa=/g;
    return $value;
}

sub format_if_or {
    my ($format, $node_type) = @_;

    # while we find nested if/or statements, recurse to find more nested if/or statements, 
    # and try to expand (process) these nested statements starting from innermost going to outermost 

    while($format =~ m/^.*\{(?:If|Or)\}\{[^\}\{]*\{/) { # contains nested if/or statement, expand it

	my ($prefix, $nested_to_process, $suffix) = $format =~ m/^(.*\{(?:If|Or)\}\{[^\}\{]*)(\{[^\}]*\}\s*\{[^\}]*\})(.*)$/g; # recursion step

	#print STDERR "prefix: |$prefix|\n\nnested: |$nested_to_process|\n\nsuffix: |$suffix|\n\n";
	$format = $prefix . &format_if_or($nested_to_process, $node_type) . $suffix;
    }

    if($format =~ m/\{(If|Or)\}\{[^\}\{]*\}/g) { # base step: contains if/or statement(s), but none nested
	# expand them
	$format =~ s/\{If\}\{([^\}]*)\}/&format_if($1, $node_type)/eg;
	$format =~ s/\{Or\}\{([^\}]*)\}/&format_or($1)/eg;
    }
    return $format;
}

sub write_format {
    my ($writer, $old_format, $node_type) = @_;

    # replace \' with '
    $old_format =~ s/\\\'/\'/g;
    # replace \" with "
    $old_format =~ s/\\\"/\"/g;

    if($convert_format_stmts) {

    #convert [] to <gsf:...>
    # now handles nested {If} and {Or}
    $old_format = &format_if_or($old_format, $node_type);
    $old_format =~ s/\[Text\]/\<gsf:text\/\>/g;
    $old_format =~ s/\[num\]/\<gsf:num\/\>/g;
    $old_format =~ s/\[link\]/\<gsf:link type=\'$node_type\'\>/g;
    $old_format =~ s/\[\/link\]/\<\/gsf:link\>/g;
    $old_format =~ s/\[srclink\]/\<gsf:link type=\'source\'\>/g;
    $old_format =~ s/\[\/srclink\]/\<\/gsf:link\>/g;
    $old_format =~ s/\[icon\]/\<gsf:icon type=\'$node_type\'\/\>/g;
    $old_format =~ s/\[srcicon\]/\<gsf:icon type=\'source\'\/\>/g;
		  
    # what to do with hightlight??
    $old_format =~ s/\[\/?highlight\]//g;

    #now do the rest of the [] which are assumed to be metadata
    $old_format =~ s/\[([^\]]*)\]/&format_metadata($1)/eg;
  
    # some html tidy
    #turn <br> into <br />
    $old_format =~ s/\<br\>/\<br \/\>/g;
    #turn <p> into <p />
    $old_format =~ s/\<p\>/\<p \/\>/g;
    
    #put quotes around any atts
    $old_format =~ s/=([a-z]+)([> ])/=\'$1\'$2/g;
    } 
    else { # not converting format statements, leave them as GS2 format stmts, 
	# so that formatconverter can convert them and users can oversee the conversion in GLI,
	# but nest the GS2 statements here in an xml tag that won't be processed by GS3 

	$old_format = &docprint::escape_text($old_format); # escape html entities inside the format statement since the <br> and <p> may not be correct for xml
	$old_format = "<gsf:format-gs2>" . $old_format . "</gsf:format-gs2>";
	
    }

    if ($node_type eq "document") {
	$writer->startTag('gsf:template', 'match'=>'documentNode');
	$writer->charactersXML($old_format);
	$writer->endTag('gsf:template');
    } elsif ($node_type eq "classifier") {
	$writer->startTag('gsf:template', 'match'=>'classifierNode');
	$writer->charactersXML($old_format);
	$writer->endTag('gsf:template');
    } elsif ($node_type eq "horizontal") {	
	$writer->startTag('gsf:template', 'match'=>'classifierNode', 'mode'=>'horizontal');
	$writer->charactersXML($old_format);
	$writer->endTag('gsf:template');

    }
}

sub format_metadata {
    my ($metadata_string) = @_;

    #print STDERR "original meta = $metadata_string\n"; 
    
    # what shall we do with cgisafe??
    my $cgisafe = $metadata_string =~ s/^cgisafe://;

    my ($select) = $metadata_string =~ /^(parent|sibling)/;
    $metadata_string =~ s/^(parent|sibling)//;
    my ($scope, $delim);
    
    if ($select) {
	($scope) = $metadata_string =~ /^\((Top|All)/;
	$metadata_string =~ s/^\((Top|All)\)?//;
	if ($scope) {
	    ($delim) = $metadata_string =~ /^\'([^\']*)\'\)/;
	    $metadata_string =~ s/^\'([^\']*)\'\)//;
	} 
    }
    $metadata_string =~ s/^://;
    # remove ex.
    $metadata_string =~ s/^ex\.//;
    
    #print STDERR "select=$select, scope=$scope, delim=|$delim|, meta = $metadata_string\n";
    
    my $new_format = "<gsf:metadata name='$metadata_string' ";
    if (defined $select) {
	if ($select eq "sibling") {
	    $new_format .= "multiple='true' ";
	    if (defined $delim) {
		$new_format .= "separator='$delim' ";
	    }
	} elsif ($select eq "parent"){
	    if (defined $scope) {
		if ($scope eq "Top") {
		    $new_format .= "select='root' ";
		} elsif ($scope eq "All") {
		    $new_format .= "select='ancestors' ";
		    if (defined $delim) {
			$new_format .= "separator='$delim' ";
		    }
		}
	    } else {
		$new_format .= "select='parent' ";
	    }	
	}
    }
    $new_format .= "/>";
    #print STDERR "$new_format\n";
    return $new_format;
    
}

sub format_if {

    my ($if_string, $node_type) = @_;
    #print STDERR "if string = $if_string\n";

    my @parts = split /,/, $if_string; 
    my $test = $parts[0];
    my $true_option = $parts[1];
    my $false_option;
    if (scalar (@parts) == 3) {
	$false_option = $parts[2];
    }
    $test =~ s/^\s*//;
    $test =~ s/\s*$//;
    my ($test_meta, $test_type, $test_value);
    if ($test =~ /^(\[.+\])$/) {
	$test_meta = $1;
	$test_type = 'exists';
    } else {
	my ($lhs, $exp, $rhs) = $test =~ /^(.+)\s+(eq|ne|lt|gt|le|ge|sw|ew)\s+(.+)$/;
	#print STDERR "lhs, exp, rhs = $lhs, $exp, $rhs\n";
	if ($exp eq "eq") {
	    $test_type = "equals";
	} elsif ($exp eq "sw") {
	    $test_type = "startsWith";
	} elsif ($exp eq "ew") {
	    $test_type = "endsWith";
	} elsif ($exp eq "ne") {
	    $test_type = "notEquals";
	} elsif ($exp eq "lt") {
	    $test_type = "lessThan";
	}elsif ($exp eq "gt") {
	    $test_type = "greaterThan";
	}elsif ($exp eq "le") {
	    $test_type = "lessThanOrEquals";
	}elsif ($exp eq "ge") {
	    $test_type = "greaterThanOrEquals";
	}
	if ($lhs =~ /^\[.+\]$/) {
	    $test_meta = $lhs;
	    $test_value = $rhs;
	} else {
	    # assume rhs has meta
	    $test_meta = $rhs;
	    $test_value = $lhs;
	}
	
	#remove beginning and end quotes
	$test_value =~ s/^[\'\"]//;
	$test_value =~ s/[\'\"]$//;
    }
    my $test_atts = "test='$test_type' ";
    if (defined $test_value) {
	$test_atts .= "test-value='$test_value' ";
    }
    #print STDERR "test, true, false = $test, $true_option, $false_option\n";
    my $new_format = "<gsf:switch>$test_meta";
    $new_format .= "<gsf:when $test_atts>$true_option</gsf:when>";
    if (defined $false_option) {
	$new_format .="<gsf:otherwise>$false_option</gsf:otherwise>";
    }
    $new_format .= "</gsf:switch>";
	
    #print STDERR "new format = $new_format\n";
    return $new_format;
}

sub format_or {
    my ($or_string) = @_;
    my @meta_list = split (',', $or_string);
    return "" unless scalar (@meta_list);
    my $new_format = "<gsf:choose-metadata>";
    foreach my $m (@meta_list) {
	if ($m =~ /^\[(.*)\]$/) {
	    $new_format .= &format_metadata($1);
	} else {
	    # a default value
	    $new_format .= "<gsf:default>$m</gsf:default>";
	    last;
	}
    }
    $new_format .= "</gsf:choose-metadata>";
    return $new_format;
}

sub open_database {
    my ($db_file) = @_;
    
    my $database = ();
#    tie (%$database, 'GDBM_File', $db_file, GDBM_READER, 0400) ||
#	die "Couldn't open database $db_file\n";

    return $database;
}

sub close_database {
    my ($database) = @_;
    untie %$database;
}
sub isHorizontalClassifier {
    my ($database, $name) = @_;

    return 0; # can't get this to work for windows
    my $record = $database->{$name};
    my ($childtype) = $record =~ /<childtype>(\w*)/;
    if ($childtype eq "HList") {
	return 1;
    }
    return 0;
}
#$writer->startTag('');
#$writer->endTag('');
#$writer->characters();
#$writer->emptyTag('');

1;

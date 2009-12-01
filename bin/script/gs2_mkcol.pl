#!/usr/bin/perl -w

###########################################################################
#
# gs2_mkcol.pl -- create the framework for a gs2 compatible collection in 
# gs3
# A component of the Greenstone digital library software
# from the New Zealand Digital Library Project at the 
# University of Waikato, New Zealand.
#
# Copyright (C) 1999 New Zealand Digital Library Project
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
#
###########################################################################


# This program will setup a new collection from a model one. It does this by
# copying the model, moving files to have the correct names, and replacing
# text within the files to match the parameters.

package gs2_mkcol;

BEGIN {
    die "GSDLHOME not set\n" unless defined $ENV{'GSDLHOME'};
    die "GSDLOS not set\n" unless defined $ENV{'GSDLOS'};
    unshift (@INC, "$ENV{'GSDLHOME'}/perllib");
}

use parsargv;
use util;
use cfgread;

sub print_usage {
    print STDOUT "\n";
    print STDOUT "gs2_mkcol.pl: Creates the directory structure for a new\n";
    print STDOUT "          Greenstone2 compatible collection in Greenstone3.\n\n";
    print STDOUT "  usage: $0 -site site-home -creator email [options] collection-name\n\n";
    print STDOUT "  options:\n";
    print STDOUT "   -optionfile file    Get options from file, useful on systems where\n";
    print STDOUT "                       long command lines may cause problems\n";
    print STDOUT "   -collectdir         Directory where new collection will be created.\n";
    print STDOUT "                       Default is " . 
	&util::filename_cat("site-home", "collect") . "\n";
    print STDOUT "   -maintainer email   The collection maintainer's email address (if\n";
    print STDOUT "                       different from the creator)\n";
    print STDOUT "   -public true|false  If this collection has anonymous access\n";
    print STDOUT "   -buildtype mg|mgpp  Whether to use mg or mgpp to build the collection\n";
    print STDOUT "                       Default is mgpp.\n";
    print STDOUT "   -title text         The title for the collection\n";
    print STDOUT "   -about text         The about text for the collection\n";
    print STDOUT "   -plugin text        perl plugin module to use (there may be multiple\n";
    print STDOUT "                       plugin entries)\n";
    print STDOUT "   -quiet              Operate quietly\n";
    print STDOUT "  Note that -creator and -site must be specified. You can make changes to all\n";
    print STDOUT "  options later by editing the collect.cfg configuration file for your\n";
    print STDOUT "  new collection (it'll be in the \"etc\" directory).\n\n";
    print STDOUT "  [Type \"perl -S mkcol.pl | more\" if this help text scrolled off your screen]";
    print STDOUT "\n" unless $ENV{'GSDLOS'} =~ /^windows$/i;
}

sub traverse_dir
{
    my ($modeldir, $coldir) = @_;
    my ($newfile, @filetext);

    if (!(-e $coldir)) {

	my $store_umask = umask(0002);
	my $mkdir_ok = mkdir ($coldir, 0777);
	umask($store_umask);

	if (!$mkdir_ok) 
	{
	    die "$!";
	}
    }

    opendir(DIR, $modeldir) || die "Can't read $modeldir";
    my @files = grep(!/^(\.\.?|CVS)$/, readdir(DIR));
    closedir(DIR);

    foreach $file (@files)
    {
	my $thisfile = &util::filename_cat ($modeldir, $file);
	if (-d $thisfile) {
	    my $colfiledir = &util::filename_cat ($coldir, $file);
	    traverse_dir ($thisfile, $colfiledir);

	} else {
	    my $destfile = $file;
	    $destfile =~ s/^modelcol/$collection/;
	    $destfile =~ s/^MODELCOL/$capcollection/;
	    print STDOUT "  doing replacements for $destfile\n" unless $quiet;
	    $destfile = &util::filename_cat ($coldir, $destfile);
	    
	    open (INFILE, $thisfile) || 
		die "ERROR: Can't read file $thisfile";
	    open (OUTFILE, ">$destfile") ||
		die "ERROR: Can't create file $destfile";

	    while (defined ($line = <INFILE>)) {
		$line =~ s/\*\*collection\*\*/$collection/g;
		$line =~ s/\*\*COLLECTION\*\*/$capcollection/g;
		$line =~ s/\*\*creator\*\*/$creator/g;
		$line =~ s/\*\*maintainer\*\*/$maintainer/g;
		$line =~ s/\*\*public\*\*/$public/g;
		$line =~ s/\*\*title\*\*/$title/g;
		$line =~ s/\*\*about\*\*/$about/g;
		$line =~ s/\*\*plugins\*\*/$pluginstring/g;
		$line =~ s/\*\*buildtype\*\*/$buildtype/g;
		$line =~ s/\*\*searchtype\*\*/$searchtype/g;
		$line =~ s/\*\*indexes\*\*/$indexes/g;
		$line =~ s/\*\*defaultindex\*\*/$defaultindex/g;
		$line =~ s/\*\*indexmeta\*\*/$indexmeta/g;
		$line =~ s/\*\*xmlindexes\*\*/$xmlindexes/g;
		#$line =~ s/\*\*xmlplugins\*\*/$xmlpluginstring/g;
		
		print OUTFILE $line;
	    }
	    
	    close (OUTFILE);
	    close (INFILE);
	}
    }
}

# get and check options
sub parse_args {
    my ($argref) = @_;
    if (!&parsargv::parse($argref,
			  'optionfile/.*/', \$optionfile,
			  'collectdir/.*/', \$collectdir,
			  'site/.*/', \$sitehome,
			  'creator/\w+\@[\w\.]+/', \$creator,
			  'maintainer/\w+\@[\w\.]+/', \$maintainer,
			  'public/true|false/true', \$public,
			  'buildtype/mg|mgpp/mgpp', \$buildtype,
			  'title/.+/', \$title,
			  'about/.+/', \$about,
			  'plugin/.+', \@plugin,
			  'quiet', \$quiet,
			  )) {
	&print_usage();
	die "\n";
    }
}

sub main {

    &parse_args (\@ARGV);
    if ($optionfile =~ /\w/) {
	open (OPTIONS, $optionfile) || die "Couldn't open $optionfile\n";
	my $line = [];
	my $options = [];
	while (defined ($line = &cfgread::read_cfg_line ('mkcol::OPTIONS'))) {
	    push (@$options, @$line);
	}
	close OPTIONS;
	&parse_args ($options);

    }

    # load default plugins if none were on command line    
    if (!scalar(@plugin)) {
	@plugin = (ZIPPlug,GAPlug,TEXTPlug,HTMLPlug,EMAILPlug,
		   PDFPlug,RTFPlug,WordPlug,PSPlug,ArcPlug,RecPlug);
    }

    # get and check the collection name
    ($collection) = @ARGV;
    if (!defined($collection)) {
	print STDOUT "ERROR: no collection name was specified\n";
	&print_usage();
	die "\n";
    }

    if (length($collection) > 8) {
	print STDOUT "ERROR: The collection name must be less than 8 characters\n";
	print STDOUT "       so compatibility with earlier filesystems can be\n";
	print STDOUT "       maintained.\n";
	die "\n";
    }

    if ($collection eq "gs2model" || $collection eq "gs3model") {
	print STDOUT "ERROR: No collection can be named gs2model or gs3model as these are  the\n";
	print STDOUT "       names of the model collections.\n";
	die "\n";
    }

    if ($collection eq "CVS") {
	print STDOUT "ERROR: No collection can be named CVS as this may interfere\n";
	print STDOUT "       with directories created by the CVS versioning system\n";
	die "\n";
    }

    #check that -site has been specified
    if (!defined($sitehome) || $sitehome eq "") {
	print STDOUT "ERROR: The site was not defined. This variable is\n";
	print STDOUT "       needed to locate the collect directory.\n";
	die "\n";
    }
    #check that its a valid directory
    if (!-d $sitehome) {
	print STDOUT "ERROR: $sitehome doesn't exist\n";
	die "\n";
    }
    if (!defined($creator) || $creator eq "") {
	print STDOUT "ERROR: The creator was not defined. This variable is\n";
	print STDOUT "       needed to recognise duplicate collection names.\n";
	die "\n";
    }

    if (!defined($maintainer) || $maintainer eq "") {
	$maintainer = $creator;
    }

    $public = "true" unless defined $public;
    $buildtype = "mgpp" unless defined $buildtype;

    $searchtype = "";
    if ($buildtype eq "mgpp") {
	$searchtype = "searchtype  plain form";
    }
    
    if (!defined($title) || $title eq "") {
	$title = $collection;
    }

    # get capitalised version of the collection
    $capcollection = $collection;
    $capcollection =~ tr/a-z/A-Z/;

    # get the strings to include.
    $pluginstring = "";
    foreach $plugin (@plugin) {
	if ($plugin eq RecPlug) {
	    $pluginstring .= "plugin         $plugin -use_metadata_files\n";
	} else {
	    $pluginstring .= "plugin         $plugin\n";
	}
    }

    $mdir = &util::filename_cat ($sitehome, "collect", "gs2model");
    
    if (defined $collectdir && $collectdir =~ /\w/) {
	if (!-d $collectdir) {
	    print STDOUT "ERROR: $collectdir doesn't exist\n";
	    die "\n";
	}
	$cdir = &util::filename_cat ($collectdir, $collection);
    } else {
	$cdir = &util::filename_cat ($sitehome, "collect", $collection);
    }

    # make sure the model collection exists
    die "ERROR: Cannot find the model collection $mdir" unless (-d $mdir);

    # make sure this collection does not already exist
    if (-e $cdir) {
	print STDOUT "ERROR: This collection already exists\n";
	die "\n";
    }

    # set up the default indexes - this could be a command line option at some stage
    # the names are added in here for the xml ones, but they should be added after building once the names are known.
    if ($buildtype eq "mg") {
	$indexes = "document:text document:Title document:Source";
	$defaultindex = "defaultindex document:text";
	$indexmeta = "collectionmeta .document:text    \"text\"\ncollectionmeta .document:Title  \"titles\"\ncollectionmeta .document:Source  \"filenames\"\n";
    } elsif ($buildtype eq "mgpp") {
	$indexes = "allfields text metadata";
	$defaultindex = "";
	$indexmeta = "collectionmeta .text    \"text\"\ncollectionmeta .allfields \"entire documents\"\n";
    } else {
	print STDOUT "Error: buildtype should be mg or mgpp, but its $buildtype\n";
    }
    # start creating the collection
    print STDOUT "\nCreating the collection $collection...\n" unless $quiet;
    &traverse_dir ($mdir, $cdir);
    print STDOUT "\nThe new collection was created successfully at\n" unless $quiet;
    print STDOUT "$cdir\n" unless $quiet;
}

&main ();

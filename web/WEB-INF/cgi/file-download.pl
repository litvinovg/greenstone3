#!/usr/bin/perl -w

use strict;
use LWP::Simple;

my $iis6_mode = 0;

if($iis6_mode)
{
	chdir("cgi-bin");
}

eval("require \"gsdlCGI.pm\"");

if($@){
	print STDOUT "Content-type:text/plain\n\n";
    print STDOUT "ERROR: $@\n";
    exit 0;
}

sub main{
	
	my $gsdl_cgi = new gsdlCGI();
	
	$gsdl_cgi->setup_gsdl();
	
	$gsdl_cgi->parse_cgi_args();
	
	$gsdl_cgi->{'xml'} = 0;
	
	my $filename = $gsdl_cgi->clean_param("filename");
	my @fileholder;
	
	if(defined $filename){
		
		my $site = $gsdl_cgi->clean_param("site");		#site name
		my $c = $gsdl_cgi->clean_param("c");			#collection name
		my $assoc = $gsdl_cgi->clean_param("assoc");	#assocfilepath value
		
		if(!defined $site){
			$gsdl_cgi->generate_error("No site specified.");
		}
		
		my $collect_dir = $gsdl_cgi->get_collection_dir($site);
		
		my $dir = &util::filename_cat($collect_dir,$c,"index/assoc");
		
		my $download_file = "$dir\\$assoc\\$filename";

		open(DOWNLOAD_FILE, "<$download_file") or die "$!";
		binmode DOWNLOAD_FILE;
		@fileholder = <DOWNLOAD_FILE>;
		
		my $size = -s $download_file;
		close DOWNLOAD_FILE;
		
		print "Content-Type: image/jpeg\n";
		print "Content-Length: $size\n";
		print "Content-Disposition:attachment;filename=".$download_file."\n\n";
		print @fileholder;
		
	}else{
		$gsdl_cgi->generate_error("No filename specified.");
	}
}

&main();
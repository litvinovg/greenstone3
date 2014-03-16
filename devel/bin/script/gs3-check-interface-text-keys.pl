#!/usr/bin/perl -w

package xxx;

BEGIN {
    die "GSDL3HOME not set\n" unless defined $ENV{'GSDL3HOME'};
    die "GSDLHOME not set\n" unless defined $ENV{'GSDLHOME'};
    unshift (@INC, "$ENV{'GSDLHOME'}/perllib");
}

use util;

use strict;
no strict 'refs'; # allow filehandles to be variables and vice versa
no strict 'subs'; # allow barewords (eg STDERR) as function arguments

my $keys_hash ={};
my $keys_not_found = {};

&main();

sub main {


    my ($interface_name) = $ARGV[0];
    print STDERR "interface _name = $interface_name\n";

    &load_interface_dictionary($interface_name);
    &load_interface_dictionary($interface_name."2");

    my $transform_dir = &util::filename_cat($ENV{'GSDL3HOME'}, "interfaces", $interface_name, "transform");

    &scan_directory($transform_dir);
   
    &output_results($interface_name);
}

sub load_interface_dictionary {
    my ($interface_name) = @_;

    my $dictionary_file = &util::filename_cat($ENV{'GSDL3HOME'}, 'WEB-INF', 'classes', "interface_$interface_name.properties");
    print STDERR "dict file = $dictionary_file\n";
    if (!-e $dictionary_file) {
	die "Error, $dictionary_file does not exist\n";
    }

    unless (open(FIN, "<$dictionary_file")) {
	die "Error, couldn't read in $dictionary_file\n";
    }

    
    while (my $line = <FIN>) {
	next if $line =~ /^#/;
	my ($key) = $line =~ /^([^=]*)=/;
	print STDERR "key = $key\n" if defined $key;
	$keys_hash->{$key} = 0 if defined $key;
    }
    close(FIN);
    
}

sub scan_directory {
    my ($src_dir) = @_;
    print STDERR "scan dir $src_dir\n";
    opendir(DIR, "$src_dir");
    my @files= readdir(DIR); 
    close(DIR);

    foreach my $file (@files) {     
	# process all except . and ..
	next if($file eq "." || $file eq ".." || $file eq ".svn");
	# make absolute
	$file = &util::filename_cat($src_dir, $file);
	if (-d $file) {
	    &scan_directory($file);
	} else {
	    &scan_file($file);
	}
    }
	    
}

sub scan_file {
    my ($src_file) = @_;
    print STDERR "scan file $src_file\n";
    open(XIN, $src_file);
    my $num=0;
    while (my $line = <XIN>) {
	$num++;
#	my ($params) = ($line =~ /util:getInterfaceText\((.*)\)/);#
#	if (defined $params ) {
#	    print STDERR "params = $params\n";
#	    my @ps = split(/,/, $params);
#	    my $kk = @ps[2];
#	    print STDERR "other key = $kk\n";
#	}
	my @matches = ($line =~ /util:getInterfaceText(?:WithDOM)?\((.*?)\)/g);
	foreach my $m (@matches) {
	    my @params = split(/,/, $m);
	    my $kk = @params[2];
	    print STDERR "old kk = $kk, ";
	    $kk =~ s/^\s+//;
	    $kk =~ s/\s+$//;
	    $kk =~ s/^'//;
	    $kk =~ s/'$//;
	    print STDERR "new kk = $kk\n";
	    if (defined $keys_hash->{$kk}) {
		$keys_hash->{$kk}++;
		print STDERR "found key $kk\n";
	    } else {
		$keys_not_found->{$kk} = "$src_file, line $num";
		print STDERR "not found key $kk, $src_file, line $num\n";
	    }

	}
    }

    close(XIN);
}

sub output_results {
    my ($interface_name) = @_;
    my $out_file = $interface_name."_notdefined.txt";
    open(ROUT, ">$out_file");
    
    foreach my $k (keys(%{$keys_not_found})) {
	print ROUT "$k, $keys_not_found->{$k}\n";
    }
    close (ROUT);
    $out_file = $interface_name."_notused.txt";
    open (ROUT, ">$out_file");

    foreach my $k (sort keys(%{$keys_hash})) {
	if ($keys_hash->{$k} == 0) {
	    print ROUT "$k\n";
	}
    }
    close ROUT;
}
1;

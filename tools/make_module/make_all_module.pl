#!/usr/bin/perl

# ����ˡ
# $ ./make_all_module.pl allclasses-frame.html | bash

# ������Υǥ��쥯�ȥ�
#$DIR = "~/workspace/lmntal/lib/java";
$DIR = "/tmp/lib";

while (<>) {
	if (/title="class in/ && /HREF="(.+)\/(\w+).html"/) {
		$class = $module = "$1/$2";
		$class =~ tr/\//./;
		$module =~ tr/\//_/;
		$module = lc($module);
		print "javap -public $class | ./make_module.pl > $DIR/$module.lmn\n";
	}
}

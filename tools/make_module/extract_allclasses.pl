#!/usr/bin/perl
# allclasses-frame.html ���� ���饹̾��ȴ���Ф�

while (<>) {
	if (/title="class in/ && /HREF="(.+)\/(\w+).html"/) {
		$class = "$1/$2";
		$class =~ tr/\//./;
		print "$class\n";
	}
}

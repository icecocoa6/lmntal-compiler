#!/usr/bin/perl
# allclasses-frame.html ���饯�饹�ޤ��ϥ��󥿥ե�������ȴ���Ф�
#
# ������(1) ���饹�����
# ./extract_all.pl allclasses-frame.html
#
# ������(2) ���󥿥ե����������
# ./extract_all.pl -i allclasses-frame.html

$type = "class";

# ���ץ�������Ϥ���
use Getopt::Std;
my $opt = {};
getopts('i', $opt);
$i = $opt->{'i'};
if ($i ne "") {
	$type = "interface";
}
 
while (<>) {
	if (/title="$type in/ && /HREF="(.+)\/(\w+).html"/) {
		$class = "$1/$2";
		$class =~ tr/\//./;
		print "$class\n";
	}
}

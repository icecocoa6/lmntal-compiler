#!/usr/bin/perl
#
# ����ˡ
# �ޤ��Ť� LMNtal �⥸�塼�뤬����ǥ��쥯�ȥ�˰�ư����
# $ ./copy_new_module.pl *.lmn | bash

# �������⥸�塼��ե����뤬����ǥ��쥯�ȥ�
$DIR = "/tmp/lib";

foreach $f (@ARGV) {
	open(DIFF, "diff $f $DIR/$f |");
	# �ǽ��8�Ԥ�̵�������վ��󤬰ۤʤ뤿��̵��
	for ($i = 0; $i < 8; $i++) {
		<DIFF>;
	}
	
	if (<DIFF>) {
		print "cp $DIR/$f .\n";
	}
	close(DIFF);
}

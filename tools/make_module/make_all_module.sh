#!/bin/sh

# ����ˡ
# $ ./make_all_module.sh allclasses.txt

# ������Υǥ��쥯�ȥ�
dir=~/workspace/lmntal/lib/java_lib;

# allclasses.txt ��1�Ԥ����ɤ߹���
for class in `cat $1`; do
	module=`echo $class | tr . _`
	javap -public $class | ./make_module.pl > $dir/$module.lmn
	echo $class
done

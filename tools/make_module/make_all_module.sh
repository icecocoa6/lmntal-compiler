#!/bin/sh

# ����ˡ
# $ ./make_all_module.sh allclasses.txt

# ������Υǥ��쥯�ȥ�
dir="~/workspace/lmntal/lib/java";

# allclasses.txt ��1�Ԥ����ɤ߹���
for class in `cat $1`; do
	module=`echo $class | tr A-Z a-z | tr . _`
	javap -public $class | ./make_module.pl > $dir/$module.lmn
	echo $class
done
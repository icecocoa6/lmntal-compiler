#!/bin/sh

# ������Υǥ��쥯�ȥ�
dir=~/workspace/lmntal/lib/java_lib;

# ���ץ�������Ϥ���
while getopts C:h option
do
	case "$option" in
	h)	echo "Usage: ./make_all_module.sh [OPTION] CLASS_LIST_FILE"
		echo "  -h show this help"
		echo "  -C change target creation directory"
		exit
		;;
	C)	dir=$OPTARG
        ;;
    esac
done
shift $(($OPTIND - 1))

# allclasses.txt ��1�Ԥ����ɤ߹���
for class in `cat $1`; do
	module=`echo $class | tr . _`
	javap -public $class | ./make_module.pl > $dir/$module.lmn
	echo $dir/$module.lmn
done

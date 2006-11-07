#!/bin/sh

# ������Υǥ��쥯�ȥ�
lib=~/workspace/lmntal/lib/java_lib;

# ���ץ�������Ϥ���
while getopts d:h option
do
	case "$option" in
	h)	echo "Usage: ./make_all_module.sh [OPTION] CLASS_LIST_FILE"
		echo "  -h show this help"
		echo "  -C change target creation directory"
		exit
		;;
	d)	lib=$OPTARG
        ;;
    esac
done
shift $(($OPTIND - 1))

# allclasses.txt? ��1�Ԥ����ɤ߹���
for class in `cat $1`; do
#	module=`echo $class | tr . / | tr A-Z a-z`	
#	dir=`dirname $module`
	module=`echo $class | tr . _`
#	mkdir -p $lib/$dir #�ǥ��쥯�ȥ꤬�ʤ��ä��Ȥ��Τ���������������
	./class2module.pl $class > $lib/$module.lmn
	echo $lib/$module.lmn
done

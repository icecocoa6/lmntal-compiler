#!/bin/bash
#
# ���ꤵ�줿�⥸�塼�뤬�Ȥ��Ƥ���ץ�����
# sample, lib/src, lib/public ����õ���ޤ�
#
# by inui
#
# ������
# $ find_module.sh integer
# 
# �ޥå�����
#   integer.use.
#   a=integer.set(1,10).
# �ޥå����ʤ�
#   biginteger.use.   <-- �⥸�塼��̾���㤦
#   integer(3).       <-- �᥽�åɸƤӽФ��ǤϤʤ�

# �Ķ��˹�碌�����ꤷ�Ƥ�������
LMNTAL_HOME=~/workspace/lmntal

# ���������⥸�塼�뤬���뤫�ɤ���Ĵ�٤�
if [ -e $LMNTAL_HOME/lib/public/$1.lmn -o -e $LMNTAL_HOME/lib/src/$1.lmn ]; then
	grep "\\b$1\\." `find $LMNTAL_HOME/sample -name *.lmn` | cut -d: -f1 | uniq
	grep "\\b$1\\." `find $LMNTAL_HOME/lib/public -name *.lmn` | cut -d: -f1 | uniq
	grep "\\b$1\\." `find $LMNTAL_HOME/lib/src -name *.lmn` | cut -d: -f1 | uniq
else
	echo $1: module not found.
	exit
fi

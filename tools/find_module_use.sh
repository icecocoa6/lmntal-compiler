#!/bin/bash
#
# ���ꤵ�줿�⥸�塼�뤬�Ȥ��Ƥ���ץ����� sample ����õ���ޤ�
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

grep "\\b$1\\." `find $LMNTAL_HOME/sample -name *.lmn` | cut -d: -f1 | uniq

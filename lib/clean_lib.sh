#!/bin/bash
#
# ���ꤵ�줿�⥸�塼��� translate ���줿�ե�����������ޤ�
# by inui
#
# ������
# $ ./clean_lib.sh io math

for i
do
	rm -rf public/translated/module_$i
	rm -f public/translated/Module_$i.java
	rm -f public/translated/Module_$i.class
	rm -f $i.jar
done

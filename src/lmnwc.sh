#!/bin/bash
#
# �����ϤιԿ���ɽ�����륹����ץ� by inui

# �ƥѥå������ιԿ���ɽ����test �ѥå������Ͻ�����
for i in `find . -type d | grep -v test`
do
	if [ -d $i ]; then
		# *.java �ե����뤬����ʤ餽������� wc ���Ϥ�
		ls $i/*.java >& /dev/null && wc `ls $i/*.java`  -l |
		# �Ǹ�ιԡʹ�׾���ˤ����Ȥ�
		tail -1 |
		# �Կ�������äƤ��줤��ɽ������
		perl -e "split(/\s+/, <>);printf\"%-30s %7d\n\",\"${i#./}\",\$_[1];" |
	   	# �ѥå�����̾���ִ�
		sed -e 's/\//\./g'
	fi
done

echo "--------------------------------------"

# ��׿���ɽ��
wc `find . -name *.java | grep -v test` -l |
tail -1 |
perl -e "split(/\s+/, <>);printf\"%-30s %7d\n\",\"TOTAL\",\$_[1];"

# compile �ѥå������ι��
pushd compile > /dev/null
wc *.java parser/*.java parser/intermediate/*.java structure/*.java | tail -1 |
perl -e "split(/\s+/, <>);printf\"%-30s %7d\n\",\"(compile)\",\$_[1];"
popd > /dev/null

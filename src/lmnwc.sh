#!/bin/bash
#
# �����ϤιԿ���ɽ�����륹����ץ� by inui

# �ƥѥå������ιԿ���ɽ����test �ѥå������Ͻ�����
for i in `find . -type d -maxdepth 1 | grep -v "\(test\|CVS\|\.$\)"`
do
#	# *.java �ե����뤬����ʤ餽������� wc ���Ϥ�
#	ls $i/*.java >& /dev/null && wc `ls $i/*.java`  -l |
	wc `find $i -name *.java` -l |
	# �Ǹ�ιԡʹ�׾���ˤ����Ȥ�
	tail -1 |
	# �Կ�������äƤ��줤��ɽ������
	perl -e "split(/\s+/, <>);printf\"%-16s %7d\n\",\"${i#./}\",\$_[1];" |
   	# �ѥå�����̾���ִ�
	sed -e 's/\//\./g'
done

echo "------------------------"

# ��׿���ɽ��
wc `find . -name *.java | grep -v test` -l |
tail -1 |
perl -e "split(/\s+/, <>);printf\"%-16s %7d\n\",\"TOTAL\",\$_[1];"

# compile �ѥå������ι��
#wc `find compile -name *.java` |
#tail -1 |
#perl -e "split(/\s+/, <>);printf\"%-30s %7d\n\",\"(compile)\",\$_[1];"

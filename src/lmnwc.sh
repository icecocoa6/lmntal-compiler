#!/bin/bash
#
# �����ϤιԿ���ɽ�����륹����ץ� by inui

# �ƥѥå������ιԿ���ɽ����test �ѥå������Ͻ�����
for i in `find . -maxdepth 1 -type d | grep -v 'test\|CVS\|\.$'`
do
	wc `find $i -name *.java` -l |
	# �Ǹ�ιԡʹ�׾���ˤ����Ȥ�
	tail -1 |
	# �Կ�������äƤ��줤��ɽ������
	perl -e "\$_=<>;s/^\s+//;split(/\s+/);printf\"%-16s %7d\n\","${i#./}",\$_[0];" |
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

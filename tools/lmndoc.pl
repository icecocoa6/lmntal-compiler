#!/usr/bin/perl
#----------------------------------------------------------------------
# LMNtal Documentation Generator
#                                              Atsuyuki Inui
#                                              2006/01/21(��) 15:46:29
# 2006.01.22 ���פ��ɲ�
#----------------------------------------------------------------------
# -�Ȥ���
#  $ ./lmndoc.pl hoge.lmn
#
# -���ץ����
# -- --summary ���פ������Ϥ���
#
# -����
# --/** */�ǰϤޤ줿��ʬ��lmndoc�Ȥ���ǧ�������
# --�ǽ�ιԤ����Ф��Ȥʤ�
# --��Ƭ�ζ����*��̵�뤵���
# 
# -lmndoc����
# --@author '''name-text'''
# --@deprecated
# --@example '''example-text'''
# --@param '''parameter-name''' '''description'''
# --@since '''since-text'''
# --@version '''version-text'''
#
# '''example-text''', '''description'''��ʣ���Ԥ��Ϥäƽ񤯤��Ȥ���ǽ
use Getopt::Long;

GetOptions("summary");

$title = $ARGV[0];

$state = 0;
while (<>) {
    chop($_);
    s/^\s+//; #��Ƭ�ζ�������
    s/^\*+//; #��Ƭ�Υ������ꥹ�������
    s/^\s+//; #��Ƭ�ζ�������

    if ($state == 0) {
	if (/\/\*\*/) { # ���ڥ���륳���Ȥγ���
	    $state = 1;
	    
	    $tag = ""; #����ǧ�����ʣ���Ԥ��Ϥ륿��

	    $comment = "";
	    $param = "";
	    @examples = "";
	    $example = 0;
	    $author = "";
	    $version = "";
	    $since = "";
	    $deprecated = 0;
	}
    } elsif ($state == 1) {
	$aname = $_;
	$aname =~ s/[\W ]+/\-/g; #���󥫡�̾�˻Ȥ��ʤ�ʸ���ϥϥ��ե���ѹ�
	if ($opt_summary) {
	    $summary .= "-$_\n";
	} else {
	    $detail .= "***$_ &aname($aname);\n";
	    $summary .= "-[[$_>#$aname]] \n";
	}
	$state = 2;
    } elsif ($state == 2) {
	if (/\//) { # ���ڥ���륳���Ȥ���λ(*�Ͻ���Ƥ���)
	    $state = 3;
	} else {
	    if ($_ eq "") {
		next;
	    }
	    
	    # ����������å�����
	    if (/\@example(.*)/) {
		$tag = "example";
		$example++;
		if ($1 ne "") {
		    $examples[$example] .= ">>$1\n";
		}
	    } elsif (/\@param[\s]+([\w\+\-]+)[\s]*(.*)/) {
		$tag = "param";
		$param .= ">>$1 - $2~\n";
	    } elsif (/\@author(.*)/) {
		if ($author eq "") {
		    $author = ">>$1";
		} else {
		    $author .= ", $1";
		}
	    } elsif (/\@version(.*)/) {
		$version .= ">>$1\n";
	    } elsif (/\@since(.*)/) {
		$since .= ">>$1\n";
	    } elsif (/\@deprecated(.*)/) {
		$deprecated = 1;
	    } else {
		if ($tag eq "example") {
		    $examples[$example] .= ">>$_~\n";
		} elsif ($tag eq "param") {
		    $param .= ">>$_~\n";
		} else {
		    $tag = "";
		    if ($comment eq "") { # ���פϺǽ�ιԤΤ�
			$summary .= "$_~\n";
		    }
		    $comment .= ">$_~\n";
		}
	    }
	}
    } elsif ($state == 3) {
	if ($deprecated) {
	    $comment = ">'''�侩����ޤ���'''\n" . $comment;
	}

	$detail .= "$comment\n";

	# �ѥ�᡼�������
	if ($param ne "") {
	    $detail .= ">''�ѥ�᡼��''\n";
	    $detail .= $param;
	}

	# Ƴ�����줿�С����������
	if ($since ne "") {
	    $detail .= ">''Ƴ�����줿�С������''\n";
	    $detail .= $since;
	}
	
	# ������
	if ($example == 1) {
	    $detail .= ">''��''\n";
	    $detail .= "$examples[1]";
	} elsif ($example > 1) {
	    for ($i = 1; $i <= $example; $i++) {
		$detail .= ">''��$i''\n";
		$detail .= "$examples[$i]";
	    }
	}

	# �����Ԥ����
	if ($author ne "") {
	    $detail .= ">''������''\n";
	    $detail .= "$author\n"; #�Ǹ�˲��Ԥ�ɬ��
	}
	
	# �С����������
	if ($version ne "") {
	    $detail .= ">''�С������''\n";
	    $detail .= $version;
	}

	$state = 0;
    }
}

if ($opt_summary) {
    print "$summary\n";
} else {
    print "*$title\n\n";
    print "**����\n";
    print "$summary\n";
    print "**�ܺ�\n";
    print $detail;
}

#!/usr/bin/perl

# ���Υե�����Τ���ǥ��쥯�ȥ�Ǽ¹Ԥ��ʤ��Ⱦ�꤯�����ʤ�
use Cwd;
$pwd = Cwd::getcwd();

require 'check.pl';

&check($pwd);

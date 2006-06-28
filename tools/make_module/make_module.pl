#!/usr/bin/perl
# javap -public Hoge �ν��Ϥ��� LMNtal �⥸�塼���ư�������ޤ�

# java �η� => Functor
%functors = (
	"int"				=> "IntegerFunctor",
	"long"				=> "IntegerFunctor",
	"float"				=> "FloatingFunctor",
	"double"			=> "FloatingFunctor",
	"boolean"			=> "Functor",
	"java.lang.String"	=> "StringFunctor",
);

# java �η� => ������������ե��󥯥��Υ᥽�å�
%getmethods = (
	"int"				=> "intValue()",
	"long"				=> "intValue()",
	"float"				=> "floatValue()",
	"double"			=> "floatValue()",
	"boolean"			=> "getName().equals(\"false\")?false:true",
	"java.lang.String"	=> "stringValue()",
);

# java �η� => ����������
%guards = (
	"int"				=> "int",
	"long"				=> "int",
	"float"				=> "float",
	"double"			=> "float",
#	"boolean"			=> "boolean",
	"java.lang.String"	=> "string",
);

# java ������ͷ� => ��̤��֤����ȥ��Ѥ� Functor
%result_functors = (
	"int"				=> "IntegerFunctor(r)",
	"long"				=> "IntegerFunctor((int)r)",
	"float"				=> "FloatingFunctor(r)",
	"double"			=> "FloatingFunctor(r)",
	"boolean"			=> "Functor(r?\"true\":\"false\", 1, null)",
	"void"				=> "Functor(\"done\", 1, null)",
	"java.lang.String"	=> "StringFunctor(r)",
);

# �ᥤ��
$time = localtime(time);
print "//", $time, "\n\n";
while (<>) {
	# ���󥹥ȥ饯����static �᥽�åɡ����󥹥��󥹥᥽�åɤ� 3 ���ब����

	if (/Compiled from "(\w+)\.java"/) {
		$class = $1;
#		$module = lc($class);
#		print "{module($module).\n";
	} elsif (/(abstract )?class (\S+)/) {
		$abstract = $1;
		$absolute_class = $2;
		$module = lc($absolute_class);
		$module =~ tr/./_/;
		print "{module($module).\n";
	} elsif (/compareTo\(java\.lang\.Object\)/) {
		# Comparable ���󥿥ե������Υ᥽�åɤ�̵��
	} elsif (/public java\.\S+\((\S*)\)/ && $abstract eq "") {# ��ݥ��饹�ϥ��󥹥ȥ饯���ʤ�
		@args = split(/\s*,\s*/, $1);
		dump_constructor(@args);
	} elsif (/(byte)|(short)|(float)/) {
		#�Ȥꤢ����̵��
	} elsif (/public static (?:synchronized )?(\S+) (\S+)\((.*)\)/) {
		# ����ͤ�long�ΤȤ��Ͻ���
		if (!($3 =~ /\[\]/) && $1 ne "long") { #TODO Java�������LMNtal�Υꥹ�Ȥǽ���
			@args = split(/\s*,\s*/, $3);
			dump_static_method($1, $2, @args);
		}
	} elsif (/public (?:synchronized )?(\S+) (\S+)\((.*)\)/) {
		# ����ͤ�long�ΤȤ��Ͻ���
		if (!($3 =~ /\[\]/) && $1 ne "long") { #TODO Java�������LMNtal�Υꥹ�Ȥǽ���
			@args = split(/\s*,\s*/, $3);
			dump_method($1, $2, @args);
		}
	}
}
print "}.\n";

# �����ɤ���Ϥ���
sub dump_guards {
	@args = @_;
	my $guards = "";
	for ($i = 0; $i <= $#args; $i++) {
		$arg = $args[$i];
		if ($arg eq "boolean") {
			next;
		}
		if (exists($guards{$arg})) {
			$guards .= "$guards{$arg}(Arg$i),";
		} else {
			$guards .= "class(Arg$i, \"$arg\"),";
		}
	}
	chop($guards); #�Ǹ�Υ���ޤ����
	return $guards;
}

# �إå���ʬ����Ϥ���
sub dump_head {
	my ($args, $method, @args) = @_;
	print "H=$module.$method($args) :- ";
	my $guards = dump_guards(@args);
	if ($guards ne "") {
		print "$guards | ";
	}
	print "H=[:/*inline*/\n";
}

# ���󥹥ȥ饯������Ϥ���
sub dump_constructor {
	@args = @_;
	$argc = $#args+1;

	$ARGS=make_lmntal_args($argc);
	dump_head($ARGS, "new", @args);

	$args = dump_args(0, @args);
	print "\ttry {\n";
	print "\t\tAtom o = mem.newAtom(new ObjectFunctor(new $absolute_class($args)));\n";
	print "\t\tmem.relink(o, 0, me, $argc);\n";
	print "\t} catch (Exception e) { System.err.println(e); }\n";

	for ($i = 0; $i < $argc; $i++) {
		print "\tme.nthAtom($i).remove();\n";
	}
	print "\tme.remove();\n";
	print "\t:]($ARGS).\n";
	print "\n";
}

# �᥽�åɤ���Ϥ���
sub dump_method {
	my ($type, $method, @args) = @_;
	$argc = $#args+1;

	$ARGS = make_lmntal_args($argc+1);

	dump_head("${ARGS}", lcfirst($method), ($absolute_class,@args));

#	print "class($class, \"$absolute_class\") | H=[:/*inline*/\n";
	print "\t$absolute_class o = ($absolute_class) ((ObjectFunctor)me.nthAtom(0).getFunctor()).getObject();\n";
	
	$args = dump_args(1, @args);

	printf "\ttry {\n";
	if ($type eq "void") {
		printf "\t\to.$method($args);\n";
	} else {
		printf "\t\t$type r = o.$method($args);\n";
	}
	printf "\t\tmem.relink(me.nthAtom(0), 0, me, %d);\n", $argc+1;
	printf "\t\t%s", dump_result_atom($type);
	printf "\t\tmem.relink(result, 0, me, %d);\n", $argc+1;
	printf "\t} catch (Exception e) { System.err.println(e); }\n";

	dump_tail($argc+1, "${ARGS}");
}

# static �᥽�åɤ���Ϥ���
sub dump_static_method {
	($type, $method, @args) = @_;
	$argc = $#args+1;

	$ARGS = make_lmntal_args($argc);

	dump_head(${ARGS}, lcfirst($method), @args);

	$args = dump_args(0, @args);

	print "\ttry {\n";
	if ($type eq "void") {
		print "\t\t$absolute_class.$method($args);\n";
	} else {
		print "\t\t$type r = $absolute_class.$method($args);\n";
	}
	print "\t\tmem.relink(me.nthAtom(0), 0, me, $argc);\n";
	print "\t\t", dump_result_atom($type);
	print "\t\tmem.relink(result, 0, me, $argc);\n";
	print "\t} catch (Exception e) { System.err.println(e); }\n";

	dump_tail($argc, $ARGS);
}

# �Ǹ�ν���
sub dump_tail {
	my ($argc, $args) = @_;
	for ($i = $argc-1; $i >= 0; $i--) {
		print "\tme.nthAtom($i).remove();\n";
	}
	print "\tme.remove();\n";
	print "\t:]($args).\n";
	print "\n";
}

# LMNtal �ΰ�����ʸ�������������
sub make_lmntal_args {
	my ($argc) = @_;
	my $args = "";
	for ($i = 0; $i < $argc; $i++) {
		$args .= "Arg$i,";
	}
	chop($args); #�Ǹ�Υ���ޤ����
	return $args;
}

# �����������������ν���
sub dump_args {
	my ($start, @args) = @_;
	my $argc = $#args;
	my $args = "";
	for ($i = 0; $i <= $argc; $i++) {
		my $type = $args[$i];
		$type =~ s/\s//;
		$args .= "v$i,";
		if (exists($functors{$type})) {
			$functor = $functors{$type};
			$cast = "";
			$getmethod = $getmethods{$type};
		} else {
			$functor = "ObjectFunctor";
			$cast = "($type) ";
			$getmethod = "getValue()";
		}
		printf "\t$type v$i = $cast(($functor)me.nthAtom(%d).getFunctor()).$getmethod;\n", $i+$start;
	}
	chop($args); #�Ǹ�Υ���ޤ����
	return $args;
}

# ��̤��֤����ȥ��������������ν���
sub dump_result_atom {
	my ($type) = @_;

	if (exists($result_functors{$type})) {
		$functor = $result_functors{$type};
	} else {
		$functor = "ObjectFunctor(r)";
	}
	return "Atom result = mem.newAtom(new $functor);\n";
}

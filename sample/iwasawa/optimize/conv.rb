#!/usr/bin/ruby
# -*- coding: utf-8 -*-

flag = true
isInit = true
rulesetIndex = 0;
ruleIndex = 0;


print "rulesets{\n\n"

STDIN.each do | line |
  line.chomp!
  if line == "" || line =~ /Inline/ 
  elsif /Compiled\sRuleset\s*@([0-9]+)/ =~line # ruleset �γ���
    if isInit
      print "initRuleset{ +RulesetIndex#{rulesetIndex}, ruleset(#{$1}), \n"
    else
      i = 1;
      print "ruleList = [ RuleIndex#{rulesetIndex}_0"
      while i < ruleIndex
        print ", RuleIndex#{rulesetIndex}_#{i}"
        i += 1;
      end
      print " ].\n\n"
      rulesetIndex += 1;
      ruleIndex = 0;
      print "}.\n"
      print "ruleset{ +RulesetIndex#{rulesetIndex}, ruleset(#{$1}), \n"
    end
  elsif line == "Compiled Rule " # rule �γ���
    print "rule{\n+RuleIndex#{rulesetIndex}_#{ruleIndex}, compiledRule = [\n"
    ruleIndex += 1;
  elsif line =~ /atommatch/
    flag = false
  elsif isInit && line =~ /body/
    flag = true
  elsif !isInit && line =~ /memmatch/
    flag = true
  elsif flag
    op = line.sub(/\'\[\]\'_([0-9]+)/, 'functor(\'listnil\', \1)') # [] ���ȥ��������̰���
    op = op.sub(/\'(\w+)\'.\'(\w+)\'_([0-9]+)/, 'moduleFunctor(\1, \2, \3)') # io.use �����̰���
    op = op.sub(/\'([\w\$]+)\'_([0-9]+)/, 'functor(\1, \2)') # �ե��󥯥��� lmntal syntax ��
    op = op.sub(/(\$\w+)_([0-9]+)/, 'proxyFunctor("\1", \2)') # �ե��󥯥��� lmntal syntax ��
    op = op.sub(/(-[0-9]+)_([0-9]+)/, 'intFunctor(\1, \2)') #��int ���ȥ�����̰���
    op = op.sub(/([0-9]+)_([0-9]+)/, 'intFunctor(\1, \2)') #��int ���ȥ�����̰���
    op = op.sub(/\"([\w\s@]+)\"_([0-9]+)/, 'stringFunctor(\1, \2)') # string ���ȥ�����̰���
    op = op.sub(/\'.\'+_([0-9]+)/, 'functor(\'.\', \1)')  # . ���ȥ�ξ������̰���
    op = op.sub(/@([0-9]+)/, 'rulesetNum(\1)') # �롼�륻�å��ֹ�� lmntal syntax ��

    op = op.sub(/([a-z]+)\s*(\[[^\]]*\])/, '[\1, \2]') # ̿��Ȱ�����ꥹ�Ȥ�

    op = op.sub(/listnil/, '[]') # ���������פ��� [] ���᤹
    if op =~ /\s*\[\s*proceed\s*,\s*\[\s*\]\s*\]$/ # rule �ν�λ
      isInit = false
      print "#{op}].\n}.\n"
    else
      print "#{op}, \n"
    end
  end
end


i = 1;
print "ruleList = [ RuleIndex#{rulesetIndex}_0"
while i < ruleIndex
  print ", RuleIndex#{rulesetIndex}_#{i}"
  i += 1;
end
print " ].\n\n"
rulesetIndex += 1;

print "}.\n"

i = 1;
print "rulesetList = [ RulesetIndex0"
while i < rulesetIndex
  print ", RulesetIndex#{i}"
  i += 1;
end
print " ]."


print "}.\n"

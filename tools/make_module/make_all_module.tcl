#!/usr/bin/wish
# make_all_module.sh �� GUI �ե��ȥ����

# �ǥ��쥯�ȥ�����ΰ�
pack [frame .f1] -fill both -expand 1
pack [label .f1.l -text "target directory"] -side left
pack [entry .f1.e -width 40 -textvariable dir] -side left
button .f1.dstButton -text ... -command {
	set dir [tk_chooseDirectory]
}
pack .f1.dstButton -side left

# ���饹�ե���������ΰ�
pack [frame .f2] -fill both -expand 1
pack [label .f2.l -text "classes list file"] -side left
pack [entry .f2.e -width 40 -textvariable allclassesfile] -side left
button .f2.b -text ... -command {
	set allclassesfile [tk_getOpenFile]
}
pack .f2.b -side left

# ��̽����ΰ�
pack [frame .tframe] -fill both -expand 1
text .tframe.t -yscroll {.tframe.y set}
scrollbar .tframe.y -command {.tframe.t yview} -orient vertical
pack .tframe.t -side left -fill both
pack .tframe.y -side right -fill y

pack [frame .f3] -fill both -expand 1

button .f3.createButton -text Create -command {
	# �ǥ��쥯�ȥ�ȥե����뤬���������ꤵ��Ƥ��뤫�����å�����
	if {[file exists $dir] && [file exists $allclassesfile]} {
		# make_all_module.sh ���Ϥ��ƽ��Ϥ�ɽ������
		set fd [open "| ./make_all_module.sh -C $dir $allclassesfile" r]
		while {[gets $fd result] >= 0} {
			.tframe.t insert end "$result\n"
			update
		}
		close $fd
	}
}
pack .f3.createButton -side left

pack [button .f3.exitButton -text Exit -command exit] -side left


/*
 * ������: 2004/11/04
 */
package compile;

import java.util.*;
import runtime.Instruction;
import runtime.InstructionList;
//import runtime.Functor;
//import runtime.Env;

//import runtime.Rule;

/**
 * @author sakurai
 *
 * �����ɴط��κ�Ŭ����Ԥ��᥽�åɤ���ĥ��饹�� 
 *
 * ���ߺ�Ŭ�����оݤȤʤ�̿��
 * ALLOCATOM,DEREFATOM
 * ISINT,ISFLOAT,ISSTRING,ISUNARY
 * FADD,FSUB,FMUL,FDIV,IADD,ISUB,IMUL,IDIV,IMOD
 * FLT,FLE,FGT,FGE,ILT,ILE,IGT,IGE,FEQ,FNQ,IEQ,INQ
 */
public class GuardOptimizer {
	/** ���ȥ॰�롼�פ�ޤ����ʤ�������̿���
     *  ������̿��˴ط����륢�ȥ॰�롼����˰�ư������
     *  �ޤ�������Ϥ���2�ĤΥ��ȥ॰�롼�פ��⡢��Ǹ��줿���ȥ॰�롼�ײ��˰�ư
     */
	public static void guardMove(List head, List guard){
		if(guard == null) return;
		
		// ��Ƭ��SPEC���ĺǸ�����JUMP�Ǥʤ���缺��
		Instruction headspec = (Instruction)head.get(0);
		if (headspec.getKind() != Instruction.SPEC) return;
		int headformal = headspec.getIntArg1();
		int headvarcount = headspec.getIntArg2();
		
		int headsize = head.size();
		Instruction headjump = (Instruction)head.get(headsize-1);
		if (headjump.getKind() != Instruction.JUMP) return;
		
		Instruction guardspec = (Instruction)guard.get(0);
		if (guardspec.getKind() != Instruction.SPEC) return;
		int guardformal = guardspec.getIntArg1();
		int guardvarcount = guardspec.getIntArg2();
		
		int guardsize = guard.size();
		Instruction guardjump = (Instruction)guard.get(guardsize-1);
		if (guardjump.getKind() != Instruction.JUMP) return;
		
		//�إå�̿�����jump̿��ΰ���
		//�Ǹ�˹�������
		//�����ɤ�jump�⹹����ɬ��?
		List headmemargs = (List)headjump.getArg2();
		List headatomargs =(List)headjump.getArg3();
		
		//�����ɤ�X<Y,X+Y<Z�Τ褦�ʼ���񤯤�isint��isfloat����ʣ����Τ�;ʬ��̿�����
		//̵����礫�ʤ�̵�̤ˤʤ�ΤǤʤ�Ȥ��ʤ�ʤ���Τ�������
		for(int gid=0; gid<guardsize; gid++){
			Instruction instg = (Instruction)guard.get(gid);
			int instid = instg.getKind();
			if(instid == Instruction.ISINT 
			 || instid == Instruction.ISFLOAT
			 || instid == Instruction.ISUNARY 
			 || instid == Instruction.ISSTRING){
			 	for(int gid2=gid+1; gid2<guardsize; gid2++){
			 		Instruction instg2 = (Instruction)guard.get(gid2);
					if(instid == instg2.getKind() 
					  && instg.getIntArg1() == instg2.getIntArg1()){
					  	guard.remove(gid2);
						gid2 -= 1;
						guardsize -= 1; 
					  }
			 	}
			 }
		}
		
		for(int gid=0; gid<guardsize; gid++){
			Instruction instg = (Instruction)guard.get(gid);
			boolean expflag = false;
			switch(instg.getKind()){
				case Instruction.ALLOCATOM:
				//ALLOCATOM�ϼ������Թ礬�����Τ�head��SPEC��ľ��˰�ư�����Ƥ���
				//�롼�뤬Ŭ�ѤǤ��ʤ����㴳̵�̤������뤬�礷������ʤ�����?
					head.add(1,instg);
					headsize += 1;
					headvarcount += 1;
					headatomargs.add(new Integer(instg.getIntArg1()));
					guard.remove(gid);
					guardsize -= 1;
					gid -= 1;
					break;
                //====ALLOCATOM�Ϥ����ޤ�====//
                
                //DEREFATOM���ư������������
                //DEREFATOM��ɬ���إå�¦�˰�ư������
                //TODO ������̤�������줬�������ϸ������Ƥ��ʤ�����ʬ�Ǥ��̣�����ˤʤäƤ���������
                case Instruction.DEREFATOM:
                	//DEREFATOM����1�����������ͤ򥭡��Ȥ��륢�ȥ�򿷤��˥إå�̿����ǰ���
                	int atomvar = instg.getIntArg1();
                	//DEREFATOM����2����������DEREFATOM����³��Υ��ȥ�򼨤���
                	//���Υ��ȥ���ն��DEREFATOM���ư��������
           	        int srcatom = instg.getIntArg2();
           	        //���Ĥ���FINDATOM����1������1������FINDATOM̿��⻲�Ȥ������Τ�2���Ѱ�
           	        int finddstatom = 0;
           	        int finddstatom2 = 0;
           	        //FINDATOM̿��νи����֡�������+1�ΰ��֤�DEREFATOM���ư������
           	        int findpoint = 0;
           	        int findpoint2 = 0;
                    //FINDATOM����1������DEREFATOM����2��������٤뤳�Ȥ�DEREFATOM���ư������Τ�����
                    //�����б��ط��ϥ��ޥ���ʬ����ʤ���
                    //�Ȥꤢ����sameatomlink,sameatomgrouplink��Ĵ�����Ĥĥޥå��󥰤�ޤ�
                    //sameatomlink,sameatomgrouplink��DEREF̿��򸫤Ĥ����Ȥ��ͤ����䤹
                    int sameatomlink = 0; 
           	        int sameatomgrouplink = 0;
                    for(int hid=1; hid<headsize; hid++){
                		Instruction insth = (Instruction)head.get(hid);
                		switch(insth.getKind()){
                			case Instruction.FINDATOM:
                			    /*
                			     * �������Ǥ���������Ȥ���������Ǥ���
                			     * findpoint   FINDATOM!) [finddstatom,,,,]
                			     * 	  ��        ���δ֤ˤ���DEREF̿���򸫤ơ�sameatomlink,sameatomgrouplink
                			     *    �� 	   �򥫥���Ȥ��롣
                			     * 	  ��		    finddstatom=srcatom-sameatomgrouplink�ʤ�findpoint
                			     * 	����			finddstatom=srcatom || findstatom=srcatom+sameatomlin�ʤ�findpoint2��
                			     * 	����			DEREFATOM���ư�����롣
                			     * finpoint2   FINDATOM!) [finddstatom2,,,,]
                			     * �������������ݾڤǤ��ʤ����줬̵����Ф����������ޤ��Ԥ��褦�˻פ��롣
                			     */
                				findpoint2 = hid;
                				finddstatom2 = insth.getIntArg1();
                				//1�����˸��Ĥ���FINDATOM�ȤΥޥå���
                			    if(finddstatom == srcatom - sameatomgrouplink){
									head.add(findpoint+1, instg);
									headvarcount += 1;
									headatomargs.add(new Integer(atomvar));
									headsize += 1;
									guard.remove(gid);
									guardsize -= 1;
									gid -= 1;
									hid = headsize;
									break;                				
                				} 
                				//���󸫤Ĥ���FINDATOM�ȤΥޥå���
                				else if(finddstatom2 == srcatom
									|| finddstatom2 == srcatom + sameatomlink){
							 		  head.add(findpoint2+1, instg);
									  headvarcount += 1;
									  headatomargs.add(new Integer(atomvar));
									  headsize += 1;
									  guard.remove(gid);
									  guardsize -= 1;
									  gid -= 1;
									  hid = headsize;
									  break;                				
									}
								 findpoint = hid;
								 finddstatom = insth.getIntArg1();
                				break;
                			
                			case Instruction.DEREF:
                			 	//DEREF����2����������
                				//������ȥ�ؤΥ�󥯤ξ�� a(X,10,20),b(Y,30)�ˤ������10,20,30�ؤΥ��
                				//�Ĥޤ�Ʊ�����ȥ���ؤΥ�󥯤ξ��
                				if(finddstatom == insth.getIntArg2()) sameatomlink += 1;
                				//a(X,A),b(A,Y,10)�ˤ�����A�ؤΥ��
                				//�Ĥޤ�Ʊ�����ȥ॰�롼�����¾�Υ��ȥ�ؤΥ�󥯤ξ��
                				else sameatomgrouplink += 1;
                				break;
                				
                			
                			//�������FINDATOM�ն�˰�ư�Ǥ��ʤ��ޤޥإå�̿��κǸ�ޤ���Ƥ��ޤä���
                			//�Ǹ�˸��Ĥ���FINDATOM�ն�˰�ư������
                			//DEREFATOM�ϲ������Ǥ�إå�¦�˰�ư���Ƥ���ʤ��Ⱥ���
                			case Instruction.JUMP:
                				head.add(findpoint+1, instg);
                				headvarcount += 1;
                				headatomargs.add(new Integer(atomvar));
                				headsize += 1;
                				guard.remove(gid);
                				guardsize -= 1;
                				gid -= 1;
                				hid = headsize;
                				break;
                				
                			default: break;
                		}
                	}
                	break;
                //====DEREFATOM�Ϥ����ޤ�====//
                
				case Instruction.ISINT:
				case Instruction.ISFLOAT:
				case Instruction.ISSTRING:
				case Instruction.ISUNARY:
					//̿����оݤȤʤ륢�ȥ���ѿ��ֹ�
					atomvar = instg.getIntArg1();
					//atomvar����1�����ˤȤ�DEREFATOM�β��˰�ư������
					//���λ������ФȤʤ�DEREFATOM�ϥإå�¦�˰�ư���Ƥ���Ϥ�
					for(int hid=0; hid<headsize; hid++){
						Instruction insth = (Instruction)head.get(hid);
						if(insth.getKind() == Instruction.DEREFATOM
						   && insth.getIntArg1() == atomvar){
						   	head.add(hid+1, instg);
						   	headsize += 1;
						   	guard.remove(gid);
						   	guardsize -= 1;
						   	gid -= 1;
						   }
					}
					break;
				//====ISINT,ISFLOAT,ISSTRING,ISUNARY�Ϥ����ޤ�====//
				
				//�黻�ط���̿�����Ӵط���̿��Ϥۤ�Ʊ���������ɤ�����
				//�黻�ξ��Ϸ׻���̤Υ��ȥ��ʬ�����ѿ������䤹ɬ�פ�����Τ�expflag�Ƕ���
				case Instruction.FADD: expflag = true;
				case Instruction.FSUB: expflag = true;
				case Instruction.FMUL: expflag = true;
				case Instruction.FDIV: expflag = true;
				case Instruction.IADD: expflag = true;
				case Instruction.ISUB: expflag = true;
				case Instruction.IMUL: expflag = true;
				case Instruction.IDIV: expflag = true;
				case Instruction.IMOD: expflag = true;	
								
				case Instruction.FLT:
				case Instruction.FLE:
				case Instruction.FGT:
				case Instruction.FGE:
				case Instruction.ILT:
				case Instruction.ILE:
				case Instruction.IGT:
				case Instruction.IGE:
				case Instruction.IEQ:
				case Instruction.INE:
				case Instruction.FEQ:
				case Instruction.FNE:	
				
     				//̿�����1��������2���������
               	    //��:FLT [atomvar1, atomvar2]
               	    //   FADD [resultvar, atomvar1, atomvar2]
					int atomvar1 = 0;
					int atomvar2 = 0;
					int resultvar = 0;		
					if(expflag){
						resultvar = instg.getIntArg1();
						atomvar1 = instg.getIntArg2();
						atomvar2 = instg.getIntArg3();
					} else {
						atomvar1 = instg.getIntArg1();
						atomvar2 = instg.getIntArg2();
					}
					//atomvar1,atomvar2�Υ��ȥ����1�Ĥ򸫤Ĥ�����true�ˤ���
					boolean flag = false;
                	//DEREFATOM,ALLOCATOM,��§�黻̿��θ���
					//���Ǥ˥إå�¦�˰�ư���Ƥ���ȤߤƤ����Ϥ�
					//DEREFATOM�򸫤Ĥ�����礽�����ˤ���FINDATOM�ޤ���DEREF�򸡺����뤳�Ȥˤʤ�
					//�����Թ�����鸡�����������ɤ�����
					for(int hid=0; hid<headsize-1; hid++){
						Instruction insth = (Instruction)head.get(hid);
						int instid = insth.getKind();
						//��§�黻̿��Ϸ������Υ��ȥ������ΤǤ����Ǥ�ALLOCATOM�Ȱ�����Ʊ��
						//��������1������atomvar1,atomvar2����٤�Ȥ��������ˤ����ư㤤�Ϥʤ�
						if(instid == Instruction.ALLOCATOM
						   || instid == Instruction.FADD
						   || instid == Instruction.FSUB
						   || instid == Instruction.FMUL
					   	   || instid == Instruction.FDIV
					   	   || instid == Instruction.IADD
					   	   || instid == Instruction.ISUB
					   	   || instid == Instruction.IMUL
					       || instid == Instruction.IDIV
					       || instid == Instruction.IMOD
					       || instid == Instruction.ISINT
					       || instid == Instruction.ISFLOAT
					       || instid == Instruction.ISUNARY
					       || instid == Instruction.ISSTRING){
							int dstatom = insth.getIntArg1();
							//�����оݤ�̿�����1������atomvar1��atomuvar2
							if(dstatom == atomvar1 || dstatom == atomvar2){
                        	    if(!flag) {
                        	    	flag = true;
                        	    	continue;
                        	    } 
							//�����оݤΥ��ȥ����1�Ĥ򸫤Ĥ��Ƥ������Ǹ��Ĥ��ä����μ���̿����ư
								else {
									head.add(hid+1, instg);
									headsize += 1;
									if(expflag){
										headvarcount += 1;
										headatomargs.add(new Integer(resultvar));
									}
									guard.remove(gid);
									guardsize -= 1;
									gid -= 1;
									break;
								}
							}
						}
					}
					break;		
				//====FLT,FLE,FGT,FGE,ILT,ILE,IGT,IGE,IEQ,INE,FEQ,FNE====//
				//====FADD,FSUB,FMUL,FDIV,IADD,ISUB,IMUL,IDIV,IMOD�Ϥ����ޤ�====//
				
				default: break;
			}

		}
		
		//spec̿��ι���
		headspec.updateSpec(headformal, headvarcount);
		//jump̿��ι���
		headjump.setArg2(headmemargs); //�ٹ𤬵��ˤʤ뎥����
		headjump.setArg3(headatomargs);
        //������̿����SPEC��JUMP�����ˤʤä����
        //jump���ܥǥ�̿����ˤ���
		if(guardsize == 2) {
			InstructionList insts = (InstructionList)guardjump.getArg1();
			headjump.setArg1(insts);
			//guard.remove(0);
			//guard.remove(0);
			//SPEC,JUMP�Ͼä��٤�?
		}
		head.set(0, headspec);
		head.set(headsize-1, headjump);
	}
	
}
/*
 * task.h
 */

#ifndef LMN_TASK_H
#define LMN_TASK_H

#include "membrane.h"

/* ���̿��ǽи�����ǡ�����¤
 * LINK_LIST    ��󥯥��֥������ȤΥꥹ��
 * LIST_AND_MAP �裱���Ǥ���󥯥��֥������ȤΥꥹ�Ȥ��裲���Ǥ��ޥå�
 * MAP          �ޥå�
 */
#define LINK_LIST     1
#define LIST_AND_MAP  2
#define MAP           3

void memstack_push(LmnMembrane *mem);

#endif

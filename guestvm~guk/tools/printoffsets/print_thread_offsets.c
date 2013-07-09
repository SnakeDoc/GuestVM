/*
 * Copyright (c) 2010, 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
clude <stdio.h>
#include <guk/sched.h>
#include <list.h>

#define member_offset(member) \
  (unsigned long)(&((struct thread *)0)->member)

#define listmember_offset(member) \
  (unsigned long)(&((struct list_head *)0)->member)

int main (int argc, char **argv) {
  printf("STRUCT_THREAD_SIZE %d\n", sizeof(struct thread));
  printf("  PREEMPT_COUNT_OFFSET %d\n", member_offset(preempt_count));
  printf("  STACK_OFFSET %d\n", member_offset(stack));
  printf("  FLAGS_OFFSET %d\n", member_offset(flags));
  printf("  REGS_OFFSET %d\n", member_offset(regs));
  printf("  FPREGS_OFFSET %d\n", member_offset(fpregs));
  printf("  ID_OFFSET %d\n", member_offset(id));
  printf("  APPSCHED_ID_OFFSET %d\n", member_offset(appsched_id));
  printf("  GUK_STACK_ALLOCATED_OFFSET %d\n", member_offset(guk_stack_allocated));
  printf("  NAME_OFFSET %d\n", member_offset(name));
  printf("  STACK_OFFSET %d\n", member_offset(stack));
  printf("  STACK_SIZE_OFFSET %d\n", member_offset(stack_size));
  printf("  SPECIFIC_OFFSET %d\n", member_offset(specific));
  printf("  TIMESLICE_OFFSET %d\n", member_offset(timeslice));
  printf("  RESCHED_RUNNING_TIME_OFFSET %d\n", member_offset(resched_running_time));
  printf("  START_RUNNING_TIME_OFFSET %d\n", member_offset(start_running_time));
  printf("  CUM_RUNNING_TIME_OFFSET %d\n", member_offset(cum_running_time));
  printf("  CPU_OFFSET %d\n", member_offset(cpu));
  printf("  LOCK_COUNT_OFFSET %d\n", member_offset(lock_count));
  printf("  SP_OFFSET %d\n", member_offset(sp));
  printf("  IP_OFFSET %d\n", member_offset(ip));
  printf("  THREAD_LIST_OFFSET %d\n", member_offset(thread_list));
  printf("  READY_LIST_OFFSET %d\n", member_offset(ready_list));
  printf("  JOINERS_OFFSET %d\n", member_offset(joiners));
  printf("  AUX_THREAD_LIST_OFFSET %d\n", member_offset(aux_thread_list));
  printf("  DB_DATA_OFFSET %d\n", member_offset(db_data));

  printf("STRUCT_LIST_HEAD_SIZE %d\n", sizeof(struct list_head));
  printf("  NEXT_OFFSET %d\n", listmember_offset(next));
  printf("  PREV_OFFSET %d\n", listmember_offset(prev));

}

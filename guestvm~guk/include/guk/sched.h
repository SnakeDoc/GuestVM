/*
 * Copyright (c) 2009, 2011, Oracle and/or its affiliates. All rights reserved.
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
#ifndef __SCHED_H__
#define __SCHED_H__

#include <types.h>
#include <list.h>

#include <guk/spinlock.h>
#include <guk/traps.h>
#include <guk/time.h>
#include <guk/bug.h>

/*
struct fp_regs {
        unsigned long xmm0;
        unsigned long xmm1;
        unsigned long xmm2;
        unsigned long xmm3;
        unsigned long xmm4;
        unsigned long xmm5;
        unsigned long xmm6;
        unsigned long xmm7;
        unsigned long xmm8;
        unsigned long xmm9;
        unsigned long xmm10;
        unsigned long xmm11;
        unsigned long xmm12;
        unsigned long xmm13;
        unsigned long xmm14;
        unsigned long xmm15;
        unsigned int mxcsr;
};
*/

struct fp_regs {
    unsigned long filler1; unsigned long filler2;
    unsigned long filler3; unsigned int mxcsr; unsigned int mxcsr_mask;
    unsigned long mm0; unsigned long reserved0;
    unsigned long mm1; unsigned long reserved1;
    unsigned long mm2; unsigned long reserved2;
    unsigned long mm3; unsigned long reserved3;
    unsigned long mm4; unsigned long reserved4;
    unsigned long mm5; unsigned long reserved5;
    unsigned long mm6; unsigned long reserved6;
    unsigned long mm7; unsigned long reserved7;
    unsigned long xmm0; unsigned long xmmh0;
    unsigned long xmm1; unsigned long xmmh1;
    unsigned long xmm2; unsigned long xmmh2;
    unsigned long xmm3; unsigned long xmmh3;
    unsigned long xmm4; unsigned long xmmh4;
    unsigned long xmm5; unsigned long xmmh5;
    unsigned long xmm6; unsigned long xmmh6;
    unsigned long xmm7; unsigned long xmmh7;
    unsigned long xmm8; unsigned long xmmh8;
    unsigned long xmm9; unsigned long xmmh9;
    unsigned long xmm10; unsigned long xmmh10;
    unsigned long xmm11; unsigned long xmmh11;
    unsigned long xmm12; unsigned long xmmh12;
    unsigned long xmm13; unsigned long xmmh13;
    unsigned long xmm14; unsigned long xmmh14;
    unsigned long xmm15; unsigned long xmmh15;
    unsigned long reserved8; unsigned long reserved9;
    unsigned long reserved10; unsigned long reserved11;
    unsigned long reserved12; unsigned long reserved13;
    unsigned long reserved14; unsigned long reserved15;
    unsigned long reserved16; unsigned long reserved17;
    unsigned long reserved18; unsigned long reserved19;
};


struct thread {
    /*  0 */ int               preempt_count; /*  0 => preemptable,
                                        >0 => preemption disabled,
                                        <0 => bug*/
                                        /* (used in x86_64.S) */
    /*  4 */ u32               flags;        /* state */
    /*  8 */ struct pt_regs    *regs;            /* Registers saved in the trap frame */
    /* 16 */ struct fp_regs    *fpregs;          /* Floating point save area */
    /* 24 */ uint16_t          id;
    /* 26 */ int16_t           appsched_id;
    /* 28 */ int16_t           guk_stack_allocated;
    /* 32 */ char              *name;
    /* 40 */ char              *stack;
    /* 48 */ unsigned long     stack_size;
    /* 56 */ void              *specific;
    /* 64 */ u64               timeslice;
    /* 72 */ u64               resched_running_time; /* end of our timeslice */
    /* 80 */ u64               start_running_time;   /* value when last scheduled */
    /* 88 */ u64               cum_running_time;     /* cumulative running time */
    /* 96 */ unsigned int      cpu;
    int               lock_count;
    unsigned long     sp;  /* Stack pointer */
    unsigned long     ip;  /* Instruction pointer */
    struct list_head  thread_list; /* links thread in the global thread list */

    struct list_head  ready_list; /* links thread into run queue, zombie queue, joiner queue */
    struct list_head  joiners; /* list of threads that wait for this thread to die */
    struct list_head  aux_thread_list; /* not really needed, could use ready_list */
    void *db_data;                 /* debugger may store info here */
    unsigned long     r14;
};

extern struct list_head thread_list; /* a list of threads in the system */
extern spinlock_t thread_list_lock;  /* the lock to the thread list */

void idle_thread_fn(void *data);


#define RUNNABLE_FLAG           0x00000001     /* Thread can be run on a CPU */
#define RUNNING_FLAG            0x00000002     /* Thread is currently running */
#define RESCHED_FLAG            0x00000004     /* Scheduler should be called at
                                                  the first opportunity.
                                                  WARN: flags used explicitly in
                                                  x86_64.S, don't change */
#define DYING_FLAG              0x00000008     /* Thread scheduled to die */

#define REQ_DEBUG_SUSPEND_FLAG  0x00000010     /* Thread is to be put to sleep in
                                                  response to suspend request/
                                                  breakpoint */
#define STEPPING_FLAG           0x00000020     /* Thread is to be single stepped */
#define DEBUG_SUSPEND_FLAG      0x00000040     /* Thread was actually put to sleep
						  because of REQ_DEBUG_SUSPEND */
#define INTERRUPTED_FLAG        0x00000080     /* Thread was interrupted during last wait */

#define UKERNEL_FLAG            0x00000100     /* Thread is a ukerrnel thread */

#define JOIN_FLAG               0x00000200     /* Thread is waiting for joinee */
#define AUX1_FLAG               0x00000400     /* Predefined spare flag */
#define AUX2_FLAG               0x00000800     /* Predefined spare flag */
#define SLEEP_FLAG              0x00001000     /* Sleeping */
#define APPSCHED_FLAG           0x00002000     /* Thread is attached to the application scheduler */
#define WATCH_FLAG              0x00004000     /* Thread is at watchpoint */

#define DEFINE_THREAD_FLAG(flag_name, flag_set_prefix, funct_name)   \
static unsigned long inline flag_set_prefix##funct_name(             \
                                        struct thread *thread)       \
{                                                                    \
    return thread->flags & flag_name##_FLAG;                         \
}                                                                    \
                                                                     \
static void inline set_##funct_name(struct thread *thread)           \
{                                                                    \
    thread->flags |= flag_name##_FLAG;                               \
}                                                                    \
                                                                     \
static void inline clear_##funct_name(struct thread *thread)         \
{                                                                    \
    thread->flags &= ~flag_name##_FLAG;                              \
}

DEFINE_THREAD_FLAG(RUNNABLE, is_, runnable);
DEFINE_THREAD_FLAG(RUNNING, is_, running);
DEFINE_THREAD_FLAG(RESCHED, , need_resched);
DEFINE_THREAD_FLAG(DYING, is_, dying);
DEFINE_THREAD_FLAG(REQ_DEBUG_SUSPEND, is_, req_debug_suspend);
DEFINE_THREAD_FLAG(DEBUG_SUSPEND, is_, debug_suspend);
DEFINE_THREAD_FLAG(STEPPING, is_, stepped);
DEFINE_THREAD_FLAG(INTERRUPTED, is_, interrupted);
DEFINE_THREAD_FLAG(UKERNEL, is_, ukernel);
DEFINE_THREAD_FLAG(JOIN, is_, joining);
DEFINE_THREAD_FLAG(AUX1, is_, aux1);
DEFINE_THREAD_FLAG(AUX2, is_, aux2);
DEFINE_THREAD_FLAG(SLEEP, is_, sleeping);
DEFINE_THREAD_FLAG(APPSCHED, is_, appsched);
DEFINE_THREAD_FLAG(WATCH, is_, watchpoint);

#define switch_threads(prev, next, last) arch_switch_threads(prev, next, last)

    /* Architecture specific setup of thread creation. */
struct thread* arch_create_thread(char *name,
                                  void (*function)(void *),
                                  void *stack,
                                  unsigned long stack_size,
                                  void *data);

extern void init_sched(char *cmd_line);
extern void init_initial_context(void);
extern void run_idle_thread(void);

struct thread* create_thread(char *name, void (*function)(void *), int ukernel, void *data);
struct thread* create_debug_thread(char *name, void (*function)(void *), void *data);
struct thread* guk_create_thread_with_stack(char *name,
                                        void (*function)(void *),
					int ukernel,
                                        void *stack,
                                        unsigned long stack_size,
                                        void *data);
struct thread* create_idle_thread(unsigned int cpu);

struct thread* create_vm_thread(char *name, 
	void (*function)(void *), 
	void *stack,
	unsigned long stack_size,
	int priority,
	void *data);

void guk_schedule(void);
int guk_join_thread(struct thread *joinee);

u32 get_flags(struct thread *thread);
struct thread *get_thread_by_id(uint16_t);


void db_wake(struct thread *thread);
void guk_wake(struct thread *thread);
void guk_block(struct thread *thread);
int guk_sleep(u32 millisecs);
int guk_nanosleep(u64 nanosecs);
void guk_interrupt(struct thread *thread);
void guk_set_priority(struct thread *thread, int priority);
/* sets timeslice for given thread in millis, returns previous */
int guk_set_timeslice(struct thread *thread, int timeslice);
int guk_sched_num_cpus(void);
/* if cpu is sleeping wake it up */
void guk_kick_cpu(int cpu);
/* returns NULL if current thread is idle or stepped, else current thread */
struct thread *guk_not_idle_or_stepped(void);
/* prints entire run queue using tprintk */
void guk_print_runqueue(void);
/* prints runqueue using given print function. Include ukernel threads iff "all" */
void print_runqueue_specific(int all, printk_function_ptr printk_function);
/* prints sleep queue using given print function. Include ukernel threads iff "all" */
void print_sleep_queue_specific(int all, printk_function_ptr printk_function);

void print_sched_stats(void);
struct thread *guk_current(void);
int guk_current_id(void);

#define PREEMPT_ACTIVE     0x10000000
#define IRQ_ACTIVE         0x00100000

void guk_preempt_schedule(void);
static void inline add_preempt_count(struct thread *ti, int val)
{
    ti->preempt_count += val;
    /* Bug on overflow */
    BUG_ON(ti->preempt_count < 0);
}
#define sub_preempt_count(thread, _x)   add_preempt_count(thread, -(_x))

#define inc_preempt_count() add_preempt_count(current, 1)
#define dec_preempt_count() sub_preempt_count(current, 1)
/* Define empty version of preempt disable/enable, support needs to built into
 * the thread structure */

#define preempt_disable()  \
do {                       \
    inc_preempt_count();   \
    barrier();             \
} while (0)


#define preempt_enable_no_resched()    \
do {                                   \
    barrier();                         \
    dec_preempt_count();               \
} while (0)

#define preempt_check_resched()              \
do {                                         \
    if (unlikely(need_resched(current)))     \
        preempt_schedule();                  \
} while (0)

#define preempt_enable()         \
do {                             \
    preempt_enable_no_resched(); \
    barrier();                   \
    preempt_check_resched();     \
} while (0)

static inline int in_spinlock(struct thread *t)
{
    return t->lock_count;
}

#define is_preemptible(_ti)     (((_ti)->preempt_count == 0) ||             \
                                 ((_ti)->preempt_count == PREEMPT_ACTIVE))

/* Pushes the specified value onto the stack of the specified thread */
static inline void stack_push(struct thread *thread, unsigned long value)
{
    thread->sp -= sizeof(unsigned long);
    *((unsigned long *)thread->sp) = value;
}

extern void guk_backtrace(void **bp, void *ip);
#define backtrace guk_backtrace


/* objects linked into the sleep queue */
struct sleep_queue
{
    uint32_t flags;
    struct list_head list;
    s_time_t wakeup_time;
    struct thread *thread;
};
/* static initializer */
#define DEFINE_SLEEP_QUEUE(name)             \
    struct sleep_queue name = {              \
	.list = LIST_HEAD_INIT((name).list), \
	.thread = current,                   \
	.flags = 0,                          \
    }

static inline void init_sleep_queue(struct sleep_queue *sq)
{
    INIT_LIST_HEAD(&sq->list);
    sq->flags = 0;
}

void *guk_create_timer(void);
void guk_delete_timer(struct sleep_queue *sq);
void guk_add_timer(struct sleep_queue *sq, s_time_t timeout);
int guk_remove_timer(struct sleep_queue *sq);

#define ACTIVE_SQ_FLAG    0x00000001
#define EXPIRED_SQ_FLAG   0x00000002

#define DEFINE_SLEEP_FLAG(flag_name, flag_set_prefix, funct_name)    \
static unsigned long inline flag_set_prefix##funct_name(             \
                                        struct sleep_queue *sq)      \
{                                                                    \
    return sq->flags & flag_name##_SQ_FLAG;                          \
}                                                                    \
                                                                     \
static void inline set_##funct_name(struct sleep_queue *sq)          \
{                                                                    \
    sq->flags |= flag_name##_SQ_FLAG;                                \
}                                                                    \
                                                                     \
static void inline clear_##funct_name(struct sleep_queue *sq)        \
{                                                                    \
    sq->flags &= ~flag_name##_SQ_FLAG;                               \
}

DEFINE_SLEEP_FLAG(ACTIVE, is_, active);
DEFINE_SLEEP_FLAG(EXPIRED, is_, expired);

/* add an object to the sleep queue */
void guk_sleep_queue_add(struct sleep_queue *sq);
/* remove an object from the sleep queue */
void guk_sleep_queue_del(struct sleep_queue *sq);

#define schedule guk_schedule
#define wake guk_wake
#define block guk_block
#define sleep guk_sleep
#define nanosleep guk_nanosleep
#define preempt_schedule guk_preempt_schedule
#define print_runqueue guk_print_runqueue
#define kick_cpu guk_kick_cpu

#define MXCSRINIT 0x1f80

/*
#define save_fp_regs_asm \
  "movsd %%xmm0, 0(%[fpr])\n\t" \
  "movsd %%xmm1, 8(%[fpr])\n\t" \
  "movsd %%xmm2, 16(%[fpr])\n\t" \
  "movsd %%xmm3, 24(%[fpr])\n\t" \
  "movsd %%xmm4, 32(%[fpr])\n\t" \
  "movsd %%xmm5, 40(%[fpr])\n\t" \
  "movsd %%xmm6, 48(%[fpr])\n\t" \
  "movsd %%xmm7, 56(%[fpr])\n\t" \
  "movsd %%xmm8, 64(%[fpr])\n\t" \
  "movsd %%xmm9, 72(%[fpr])\n\t" \
  "movsd %%xmm10, 80(%[fpr])\n\t" \
  "movsd %%xmm11, 88(%[fpr])\n\t" \
  "movsd %%xmm12, 96(%[fpr])\n\t" \
  "movsd %%xmm13, 104(%[fpr])\n\t" \
  "movsd %%xmm14, 112(%[fpr])\n\t" \
  "movsd %%xmm15, 120(%[fpr])\n\t" \
  "stmxcsr 128(%[fpr])\n\t"

#define restore_fp_regs_asm \
  "movsd 0(%[fpr]), %%xmm0\n\t" \
  "movsd 8(%[fpr]), %%xmm1\n\t" \
  "movsd 16(%[fpr]), %%xmm2\n\t" \
  "movsd 24(%[fpr]), %%xmm3\n\t" \
  "movsd 32(%[fpr]), %%xmm4\n\t" \
  "movsd 40(%[fpr]), %%xmm5\n\t" \
  "movsd 48(%[fpr]), %%xmm6\n\t" \
  "movsd 56(%[fpr]), %%xmm7\n\t" \
  "movsd 64(%[fpr]), %%xmm8\n\t" \
  "movsd 72(%[fpr]), %%xmm9\n\t" \
  "movsd 80(%[fpr]), %%xmm10\n\t" \
  "movsd 80(%[fpr]), %%xmm11\n\t" \
  "movsd 96(%[fpr]), %%xmm12\n\t" \
  "movsd 104(%[fpr]), %%xmm13\n\t" \
  "movsd 112(%[fpr]), %%xmm14\n\t" \
  "movsd 120(%[fpr]), %%xmm15\n\t" \
  "ldmxcsr 128(%[fpr])\n\t"
*/


#define save_fp_regs_asm "fxsave (%[fpr])\n\t"

#define restore_fp_regs_asm "fxrstor (%[fpr])\n\t"


#endif /* __SCHED_H__ */

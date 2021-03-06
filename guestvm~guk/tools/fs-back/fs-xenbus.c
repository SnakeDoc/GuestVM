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
/*
 * xenbus connection handling
 *
 * Author: Grzegorz Milos
 */
#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>
#include <string.h>
#include <assert.h>
#include <xenctrl.h>
#include <xs.h>
#include "fsif.h"
#include "fs-backend.h"


static bool xenbus_printf(struct xs_handle *xsh,
                          xs_transaction_t xbt,
                          char* node,
                          char* path,
                          char* fmt,
                          ...)
{
    char fullpath[1024];
    char val[1024];
    va_list args;
    
    va_start(args, fmt);
    sprintf(fullpath,"%s/%s", node, path);
    vsprintf(val, fmt, args);
    va_end(args);
    printf("xenbus_printf (%s) <= %s.\n", fullpath, val);    

    return xs_write(xsh, xbt, fullpath, val, strlen(val));
}

bool xenbus_create_request_node(void)
{
    bool ret;
    struct xs_permissions perms;

    assert(xsh != NULL);
    xs_rm(xsh, XBT_NULL, WATCH_NODE);
    ret = xs_mkdir(xsh, XBT_NULL, WATCH_NODE); 
    if (!ret)
        return false;

    perms.id = 0;
    perms.perms = XS_PERM_WRITE;
    ret = xs_set_permissions(xsh, XBT_NULL, WATCH_NODE, &perms, 1);

    return ret;
}

int xenbus_register_export(struct fs_export *export)
{
    xs_transaction_t xst = 0;
    char node[1024];
    char path[1024];
    struct xs_permissions perms;

    assert(xsh != NULL);
    if(xsh == NULL)
    {
        printf("Could not open connection to xenbus deamon.\n");
        goto error_exit;
    }
    printf("Connection to the xenbus deamon opened successfully.\n");

    /* Start transaction */
    xst = xs_transaction_start(xsh);
    if(xst == 0)
    {
        printf("Could not start a transaction.\n");
        goto error_exit;
    }
    printf("XS transaction is %d\n", xst);

    /* Create node string */
    snprintf(node, sizeof(node), "%s/%d", EXPORTS_NODE, export->export_id);
    /* Remove old export (if exists) */
    xs_rm(xsh, xst, node);

	if(!(xenbus_printf(xsh, xst, node, "name", "%s", export->name) &&
         xenbus_printf(xsh, xst, node, "path", "%s", export->export_path)))
    {
        printf("Could not write the export node.\n");
        goto error_exit;
    }

    /* People need to be able to read our export */
    perms.id = 0;
    perms.perms = XS_PERM_READ;
    if(!xs_set_permissions(xsh, xst, EXPORTS_NODE, &perms, 1))
    {
        printf("Could not set permissions on the export node.\n");
        goto error_exit;
    }

    snprintf(path, sizeof(path), "%s/%s", node, "path");
    if(!xs_set_permissions(xsh, xst, path, &perms, 1))
    {
        printf("Could not set permissions on the path node.\n");
        goto error_exit;
    }

    xs_transaction_end(xsh, xst, 0);
    return 0;

error_exit:
    if(xst != 0)
        xs_transaction_end(xsh, xst, 1);
    return -1;
}

int xenbus_get_watch_fd(void)
{
    assert(xsh != NULL);
    assert(xs_watch(xsh, WATCH_NODE, "conn-watch"));
    return xs_fileno(xsh); 
}

void xenbus_read_mount_request(struct mount *mount)
{
    char *frontend, node[1024];

    sprintf(node, WATCH_NODE"/%d/%d/frontend", 
                           mount->dom_id, mount->export->export_id);
    assert(xsh != NULL);
    frontend = xs_read(xsh, XBT_NULL, node, NULL);
    mount->frontend = frontend;
    sprintf(node, "%s/state", frontend);
    assert(strcmp(xs_read(xsh, XBT_NULL, node, NULL), STATE_READY) == 0);
    sprintf(node, "%s/ring-ref", frontend);
    mount->gref = atoi(xs_read(xsh, XBT_NULL, node, NULL));
    sprintf(node, "%s/event-channel", frontend);
    mount->remote_evtchn = atoi(xs_read(xsh, XBT_NULL, node, NULL));
}

/* Small utility function to figure out our domain id */
static int get_self_id(void)
{
    char *dom_id;
    int ret;

    assert(xsh != NULL);
    dom_id = xs_read(xsh, XBT_NULL, "domid", NULL);
    sscanf(dom_id, "%d", &ret);

    return ret;
}


void xenbus_write_backend_node(struct mount *mount)
{
    char node[1024], backend_node[1024];
    int self_id;

    assert(xsh != NULL);
    self_id = get_self_id();
    printf("Our own dom_id=%d\n", self_id);
    sprintf(node, "%s/backend", mount->frontend);
    sprintf(backend_node, "/local/domain/%d/"ROOT_NODE"/%d",
                                self_id, mount->mount_id);
    xs_write(xsh, XBT_NULL, node, backend_node, strlen(backend_node));

    sprintf(node, ROOT_NODE"/%d/state", mount->mount_id);
    xs_write(xsh, XBT_NULL, node, STATE_INITIALISED, strlen(STATE_INITIALISED));
}

void xenbus_write_backend_ready(struct mount *mount)
{
    char node[1024];
    int self_id;

    assert(xsh != NULL);
    self_id = get_self_id();
    sprintf(node, ROOT_NODE"/%d/state", mount->mount_id);
    printf("backend ready: set %s to %s\n", node, STATE_READY);
    xs_write(xsh, XBT_NULL, node, STATE_READY, strlen(STATE_READY));
}


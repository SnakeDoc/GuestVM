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
package com.sun.max.ve.jdk;

import static com.sun.max.ve.jdk.AliasCast.*;

import java.io.*;

import com.sun.max.annotate.*;

/**
 * Substitutions for @see java.io.FileDescriptor.
 * @author Mick Jordan
 *
 */

@METHOD_SUBSTITUTIONS(FileDescriptor.class)
public class JDK_java_io_FileDescriptor {

    @ALIAS(declaringClass = FileDescriptor.class)
    int fd;
    
    @SuppressWarnings("unused")
    @SUBSTITUTE
    private static void initIDs() {
    }
    
    @INLINE
    public static int getFd(FileDescriptor fdObj) {
        JDK_java_io_FileDescriptor thisFileDescriptor = asJDK_java_io_FileDescriptor(fdObj);
        return thisFileDescriptor.fd;
    }

    @INLINE
    public static int setFd(FileDescriptor fdObj, int fd) {
        JDK_java_io_FileDescriptor thisFileDescriptor = asJDK_java_io_FileDescriptor(fdObj);
        return thisFileDescriptor.fd = fd;
    }

}

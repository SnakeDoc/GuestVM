/*
 * Copyright (c) 1997, 2007, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.sun.nfs;

/**
 * This exception is thrown whenever an NFS error occurs.
 */
public class NfsException extends java.io.IOException {
    int error;

    /*
     * We're fortunate that NFS v2 and v3
     * share the same error codes
     */
    public static final int NFS_OK = 0;
    public static final int NFSERR_PERM = 1;
    public static final int NFSERR_NOENT = 2;
    public static final int NFSERR_IO = 5;
    public static final int NFSERR_NXIO = 6;
    public static final int NFSERR_ACCES = 13;
    public static final int NFSERR_EXIST = 17;
    public static final int NFSERR_XDEV = 18;
    public static final int NFSERR_NODEV = 19;
    public static final int NFSERR_NOTDIR = 20;
    public static final int NFSERR_ISDIR = 21;
    public static final int NFSERR_INVAL = 22;
    public static final int NFSERR_FBIG = 27;
    public static final int NFSERR_NOSPC = 28;
    public static final int NFSERR_ROFS = 30;
    public static final int NFSERR_MLINK = 31;
    public static final int NFSERR_NAMETOOLONG = 63;
    public static final int NFSERR_NOTEMPTY = 66;
    public static final int NFSERR_DQUOT = 69;
    public static final int NFSERR_STALE = 70;
    public static final int NFSERR_REMOTE = 71;
    public static final int NFSERR_BADHANDLE = 10001;
    public static final int NFSERR_NOT_SYNC = 10002;
    public static final int NFSERR_BAD_COOKIE = 10003;
    public static final int NFSERR_NOTSUPP = 10004;
    public static final int NFSERR_TOOSMALL = 10005;
    public static final int NFSERR_SERVERFAULT = 10006;
    public static final int NFSERR_BADTYPE = 10007;
    public static final int NFSERR_JUKEBOX = 10008;

    /**
     * Create a new NfsException
     *
     * @param NFS error number for this error
     */
    public NfsException(int error) {
        super("NFS error: " + error);
        this.error = error;
    }

    @Override
    public String toString() {

        switch (error) {
    case NFS_OK:
            return ("OK");
    case NFSERR_PERM:
            return ("Not owner");
    case NFSERR_NOENT:
            return ("No such file or directory");
    case NFSERR_IO:
            return ("I/O error");
    case NFSERR_NXIO:
            return ("No such device or address");
    case NFSERR_ACCES:
            return ("Permission denied");
    case NFSERR_EXIST:
            return ("File exists");
    case NFSERR_XDEV:
            return ("Attempted cross-device link");
    case NFSERR_NODEV:
            return ("No such device");
    case NFSERR_NOTDIR:
            return ("Not a directory");
    case NFSERR_ISDIR:
            return ("Is a directory");
    case NFSERR_INVAL:
            return ("Invalid argument");
    case NFSERR_FBIG:
            return ("File too large");
    case NFSERR_NOSPC:
            return ("No space left on device");
    case NFSERR_ROFS:
            return ("Read-only file system");
    case NFSERR_MLINK:
            return ("Too many links");
    case NFSERR_NAMETOOLONG:
            return ("File name too long");
    case NFSERR_NOTEMPTY:
            return ("Directory not empty");
    case NFSERR_DQUOT:
            return ("Disk quota exceeded");
    case NFSERR_STALE:
            return ("Stale NFS file handle");
    case NFSERR_REMOTE:
            return ("Too many levels of remote in path");
    case NFSERR_BADHANDLE:
            return ("Illegal NFS file handle");
    case NFSERR_NOT_SYNC:
            return ("Update sync mismatch");
    case NFSERR_BAD_COOKIE:
            return ("Readdir cookie is stale");
    case NFSERR_NOTSUPP:
            return ("Operation not supported");
    case NFSERR_TOOSMALL:
            return ("Buffer/request too small");
    case NFSERR_SERVERFAULT:
            return ("Server fault");
    case NFSERR_BADTYPE:
            return ("Bad type");
    case NFSERR_JUKEBOX:
            return ("Jukebox error: try later");
        }
        return ("Unknown NFS error: " + error);
    }
}

/*******************************************************************************
 * openthinclient.org ThinClient suite
 *
 * Copyright (C) 2004, 2007 levigo holding GmbH. All Rights Reserved.
 *
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 *******************************************************************************/
/*
 * This code is based on: JNFSD - Free NFSD. Mark Mitchell 2001
 * markmitche11@aol.com http://hometown.aol.com/markmitche11
 */
package org.openthinclient.nfsd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openthinclient.mountd.NFSExport;
import org.openthinclient.nfsd.tea.fattr;
import org.openthinclient.nfsd.tea.ftype;
import org.openthinclient.nfsd.tea.nfs_fh;
import org.openthinclient.nfsd.tea.nfspath;
import org.openthinclient.nfsd.tea.nfstime;

/**
 * @author Joerg Henne
 */
public class NFSFile {
    private static final Logger logger = Logger.getLogger(NFSFile.class.getCanonicalName());

    public long lastAccess;
    private fattr attributes;

    private FileChannel channel;
    private boolean channelIsRW;

    private final NFSExport export;
    private final File file;
    private final nfs_fh handle;
    private nfspath linkDestination;

    // has to be non-final, since we need to set it during handle database load
    private NFSFile parentDirectory;

    NFSFile(nfs_fh handle, File file, NFSFile parentDirectory, NFSExport export) {
        this.file = file;
        this.handle = handle;
        this.parentDirectory = parentDirectory;
        this.export = export;
        updateTimestamp();
    }

    public void flushCachedAttributes() {
        attributes = null;
    }

    public nfs_fh getHandle() {
        return handle;
    }

    public long getLastAccessTimestamp() {
        return lastAccess;
    }

    public boolean isChannelOpen() {
        return channel != null;
    }

    public void setParentDirectory(NFSFile parentFile) {
        this.parentDirectory = parentFile;
    }

    public void flushCache() throws IOException {
        attributes = null;
        linkDestination = null;
        try {
            if (null != channel) {
                channel.close();
            }
        } finally {
            channel = null;
        }
    }

    synchronized fattr getAttributes() throws FileNotFoundException {
        if (!file.exists()) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("File doesn't exist (anymore?) " + file);
            }
            throw new FileNotFoundException(file.getPath());
        }

        updateTimestamp();

        if (null == attributes) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Caching attributes " + file);
            }

            attributes = new fattr();

            attributes.mode = 0777; // Note that I'm setting the permissions to ALL, because...

            attributes.uid = 1000; // ARBITRARY
            attributes.gid = 100; // ARBITRARY

            // public int size;
            attributes.size = (int) file.length();

            // public static int type;
            attributes.type = ftype.NFNON;
            if (file.isDirectory()) {
                attributes.type = ftype.NFDIR;
                attributes.mode |= NFSConstants.MISC_STDIR;
                if (attributes.size == 0) {
                    attributes.size = 1; // IRIX falls over without this
                }
            } else if (file.getName().endsWith(NFSServer.SOFTLINK_TAG)) {
                attributes.type = ftype.NFLNK;
                attributes.mode |= NFSConstants.NFSMODE_LNK;
            } else {
                attributes.type = ftype.NFREG;
                attributes.mode |= NFSConstants.MISC_STFILE;
            }

            // public int mode;
            if (file.canRead()) {
                attributes.mode |= NFSConstants.MISC_STREAD;
            }
            if (file.canWrite()) {
                attributes.mode |= NFSConstants.MISC_STWRITE;
            }

            attributes.nlink = 1; // ARBITRARY
            attributes.blocksize = 1024; // ARBITRARY
            attributes.rdev = 19; // ARBITRARY

            if (file.length() != 0) {
                attributes.blocks = (int) (1024 / file.length());
            }

            attributes.fsid = file.hashCode(); // ARBITRARY

            // Get some value for this file/dir
            attributes.fileid = PathManager.handleToInt(handle);

            attributes.atime = new nfstime(file.lastModified());
            attributes.mtime = attributes.atime;
            attributes.ctime = attributes.atime;

            // register with expiry
            CacheCleaner.registerDirtyFile(this);
        }

        return attributes;
    }

    synchronized FileChannel getChannel(boolean rw) throws IOException {
        updateTimestamp();

        if (null != channel) {
            if (rw && !channelIsRW) {
                // we need to promote the channel to rw
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Promoting channel for " + file + " to rw.");
                }

                synchronized (channel) {
                    channel.close();
                }
                RandomAccessFile raf = new RandomAccessFile(file, "rw");
                channel = raf.getChannel();
            }
        } else {
            // open the channel
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Opening channel for " + file + ". rw=" + rw);
            }
            RandomAccessFile raf = new RandomAccessFile(file, rw ? "rw" : "r");
            channel = raf.getChannel();
            channelIsRW = rw;

            // register with expiry
            CacheCleaner.registerDirtyFile(this);
        }

        return channel;
    }

    public NFSExport getExport() {
        return export;
    }

    public File getFile() {
        return file;
    }

    synchronized nfspath getLinkDestination() throws IOException {
        updateTimestamp();

        if (null == linkDestination) {
            BufferedReader r = new BufferedReader(new FileReader(file));
            String linkres = r.readLine();
            r.close();
            linkDestination = new nfspath(linkres);
            // register with expiry
            CacheCleaner.registerDirtyFile(this);
        }

        return linkDestination;
    }

    NFSFile getParentDirectory() {
        return parentDirectory;
    }

    void updateTimestamp() {
        lastAccess = System.currentTimeMillis();
    }

    boolean validateHandle(nfs_fh handle) {
        for (int i = 0; i < 8; i++) {
            if (this.handle.data[i] != handle.data[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "NFSFile for: " + file;
    }
}

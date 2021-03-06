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
package org.openthinclient.mountd;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * The default exporter simply exports all filesystem roots to anybody.
 *
 * @author levigo
 */
public class DefaultExporter implements Exporter {

    public NFSExport getExport(InetAddress peer, String mountRequest) {
        File roots[] = File.listRoots();
        mountRequest = mountRequest.toLowerCase();
        for (File root : roots) {
            if (root.getAbsolutePath().toLowerCase().startsWith(mountRequest)) {
                return new NFSExport(root.getAbsolutePath(), root);
            }
        }

        return null;
    }

    public List<NFSExport> getExports() {
        File roots[] = File.listRoots();

        List<NFSExport> exports = new ArrayList<NFSExport>(roots.length);
        for (File root : roots) {
            exports.add(new NFSExport(root.getAbsolutePath(),
                                      root.getAbsoluteFile()));
        }

        return exports;
    }
}

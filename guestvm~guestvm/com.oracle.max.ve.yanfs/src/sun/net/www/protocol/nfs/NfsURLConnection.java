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

/**
 * Open an nfs input stream given a URL.
 * @author      Robert Mines
 * @author      Caveh Jalali
 * @author      Ted Schuh
 * @version     2.00, 06/01/97
 */

package sun.net.www.protocol.nfs;

import java.net.URL;
import java.io.*;
import sun.net.www.*;
import com.sun.xfile.*;
import sun.misc.Sort;
import sun.misc.Compare;

/*
 * A class to represent an WebNFS connection to a remote object.
 *
 */

public class NfsURLConnection extends URLConnection {

    InputStream         is = null;
    XFileInputStream  nis = null;
    XFile             nfsFile = null;
    boolean             isConnected = false;

    NfsURLConnection(URL u) {
    super(u);
    }

    @Override
    public void connect() throws IOException {

        String host = url.getHost();

        try {
            java.net.InetAddress.getByName(host);
        } catch (Exception e) {
            throw new IOException("Unknown Host");
        }

        if (isConnected) {
            return;
        }

        try {
            nfsFile = new XFile(url.toString());
        } catch (Exception e) {
            throw new IOException("Unable to open xfile");
        }

        isConnected = true;  // From the URLConnection class

    }


    @Override
    public InputStream getInputStream() throws IOException {

    boolean root_directory = false;

    if (!isConnected) {
        connect();
    }

        // Check access to file
        if (!nfsFile.exists()) {
            throw new IOException("Cannot Access File " +
                nfsFile.getPath() + "!");
        }
        if (!nfsFile.canRead()) {
            throw new IOException("Read Access Not Allowed for " +
                nfsFile.getPath());
        }

        // Filter path for Web NFS
        String path = url.getFile();
        if (path.equals("/")) {
            // Convert empty paths to "/." to be more acceptible for NFS servers
            path = "/.";
        root_directory = true;
        } else if (path.charAt(0) == '/') {
            // Remove extra "/" at the beginning of paths since it is not
            // used by NFS servers
            path = path.substring(1, path.length());
        }

    MessageHeader props = new MessageHeader();
        MimeTable mt = MimeTable.getDefaultTable();
        MimeEntry entry;

    if (nfsFile.isDirectory()) {
        String[] dirList;
        StringBuffer buf = new StringBuffer();

        // Get height and width of icons for entries in directory list
        int iconHeight = Integer.getInteger("hotjava.file.iconheight",
            32).intValue();
            int iconWidth = Integer.getInteger("hotjava.file.iconwidth",
                32).intValue();

            // Mark the input stream we return as containing an HTML document
            props.add("content-type", "text/html");
            setProperties(props);

            // Begin the HTML document heading and title
            buf.append("<HTML>\n<HEAD>\n<TITLE>");
            buf.append(System.getProperty("file.dir.title",
                "Directory Listing"));
            buf.append("</TITLE>\n");

        // Set the base URL for the file name anchors
        buf.append("<BASE HREF=\"" + url.toString());
        if (url.toString().endsWith("/")) {
        buf.append("\">");
        } else {
        buf.append("/\">");
        }

            // Finish the document header, start the document body
            buf.append("</HEAD>\n<BODY>\n");

            // Display the directory name as a heading
        // In the case of the root_directory display "/"
        if (root_directory) {
                buf.append("<H1>\n/</H1>\n<HR>\n");
        } else {
                buf.append("<H1>\n" + path + "</H1>\n<HR>\n");
            }

            // Display a URL link to the parent directory if this is
        // not the root directory
        if (!root_directory) {
                String parentURL = url.toString();
                int limit = parentURL.length() - 1;
                if (url.getFile() != null) {
                if (parentURL.endsWith("/")) {
            limit--;
            }

            parentURL = parentURL.substring(0,
            parentURL.lastIndexOf('/', limit));
            buf.append("<A HREF=\"" + parentURL + "\">");
            buf.append("<H2>Go To Parent Directory</H2></A>\n<BR>\n");
        }
        }

        // Display the list of files in the directory
        dirList = nfsFile.list();
            if (dirList != null) {

                // Sort the entries in the directory list
                StringCompare strComp = new StringCompare();
                Sort.quicksort(dirList, strComp);

                boolean hideDotFiles = Boolean.getBoolean("file.hidedotfiles");

                for (int i = 0 ; i < dirList.length ; i++) {
                    XFile dirEntry;

                    // Don't display the ".." or "." directory entries
                    if (dirList[i].equals("..") || dirList[i].equals(".")) {
                        continue;
            }

                    // Skip files beginning with '.' if the file.hidedotfiles
                    // property is set
                    if (hideDotFiles) {
                        if (dirList[i].charAt(0) == '.') {
                            continue;
                        }
                    }

                    // Display an image file for each directory entry
                    buf.append("<IMG ALIGN=middle SRC=\"");
                    dirEntry = new XFile((XFile)nfsFile, dirList[i]);
            if (dirEntry.isDirectory()) {
                // TODO fix compiler error
                /*
                buf.append(MimeEntry.defaultImagePath +
                       "/directory.gif\" WIDTH=" + iconWidth +
                       " HEIGHT=" + iconHeight + ">\n");
               */
            } else if (dirEntry.isFile()) {
                String imageFileName = null;
                // TODO fix compiler error
                /*
                imageFileName = MimeEntry.defaultImagePath + "/file.gif";
                */

                // Find the file image to use using the file's .suffix
                entry = mt.findByFileName(dirList[i]);
                if (entry != null) {
                String realImageName = entry.getImageFileName();
                if (realImageName != null) {
                    imageFileName = realImageName;
                }
                }

                buf.append(imageFileName);
                buf.append("\" WIDTH=" + iconWidth + " HEIGHT=" + iconHeight +
                   ">\n");
            } else {
                // Entry is a symbolic link.  Use the default file image for now.
                // TODO fix compiler error
                /*
                buf.append(MimeEntry.defaultImagePath +
                       "/file.gif\" WIDTH=" + iconWidth +
                       " HEIGHT=" + iconHeight + ">\n");
               */
            }

            //dirEntry.close();

                    // Display the directory entry's name
                    buf.append("<A HREF=\"" + dirList[i] + "\">");
                    buf.append(dirList[i] + "</A>\n<BR>");

                }
            }

            // Finish the HTML document
            buf.append("</BODY>\n</HTML>\n");

        // Hand the input stream off to HotJava
        is = new ByteArrayInputStream(buf.toString().getBytes());

        } else {
            // Mark the input stream we return as containing a certain file type
            // by looking up the file name in the mimetable
            entry = mt.findByFileName(path);
            if (entry != null) {
                props.add("content-type", entry.getType());
            }
            setProperties(props);

            if (!nfsFile.exists()) {
                throw new IOException("Cannot Access File " + nfsFile.getPath() + "!");
            }

        // Hand the input stream off to HotJava
            is = new XFileInputStream(nfsFile);
            if (is == null) {
                throw new IOException("Unable to Open InputStream for " + url.getFile());
            }
        }

    return is;

    }

}


/**
 * StringCompare implements the Compare interface.
 * Enables the comparison of two String objects.
 */
class StringCompare implements Compare {

    /**
     * doCompare
     * @param str1 - an Object that is presumes is a String Object.
     * @param str1 - an Object that is presumes is a String Object.
     * @return -1 if str1 < str2, 0 if str1 == str2, 1 if str1 > str2
     */
    public int doCompare(Object str1, Object str2) {
        String s1, s2;

        s1 = ((String) str1).toLowerCase();
        s2 = ((String) str2).toLowerCase();
        return s1.compareTo(s2);
    }
}

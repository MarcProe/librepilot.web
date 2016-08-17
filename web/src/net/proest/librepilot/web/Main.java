/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package net.proest.librepilot.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.proest.librepilot.web.handler.DefinitionHandler;
import net.proest.librepilot.web.handler.RootHandler;
import net.proest.librepilot.web.handler.ObjectHandler;
import net.proest.librepilot.web.uavtalk.UAVTalkXMLObject;
import net.proest.librepilot.web.uavtalk.device.FcDevice;
import net.proest.librepilot.web.uavtalk.device.FcUsbDevice;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Main {
    private Map<String, UAVTalkXMLObject> mXmlObjects = null;
    private String mUavoLongHash;
    private boolean mDoReconnect;

    public static void main( String[] args ) throws Exception {

        Main o = new Main();
        System.out.println("calling load");
        o.loadXmlObjects();

        FcDevice dev = new FcUsbDevice(o.mXmlObjects);
        dev.start();

        Server server = new Server(8080);

        ContextHandler context = new ContextHandler("/");
        context.setContextPath("/");
        context.setHandler((Handler) new RootHandler());

        ContextHandler contextSettings = new ContextHandler("/settings");
        contextSettings.setHandler(new ObjectHandler(dev, true, false));

        ContextHandler contextState = new ContextHandler("/state");
        contextState.setHandler(new ObjectHandler(dev, false, true));

        ContextHandler contextObjects = new ContextHandler("/objects");
        contextObjects.setAllowNullPathInfo(true);
        contextObjects.setHandler(new ObjectHandler(dev, true, true));

        ContextHandler contextDefSettings = new ContextHandler("/defsettings");
        contextDefSettings.setHandler(new DefinitionHandler(dev, true, false));

        ContextHandler contextDefState = new ContextHandler("/defstate");
        contextDefState.setHandler(new DefinitionHandler(dev, false, true));

        ContextHandler contextDefObjects = new ContextHandler("/defobjects");
        contextDefObjects.setHandler(new DefinitionHandler(dev, true, true));

        ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.setHandlers(new Handler[] { context,
                contextSettings, contextState, contextObjects,
                contextDefObjects, contextDefSettings, contextDefState
        });

        server.setHandler(contexts);

        server.start();
        server.join();

    }

    private boolean loadXmlObjects() {
        System.out.println("starting load");
        if (mXmlObjects == null) {
            mXmlObjects = new TreeMap<>();

            String file = "web/assets/15.09-85efdd63.zip";
            ZipInputStream zis = null;
            MessageDigest crypt;
            MessageDigest cumucrypt;
            try {
                InputStream is = null;
                try {
                    is = this.getClass().getClassLoader().getResourceAsStream(file);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                        //openFileInput(UAVO_INTERNAL_PATH + getString(R.string.DASH) + file);
                zis = new ZipInputStream(new BufferedInputStream(is));
                ZipEntry ze;

                //we need to sort the files to generate the correct hash
                SortedMap<String, String> files = new TreeMap<>();

                while ((ze = zis.getNextEntry()) != null) {
                    if (ze.getName().endsWith("xml")) {

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int count;
                        while ((count = zis.read(buffer)) != -1) {
                            baos.write(buffer, 0, count);
                        }

                        String xml = baos.toString();
                        files.put(ze.getName(), xml);

                        if (xml.length() > 0) {
                            UAVTalkXMLObject obj = new UAVTalkXMLObject(xml);
                            mXmlObjects.put(obj.getName(), obj);
                        }
                    }
                }

                crypt = MessageDigest.getInstance("SHA-1");     //single files hash
                cumucrypt = MessageDigest.getInstance("SHA-1"); //cumulative hash
                cumucrypt.reset();
                for (String xmle : files.values()) {            //cycle over the sorted files
                    crypt.reset();
                    crypt.update(xmle.getBytes());              //hash the file
                    //update a hash over the file hash string representations (yes.)
                    cumucrypt.update(H.bytesToHex(crypt.digest()).toLowerCase().getBytes()); //sic!
                }

                mUavoLongHash = H.bytesToHex(cumucrypt.digest()).toLowerCase();
                VisualLog.d("SHA1", H.bytesToHex(cumucrypt.digest()).toLowerCase());

            } catch (IOException | SAXException
                    | ParserConfigurationException | NoSuchAlgorithmException e) {
                VisualLog.e("UAVO", "UAVO Load Error", e);
            } finally {
                try {
                    if (zis != null) {
                        zis.close();
                    }
                } catch (IOException e) {
                    VisualLog.e("LoadXML", "Exception on Close");
                }
            }
            mDoReconnect = true;

            return true;
        }
        return false;
    }
}
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

package net.proest.librepilot.web.handler;

import net.proest.librepilot.web.VisualLog;
import net.proest.librepilot.web.uavtalk.UAVTalkMissingObjectException;
import net.proest.librepilot.web.uavtalk.UAVTalkXMLObject;
import net.proest.librepilot.web.uavtalk.device.FcDevice;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

public class WebHandler extends AbstractHandler {
    final static short vendorId = 0x20a0;
    final static short productId = 0x415b;
    private final FcDevice mFcDevice;

    public WebHandler(FcDevice device){

        mFcDevice = device;
    }

    public void handle( String target,
                        Request baseRequest,
                        HttpServletRequest request,
                        HttpServletResponse response ) throws IOException,
            ServletException
    {
        response.setContentType("application/json; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);

        PrintWriter out = response.getWriter();


        Set<UAVTalkXMLObject> objects = new HashSet<>();
        //get all settings objects
        for(UAVTalkXMLObject xmlObj : mFcDevice.getObjectTree().getXmlObjects().values()) {
            if (xmlObj.isSettings()) {
                mFcDevice.requestObject(xmlObj.getName());
                objects.add(xmlObj);
            }
        }

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        out.print("{\r\n\t\"uavo\":\r\n{");

        int j = 0;
        for (UAVTalkXMLObject xmlObj : objects) {
            j++;
            if(request.getParameter("name") == null || request.getParameter("name").equals(xmlObj.getName())) {
                if (xmlObj.isSettings()) {

                    out.println("\t\t\"" + xmlObj.getName() + "\": {");
                    int i = 0;
                    for (UAVTalkXMLObject.UAVTalkXMLObjectField xmlField : xmlObj.getFields().values()) {
                        i++;
                        try {
                            Object res = mFcDevice.getObjectTree().getData(xmlObj.getName(), xmlField.getName());
                            out.print("\t\t\t\"" + xmlField.getName() + "\": \"" + res + "\"");
                            if(i < xmlObj.getFields().size()) {
                                out.println(",");
                            } else {
                                out.println("");
                            }
                        } catch (UAVTalkMissingObjectException e) {
                            VisualLog.d("SETTINGS", e.getMessage());
                        }
                    }
                    out.print("\t\t}");
                    if(j < objects.size() && request.getParameter("name") == null) {
                        out.println(",");
                    } else {
                        out.println("");
                    }
                }
            }

        }


        out.println("\t}\r\n}");

        baseRequest.setHandled(true);
    }
}
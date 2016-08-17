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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import net.proest.librepilot.web.uavtalk.UAVTalkXMLObject;
import net.proest.librepilot.web.uavtalk.device.FcDevice;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import static net.proest.librepilot.web.uavtalk.UAVTalkXMLObject.*;

public class DefinitionHandler extends AbstractHandler {
    private final FcDevice mFcDevice;
    private boolean mShowSettings = true;
    private boolean mShowState = true;

    public DefinitionHandler(FcDevice device, boolean showSettings, boolean showState){
        mFcDevice = device;

        mShowSettings = showSettings;
        mShowState = showState;
    }

    public void handle( String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response )
            throws IOException, ServletException
    {

        response.setCharacterEncoding("utf-8");
        response.setStatus(HttpServletResponse.SC_OK);

        PrintWriter out = response.getWriter();

        List<String> targetObjects = Arrays.asList(target.split("/"));

        SortedMap<String, UAVTalkXMLObject> objects = new TreeMap<>();

        for(UAVTalkXMLObject xmlObj : mFcDevice.getObjectTree().getXmlObjects().values()) {
            if(targetObjects.size() == 0 || targetObjects.contains(xmlObj.getName())) {
                if (mShowSettings && xmlObj.isSettings() || mShowState && !xmlObj.isSettings()) {
                    objects.put(xmlObj.getName(), xmlObj);
                }
            }
        }

        String callback = request.getParameter("callback");

        if(callback != null) {
            response.setContentType("application/javascript");
            out.println(callback + "(");
        } else {
            response.setContentType("application/json");
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        out.println(mapper.writeValueAsString(objects));

        if(callback != null) {
            out.print(")");
        }

        baseRequest.setHandled(true);
    }

    private String getFieldTypeName(int type) {
        switch (type) {
            case FIELDTYPE_ENUM: return FIELDNAME_ENUM;
            case FIELDTYPE_FLOAT32: return FIELDNAME_FLOAT32;
            case FIELDTYPE_INT8: return FIELDNAME_INT8;
            case FIELDTYPE_INT16: return FIELDNAME_INT16;
            case FIELDTYPE_INT32: return FIELDNAME_INT32;
            case FIELDTYPE_UINT8: return FIELDNAME_UINT8;
            case FIELDTYPE_UINT16: return FIELDNAME_UINT16;
            case FIELDTYPE_UINT32: return FIELDNAME_FLOAT32;
        }
        return "";
    }
}
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

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class RootHandler extends AbstractHandler {

    public RootHandler(){
    }

    public void handle( String target,
                        Request baseRequest,
                        HttpServletRequest request,
                        HttpServletResponse response ) throws IOException,
            ServletException
    {
        response.setContentType("text/html; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);

        PrintWriter out = response.getWriter();

    out.println("<h1>LibrePilot.web</h1>");
        out.println("<h2>Examples:</h2>");
       out.println("All Settings Objects: <a href=\"/settings\">/settings</a><br />");
        out.println("Specific Settings Object: <a href=\"/settings/StabilizationSettingsBank2\">/settings/StabilizationSettingsBank2</a><br />");

        out.println("All State Objects: <a href=\"/state\">/state</a><br />");
        out.println("Specifics State Object: <a href=\"/state/AttitudeState\">/state/AttitudeState</a><br />");

        out.println("All Objects: <a href=\"/objects\">/objects</a><br />");
        out.println("Specific Object: <a href=\"/objects/ActuatorDesired\">/objects/ActuatorDesired</a><br />");

        baseRequest.setHandled(true);
    }
}
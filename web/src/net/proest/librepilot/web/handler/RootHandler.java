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

        out.println("Multiple Objects: <a href=\"/settings/StabilizationSettingsBank2/StabilizationSettingsBank1/StabilizationSettingsBank3/\">/settings/StabilizationSettingsBank2/StabilizationSettingsBank1/StabilizationSettingsBank3/</a>");

        out.println("Add a JavaScript Callback: <a href=\"/objects/ActuatorDesired/?callback=getUavo\">/objects/ActuatorDesired/?callback=getUavo</a><br />");

        out.println("<hr />All Settings Definitions: <a href=\"/defsettings\">/defsettings</a><br />");
        out.println("Specific Settings Definition: <a href=\"/defsettings/StabilizationSettingsBank2\">/defsettings/StabilizationSettingsBank2</a><br />");

        out.println("All State Definitions: <a href=\"/defstate\">/defstate</a><br />");
        out.println("Specifics State Definition: <a href=\"/defstate/AttitudeState\">/defstate/AttitudeState</a><br />");

        out.println("All Definitions: <a href=\"/defobjects\">/defobjects</a><br />");
        out.println("Specific Definition: <a href=\"/defobjects/ActuatorDesired\">/defobjects/ActuatorDesired</a><br />");

        out.println("Add a JavaScript Callback: <a href=\"/defobjects/ActuatorDesired/?callback=getDef\">/defobjects/ActuatorDesired/?callback=getDef</a><br />");

        out.println("Multiple Definitions: <a href=\"/defsettings/StabilizationSettingsBank2/StabilizationSettingsBank1/StabilizationSettingsBank3/\">/defsettings/StabilizationSettingsBank2/StabilizationSettingsBank1/StabilizationSettingsBank3/</a>");

        out.println("<hr />");

        out.println("(This does not work yet)<form action=\'/objects\' method=\'post\'><input name=\'json\' type=\"text\" value=\'{\"uavo\":{\"AccelGyroSettings\": {\"gyro_temp_coeff\": \"0.0\"}}}\'/> <input type=\'submit\' value=\'Submit\'></form>");

        baseRequest.setHandled(true);
    }
}
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

import org.eclipse.jetty.util.log.Log;

public class VisualLog {

    private static String nullstring;

    static public void d(String tag, String msg) {
        if (msg == null) {
            msg = nullstring;
        }
        //Log.d(tag, msg);
        printToDebug(tag, msg);
    }

    static public void d(String tag, String msg, Throwable tr) {
        if (msg == null) {
            msg = nullstring;
        }
        //Log.e(tag, msg, tr);
        printToDebug(tag, msg);
        //printToDebug(tr.getClass().getSimpleName(), Log.getStackTraceString(tr));
    }

    static public void w(String tag, String msg) {
        if (msg == null) {
            msg = nullstring;
        }
        //Log.w(tag, msg);
        printToDebug(tag, msg);
    }

    static public void e(String tag, String msg) {
        if (msg == null) {
            msg = nullstring;
        }
        //Log.e(tag, msg);
        printToDebug(tag, msg);
    }

    static public void e(String tag, String msg, Throwable tr) {
        if (msg == null) {
            msg = nullstring;
        }
        //Log.e(tag, msg, tr);
        printToDebug(tag, msg);
        printToDebug(tr.getClass().getSimpleName(), tr.getMessage());
    }

    static public void e(Throwable tr) {
        VisualLog.e(tr.getClass().getSimpleName(), tr.getMessage(), tr);
    }

    static public void i(String tag, String msg) {
        if (msg == null) {
            msg = nullstring;
        }
        //Log.i(tag, msg);
        printToDebug(tag, msg);
    }

    static private void printToDebug(final String t, final String s) {
        System.out.println(t + " " + s);
        /*activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (txtDebugLog != null) {
                    txtDebugLog.append(t + " " + s + "\n");
                }
            }
        });
        */
    }
}

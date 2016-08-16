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

package net.proest.librepilot.web.uavtalk.device;

import net.proest.librepilot.web.H;
import net.proest.librepilot.web.VisualLog;
import net.proest.librepilot.web.uavtalk.UAVTalkMessage;
import net.proest.librepilot.web.uavtalk.UAVTalkObject;
import net.proest.librepilot.web.uavtalk.UAVTalkObjectInstance;

import javax.usb.UsbEndpoint;
import javax.usb.UsbException;
import javax.usb.UsbPipe;
import java.util.ArrayDeque;
import java.util.Queue;

class FcUsbWaiterThread extends FcWaiterThread {

    private final UsbEndpoint mEndpointIn;
    private boolean mStop;
    private Queue<Byte> queue;

    public FcUsbWaiterThread(FcDevice device, UsbEndpoint endpointIn) {
        super(device);
        this.mEndpointIn = endpointIn;
        this.setName("LP2GoDeviceUsbWaiterThread");
    }

    @Override
    protected void stopThread() {
        this.mStop = true;
    }

    private byte[] bufferRead(int len) {
        byte[] retval = new byte[len];
        for (int i = 0; i < len; i++) {
            if (!queue.isEmpty()) {
                retval[i] = queue.remove();
            }
        }
        return retval;
    }

    public void run() {

        queue = new ArrayDeque<>();

        byte[] syncbuffer = new byte[1];
        byte[] msgtypebuffer = new byte[1];
        byte[] lenbuffer = new byte[2];
        byte[] oidbuffer = new byte[4];
        byte[] iidbuffer = new byte[2];
        byte[] timestampbuffer = new byte[2];
        byte[] databuffer;
        byte[] crcbuffer = new byte[1];

        //mDevice.mActivity.setRxObjectsGood(0);
        //mDevice.mActivity.setRxObjectsBad(0);
        //mDevice.mActivity.setTxObjects(0);

        while (true) {

            if (mStop) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    //Thread wakes up
                }
                continue;
            }

            byte[] buffer = new byte[64];
            //System.out.println("Loop in " + queue.size() + " " + mEndpointIn.getUsbEndpointDescriptor().wMaxPacketSize());

            while (queue.size() < 350) {


                //mUsbDeviceConnection.bulkTransfer(mEndpointIn, buffer, buffer.length, 1000);
                UsbPipe pipe = mEndpointIn.getUsbPipe();

                try {
                    pipe.open();
                    int received = pipe.syncSubmit(buffer);
                    //System.out.println(received + " bytes received");
                } catch (UsbException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        pipe.close();
                    } catch (UsbException e) {
                        e.printStackTrace();
                    }
                }


                try {
                    for (int i = 2; i < (buffer[1] & 0xff) + 2; i++) {
                        queue.add(buffer[i]);
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    VisualLog.e("FcUsbWaiterThread", "AIOOBE in Usb Queue filler");
                }
            }

            syncbuffer[0] = 0x00;
            while (syncbuffer[0] != 0x3c) {
                syncbuffer = bufferRead(1);
            }

            msgtypebuffer = bufferRead(msgtypebuffer.length);

            lenbuffer = bufferRead(lenbuffer.length);

            int lb1 = lenbuffer[1] & 0x000000ff;
            int lb2 = lenbuffer[0] & 0x000000ff;
            int len = lb1 << 8 | lb2;

            if (len > 266 || len < 10) {
                //mDevice.mActivity.incRxObjectsBad();
                continue; // maximum possible packet size
            }

            oidbuffer = bufferRead(oidbuffer.length);
            iidbuffer = bufferRead(iidbuffer.length);

            int tsoffset = 0;
            if ((MASK_TIMESTAMP & msgtypebuffer[0]) == MASK_TIMESTAMP) {
                timestampbuffer = bufferRead(timestampbuffer.length);
                tsoffset = 2;
            }

            databuffer = bufferRead(len - (10 + tsoffset));
            crcbuffer = bufferRead(crcbuffer.length);

            byte[] bmsg = H.concatArray(syncbuffer, msgtypebuffer);
            bmsg = H.concatArray(bmsg, lenbuffer);
            bmsg = H.concatArray(bmsg, oidbuffer);
            bmsg = H.concatArray(bmsg, iidbuffer);
            if ((MASK_TIMESTAMP & msgtypebuffer[0]) == MASK_TIMESTAMP) {
                bmsg = H.concatArray(bmsg, timestampbuffer);
            }
            bmsg = H.concatArray(bmsg, databuffer);
            int crc = H.crc8(bmsg, 0, bmsg.length);
            bmsg = H.concatArray(bmsg, crcbuffer);

            if ((((int) crcbuffer[0] & 0xff) == (crc & 0xff))) {
                //mDevice.mActivity.incRxObjectsGood();
            } else {
                //mDevice.mActivity.incRxObjectsBad();
                VisualLog.d("USB", "Bad CRC");
                continue;
            }

            try {
                UAVTalkMessage msg = new UAVTalkMessage(bmsg, 0);
                UAVTalkObject myObj =
                        mDevice.mObjectTree.getObjectFromID(H.intToHex(msg.getObjectId()));
                UAVTalkObjectInstance myIns;

                try {
                    myIns = myObj.getInstance(msg.getInstanceId());
                    myIns.setData(msg.getData());
                    myObj.setInstance(myIns);
                } catch (Exception e) {
                    myIns = new UAVTalkObjectInstance(msg.getInstanceId(), msg.getData());
                    myObj.setInstance(myIns);
                }

                if (handleMessageType(msgtypebuffer[0], myObj)) {
                    mDevice.mObjectTree.updateObject(myObj);
                    if (mDevice.isLogging()) {
                        mDevice.log(msg);
                    }
                }
            } catch (Exception e) {
                VisualLog.e(e);
            }
        }
    }
}
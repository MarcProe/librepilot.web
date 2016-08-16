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

/* This file incorporates work covered by the following copyright and
 * permission notice:
 */

/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.proest.librepilot.web.uavtalk.device;

import net.proest.librepilot.web.H;
import net.proest.librepilot.web.VisualLog;
import net.proest.librepilot.web.uavtalk.UAVTalkDeviceHelper;
import net.proest.librepilot.web.uavtalk.UAVTalkObject;
import net.proest.librepilot.web.uavtalk.UAVTalkObjectTree;
import net.proest.librepilot.web.uavtalk.UAVTalkXMLObject;

import javax.usb.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FcUsbDevice extends FcDevice {

    //private final UsbDeviceConnection mDeviceConnection;
    private final UsbEndpoint mEndpointIn;
    private final UsbEndpoint mEndpointOut;
    private final FcWaiterThread mWaiterThread;
    private boolean connected = false;
    //private UsbRequest mOutRequest = null;

    final static short vendorId = 0x20a0;
    final static List<Short> productId = Arrays.asList(new Short[] {
            0x415A, //USB_PRODUCT_ID_OPENPILOT_MAIN
            0x415B, //USB_PRODUCT_ID_COPTERCONTROL
            0x415C, //USB_PRODUCT_ID_OPLINK
            0x415D, //USB_PRODUCT_ID_CC3D
            0x415E, //USB_PRODUCT_ID_REVOLUTION
            0x41D0, // was 0x415E during LP testing // USB_PRODUCT_ID_SPARKY2
            0x4194, // USB_PRODUCT_ID_OSD
            0x4195 // USB_PRODUCT_ID_SPARE
    });

    public static final int USB_ENDPOINT_XFER_INT = 3;
    public static final int USB_DIR_OUT = 0;

    public FcUsbDevice(Map<String, UAVTalkXMLObject> xmlObjects) {
        super();

        //mActivity = activity;
        //mDeviceConnection = connection;
        mObjectTree = new UAVTalkObjectTree();
        mObjectTree.setXmlObjects(xmlObjects);
        //mActivity.setPollThreadObjectTree(mObjectTree);


        UsbEndpoint epOut = null;
        UsbEndpoint epIn = null;

        UsbHub root = null;

        try {
            root = ((org.usb4java.javax.Services) UsbHostManager.getUsbServices()).getRootUsbHub();
        } catch (UsbException e) {
            e.printStackTrace();

        }
        UsbDevice device = findDevice(root, vendorId, productId);
        if(device == null) {
            System.out.println("No device found");
            throw new IllegalStateException("No device found");
        }
        UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
        UsbConfiguration configuration = device.getActiveUsbConfiguration();
        UsbInterface iface = null;

        /*
        for (int i = 0; i < count; i++) {
            UsbInterface intf = device.getInterface(i);
            if (intf.getInterfaceClass() == 3
                    && intf.getInterfaceSubclass() == 0
                    && intf.getInterfaceProtocol() == 0) {
                return intf;
            }
        }
        */

        for (Object o : configuration.getUsbInterfaces()) {
            iface = (UsbInterface) o;
            UsbInterfaceDescriptor idesc = iface.getUsbInterfaceDescriptor();
            if(idesc.bInterfaceClass() == 3 && idesc.bInterfaceSubClass() == 0 && idesc.bInterfaceProtocol() == 0) {
                try {

                    iface.claim(new UsbInterfacePolicy()
                    {
                        @Override
                        public boolean forceClaim(UsbInterface usbInterface)
                        {
                            return true;
                        }
                    });

                    System.out.println("CLAIMED!");

                    break;
                } catch (UsbException e) {
                    System.out.println("NOT CLAIMED: " + e.getMessage());
                    throw new IllegalStateException(e);
                    //e.printStackTrace();
                }
            }
        }

        // look for our bulk endpoints
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (Object o : iface.getUsbEndpoints()) {
                UsbEndpoint ep = (UsbEndpoint) o;
                System.out.println(ep.getDirection());
                if (ep.getType() == USB_ENDPOINT_XFER_INT) {
                    if (ep.getDirection() == USB_DIR_OUT) {
                        if (epOut == null) {
                            epOut = ep;
                        }
                    } else {
                        if (epIn == null) {
                            epIn = ep;
                        }
                    }
                }


        }

        if (epOut == null || epIn == null) {
            try {
                iface.release();
            } catch (UsbException e) {
                e.printStackTrace();
            }
            throw new IllegalArgumentException("not all endpoints found " + (epOut == null) + " " + (epIn == null));
        }
        mEndpointOut = epOut;
        mEndpointIn = epIn;

        mWaiterThread = new FcUsbWaiterThread(this, mEndpointIn);
    }

    @Override
    public boolean isConnected() {
        return this.connected;
    }

    @Override
    public boolean isConnecting() {
        return this.connected;
    }

    @Override
    public void start() {
        this.connected = true;
        mWaiterThread.start();
    }

    @Override
    public void stop() {
        synchronized (mWaiterThread) {
            mWaiterThread.stopThread();
        }
        this.connected = false;
    }

    @Override
    protected boolean writeByteArray(byte[] bytes) {
        boolean retval = false;

        int psize = 62;//mEndpointOut.getMaxPacketSize() - 2;
        int toWrite = bytes.length;

        while (toWrite > 0) {
            int sendlen = toWrite - psize > 0 ? psize : toWrite;
            byte[] buffer = new byte[sendlen + 2];

            System.arraycopy(bytes, bytes.length - toWrite, buffer, 2, sendlen);
            buffer[0] = (byte) 0x02;//report id, is always 2. Period.
            buffer[1] = (byte) ((sendlen) & 0xff);//bytes to send, which is packet.size()-2

            /*if (mOutRequest == null) {
                mOutRequest = new UsbRequest();
                mOutRequest.initialize(mDeviceConnection, mEndpointOut);
            }

            retval = mOutRequest.queue(ByteBuffer.wrap(buffer), buffer.length);
            */
            UsbPipe pipe = mEndpointOut.getUsbPipe();
            try {
                pipe.open();
                int sent = pipe.syncSubmit(buffer);
                retval = true;
            } catch (UsbException e) {
                return false;
            } finally {
                try {
                    pipe.close();
                } catch (UsbException e) {
                    e.printStackTrace();
                }
            }

            toWrite -= sendlen;
        }

        return retval;
    }

    @Override
    public boolean sendAck(String objectId, int instance) {
        byte[] send = mObjectTree.getObjectFromID(objectId).toMessage((byte) 0x23, instance, true);
        VisualLog.d("SEND_ACK_USB", "" + H.bytesToHex(send));
        if (send != null) {
            //mActivity.incTxObjects();

            writeByteArray(send);

            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean sendSettingsObject(String objectName, int instance) {
        byte[] send;
        send = mObjectTree.getObjectFromName(objectName).toMessage((byte) 0x22, instance, false);

        if (send != null) {
            //mActivity.incTxObjects();

            writeByteArray(send);

            requestObject(objectName, instance);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean sendSettingsObject(String objectName, int instance, String fieldName,
                                      int element, byte[] newFieldData, final boolean block) {
        if (block) {
            mObjectTree.getObjectFromName(objectName).setWriteBlocked(true);
        }
        UAVTalkDeviceHelper
                .updateSettingsObject(mObjectTree, objectName, instance, fieldName, element,
                        newFieldData);

        sendSettingsObject(objectName, instance);

        if (block) {
            mObjectTree.getObjectFromName(objectName).setWriteBlocked(false);
        }
        return true;
    }

    @Override
    public boolean requestObject(String objectName) {
        return requestObject(objectName, 0);
    }

    @Override
    public boolean requestObject(String objectName, int instance) {
        UAVTalkXMLObject xmlObj = mObjectTree.getXmlObjects().get(objectName);

        if (xmlObj == null) {
            return false;
        }

        if (nackedObjects.contains(xmlObj.getId())) {
            //VisualLog.d("NACKED", xmlObj.getId());
            return false;  //if it was already nacked, don't try to get it again
        }

        byte[] send = UAVTalkObject.getReqMsg((byte) 0x21, xmlObj.getId(), instance);
        //mActivity.incTxObjects();
        writeByteArray(send);

        return true;
    }

    private UsbDevice findDevice(UsbHub hub, short vendorId, List<Short> productId) {
        for (Object o : hub.getAttachedUsbDevices()) {
            UsbDevice device = (UsbDevice) o;
            UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
            if (desc.idVendor() == vendorId && productId.contains(desc.idProduct())) {
                return device;
            }
            if (device.isUsbHub()) {
                device = findDevice((UsbHub) device, vendorId, productId);
                if (device != null) return device;
            }
        }
        return null;
    }

}


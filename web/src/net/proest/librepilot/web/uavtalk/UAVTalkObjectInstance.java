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

package net.proest.librepilot.web.uavtalk;

import java.util.SortedMap;

public class UAVTalkObjectInstance {

    private final int mId;
    private byte[] mData;
    private SortedMap<String, Object> mDecodedFields;

    public UAVTalkObjectInstance(int id, byte[] data) {
        this.mId = id;
        this.mData = data;
    }

    public byte[] getData() {
        return mData;
    }

    public SortedMap<String, Object> getFields() {
        return this.mDecodedFields;
    }

    public void setFields(SortedMap<String, Object> fields) {
        this.mDecodedFields = fields;
    }

    public void setData(byte[] data) {
        this.mData = data;
    }

    public int getId() {
        return mId;
    }
}

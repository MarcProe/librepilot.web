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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.proest.librepilot.web.H;
import net.proest.librepilot.web.VisualLog;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

@SuppressWarnings("WeakerAccess")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class UAVTalkXMLObject implements Comparable {

    public static final String FIELDNAME_ENUM = "enum";
    public static final String FIELDNAME_FLOAT32 = "float";
    public static final String FIELDNAME_INT16 = "int16";
    public static final String FIELDNAME_INT32 = "int32";
    public static final String FIELDNAME_INT8 = "int8";
    public static final String FIELDNAME_UINT16 = "uint16";
    public static final String FIELDNAME_UINT32 = "uint32";
    public static final String FIELDNAME_UINT8 = "uint8";
    public static final int FIELDTYPE_ENUM = 7;
    public static final int FIELDTYPE_FLOAT32 = 6;
    public static final int FIELDTYPE_INT16 = 1;
    public static final int FIELDTYPE_INT32 = 2;
    public static final int FIELDTYPE_INT8 = 0;
    public static final int FIELDTYPE_UINT16 = 4;
    public static final int FIELDTYPE_UINT32 = 5;
    public static final int FIELDTYPE_UINT8 = 3;
    final private static String REPLACE_ELEMENT_NODES = "\\r\\n|\\r|\\n| |\\t";
    final private static String REPLACE_OPTION_NODES = "\\r\\n|\\r|\\n| |\\t";
    final private static String XML_ATTRIBUTE_SPLITTER = "\\s*,\\s*";
    private static final String XML_ATT_CATEGORY = "category";
    private static final String XML_ATT_CLONEOF = "cloneof";
    private static final String XML_ATT_ELEMENTNAMES = "elementnames";
    private static final String XML_ATT_ELEMENTS = "elements";
    private static final String XML_ATT_NAME = "name";
    private static final String XML_ATT_OPTIONS = "options";
    private static final String XML_ATT_SETTINGS = "settings";
    private static final String XML_ATT_SINGLEINSTANCE = "singleinstance";
    private static final String XML_ATT_TYPE = "type";
    private static final String XML_TAG_ELEMENTNAMES = "elementnames";
    private static final String XML_TAG_FIELD = "field";
    private static final String XML_TAG_OBJECT = "object";
    private static final String XML_TAG_OPTIONS = "options";
    private static final String XML_TRUE = "true";
    private final boolean DBG = 1 == 0;
    private String mCategory;
    private UAVTalkXMLObjectField[] mFieldArray;
    private int[] mFieldLengths;
    private HashMap<String, UAVTalkXMLObjectField> mFields;
    private int mId;
    private Boolean mIsSettings;
    private Boolean mIsSingleInst;
    private String mName;

    public UAVTalkXMLObject(String xml)
            throws IOException, SAXException, ParserConfigurationException {

        HashMap<String, Integer> fieldNames = new HashMap<>();
        fieldNames.put(FIELDNAME_INT8, FIELDTYPE_INT8);
        fieldNames.put(FIELDNAME_INT16, FIELDTYPE_INT16);
        fieldNames.put(FIELDNAME_INT32, FIELDTYPE_INT32);
        fieldNames.put(FIELDNAME_UINT8, FIELDTYPE_UINT8);
        fieldNames.put(FIELDNAME_UINT16, FIELDTYPE_UINT16);
        fieldNames.put(FIELDNAME_UINT32, FIELDTYPE_UINT32);
        fieldNames.put(FIELDNAME_FLOAT32, FIELDTYPE_FLOAT32);
        fieldNames.put(FIELDNAME_ENUM, FIELDTYPE_ENUM);

        Document doc = loadXMLFromString(xml);

        NodeList objectNodeList = doc.getElementsByTagName(XML_TAG_OBJECT);
        Node objectNode = objectNodeList.item(0);
        Element e = (Element) objectNode;

        try {
            mName = e.getAttribute(XML_ATT_NAME);
            mCategory = e.getAttribute(XML_ATT_CATEGORY);
            mIsSingleInst = e.getAttribute(XML_ATT_SINGLEINSTANCE).equals(XML_TRUE);
            mIsSettings = e.getAttribute(XML_ATT_SETTINGS).equals(XML_TRUE);
        } catch (NullPointerException ex) {
            VisualLog.e("XML", "Attribute Error");
            return;
        }

        NodeList fieldNodeList = doc.getElementsByTagName(XML_TAG_FIELD);
        mFields = new HashMap<>();
        mFieldArray = new UAVTalkXMLObjectField[fieldNodeList.getLength()];
        mFieldLengths = new int[fieldNodeList.getLength()];
        int x = 0;

        for (int i = 0; i < fieldNodeList.getLength(); i++) {
            Node fieldNode = fieldNodeList.item(i);
            UAVTalkXMLObjectField uavField = new UAVTalkXMLObjectField();
            Element f = (Element) fieldNode;

            String sclone = f.getAttribute(XML_ATT_CLONEOF);
            uavField.mName = f.getAttribute(XML_ATT_NAME);

            if (sclone != null && !sclone.equals("")) {
                String tn = uavField.mName;
                uavField = mFields.get(sclone);
                uavField.mName = tn;
            } else {

                uavField.mType = fieldNames.get(f.getAttribute(XML_ATT_TYPE));

                String elementString = f.getAttribute(XML_ATT_ELEMENTNAMES);

                uavField.mElements =
                        new ArrayList<>(
                                Arrays.asList(elementString.split(XML_ATTRIBUTE_SPLITTER)));

                String elementCountString = f.getAttribute(XML_ATT_ELEMENTS);

                if (!elementCountString.equals("") && elementCountString != null) {
                    uavField.mElementCount = Integer.parseInt(elementCountString);
                    if (uavField.mType == FIELDTYPE_ENUM
                            && f.getElementsByTagName(XML_TAG_ELEMENTNAMES).getLength() == 0) {
                        uavField.mElements.clear();
                        for (int j = 0; j < uavField.mElementCount; j++) {
                            uavField.mElements.add(String.valueOf(j));
                        }
                    }
                }

                if (uavField.mType == FIELDTYPE_ENUM) {

                    String optionsString = f.getAttribute(XML_ATT_OPTIONS);
                    try {
                        Object[] oa = Arrays.asList(optionsString.split(XML_ATTRIBUTE_SPLITTER)).toArray();

                        uavField.mOptions = Arrays.copyOf(oa, oa.length, String[].class);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }

                    if (uavField.mOptions == null || uavField.mOptions.length == 0
                            || ((uavField.mOptions.length == 1) &&
                            (uavField.mOptions[0].equals("")))) {
                        if (f.getElementsByTagName(XML_TAG_OPTIONS).getLength() > 0) {
                            NodeList optionnodes =
                                    f.getElementsByTagName(XML_TAG_OPTIONS).item(0).getChildNodes();

                            ArrayList<String> options = new ArrayList<>();
                            for (int j = 0; j < optionnodes.getLength(); j++) {
                                String content =
                                        optionnodes.item(j).getTextContent()
                                                .replaceAll(REPLACE_OPTION_NODES, "");
                                if (content != null && !content.equals("")) {
                                    options.add(content);
                                }
                            }
                            uavField.mOptions = options.toArray(new String[options.size()]);
                        }
                    }
                }

                if (f.getElementsByTagName(XML_TAG_ELEMENTNAMES).getLength() > 0) {
                    NodeList elementnodes =
                            f.getElementsByTagName(XML_TAG_ELEMENTNAMES).item(0).getChildNodes();

                    uavField.mElements = new ArrayList<>();
                    for (int j = 0; j < elementnodes.getLength(); j++) {
                        String content = elementnodes.item(j).getTextContent()
                                .replaceAll(REPLACE_ELEMENT_NODES, "");
                        if (content != null && !content.equals("")) {
                            uavField.mElements.add(content);
                        }
                    }
                }

                if (uavField.mElementCount == 0) {
                    uavField.mElementCount = uavField.mElements.size();
                }
            }

            if (uavField.mType == FIELDTYPE_INT8 || uavField.mType == FIELDTYPE_UINT8) {
                mFieldLengths[x] = /*1 * */uavField.mElementCount;
                uavField.mSize = /*1 * */uavField.mElementCount;
                uavField.mTypelength = 1;
            } else if (uavField.mType == FIELDTYPE_INT16 || uavField.mType == FIELDTYPE_UINT16) {
                mFieldLengths[x] = 2 * uavField.mElementCount;
                uavField.mSize = 2 * uavField.mElementCount;
                uavField.mTypelength = 2;
            } else if (uavField.mType == FIELDTYPE_INT32 || uavField.mType == FIELDTYPE_UINT32 ||
                    uavField.mType == FIELDTYPE_FLOAT32) {
                mFieldLengths[x] = 4 * uavField.mElementCount;
                uavField.mSize = 4 * uavField.mElementCount;
                uavField.mTypelength = 4;
            } else if (uavField.mType == FIELDTYPE_ENUM) {
                mFieldLengths[x] = uavField.mElementCount;
                uavField.mSize = uavField.mElementCount;
                uavField.mTypelength = 1;
            }

            x++;

            mFields.put(uavField.mName, uavField);
            mFieldArray[i] = uavField;
        }

        Arrays.sort(mFieldArray);
        mFields.clear(); //TODO:This is crappy code!
        int j = 0;
        for (UAVTalkXMLObjectField xuav : mFieldArray) {
            xuav.mPos = j;     //set new position
            j += xuav.mTypelength * xuav.mElementCount;
            mFields.put(xuav.mName, xuav);
        }

        this.mId = calculateID();
    }

    private static Document loadXMLFromString(String xml)
            throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        return builder.parse(is);
    }

    public String getCategory() {
        return mCategory;
    }

    public HashMap<String, UAVTalkXMLObjectField> getFields() {
        return mFields;
    }

    public String getId() {
        return H.bytesToHex(H.toBytes(mId));
    }

    @JsonIgnore
    public int getLength() {
        int retval = 0;
        for (int i : mFieldLengths) {
            retval += i;
        }
        return retval;
    }

    public String getName() {
        return mName;
    }

    public Boolean isSettings() {
        return mIsSettings;
    }

    private int calculateID() {
        // Hash object name
        if (DBG) {
            VisualLog.d("HASH 1 ", " ");
        }
        if (DBG) {
            VisualLog.d("HASH 2 ", this.mName);
        }
        int hash = updateHash(this.mName, 0);
        // Hash object attributes
        hash = updateHash(this.mIsSettings ? 1 : 0, hash);
        hash = updateHash(this.mIsSingleInst ? 1 : 0, hash);
        // Hash field information
        for (UAVTalkXMLObjectField aMFieldArray : this.mFieldArray) {
            if (DBG) {
                VisualLog.d("HASH 3 ", aMFieldArray.mName);
            }
            hash = updateHash(aMFieldArray.mName, hash);
            hash = updateHash((aMFieldArray.mElementCount), hash);
            hash = updateHash(aMFieldArray.mType, hash);
            if (aMFieldArray.mType == FIELDTYPE_ENUM) {
                String[] options = aMFieldArray.mOptions;
                if (DBG) {
                    VisualLog.d("HASH 6 ", "" + (options == null));
                }
                for (String option : options) {
                    if (DBG) {
                        VisualLog.d("HASH 4 ", option);
                    }
                    hash = updateHash(option, hash);
                }
            }
        }
        return hash & 0xFFFFFFFE;
    }

    private int updateHash(int value, int hash) {
        int ret = hash ^ ((hash << 5) + (hash >>> 2) + value);
        if (DBG) {
            long y = ret & 0x00000000ffffffffL;
            long in = value & 0x00000000ffffffffL;
            VisualLog.d("HASH 5 ", "" + in + "=>" + y);
        }
        return ret;
    }

    /**
     * Update the hash given a string
     */
    private int updateHash(String value, int hash) {
        byte[] bytes = value.getBytes();
        int hashout = hash;
        for (byte aByte : bytes) {
            hashout = updateHash(aByte, hashout);
        }

        return hashout;
    }

    @Override
    public int compareTo(Object o) {
        return mName.compareTo(((UAVTalkXMLObject)o).getName());
    }

    //TODO: Getter and Setter
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public class UAVTalkXMLObjectField implements Comparable<UAVTalkXMLObjectField> {
        int mElementCount;
        ArrayList<String> mElements;
        String mName;
        String[] mOptions;
        int mPos;
        int mSize;
        int mType;
        int mTypelength;

        @JsonIgnore
        public ArrayList<String> getElements() {
            return mElements;
        }

        @JsonProperty("elements")
        public ArrayList<String> getJsonElements() {
            if(mElements.size() <= 1 && (mElements.get(0) == null || mElements.get(0).equals("") || mElements.get(0).equals("0"))) {
                System.out.println("E");
                return new ArrayList<>();
            } else {
                System.out.println("F");
                return mElements;
            }

        }

        public String getName() {
            return mName;
        }

        public String[] getOptions() {
            return mOptions;
        }

        @JsonIgnore
        public int getType() {
            return mType;
        }

        @Override
        public int compareTo(UAVTalkXMLObjectField another) {
            return another.mTypelength - mTypelength;
        }

        public String toString() {
            return mName + " Pos: " + mPos + " ElementCount: " + mElementCount + " Size:" + mSize +
                    " Type:" + mType;
        }

        @JsonProperty("type")
        public String getTypeString() {
            return getFieldTypeName(mType);
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
}

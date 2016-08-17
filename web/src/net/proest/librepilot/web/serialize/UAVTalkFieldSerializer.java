package net.proest.librepilot.web.serialize;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by marc on 17.08.2016.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UAVTalkFieldSerializer {
    @JsonAnyGetter
    public SortedMap<String, Object> getElements() {
        return elements;
    }

    public void setElements(SortedMap<String, Object> elements) {
        this.elements = elements;
    }

    private SortedMap<String, Object> elements;

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    private Object value;

    public UAVTalkFieldSerializer() {
        elements = new TreeMap<>();
    }
}

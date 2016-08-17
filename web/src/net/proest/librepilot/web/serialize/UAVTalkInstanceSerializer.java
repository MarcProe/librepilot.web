package net.proest.librepilot.web.serialize;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by marc on 17.08.2016.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UAVTalkInstanceSerializer {
    private SortedMap<String, Object> fields;

    @JsonAnyGetter
    public SortedMap<String, Object> getFields() {
        return fields;
    }

    public void setFields(SortedMap<String, Object> fields) {
        this.fields = fields;
    }


    public UAVTalkInstanceSerializer() {
        fields = new TreeMap<>();
    }

}

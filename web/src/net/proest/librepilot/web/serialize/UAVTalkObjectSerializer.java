package net.proest.librepilot.web.serialize;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by marc on 17.08.2016.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UAVTalkObjectSerializer {
    @JsonAnyGetter
    public SortedMap<Integer, UAVTalkInstanceSerializer> getInstances() {
        return instances;
    }

    public void setInstances(SortedMap<Integer, UAVTalkInstanceSerializer> instances) {
        this.instances = instances;
    }

    private SortedMap<Integer, UAVTalkInstanceSerializer> instances;

    public UAVTalkObjectSerializer() {
        instances = new TreeMap<>();
    }
}

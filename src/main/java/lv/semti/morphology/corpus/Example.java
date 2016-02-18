package lv.semti.morphology.corpus;

import org.json.simple.JSONValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by pet on 2016-01-07.
 */
public class Example implements Comparable<Example> {
    private String example;
    private Document document;
    private Integer priority; // returns the 'quality' of example, lower is better

    public Example(String example, Document document, int priority) {
        this.example = example;
        this.document = document;
        this.priority = priority;
    }

    @Override public String toString() {
        Map<String, String> output = new HashMap<>(document.metadata);
        output.put("example", this.example);
        return JSONValue.toJSONString(output);
    }

    @Override
    public int compareTo(Example other) {
        return this.priority.compareTo(other.priority);
    }
}

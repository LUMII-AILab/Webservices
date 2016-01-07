package lv.semti.morphology.corpus;

/**
 * Created by pet on 2016-01-07.
 */
public class Example {
    private String example;
    private String description;

    public Example(String example, String description) {
        this.example = example;
        this.description = description;
    }

    @Override public String toString() {
        return String.format("{\"example\" : %s, \"document\" : %s}", toJSONString(example), toJSONString(description));
    }

    private static String toJSONString(String text) {
        return "\"" + text.replaceAll("\"", "\\\\\"") + "\"";
    }
}

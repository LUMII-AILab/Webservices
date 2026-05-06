package lv.semti.morphology.webservice;


import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.util.*;

public class TezaursWordResource extends ServerResource {
    private static Map<String, String> entries;

    @Get("json")
	public String retrieve() throws Exception {
        getResponse().setAccessControlAllowOrigin("*");
		String query = (String) getRequest().getAttributes().get("query");
        if (query != null)
            try {
                query = URLDecoder.decode(query,"UTF8");
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        if (query == null || query.isEmpty()) {
            System.out.println("redirecting to wordlist");
            this.redirectPermanent("http://api.tezaurs.lv/wordlist_sorted.txt");
        }

        getEntries();

        String result = entries.get(query);
        return result;
	}

    private static class JsonElementJsonDeserializer implements JsonDeserializer<String> {
        @Override
        public String deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return json.toString();
        }
    }
	
    public static Map<String, String> getEntries() throws Exception {
        if (entries != null) return entries;

        entries = new HashMap<>();
        Map<String, JsonArray> temp_entries = new HashMap<>();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(String.class, new JsonElementJsonDeserializer())
                .create();
        JsonParser parser = new JsonParser();
        System.out.println("Loading thesaurus entries");
//        List<String> files = Arrays.asList("entries-bad.json", "entries-good.json", "entries-noParadigm.json", "references-bad.json", "references-good.json", "references-noParadigm.json");
        List<String> files = Arrays.asList("analyzed_tezaurs.json");
        for (String filename : files) {
            filename = "/tezaurs/"+filename;
            InputStream stream = MorphoServer.class.getClass().getResourceAsStream(filename);
            if (stream == null) {
                throw new Exception("Resource " + filename + " not found.");
            }
            JsonReader reader = new JsonReader(new InputStreamReader(stream));
            reader.setLenient(true);
            reader.beginArray();
            while (reader.hasNext()) {
                String entry = gson.fromJson(reader, String.class);
                if (entry == null || entry.isEmpty()) continue;
                JsonObject obj = parser.parse(entry).getAsJsonObject();
                String lemma = obj.getAsJsonObject("Header").get("Lemma").getAsString();
                JsonArray entrylist = temp_entries.get(lemma);
                if (entrylist == null) entrylist = new JsonArray();
                entrylist.add(obj);
                temp_entries.put(lemma, entrylist);
                entries.put(lemma, entrylist.toString());
            }
            System.out.println(filename + " loaded.");
        }
        return entries;
    }
}
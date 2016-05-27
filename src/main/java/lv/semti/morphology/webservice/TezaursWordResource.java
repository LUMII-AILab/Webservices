/*******************************************************************************
 * Copyright 2012, 2013, 2014 Institute of Mathematics and Computer Science, University of Latvia
 * Author: Pēteris Paikens
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package lv.semti.morphology.webservice;


import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import lv.semti.morphology.corpus.Example;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TezaursWordResource extends ServerResource {
    private static Map<String, String> entries;

    @Get("json")
	public String retrieve() throws Exception {
        Utils.allowCORS(this);
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
            this.redirectPermanent("/wordlist_sorted.txt");
        }

        getEntries();

        String result = entries.get(query);
//        if (result != null) return result;
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
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(String.class, new JsonElementJsonDeserializer())
                .create();
        JsonParser parser = new JsonParser();
        System.out.println("Loading thesaurus entries");
        List<String> files = Arrays.asList("entries-bad.json", "entries-good.json", "entries-noParadigm.json", "references-bad.json", "references-good.json", "references-noParadigm.json");
//        List<String> files = Arrays.asList("entries-bad.json");
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
                entries.put(lemma, entry);
            }
            System.out.println(filename + " loaded.");
        }
        return entries;
    }
}
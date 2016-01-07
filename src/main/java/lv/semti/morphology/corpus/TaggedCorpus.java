package lv.semti.morphology.corpus;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by pet on 2016-01-06.
 * Reads a morphotagged corpus in the so-called 'vert' format (one token per line, tab-separated token/tag/lemma, and xml-like tags for documents, paragraphs and missing spaces between tokens
 */
public class TaggedCorpus {
    private List<Document> documents;

    public List<Example> findExamples(String lemma) {
        List<Example> result = new LinkedList<>();
        for (Document doc : documents) { // TODO - indekss ?
            result.addAll(doc.findExamples(lemma));
        }
        return result;
    }

    public TaggedCorpus (String filename) throws IOException {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(filename);
        if (stream == null) {
            stream = new FileInputStream(filename);
        }

        BufferedReader in = new BufferedReader(
                new InputStreamReader(stream, "UTF-8"));

        documents = loadDocuments(in);
        in.close();
    }

    private List<Document> loadDocuments(BufferedReader in) throws IOException {
        documents = new LinkedList<>();
        Document doc = loadDocument(in);
        while (doc != null) {
            documents.add(doc);
            doc = loadDocument(in);
        }
        return documents;
    }

    private Document loadDocument(BufferedReader in) throws IOException {
        String header = in.readLine();
        if (header == null) // End of file, no more documents
            return null;
        if (!header.startsWith("<doc"))
            throw new IOException(String.format("Bad corpus file format, expected <doc found %s", header));

        String line = in.readLine();
        List<String> lines = new LinkedList<>();
        while (line != null && !line.startsWith("</doc>")) {
            lines.add(line);
            line = in.readLine();
        }
        return new Document(header, lines);
    }
}

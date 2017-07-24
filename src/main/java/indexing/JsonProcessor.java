package main.java.indexing;

import java.io.BufferedReader;
import java.io.FileReader;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.Term;

public class JsonProcessor {
	
	String filename;
	LuceneIndexer li;
	
	public JsonProcessor (String filename) {
		this.filename = filename;
		li = new LuceneIndexer();
	}
	
	public void index() {
		
		try {
			
			BufferedReader br = new BufferedReader(new FileReader(main.java.constants.Constants.filePath + this.filename));
			String json;
			JSONParser par = new JSONParser();
			li.initIndexWriter(main.java.constants.Constants.indexPath);
			
			while ((json = br.readLine()) != null && !json.equals("")) {
				
				JSONObject review = (JSONObject) par.parse(json);
				String rID = (String) review.get("reviewerID");
				String rName = (String) review.get("reviewerName");
				String rSummary = (String) review.get("summary");
				String rText =  (String) review.get("reviewText");
				Double rStars = (Double) review.get("overall");
				Long rTime = (Long) review.get("unixReviewTime");
				String docID = rID + rTime.toString();
				
				if (rID == null || rID.equals(""))
					rID = "defaultUser";
				
				if (rName == null || rName.equals(""))
					rName = "defaultUser";
				
				Review r = new Review(docID, rID, rName, rSummary, rText, rTime, rStars);
				Document doc = createDocument(r);
				Term t = new Term("docID", doc.get("docID"));
				
				li.updateDocument(doc, t);
			}
			
			li.close();
			br.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public Document createDocument(Review r) {

		Document doc = new Document();
		doc.add(new StringField("docID", r.docID, Field.Store.YES));
		doc.add(new StringField("rID", r.rID, Field.Store.YES));
		doc.add(new StringField("rName", r.rName, Field.Store.YES));
		doc.add(new TextField("rSummary", r.rSummary, Field.Store.YES));
		doc.add(new LongPoint("rTime", r.rTime));
		doc.add(new DoublePoint("rStars", r.rStars));
		
		FieldType rText = new FieldType();
		rText.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
		rText.setTokenized(true);
		rText.setStored(true);
		rText.setStoreTermVectors(true);

		doc.add(new Field("rText", r.rText, rText));
		
		return doc;
		
	}
}
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;

//import org.apache.lucene.queryParser.ParseException;
//import org.apache.lucene.queryParser.QueryParser;
//import org.apache.lucene.search.Hits.*;
//import org.apache.lucene.search.Hit;
//import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.QueryBuilder;
import org.apache.lucene.util.Version;

public class SearchIndex {
	public static final String FIELD_PATH = "path";
	public static final String FIELD_CONTENTS = "contents";

	public static void main(String[] args) throws Exception {
		
		IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
		IndexWriter indexWri = createIndexWriter(config, "doc_base");
		
		createIndex(indexWri, "text_base");
		
		
		searchIndex(DirectoryReader.open(indexWri), indexWri.getAnalyzer(), "Skip and to and main and content", 200);
		
		indexWri.close();


	}

	public static void createIndex(IndexWriter indexWri, String basePath) throws CorruptIndexException, LockObtainFailedException, IOException {
		
		File baseDir = new File(basePath);
		File[] files = baseDir.listFiles();
		for (File file : files) {
			Document doc = new Document();

			String docPath = file.getCanonicalPath();
		
			doc.add(new StoredField(FIELD_PATH, docPath));
			
			Reader reader = new FileReader(file);
			
			doc.add(new TextField(FIELD_CONTENTS, reader));

			indexWri.addDocument(doc);
		}
		indexWri.commit();
	}
	
	public static IndexWriter createIndexWriter(IndexWriterConfig config, String indexPath) throws IOException{
		Path path = Paths.get(indexPath);
		Directory indexDir = FSDirectory.open(path);
		
		
		config.setCommitOnClose(true);
		return new IndexWriter(indexDir, config );
	}
	
	public static void searchIndex(IndexReader indexRea, Analyzer ana, String queryTex, int maxDocs)
			throws IOException {
//		QueryParser queryPar
		System.out.println("Searching for '" + queryTex + "'");
		
		
		
		QueryBuilder queryBui = new QueryBuilder(ana);
		Query query = queryBui.createBooleanQuery(FIELD_CONTENTS, queryTex);
		
		
		IndexSearcher indexSea = new IndexSearcher(indexRea);
		
		ScoreDoc[] scoreDocs = indexSea.search(query, maxDocs).scoreDocs;
		System.out.println("Number of hits: " + scoreDocs.length);
		
		Document[] docs = new Document[scoreDocs.length];
		
		for(int x=0;x<scoreDocs.length;x++){
			docs[x] = indexSea.doc(scoreDocs[x].doc);
			String path = docs[x].get(FIELD_PATH);
			System.out.println("Hit: " + path);
		}
		
	}

}
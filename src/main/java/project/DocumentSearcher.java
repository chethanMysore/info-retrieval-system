package ir_pa.project;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;

public class DocumentSearcher {
	private Path indexPath;
	private IndexReader indexReader;
	private Analyzer analyzer;
	private IndexSearcher indexSearcher;
	private TopScoreDocCollector docCollector;

	public DocumentSearcher(Path indexPath, TopScoreDocCollector docCollector, Analyzer analyzer) {
		if (indexPath == null) {
			writeError(new NullPointerException("indexReader is null"));
			System.exit(-1);
		}
		if (docCollector == null) {
			writeError(new NullPointerException("docCollector is null"));
			System.exit(-1);
		}
		if (analyzer == null) {
			writeError(new NullPointerException("analyzer is null"));
			System.exit(-1);
		}

		this.indexPath = indexPath;
		this.docCollector = docCollector;
		this.analyzer = analyzer;
	}

	public ScoreDoc[] Search(String query) throws IOException {
		this.indexReader = DirectoryReader.open(FSDirectory.open(this.indexPath));
		this.indexSearcher = new IndexSearcher(this.indexReader);
		ScoreDoc[] searchResults = null;
		try {
			MultiFieldQueryParser queryParser = new MultiFieldQueryParser(new String[] { "contents", "title" },
					analyzer);
			Query searchQuery = queryParser.parse(query);
			indexSearcher.search(searchQuery, this.docCollector);
			searchResults = docCollector.topDocs().scoreDocs;
		} catch (IOException ex) {
			writeError(ex);
		} catch (ParseException ex) {
			System.out.println("No Results Found");
		}
		return searchResults;
	}

	public IndexSearcher getIndexSearcher() {
		return this.indexSearcher;
	}

	public void closeReader() throws IOException {
		this.indexReader.close();
	}

	private void writeError(Exception ex) {
		System.out.println("Error Type: " + ex.getClass().getName() + "\nError Message: " + ex.getMessage());
	}
}

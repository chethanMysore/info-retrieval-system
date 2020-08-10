package ir_pa.project;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

import org.apache.lucene.document.Document;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;

public class InformationRetrievalSystem {
	private static Analyzer analyzer;
	private static IndexWriter indexWriter;
	private static TopScoreDocCollector docCollector;
	private static File initFile;
	private static Path indexPath;
	private static DocumentParser docParser;;
	private static DocumentSearcher docSearcher;
	private static final int NUM_OF_HITS = 5;
	private static final int TOTAL_HITS_THRESHOLD = 10;

	public static void main(String[] args) {

		if (args.length == 1 && !args[0].isEmpty()) {
			String inputDirpath = args[0];
			try {
				__inject_Dependencies__(inputDirpath);
				docParser.parseDocuments();
				docParser.closeIndexWriter();
				BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
				while (true) {
					System.out.println("Enter keywords/phrase to search or \"q\" to quit: ");
					String searchQuery = inputReader.readLine();
					if (searchQuery.equalsIgnoreCase("q"))
						break;

					ScoreDoc[] searchResults = docSearcher.Search(searchQuery);
					printResults(searchResults, searchQuery, docSearcher.getIndexSearcher());
				}
				docSearcher.closeReader();
			} catch (IOException ex) {
				writeError(ex);
			} finally {
				System.gc();
			}
		} else {
			writeError("ArgumentsMismatch", "Please specify the input folder path for indexing");
		}

	}

	private static void __inject_Dependencies__(String inputDirpath) throws IOException {
		analyzer = new TokenAnalyzer();
		initFile = new File(inputDirpath);
		indexPath = Files.createTempDirectory(initFile.toPath().getParent(), "index");
		// indexPath = initFile.toPath().getParent();
		FSDirectory indexDir = FSDirectory.open(indexPath);
		IndexWriterConfig indexConfig = new IndexWriterConfig(analyzer);
		indexConfig.setOpenMode(OpenMode.CREATE);
		indexWriter = new IndexWriter(indexDir, indexConfig);
		docCollector = TopScoreDocCollector.create(NUM_OF_HITS, TOTAL_HITS_THRESHOLD);

		docParser = new DocumentParser(initFile, indexWriter);
		docSearcher = new DocumentSearcher(indexPath, docCollector, analyzer);
	}

	private static void printResults(ScoreDoc[] searchResults, String searchQuery, IndexSearcher indexSearcher) {
		if (searchResults != null && searchResults.length > 0) {
			System.out.println("-------------------------- SearchResults --------------------------");
			System.out.println(searchResults.length + " documents found");
			for (int i = 0; i < searchResults.length; i++) {
				int docIndex = searchResults[i].doc;
				Document doc;
				try {
					doc = indexSearcher.doc(docIndex);
					String filepath = doc.get("filepath");
					File file = new File(filepath);
					String filename = file.getName();
					System.out.println("Document Name: " + filename);
					System.out.println("Rank: " + (i + 1));
					System.out.println("Path: " + filepath);
					System.out.println("Last Modification Time: " + new Date(file.lastModified()));
					System.out.println("Relevance Score: " + searchResults[i].score);
					if (filename.endsWith(DocumentParser.FileType.HTML.toString().toLowerCase())) {
						System.out.println("Title: " + doc.get("title"));
						System.out.println("Summary: " + doc.get("summary"));
					}
					System.out.println("-----------------------------------------");
				} catch (IOException e) {
					System.out.println("Document with id - " + docIndex + " no longer exists");
				}
			}
		} else
			System.out.println("No documents found for the query: " + searchQuery);
	}

	private static void writeError(String errorMessage, String errorType) {
		System.out.println("An error occurred. Type: " + errorType + ", Message: " + errorMessage);
	}

	private static void writeError(IOException ex) {
		System.out.println("An error occurred. Type: " + ex.getClass() + ", Message: " + ex.getMessage());
	}
}

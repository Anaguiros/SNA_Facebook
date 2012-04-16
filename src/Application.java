import java.io.File;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import SocialGraph.SocialGraph;

public class Application {

	private static final String DB_PATH = "/home/matt/testDB";
	private static EmbeddedGraphDatabase graphDB;
	private static Index<Node> nodeIndex;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		deleteFileOrDirectory( new File( DB_PATH ) );
		EmbeddedGraphDatabase graphDB = new EmbeddedGraphDatabase( DB_PATH );
		nodeIndex = graphDB.index().forNodes( "nodes" );
		SocialGraph graph = new SocialGraph(graphDB, nodeIndex);
		
		System.out.println(graph);
		
		File file = new File("concrete.exemple.json");
		
		graph.readFromJSon(file);
		
		System.out.println(graph);
		//System.out.println(graph.getPersonById(1).getKeywordTable());
		//System.out.println(graph.getPersonById(2).getKeywordTable());
	}
	
	private static void deleteFileOrDirectory( final File file ) {
        if ( !file.exists() ) { return; }

        if ( file.isDirectory() ) {
            for ( File child : file.listFiles() ) { deleteFileOrDirectory( child ); }
        }
        else {
            file.delete();
        }
    }
}

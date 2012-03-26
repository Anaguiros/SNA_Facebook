package SocialGraph;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.*;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.helpers.collection.IterableWrapper;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.kernel.Traversal;
import org.neo4j.kernel.Uniqueness;

import java.util.*;

import static SocialGraph.RelTypes.*;

public class Person
{
    static final String NAME = "NAME";
    static final String ID = "ID";
    private HashMap<String, Integer> keywordTable;
    
    // START SNIPPET: the-node
    private Node underlyingNode;

    public Person( Node personNode )
    {
        this.underlyingNode = personNode;
        this.keywordTable = new HashMap<String, Integer>();
        for(String currentKey : this.getUnderlyingNode().getPropertyKeys()){
    		if((currentKey != Person.NAME) && (currentKey != Person.ID)){
    			this.keywordTable.put(currentKey,(Integer)this.underlyingNode.getProperty(currentKey));
    		}
    	}
    }

    protected Node getUnderlyingNode()
    {
        return underlyingNode;
    }

    // END SNIPPET: the-node

    
    
    // START SNIPPET: delegate-to-the-node
    public String getName()
    {
        return (String)underlyingNode.getProperty( NAME );
    }

    // END SNIPPET: delegate-to-the-node

    public HashMap<String,Integer> getKeywordTable() {
		return this.keywordTable;
	}

	// START SNIPPET: override
    @Override
    public int hashCode()
    {
        return underlyingNode.hashCode();
    }

    @Override
    public boolean equals( Object o )
    {
        return o instanceof Person &&
                underlyingNode.equals( ( (Person)o ).getUnderlyingNode() );
    }

    @Override
    public String toString()
    {
        String result = "";
        
    	result += "Person[" + getName() + "]["+getUnderlyingNode()+"]["+getKeywordTable()+"]";
    	
    	return result;
    }

    // END SNIPPET: override

    public void addFriend( Person otherPerson )
    {
        Transaction tx = underlyingNode.getGraphDatabase().beginTx();
        try
        {
            if ( !this.equals( otherPerson ) )
            {
                Relationship friendRel = getFriendRelationshipTo( otherPerson );
                if ( friendRel == null )
                {
                    underlyingNode.createRelationshipTo( otherPerson.getUnderlyingNode(), FRIEND );
                }
                tx.success();
            }
        }
        finally
        {
            tx.finish();
        }
    }

    public int getNrOfFriends()
    {
        return IteratorUtil.count( getFriends() );
    }

    public Iterable<Person> getFriends()
    {
        return getFriendsByDepth( 1 );
    }

    public void removeFriend( Person otherPerson )
    {
        Transaction tx = underlyingNode.getGraphDatabase().beginTx();
        try
        {
            if ( !this.equals( otherPerson ) )
            {
                Relationship friendRel = getFriendRelationshipTo( otherPerson );
                if ( friendRel != null )
                {
                    friendRel.delete();
                }
                tx.success();
            }
        }
        finally
        {
            tx.finish();
        }
    }

    public Iterable<Person> getFriendsOfFriends()
    {
        return getFriendsByDepth( 2 );
    }

    public Iterable<Person> getShortestPathTo( Person otherPerson,
                                               int maxDepth )
    {
        // use graph algo to calculate a shortest path
        PathFinder<Path> finder = GraphAlgoFactory.shortestPath(
                Traversal.expanderForTypes( FRIEND, Direction.BOTH ), maxDepth );

        Path path = finder.findSinglePath( underlyingNode,
                otherPerson.getUnderlyingNode() );
        return createPersonsFromNodes( path );
    }

//    public void likeStatus(StatusUpdate status){
//    	Transaction tx = getGraphDb().beginTx();
//    	
//    	String[] keywords = (String[])status.getUnderlyingNode().getProperty(StatusUpdate.KEYWORDS);
//        try
//        {
//           Node destinataire = status.getPerson().getUnderlyingNode();
//
//           for(int i=0;i<keywords.length;i++){
//        	   Relationship likerel = this.getUnderlyingNode().createRelationshipTo( destinataire, RelTypes.LIKE );
//               likerel.setProperty(POIDS, 4);
//               likerel.setProperty(StatusUpdate.KEYWORDS, keywords[i]);
//   		}
//            tx.success();
//        }
//        finally
//        {
//            tx.finish();
//        }
//    }
    
//    public void commentStatus(StatusUpdate status, ArrayList<String> keywords){
//    	Transaction tx = getGraphDb().beginTx();
//    	
//        try
//        {
//           Node destinataire = status.getPerson().getUnderlyingNode();
//
//           for(String keyW : keywords){
//        	   Relationship likerel = this.getUnderlyingNode().createRelationshipTo( destinataire, RelTypes.COMMENT );
//               likerel.setProperty(POIDS, 6);
//               likerel.setProperty(StatusUpdate.KEYWORDS, keyW);
//   			}
//            tx.success();
//        }
//        finally
//        {
//            tx.finish();
//        }
//        for(String keyW : keywords){
//     	   if(this.keywordTable.containsKey((String)keyW)){
//     		  this.keywordTable.put((String)keyW, this.keywordTable.get((String)keyW)+6);
//     	   }
//     	   else{
//     		  this.keywordTable.put((String)keyW, 6);
//     	   }
//			}
//    }
    
    private GraphDatabaseService getGraphDb()
    {
        return underlyingNode.getGraphDatabase();
    }

//    private Node createNewStatusNode( ArrayList<String> text )
//    {
//    	String[] keywordArray = new String[text.size()];
//    	for(int i=0; i<text.size();i++){
//    		keywordArray[i]=text.get(i);
//    	}
//    	
//        Node newStatus = getGraphDb().createNode();
//        newStatus.setProperty( StatusUpdate.KEYWORDS, keywordArray );
//        newStatus.setProperty( StatusUpdate.DATE, new Date().getTime() );
////        newStatus.setProperty(StatusUpdate.ID, ""+this.underlyingNode.getProperty(NAME)+"_"+newStatus.getProperty(StatusUpdate.KEYWORDS));
//        return newStatus;
//    }

    private Relationship getFriendRelationshipTo( Person otherPerson )
    {
        Node otherNode = otherPerson.getUnderlyingNode();
        for ( Relationship rel : underlyingNode.getRelationships( FRIEND ) )
        {
            if ( rel.getOtherNode( underlyingNode ).equals( otherNode ) )
            {
                return rel;
            }
        }
        return null;
    }

    private Iterable<Person> getFriendsByDepth( int depth )
    {
        // return all my friends and their friends using new traversal API
        TraversalDescription travDesc = Traversal.description()
                .breadthFirst()
                .relationships( FRIEND )
                .uniqueness( Uniqueness.NODE_GLOBAL )
                .prune( Traversal.pruneAfterDepth( depth ) )
                .filter( Traversal.returnAllButStartNode() );

        return createPersonsFromPath( travDesc.traverse( underlyingNode ) );
    }

    private IterableWrapper<Person, Path> createPersonsFromPath( Traverser iterableToWrap ) {
        return new IterableWrapper<Person, Path>( iterableToWrap )
        {
            @Override
            protected Person underlyingObjectToObject( Path path )
            {
                return new Person( path.endNode() );
            }
        };
    }

    private int getNumberOfPathsToPerson( Person otherPerson )
    {
        PathFinder<Path> finder = GraphAlgoFactory.allPaths( Traversal.expanderForTypes( FRIEND, Direction.BOTH ), 2 );
        Iterable<Path> paths = finder.findAllPaths( getUnderlyingNode(), otherPerson.getUnderlyingNode() );
        return IteratorUtil.count( paths );
    }

    private Iterable<Person> createPersonsFromNodes( final Path path ) {
        return new IterableWrapper<Person, Node>( path.nodes() )
        {
            @Override
            protected Person underlyingObjectToObject( Node node )
            {
                return new Person( node );
            }
        };
    }

}

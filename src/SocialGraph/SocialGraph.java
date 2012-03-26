package SocialGraph;

import static SocialGraph.RelTypes.REF_PERSONS;
import static SocialGraph.RelTypes.A_PERSON;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.helpers.collection.IterableWrapper;
import org.neo4j.tooling.GlobalGraphOperations;

public class SocialGraph {

	private final GraphDatabaseService graphDb;
	private final Index<Node> index;
    private final Node personRefNode;
    private LinkedList<String> keywords;
    
    public SocialGraph(GraphDatabaseService graphDb, Index<Node> index){
    	this.graphDb = graphDb;
    	this.index = index;
    	registerShutdownHook(this.graphDb);
        this.personRefNode = getPersonsRootNode( graphDb );
        this.keywords = new LinkedList<String>();
    }
    
    public LinkedList<String> getKeywords() {
		return keywords;
	}

	public void readFromJSon(File file){
    	
    	JsonFactory f = new JsonFactory();
		try {
			JsonParser jp = f.createJsonParser(file);
			if (jp.nextToken() != JsonToken.START_OBJECT) {
			    throw new IOException("Expected data to start with an Object");
			}
			jp.nextToken();
			while (jp.nextToken() != JsonToken.END_ARRAY) { //Array de nodes
				String fieldname = jp.getCurrentName();				
				String nodeName = null;
				int nodeId = 0;
				while(jp.nextToken() != JsonToken.END_OBJECT){
					fieldname = jp.getCurrentName();
					if("id".equals(fieldname)){
						jp.nextToken();
						nodeId = jp.getIntValue();
					}
					if("nom".equals(fieldname)){
						jp.nextToken();
						nodeName = jp.getText();
					}
				}
				try {
					this.createPerson(nodeId, nodeName);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			jp.nextToken();
			while(jp.nextToken() != JsonToken.END_ARRAY){ //Array de relations				
				int person1 = 0, person2 = 0, poids = 0, date = 0;
				ArrayList<String> keywords = new ArrayList<String>();
				String fieldname = jp.getCurrentName();
				while(jp.nextToken() != JsonToken.END_OBJECT){
					fieldname = jp.getCurrentName();
					if("personne1".equals(fieldname)){
						jp.nextToken();
						person1 = jp.getIntValue();
					}
					if("personne2".equals(fieldname)){
						jp.nextToken();
						if(jp.getCurrentToken().isNumeric()){
							person2 = jp.getIntValue();
						}
						else{
							person2 = 0;
						}
					}
					if("poids".equals(fieldname)){
						jp.nextToken();
						poids = jp.getIntValue();
					}
					if("date".equals(fieldname)){
						jp.nextToken();
						date = jp.getIntValue();
					}
					if("message".equals(fieldname)){
						jp.nextToken();
						while(jp.nextToken() != JsonToken.END_ARRAY){
							keywords.add(jp.getText());
							if(!(this.keywords.contains( (String)jp.getText() ))){
								this.keywords.add(jp.getText());
							}
						}
					}
				}
				Transaction tx = graphDb.beginTx();
				try {
					for(String keyW : keywords){
						int idKey;
						idKey = this.keywords.indexOf((String)keyW);
						Person pers = this.getPersonById(person1);
						
						if(person2 > 0){
							Relationship newRel = pers.getUnderlyingNode().createRelationshipTo(getPersonById(person2).getUnderlyingNode(), RelTypes.FRIEND);
							newRel.setProperty(this.keywords.get(idKey), poids);
						}
						else{
							
							if(pers.getKeywordTable().containsKey((String)keyW)){
								int value = pers.getKeywordTable().get(keyW);								
								pers.getKeywordTable().put(keyW, value + 10);
								pers.getUnderlyingNode().setProperty(keyW, value +10 );
							}
							else{
								pers = this.getPersonById(person1);
								pers.getKeywordTable().put(keyW, 10);
								pers.getUnderlyingNode().setProperty(keyW, 10);
							}
						}
					}
					tx.success();
		        }
		        finally {
		            tx.finish();
		        }
			}
			jp.close(); // ensure resources get cleaned up timely and properly
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private Node getPersonsRootNode( GraphDatabaseService graphDb ){
        Relationship rel = graphDb.getReferenceNode().getSingleRelationship( REF_PERSONS, Direction.OUTGOING );
        if ( rel != null ){
            return rel.getEndNode();
        }
        else{
            Transaction tx = this.graphDb.beginTx();
            try{
                Node refNode = this.graphDb.createNode();
                this.graphDb.getReferenceNode().createRelationshipTo( refNode, REF_PERSONS );
                tx.success();
                return refNode;
            }
            finally{
                tx.finish();
            }
        }
    }
    
    
    public Person createPerson( int id, String name ) throws Exception {
        Transaction tx = graphDb.beginTx();
        try {
            Node newPersonNode = graphDb.createNode();
            personRefNode.createRelationshipTo( newPersonNode, A_PERSON );
            // lock now taken, we can check if already exist in index
            Node alreadyExist = index.get( Person.ID, id ).getSingle();
            if ( alreadyExist != null ) {
                tx.failure();
                throw new Exception( "Person with this id already exists " );
            }
            newPersonNode.setProperty( Person.NAME, name );
            newPersonNode.setProperty(Person.ID, id);
            index.add( newPersonNode, Person.ID, id );
            tx.success();
            return new Person(newPersonNode) ;
        }
        finally {
            tx.finish();
        }
    }
    
    public Person getPersonById( int id ) {
        Node personNode = index.get( Person.ID, id ).getSingle();
        if ( personNode == null )
        {
            throw new IllegalArgumentException( "Person[" + id
                    + "] not found" );
        }
        return new Person( personNode );
    }
    
    public Iterable<Person> getAllPersons()
    {
        return new IterableWrapper<Person, Relationship>(
                personRefNode.getRelationships( A_PERSON ) )
        {
            @Override
            protected Person underlyingObjectToObject( Relationship personRel )
            {
                return new Person( personRel.getEndNode() );
            }
        };
    }
    
    private static void registerShutdownHook( final GraphDatabaseService graphDb ) {
		// Registers a shutdown hook for the Neo4j instance so that it
		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
		// running example before it's completed)
		Runtime.getRuntime().addShutdownHook( new Thread() {
			@Override
			public void run() {
				System.out.println( "Shutting down database ..." );
				graphDb.shutdown();
			}
		} );
	}
    
    @Override
	public String toString() {
		String ret = "[Person] : \n";
		
		for(Person pers : getAllPersons()){
			ret += pers + "\n";
		}
		
		ret += "\n[Relations] : \n";
		
		for(Relationship rel : GlobalGraphOperations.at(graphDb).getAllRelationships()){
			String propert = "";
			for( String currentKey : rel.getPropertyKeys()){
				propert += "("+currentKey+","+rel.getProperty(currentKey)+")";
			}
			ret += "["+rel.getId()+"] : "+rel.getStartNode()+" --> "+rel.getEndNode()+"\t"+rel.getType()+"\t"+propert+"\n";
		}
		
		ret += "\nPersonRepository [graphDb=" + graphDb + ", \nindex=" + index
				+ ", \npersonRefNode=" + personRefNode + ", \nkeywords=" + keywords + "]";
		return ret;
	}
}

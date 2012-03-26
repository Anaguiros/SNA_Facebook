package SocialGraph;

import org.neo4j.graphdb.RelationshipType;

public enum RelTypes implements RelationshipType {
	REF_PERSONS,
    A_PERSON,
    FRIEND,
    SHARE_ITERN_SCRATCH,
    SHARE_EXTERN_SCRATCH,
    SHARE_INTERN_CONETENT,
    COMMENT,
    LIKE,
    LIKE_SELF,
    SHARE_EXTERN_CONTENT
}

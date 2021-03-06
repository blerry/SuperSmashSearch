package modules.queries;
import java.util.List;

import modules.indexes.*;

/**
 * A QueryComponent is one piece of a larger query, whether that piece is a literal string or represents a merging of
 * other components. All nodes in a query parse tree are QueryComponent objects.
 */
public interface QueryComponent {
    /**
     * Retrieves a list of postings for the query component, using an Index as the source.
     */
    List<Posting> getPostingsPositions(Index index);
    List<Posting> getPostings(Index index);
    
}
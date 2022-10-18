package org.eclipse.pass.object;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.pass.object.model.PassEntity;

/**
 * PassClientResult represents the sublist in the list of total objects which match a selector.
 */
public class PassClientResult<T extends PassEntity> {
    private final List<T> entities;
    private final long total;

    public PassClientResult(long total) {
        this.entities = new ArrayList<>();
        this.total = total;
    }

    /**
     * @return The total number of matching objects or -1 if not known.
     */
    public long getTotal() {
        return total;
    }

    /**
     * @return Matching objects.
     */
    public List<T> getEntities() {
        return entities;
    }
}

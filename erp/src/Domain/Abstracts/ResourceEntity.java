package Domain.Abstracts;

import Domain.Exceptions.InvalidEntityIdentityException;
import Domain.Exceptions.InvalidEntityNameException;
import java.sql.SQLException;

public abstract class ResourceEntity extends Domain.Abstracts.EntityABC {

    public ResourceEntity(String entity_id, String entity_name)
            throws InvalidEntityIdentityException, InvalidEntityNameException
    { super(entity_id, entity_name); }

    // Forces concrete classes (like Section) to handle their own DB logic
    public abstract void onPresistenceSave() throws SQLException;
    public abstract void onPresistenceDelete() throws SQLException;
}
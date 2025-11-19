package Concretes;

import Abstracts.EntityABC;
import Exceptions.InvalidEntityIdentityException;
import Exceptions.InvalidEntityNameException;

public class ResourceEntity extends EntityABC {
    public ResourceEntity(String entity_id, String entity_name)
        throws InvalidEntityIdentityException, InvalidEntityNameException
    { super(entity_id, entity_name); }

}

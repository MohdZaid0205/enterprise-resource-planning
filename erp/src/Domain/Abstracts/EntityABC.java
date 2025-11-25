package Domain.Abstracts;

import Domain.Exceptions.InvalidEntityIdentityException;
import Domain.Exceptions.InvalidEntityNameException;
import Domain.Validators.EntityIdentityValidator;
import Domain.Validators.EntityNameValidator;


public abstract class EntityABC {
    public String entity_id;
    public String entity_name;

    /**
     * ABSTRACT BASE CLASS (ABC) forming basis for all entity in out application.
     * each entity be it RESOURCE Entity or USER Entity it requires an identity [UNIQUE]
     * and a name to display entity with.
     *
     * @param entity_id if UNIQUE identity for each entity.
     * @param entity_name is VISIBLE name of [that] entity.
     *
     * @throws InvalidEntityIdentityException
     *                      if identity is already present.
     * @throws InvalidEntityNameException
     *                      if name is null space or filled.
    */
    public EntityABC(String entity_id, String entity_name)
        throws InvalidEntityIdentityException, InvalidEntityNameException
    {

        if (entity_id == null || !EntityIdentityValidator.isValid(entity_id)) {
            throw new InvalidEntityIdentityException("Provided entity_id is invalid");
        }
        this.entity_id   = entity_id;

        if (entity_name == null || !EntityNameValidator.isValid(entity_name)) {
            throw new InvalidEntityNameException("Provided entity_name is invalid");
        }
        this.entity_name = entity_name;
    }

    // GETTERS for all public defined values for [ENCAPSULATION]
    public String getId  () { return entity_id;   }
    public String getName() { return entity_name; }

    // SETTERS for all public defined values for [ENCAPSULATION]
    public void setName(String new_name)
        throws InvalidEntityNameException
    {
        if (entity_name == null || !EntityNameValidator.isValid(entity_name)) {
            throw new InvalidEntityNameException("Provided entity_name is invalid");
        }
        entity_name = new_name;
    }

    public void setId(String new_id)
        throws InvalidEntityIdentityException
    {
        if (entity_id == null || !EntityIdentityValidator.isValid(entity_id)) {
            throw new InvalidEntityIdentityException("Provided entity_id is invalid");
        }
        entity_id = new_id;
    }
}

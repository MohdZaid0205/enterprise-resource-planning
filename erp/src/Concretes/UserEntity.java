package Concretes;

import Abstracts.EntityABC;
import Exceptions.InvalidEntityIdentityException;
import Exceptions.InvalidEntityNameException;

public class UserEntity extends EntityABC {

    // no permission given to any USER-ENTITY needs overwriting in deriving subclasses.
    public Permission permission = Permission.PERMISSION_NONE;

    public UserEntity(String entity_id, String entity_name)
        throws InvalidEntityIdentityException, InvalidEntityNameException
    { super(entity_id, entity_name); }

    public enum Permission {
        PERMISSION_NONE,                // by default any user has no permission, deriving.
        PERMISSION_ADMIN,               // for super-user called ADMIN. unrestricted access
        PERMISSION_INSTRUCTOR,          // for instructor/manager of courses or [sections.]
        PERMISSION_STUDENT,             // for accessor/choose of resources and data viewer
        PERMISSION_STUDENT_INSTRUCTOR   // for accessor selected as an instructor [FELLOW.]
    }

    // TODO: implementation of a private class that holds all data [DATACLASS] for given
    // TODO: [USER] and expose all functions for getting defined attributes of this class
    // TODO: keep [RESOURCE] entity and [USER] entity separate from each other for import
    // TODO: created inner class for local [RESOURCE] Entity Management.

    // TODO: keep [RESOURCE] entity as an inner class for all user entity and their business
    // TODO: create concrete class for each [USER] entity level which create abstract method
    // TODO: to interact with different functionality. move this class to ABSTRACTS
}

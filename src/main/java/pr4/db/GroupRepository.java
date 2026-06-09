package pr4.db;

import pr4.model.Group;

import java.util.Optional;

public interface GroupRepository {

    int insert(Group group);

    Optional<Group> getById(int id);
}

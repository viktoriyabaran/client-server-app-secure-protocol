package pr4.service;

import pr4.db.GroupRepository;
import pr4.model.Group;

import java.util.Optional;

public class GroupService {

    private final GroupRepository db;

    public GroupService(GroupRepository db) {
        this.db = db;
    }

    public int create(Group group) {
        validate(group);
        return db.insert(group);
    }

    public Optional<Group> read(int id) {
        return db.getById(id);
    }

    private void validate(Group group) {
        if (group == null) {
            throw new IllegalArgumentException("Group can't be null");
        }
        if (group.getName() == null || group.getName().isBlank()) {
            throw new IllegalArgumentException("Group name can't be blank");
        }
    }
}

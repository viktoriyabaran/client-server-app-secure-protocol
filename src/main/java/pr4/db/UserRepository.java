package pr4.db;

import pr4.model.User;

import java.util.Optional;

public interface UserRepository {
    boolean comparePasswordById(int id, String passwordToCheck);
    Optional<User> getUserByUsername(String username);
}

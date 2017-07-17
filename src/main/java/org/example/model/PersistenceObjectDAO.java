package org.example.model;


import java.util.List;

public interface PersistenceObjectDAO {
    List<PersistenceObject> getAllPersistenceObjects(Class clazz);

    PersistenceObject getPersistenceObject(Class clazz, String uuid);

    void update(PersistenceObject persistenceObject);

    void delete(Class clazz, String uuid);

//    void delete(Class clazz, String uuid);
}

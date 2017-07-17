package org.example.model;

import org.eclipse.persistence.annotations.UuidGenerator;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;


@MappedSuperclass
@UuidGenerator(name = "ID_GEN")
public abstract class AbstractPersistenceObject implements PersistenceObject {
    @Id
    @GeneratedValue(generator = "ID_GEN")
    protected String id;

    public String getId() {
        return id;
    }
}
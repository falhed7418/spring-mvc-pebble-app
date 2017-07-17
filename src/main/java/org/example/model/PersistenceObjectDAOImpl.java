package org.example.model;


import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import javax.persistence.Query;
import java.util.List;
import java.util.logging.Logger;

public class PersistenceObjectDAOImpl implements PersistenceObjectDAO {
    private static final Logger LOGGER = Logger.getLogger(PersistenceObjectDAOImpl.class.getName());
    private EntityManager entityManager;

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public PersistenceObjectDAOImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<PersistenceObject> getAllPersistenceObjects(Class clazz) {
        String sql = "select po from " + clazz.getSimpleName() + " po";
        LOGGER.info("sql:" + sql);
        Query query = entityManager.createQuery(sql, clazz);

        return (List<PersistenceObject>) query.getResultList();
    }

    @Override
    public PersistenceObject getPersistenceObject(Class clazz, String uuid) {
        String sql = "select po from " + clazz.getSimpleName() + " po where po.id = :uuid";
        Query query = entityManager.createQuery(sql, clazz);
        query.setParameter("uuid", uuid);
//        javax.persistence.NoResultException
        return (PersistenceObject) query.getSingleResult();
    }

    @Override
    public void update(PersistenceObject persistenceObject) {
        entityManager.getTransaction().begin();
        entityManager.persist(persistenceObject);
        entityManager.getTransaction().commit();
    }

    @Override
    public void delete(Class clazz, String uuid) {
//        entityManager.getTransaction().begin();
//        entityManager.remove(persistenceObject);
//        entityManager.getTransaction().commit();
        entityManager.getTransaction().begin();
        int isSuccessful = entityManager.createQuery("delete from " + clazz.getSimpleName() + " po where po.id=:id").setParameter("id", uuid).executeUpdate();
        if (isSuccessful == 0) {
            throw new OptimisticLockException("PersistenceObject modified concurrently");
        }
        entityManager.getTransaction().commit();
    }
}
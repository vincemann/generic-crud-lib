package com.github.vincemann.logutil;

import com.github.vincemann.logutil.model.*;
import com.github.vincemann.logutil.repo.*;
import com.github.vincemann.logutil.service.*;
import com.github.vincemann.springrapid.core.service.exception.BadEntityException;
import com.github.vincemann.springrapid.core.slicing.RapidProfiles;
import com.github.vincemann.springrapid.core.util.LazyLogger;
import com.github.vincemann.springrapid.core.util.TransactionalTemplate;
import com.github.vincemann.springrapid.coretest.slicing.RapidTestProfiles;
import com.github.vincemann.springrapid.coretest.util.TransactionalRapidTestUtil;
import com.google.common.collect.Sets;
import lombok.SneakyThrows;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnitUtil;
import java.security.acl.Owner;
import java.util.Set;

@ActiveProfiles(value = {RapidTestProfiles.TEST, RapidTestProfiles.SERVICE_TEST, RapidProfiles.SERVICE})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class LazyLoggerTest {

//    @Autowired
//    LazyItemService lazyItemService;


//    @Autowired
//    OwnerRepository ownerRepository;

    static final String LOG_ENTITY_NAME = "log entity";
    static final String LAZY_COL1_ENTITY1_NAME = "lazy Col1 Entity1";
    static final String LAZY_COL1_ENTITY2_NAME = "lazy Col1 Entity2";
    static final String EAGER_ENTITY1_NAME = "eager col Entity1";
    static final String EAGER_ENTITY2_NAME = "eager col Entity2";
    static final String LAZY_PARENT_NAME = "lazy parent col Entity2";
    static final String LAZY_CHILD_NAME = "lazy child";
    static final String EAGER_CHILD_NAME = "eager child";
    static final String LAZY_COL2_ENTITY1_NAME = "lazy Col2 Entity1";
    static final String LAZY_COL2_ENTITY2_NAME = "lazy Col2 Entity2";

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    TransactionalTemplate transactionalTemplate;
    @Autowired
    LogChildService logChildService;

    @Autowired
    LogParentService logParentService;

    @Autowired
    LazySingleLogChildService lazySingleLogChildService;

    @Autowired
    LogEntityService logEntityService;

    @Autowired
    EagerSingleLogChildService eagerSingleLogChildService;


    LogEntity logEntity;
    LogChild lazyCol1_child1;
    LogChild lazyCol1_child2;
    LogChild lazyCol2_child1;
    LogChild lazyCol2_child2;

    LogChild eager_child1;
    LogChild eager_child2;
    LogParent lazyParent;
    LazySingleLogChild lazySingleChild;
    EagerSingleLogChild eagerSingleChild;

    LazyLogger lazyLogger;


    @BeforeEach
    void setUp() {
        logEntity = LogEntity.builder()
                .name(LOG_ENTITY_NAME)
                .build();

        lazyParent = LogParent.builder()
                .name(LAZY_PARENT_NAME)
                .build();

        lazyCol1_child1 = LogChild.builder()
                .name(LAZY_COL1_ENTITY1_NAME)
                .build();
        lazyCol1_child2 = LogChild.builder()
                .name(LAZY_COL1_ENTITY2_NAME)
                .build();

        eager_child1 = LogChild.builder()
                .name(EAGER_ENTITY1_NAME)
                .build();

        eager_child2 = LogChild.builder()
                .name(EAGER_ENTITY2_NAME)
                .build();

        lazyCol2_child1 = LogChild.builder()
                .name(LAZY_COL2_ENTITY1_NAME)
                .build();
        lazyCol2_child2 = LogChild.builder()
                .name(LAZY_COL2_ENTITY2_NAME)
                .build();

        lazySingleChild = new LazySingleLogChild(LAZY_CHILD_NAME);
        eagerSingleChild = new EagerSingleLogChild(EAGER_CHILD_NAME);
    }

    @Test
    void canIgnoreLazyInitException() throws BadEntityException {
        lazyLogger = LazyLogger.builder()
                .ignoreLazyException(Boolean.TRUE)
                .ignoreEntities(Boolean.FALSE)
                .onlyLogLoaded(Boolean.FALSE)
                .build();


        EagerSingleLogChild savedEagerSingleChild = eagerSingleLogChildService.save(eagerSingleChild);
        logEntity.setEagerChild(savedEagerSingleChild);


        logEntity.getLazyChildren1().add(lazyCol1_child1);

        LogEntity e = logEntityService.save(logEntity);
        LogEntity savedLogEntity = logEntityService.findById(e.getId()).get();


        String s = lazyLogger.toString(savedLogEntity);

        System.err.println(s);

        Assertions.assertTrue(s.contains(LazyLogger.LAZY_INIT_EXCEPTION_LIST_STRING));
        Assertions.assertFalse(s.contains(LAZY_COL1_ENTITY1_NAME));

        Assertions.assertTrue(s.contains(EAGER_CHILD_NAME));
        Assertions.assertTrue(s.contains(LOG_ENTITY_NAME));
    }

    @Test
    void canThrowLazyInitException() throws BadEntityException {
        lazyLogger = LazyLogger.builder()
                .ignoreLazyException(Boolean.FALSE)
                .ignoreEntities(Boolean.FALSE)
                .onlyLogLoaded(Boolean.FALSE)
                .build();


        EagerSingleLogChild savedEagerSingleChild = eagerSingleLogChildService.save(eagerSingleChild);
        logEntity.setEagerChild(savedEagerSingleChild);


        logEntity.getLazyChildren1().add(lazyCol1_child1);

        LogEntity e = logEntityService.save(logEntity);
        LogEntity savedLogEntity = logEntityService.findById(e.getId()).get();


        Assertions.assertThrows(LazyInitializationException.class, () -> lazyLogger.toString(savedLogEntity));
    }

    @Test
    void canIgnoreAllEntities() throws BadEntityException {
        lazyLogger = LazyLogger.builder()
                .ignoreLazyException(Boolean.TRUE)
                .ignoreEntities(Boolean.TRUE)
                .onlyLogLoaded(Boolean.FALSE)
                .build();

        // lazy Child 1
        // eager child set
        // -> nothing gets logged


        EagerSingleLogChild savedEagerSingleChild = eagerSingleLogChildService.save(eagerSingleChild);
        logEntity.setEagerChild(savedEagerSingleChild);


        logEntity.getLazyChildren1().add(lazyCol1_child1);

        LogEntity e = logEntityService.save(logEntity);
        LogEntity savedLogEntity = logEntityService.findById(e.getId()).get();


        String s = lazyLogger.toString(savedLogEntity);

        System.err.println(s);

        Assertions.assertFalse(s.contains(LazyLogger.LAZY_INIT_EXCEPTION_LIST_STRING));
        Assertions.assertFalse(s.contains(LazyLogger.LAZY_INIT_EXCEPTION_STRING));
        Assertions.assertFalse(s.contains(LAZY_COL1_ENTITY1_NAME));
        Assertions.assertFalse(s.contains(EAGER_CHILD_NAME));

        Assertions.assertTrue(s.contains(LOG_ENTITY_NAME));

    }

    @Test
    void canBlacklistFields() throws BadEntityException {
        lazyLogger = LazyLogger.builder()
                .ignoreLazyException(Boolean.TRUE)
                .ignoreEntities(Boolean.FALSE)
                .onlyLogLoaded(Boolean.FALSE)
                .propertyBlackList(Sets.newHashSet("eagerChild", "lazyChildren1"))
                .build();
        lazyLogger.setEntityManager(entityManager);


//        LogChild savedEagerChild1 = logChildService.save(eager_child1);
//        LogChild savedEagerChild2 = logChildService.save(eager_child2);

//        logEntity.getEagerChildren().add(savedEagerChild1);
//        logEntity.getEagerChildren().add(savedEagerChild2);


        // TRANSACTIONAL CONTEXT
        final String[] s = new String[1];
        transactionalTemplate.doInTransaction(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                EagerSingleLogChild savedEagerSingleChild = eagerSingleLogChildService.save(eagerSingleChild);
                logEntity.setEagerChild(savedEagerSingleChild);

                logEntity.getLazyChildren1().add(lazyCol1_child1);

                logEntity.getLazyChildren2().add(lazyCol2_child1);
                logEntity.getLazyChildren2().add(lazyCol2_child2);


                // only eagerCollection persists

                LogEntity e = logEntityService.save(logEntity);
                LogEntity savedLogEntity = logEntityService.findById(e.getId()).get();


                s[0] = lazyLogger.toString(savedLogEntity);
            }
        });

        String logResult = s[0];

        System.err.println(logResult);

        Assertions.assertFalse(logResult.contains(LAZY_COL1_ENTITY1_NAME));
        Assertions.assertFalse(logResult.contains(EAGER_CHILD_NAME));

        Assertions.assertTrue(logResult.contains(LazyLogger.IGNORED_STRING));
        Assertions.assertTrue(logResult.contains(LOG_ENTITY_NAME));
        Assertions.assertTrue(logResult.contains(LAZY_COL2_ENTITY1_NAME));
        Assertions.assertTrue(logResult.contains(LAZY_COL2_ENTITY2_NAME));
    }


    @Test
    void canIgnoreUnloadedEntities_andLogLoaded() throws BadEntityException {
        lazyLogger = LazyLogger.builder()
                .ignoreLazyException(Boolean.TRUE)
                .ignoreEntities(Boolean.FALSE)
                .onlyLogLoaded(Boolean.TRUE)
                .build();
        lazyLogger.setEntityManager(entityManager);

        // fill both lazy cols
        // lazyCol1 loaded -> gets Logged
        // lazyCol2 not loaded -> <ignored unloaded>
        // eager child -> gets logged


//        LogChild savedEagerChild1 = logChildService.save(eager_child1);
//        LogChild savedEagerChild2 = logChildService.save(eager_child2);

//        logEntity.getEagerChildren().add(savedEagerChild1);
//        logEntity.getEagerChildren().add(savedEagerChild2);


        // TRANSACTIONAL CONTEXT
//        final String[] s = new String[1];
//        transactionalTemplate.doInTransaction(new Runnable() {
//            @SneakyThrows
//            @Override
//            public void run() {
//                EagerSingleLogChild savedEagerSingleChild = eagerSingleLogChildService.save(eagerSingleChild);
//                logEntity.setEagerChild(savedEagerSingleChild);
//
//                logEntity.getLazyChildren1().add(lazyCol1_child1);
//                logEntity.getLazyChildren1().add(lazyCol1_child2);
//
//
//                // only eagerCollection persists
//
//                LogEntity e = logEntityService.save(logEntity);
//                LogEntity savedLogEntity = logEntityService.findById(e.getId()).get();
//
//                // load entities of col 1 in transactional context but not col2
//                logEntity.getLazyChildren1().size();
//
//            }
//        });

        // children1 are loaded and logged
        // children2 are not loaded and not logged

        EagerSingleLogChild savedEagerSingleChild = eagerSingleLogChildService.save(eagerSingleChild);
        logEntity.setEagerChild(savedEagerSingleChild);


        final LogEntity[] e = new LogEntity[1];
        transactionalTemplate.doInTransaction(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {

                logEntity.getLazyChildren1().add(logChildService.save(lazyCol1_child1));
                logEntity.getLazyChildren1().add(logChildService.save(lazyCol1_child2));

                logEntity.getLazyChildren2().add(logChildService.save(lazyCol2_child1));
                logEntity.getLazyChildren2().add(logChildService.save(lazyCol2_child2));

                e[0] = logEntityService.save(logEntity);
            }
        });

        e[0] = logEntityService.findByIdAndLoadCol1(e[0].getId()).get();



        LogEntity savedLogEntity = e[0];
        String logResult = lazyLogger.toString(savedLogEntity);
        Assertions.assertTrue(isLoaded(savedLogEntity, "lazyChildren1"));
        Assertions.assertFalse(isLoaded(savedLogEntity, "lazyChildren2"));

        System.err.println(logResult);


//        transactionalTemplate.doInTransaction(new Runnable() {
//            @SneakyThrows
//            @Override
//            public void run() {
//                Assertions.assertFalse(isLoaded(logEntity, "lazyChildren2"));
//                s[0] = lazyLogger.toString(savedLogEntity);
//            }
//        });
//
//
//        String logResult = s[0];
//
//        System.err.println(logResult);

        Assertions.assertTrue(logResult.contains(LAZY_COL1_ENTITY1_NAME));
        Assertions.assertTrue(logResult.contains(LAZY_COL1_ENTITY2_NAME));
        Assertions.assertTrue(logResult.contains(EAGER_CHILD_NAME));
        Assertions.assertTrue(logResult.contains(LOG_ENTITY_NAME));

//        Assertions.assertTrue(logResult.contains(LazyLogger.IGNORED_UNLOADED_STRING));
        Assertions.assertFalse(logResult.contains(LAZY_COL2_ENTITY1_NAME));
        Assertions.assertFalse(logResult.contains(LAZY_COL2_ENTITY2_NAME));
    }

    private boolean isLoaded(Object parent, String childPropertyName) {
        PersistenceUnitUtil persistenceUtil =
                entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
        Boolean loaded = persistenceUtil.isLoaded(parent, childPropertyName);
        return loaded;
    }
//
//    @Test
//    void canShowLoadedLazyCollection_andIgnoreNotLoadedLazyCollectionsException() throws BadEntityException {
//        LogParent lazyItem = new LogParent();
//        LogChild logChild = new LogChild("loaded");
//
//        logEntity.getLazyChildren1().add(lazyItem);
//        logEntity.getLazyChildren2().add(logChild);
//
//        Owner savedKahn = ownerService.save(logEntity);
//
//
//        Owner found = ownerService.lazyLoadFindById(savedKahn.getId());
//        // would result in lazyinit exception
////        found.getLazyItems().size();
//
//        String s = LazyLogUtils.toString(found, Boolean.FALSE);
//        System.err.println(s);
//
//        Assertions.assertTrue(s.contains("LazyInitializationException"));
//        Assertions.assertTrue(s.contains("loaded"));
//    }
//
//    @Test
//    void canIgnoreCollections() throws BadEntityException {
//        LogParent lazyItem = new LogParent();
//        LogChild logChild = new LogChild("loaded");
//
//        logEntity.getLazyChildren1().add(lazyItem);
//        logEntity.getLazyChildren2().add(logChild);
//        logEntity.getPets().add(bello);
//
//        Owner savedKahn = ownerService.save(logEntity);
//
//
//        Owner found = ownerService.lazyLoadFindById(savedKahn.getId());
//        // would result in lazyinit exception
////        found.getLazyItems().size();
//
//        String s = LazyLogUtils.toString(found);
//        System.err.println(s);
//
//        Assertions.assertFalse(s.contains("LazyInitializationException"));
//        Assertions.assertFalse(s.contains("loaded"));
//        Assertions.assertFalse(s.contains("bello"));
//        Assertions.assertTrue(s.contains(logEntity.getFirstName()));
//        Assertions.assertTrue(s.contains(logEntity.getLastName()));
//        Assertions.assertTrue(s.contains(logEntity.getCity()));
//    }
//
//    @Transactional
//    @Test
//    void canIgnoreSomeEntitiesAndCollections() throws BadEntityException {
//        logEntity.setClinicCard(clinicCardService.save(clinicCard));
//        logEntity.getPets().add(bello);
//
//        Owner savedKahn = ownerService.save(logEntity);
//
//        String s = LazyLogUtils.toString(savedKahn,Boolean.TRUE, Sets.newHashSet("clinicCard"));
//        System.err.println(s);
//
//        Assertions.assertFalse(s.contains("bello"));
//        Assertions.assertTrue(s.contains(clinicCard.getRegistrationReason()));
//
//        Assertions.assertTrue(s.contains(logEntity.getFirstName()));
//        Assertions.assertTrue(s.contains(logEntity.getLastName()));
//        Assertions.assertTrue(s.contains(logEntity.getCity()));
//    }
//
//
//    @Test
//    void canThrowLazy() throws BadEntityException {
//        LogParent lazyItem = new LogParent();
////        LazyItem savedLazyItem = getService().save(lazyItem);
//
//        logEntity.getLazyChildren1().add(lazyItem);
//
//        Owner savedKahn = ownerService.save(logEntity);
//
//
//        Owner found = ownerService.findById(savedKahn.getId()).get();
//
//        Assertions.assertThrows(LazyInitializationException.class,
//                () -> LazyLogUtils.toString(found,new HashSet<>(), Boolean.FALSE,Boolean.FALSE,Boolean.FALSE));
//    }

    @AfterEach
    void tearDown() {
        TransactionalRapidTestUtil.clear(logChildService);
        TransactionalRapidTestUtil.clear(logParentService);
        TransactionalRapidTestUtil.clear(lazySingleLogChildService);
        TransactionalRapidTestUtil.clear(eagerSingleLogChildService);
        TransactionalRapidTestUtil.clear(logEntityService);

//        clinicCardService.deleteAll();
//        petService.deleteAll();

//
//        logChildService.deleteAll();
//        logParentService.deleteAll();
//        lazySingleLogChildService.deleteAll();
//        eagerSingleLogChildService.deleteAll();
    }

    //    @Test
//    void doesNotLoadAdditionalEntitiesInTransaction() throws BadEntityException {
//        final Long[] id = {null};
//        transactionalTestTemplate.doInTransaction(new Runnable() {
//            @SneakyThrows
//            @Override
//            public void run() {
//                LazyLoadedItem lazyLoadedItem = new LazyLoadedItem("loaded");
//                LazyLoadedItem lazyLoadedItem2 = new LazyLoadedItem("loaded2");
//
//                LazyExceptionItem notLoadedItem = new LazyExceptionItem("not-loaded");
//
//
//                kahn.getLazyLoadedItems().add(lazyLoadedItem);
//                kahn.getLazyLoadedItems().add(lazyLoadedItem2);
//                kahn.getLazyExceptionItems().add(notLoadedItem);
//
//                Owner savedKahn = ownerService.save(kahn);
//                id[0] = savedKahn.getId();
//            }
//        });
//
//        final Owner[] found = new Owner[1];
//        transactionalTestTemplate.doInTransaction(new Runnable() {
//            @Override
//            public void run() {
//                found[0] = ownerService.lazyLoadFindById(id[0]);
//                String s = LazyLogUtils.toString(found[0], Boolean.FALSE);
//                System.err.println(s);
//
////                Assertions.assertFalse(s.contains("LazyInitializationException"));
//                Assertions.assertTrue(s.contains("loaded"));
//                Assertions.assertTrue(s.contains("loaded2"));
//                Assertions.assertFalse(s.contains("not-loaded"));
//            }
//        });
//        // to string has not loaded more items
//        Assertions.assertThrows(LazyInitializationException.class, () -> found[0].getLazyExceptionItems().size());
//        System.err.println("done");
//
//
//    }
}
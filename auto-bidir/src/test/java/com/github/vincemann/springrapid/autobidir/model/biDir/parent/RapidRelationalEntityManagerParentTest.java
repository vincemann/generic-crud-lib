package com.github.vincemann.springrapid.autobidir.model.biDir.parent;

import com.github.vincemann.springrapid.autobidir.RapidRelationalEntityManager;
import com.github.vincemann.springrapid.autobidir.RelationalEntityManager;
import com.github.vincemann.springrapid.core.model.IdentifiableEntity;
import com.github.vincemann.springrapid.core.util.Lists;
import com.github.vincemann.springrapid.core.model.IdentifiableEntityImpl;

import com.github.vincemann.springrapid.autobidir.model.parent.annotation.BiDirParentEntity;

import com.github.vincemann.springrapid.autobidir.model.child.annotation.BiDirChildCollection;
import com.github.vincemann.springrapid.autobidir.model.child.annotation.BiDirChildEntity;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

class RapidRelationalEntityManagerParentTest {

    @Getter
    @Setter
    private class EntityChild extends IdentifiableEntityImpl<Long>  {
        @BiDirParentEntity
        private EntityParent entityParent;
        private String name;
        private EntityParent unusedParent;
        @BiDirParentEntity
        private SecondEntityParent secondEntityParent;
    }

    @Getter
    @Setter
    private class SecondEntityChild extends IdentifiableEntityImpl<Long>  {
        @BiDirParentEntity
        private EntityParent entityParent;
    }
    @Getter
    @Setter
    private class SecondEntityParent extends IdentifiableEntityImpl<Long> {
        @BiDirChildEntity
        private EntityChild entityChild;
    }
    @Getter
    @Setter
    private class EntityParent extends IdentifiableEntityImpl<Long> {
        @BiDirChildEntity
        private EntityChild entityChild;
        @BiDirChildCollection(SecondEntityChild.class)
        private Set<SecondEntityChild> secondEntityChildSet = new HashSet<>();
    }

    private EntityChild testEntityChild;
    private EntityParent testEntityParent;
    private SecondEntityParent testSecondEntityParent;
    private SecondEntityChild testSecondEntityChild;

    private RelationalEntityManager relationalEntityManager;

    @BeforeEach
    void setUp() {
        this.relationalEntityManager = new RapidRelationalEntityManager();
        this.testEntityChild= new EntityChild();
        testEntityChild.setId(1L);
        this.testEntityParent = new EntityParent();
        testEntityParent.setId(2L);
        this.testSecondEntityParent = new SecondEntityParent();
        testSecondEntityParent.setId(3L);
        this.testSecondEntityChild = new SecondEntityChild();
        testSecondEntityChild.setId(4L);
    }


    @Test
    void unlinkChildrensParent()  {
        //given
        testEntityParent.setEntityChild(testEntityChild);
        testEntityChild.setEntityParent(testEntityParent);
        //when

        relationalEntityManager.unlinkChildrensParent(testEntityParent);
        //then
        Assertions.assertNull(testEntityChild.getEntityParent());
        Assertions.assertNotNull(testEntityParent.getEntityChild());
    }
    @Test
    void unlinkChildrensCollectionParent()  {
        //given
        testEntityParent.setSecondEntityChildSet(new HashSet<>(Lists.newArrayList(testSecondEntityChild)));
        testSecondEntityChild.setEntityParent(testEntityParent);
        //when
        relationalEntityManager.unlinkChildrensParent(testEntityParent);
        //then
        Assertions.assertFalse(testEntityParent.getSecondEntityChildSet().stream().findFirst().isPresent());
        Assertions.assertNull(testSecondEntityChild.getEntityParent());
    }
    @Test
    void unlinkAllChildrensParent()  {
        //given
        testEntityParent.setSecondEntityChildSet(new HashSet<>(Lists.newArrayList(testSecondEntityChild)));
        testSecondEntityChild.setEntityParent(testEntityParent);
        testEntityParent.setEntityChild(testEntityChild);
        testEntityChild.setEntityParent(testEntityParent);
        //when
        relationalEntityManager.unlinkChildrensParent(testEntityParent);
        //then
        Assertions.assertFalse(testEntityParent.getSecondEntityChildSet().stream().findFirst().isPresent());
        Assertions.assertNull(testSecondEntityChild.getEntityParent());
        Assertions.assertNull(testEntityChild.getEntityParent());
        Assertions.assertNotNull(testEntityParent.getEntityChild());
    }

    @Test
    void addChild()  {
        //when
        relationalEntityManager.linkBiDirChild(testEntityParent, testEntityChild);
        //then
        Assertions.assertSame(testEntityChild,testEntityParent.getEntityChild());
    }

    @Test
    void addChildToCollection()  {
        //given
        Assertions.assertNotNull(testEntityParent.getSecondEntityChildSet());
        //when
        relationalEntityManager.linkBiDirChild(testEntityParent,testSecondEntityChild);
        //then
        Assertions.assertEquals(1,testEntityParent.getSecondEntityChildSet().size());
        Assertions.assertSame(testSecondEntityChild,testEntityParent.getSecondEntityChildSet().stream().findFirst().get());
    }

    @Test
    void addChildToNullCollection_shouldAutoCreateCollectionAndWork()  {
        //given
        testEntityParent.setSecondEntityChildSet(null);
        relationalEntityManager.linkBiDirChild(testEntityParent,testSecondEntityChild);
        Assertions.assertEquals(testSecondEntityChild,testEntityParent.getSecondEntityChildSet().stream().findFirst().get());
    }

    @Test
    void unlinkChild()  {
        //given
        testEntityParent.setEntityChild(testEntityChild);
        //when
        relationalEntityManager.unlinkBiDirChild(testEntityParent,testEntityChild);
        //then
        Assertions.assertNull(testEntityParent.getEntityChild());
    }

    @Test
    void unlinkChildFromCollection()  {
        //given
        testEntityParent.setSecondEntityChildSet(new HashSet<>(Collections.singleton(testSecondEntityChild)));
        //when
        relationalEntityManager.unlinkBiDirChild(testEntityParent,testSecondEntityChild);
        //then
        Assertions.assertTrue(testEntityParent.getSecondEntityChildSet().isEmpty());
    }

    @Test
    void unlinkChildFromFilledCollection()  {
        //given
        SecondEntityChild second = new SecondEntityChild();
        second.setId(99L);
        Set<SecondEntityChild> secondEntityChildren = new HashSet<>();
        secondEntityChildren.add(testSecondEntityChild);
        secondEntityChildren.add(second);
        testEntityParent.setSecondEntityChildSet(secondEntityChildren);
        //when
        relationalEntityManager.unlinkBiDirChild(testEntityParent,testSecondEntityChild);
        //then
        Assertions.assertEquals(1,testEntityParent.getSecondEntityChildSet().size());
        Assertions.assertSame(second,testEntityParent.getSecondEntityChildSet().stream().findFirst().get());
    }

//    @Test
//    void findChildrenCollectionFields() {
//        //when
//        Field[] childrenCollectionFields = testEntityParent.findChildrenCollectionFields();
//        //then
//        Assertions.assertEquals(1,childrenCollectionFields.length);
//        Assertions.assertEquals("secondEntityChildSet",childrenCollectionFields[0].getName());
//    }
//
//    @Test
//    void findChildrenEntityFields() {
//        //when
//        Field[] childrenEntityFields = testEntityParent.findChildrenEntityFields();
//        //then
//        Assertions.assertEquals(1,childrenEntityFields.length);
//        Assertions.assertEquals("entityChild",childrenEntityFields[0].getName());
//
//    }

    @Test
    void getChildrenCollections()  {
        //given
        HashSet<SecondEntityChild> secondEntityChildSet = new HashSet<>();
        testEntityParent.setSecondEntityChildSet(secondEntityChildSet);
        //when
        Map<Collection<IdentifiableEntity>, Class<IdentifiableEntity>> childrenCollections = relationalEntityManager.findBiDirChildCollections(testEntityParent);
        //then
        Assertions.assertEquals(1,childrenCollections.size());
        Map.Entry<Collection<IdentifiableEntity>, Class<IdentifiableEntity>> entry = childrenCollections.entrySet().stream().findFirst().get();
        Assertions.assertEquals(SecondEntityChild.class,entry.getValue());
        Assertions.assertSame(secondEntityChildSet,entry.getKey());
    }

    @Test
    void getNullChildrenCollection_shouldCreateEmptyCollection()  {
        //given
        testEntityParent.setSecondEntityChildSet(null);
        Map<Collection<IdentifiableEntity>, Class<IdentifiableEntity>> childrenCollections = relationalEntityManager.findBiDirChildCollections(testEntityParent);
        for (Map.Entry<Collection<IdentifiableEntity>, Class<IdentifiableEntity>> collectionClassEntry : childrenCollections.entrySet()) {
            Assertions.assertNotNull(collectionClassEntry.getKey());
            Assertions.assertTrue(collectionClassEntry.getKey().isEmpty());
            Assertions.assertTrue(collectionClassEntry.getKey() instanceof Set);
        }
    }

    @Test
    void getChildren()  {
        //given
        testEntityParent.setEntityChild(testEntityChild);
        //when
        Set<? extends IdentifiableEntity> children = relationalEntityManager.findSingleBiDirChildren(testEntityParent);
        //then
        Assertions.assertEquals(1,children.size());
        Assertions.assertSame(testEntityChild,children.stream().findFirst().get());
    }

    @Test
    void getNullChildren()  {
        //given
        testEntityParent.setEntityChild(null);
        //when
        Set<? extends IdentifiableEntity> children = relationalEntityManager.findSingleBiDirChildren(testEntityParent);
        //then
        Assertions.assertTrue(children.isEmpty());
    }
}
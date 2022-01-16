package com.github.vincemann.springrapid.autobidir;

import com.github.vincemann.springrapid.autobidir.model.RelationalEntityType;
import com.github.vincemann.springrapid.core.model.IdentifiableEntity;
import com.github.vincemann.springrapid.core.service.exception.BadEntityException;
import com.github.vincemann.springrapid.core.service.exception.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;

@Slf4j
@Transactional
public class RapidRelationalEntityManager implements RelationalEntityManager {

    private RelationalEntityManagerUtil relationalEntityManagerUtil;
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public <E extends IdentifiableEntity> E save(E entity, String... membersToCheck) {
        if (entity.getId() != null) {
            throw new IllegalArgumentException("save needs null id");
        }
        Set<RelationalEntityType> relationalEntityTypes = relationalEntityManagerUtil.inferTypes(entity.getClass());
        if (relationalEntityTypes.contains(RelationalEntityType.BiDirParent)) {
            log.debug("applying pre persist BiDirParent logic for: " + entity);
            // also filter for class obj stored in annotation, so if I update only one BiDirChildCollection, only init this one
            // with the right class
//            entity = BiDirJpaUtils.initializeSubEntities(entity, BiDirChildCollection.class);
//            entity = BiDirJpaUtils.initializeSubEntities(entity, BiDirChildEntity.class);
            relationalEntityManagerUtil.linkChildrensParent(entity);

        }

        if (relationalEntityTypes.contains(RelationalEntityType.BiDirChild)) {
            log.debug("applying pre persist BiDirChild logic for: " + entity);
//            entity = BiDirJpaUtils.initializeSubEntities(entity, BiDirParentEntity.class);
//            entity = BiDirJpaUtils.initializeSubEntities(entity, BiDirParentCollection.class);
            relationalEntityManagerUtil.linkParentsChild(entity);
        }
        return entity;
    }

    @Override
    public void remove(IdentifiableEntity entity, String... membersToCheck) throws EntityNotFoundException, BadEntityException {
        Set<RelationalEntityType> relationalEntityTypes = relationalEntityManagerUtil.inferTypes(entity.getClass());

        if (relationalEntityTypes.contains(RelationalEntityType.BiDirParent)) {
            log.debug("applying pre remove BiDirParent logic for: " + entity.getClass());
            relationalEntityManagerUtil.unlinkChildrensParent(entity);
        }
        if (relationalEntityTypes.contains(RelationalEntityType.BiDirChild)) {
            log.debug("applying pre remove BiDirChild logic for: " + entity);
            relationalEntityManagerUtil.unlinkParentsChild(entity);
        }
    }


    @Override
    public <E extends IdentifiableEntity> E partialUpdate(E oldEntity, E updateEntity, E partialUpdateEntity, String... membersToCheck) throws EntityNotFoundException, BadEntityException {
        // only operate on non null fields of partialUpdateEntity
        Set<RelationalEntityType> relationalEntityTypes = relationalEntityManagerUtil.inferTypes(updateEntity.getClass());
        if (relationalEntityTypes.contains(RelationalEntityType.BiDirParent)) {
            log.debug("applying pre partial-update BiDirParent logic for: " + updateEntity.getClass());
            updateBiDirParentRelations(oldEntity, updateEntity);
        }
        if (relationalEntityTypes.contains(RelationalEntityType.BiDirChild)) {
            log.debug("applying pre partial-update BiDirChild logic for: " + updateEntity.getClass());
            updateBiDirChildRelations(oldEntity, updateEntity);
        }
        return updateEntity;
    }

    @Override
    public <E extends IdentifiableEntity> E update(E oldEntity, E updateEntity, String... membersToCheck) throws EntityNotFoundException, BadEntityException {
        Set<RelationalEntityType> relationalEntityTypes = relationalEntityManagerUtil.inferTypes(updateEntity.getClass());

        if (relationalEntityTypes.contains(RelationalEntityType.BiDirParent)) {
            log.debug("applying pre full-update BiDirParent logic for: " + updateEntity.getClass());
            updateBiDirParentRelations(oldEntity, updateEntity);
        }
        if (relationalEntityTypes.contains(RelationalEntityType.BiDirChild)) {
            log.debug("applying pre full-update BiDirChild logic for: " + updateEntity.getClass());
            updateBiDirChildRelations(oldEntity, updateEntity);
        }

        return updateEntity;
    }

    public Collection<IdentifiableEntity> updateBiDirChildRelations(IdentifiableEntity oldChild, IdentifiableEntity child, String... membersToCheck) throws BadEntityException, EntityNotFoundException {

        Collection<IdentifiableEntity> oldParents = relationalEntityManagerUtil.findAllBiDirParents(oldChild);
        Collection<IdentifiableEntity> newParents = relationalEntityManagerUtil.findAllBiDirParents(child);

        //find parents to unlink
        List<IdentifiableEntity> removedParents = new ArrayList<>();
        for (IdentifiableEntity oldParent : oldParents) {
            if (!newParents.contains(oldParent)) {
                removedParents.add(oldParent);
            }
        }

        //find added parents
        List<IdentifiableEntity> addedParents = new ArrayList<>();
        for (IdentifiableEntity newParent : newParents) {
            if (!oldParents.contains(newParent)) {
                addedParents.add(newParent);
            }else {
                // all parents need to be merged, non added children can be merged here already
                entityManager.merge(newParent);
            }
        }

        adjustUpdatedEntities(addedParents, removedParents);

        // unlink Child from certain Parents
        for (IdentifiableEntity removedParent : removedParents) {
            log.debug("update: unlinking parent: " + removedParent + " from child: " + child);
//            relationalEntityManagerUtil.unlinkBiDirChild(removedParent, oldChild);
            relationalEntityManagerUtil.unlinkBiDirChild(removedParent, child);  // somehow does not make a difference but makes more sense like that imo
        }

        // link added Parent to child
        for (IdentifiableEntity addedParent : addedParents) {
            log.debug("update: linking parent: " + addedParent + " to child: " + child);
            relationalEntityManagerUtil.linkBiDirChild(addedParent, child);
            // new parents may be detached, so merge them, must happen after linking!
            entityManager.merge(addedParent);
        }
//        entityManager.merge(child); wont do no harm, maybe needed if child is detached?
        return newParents;
    }

    public Collection<IdentifiableEntity> updateBiDirParentRelations(IdentifiableEntity oldParent, IdentifiableEntity parent, String... membersToCheck) throws BadEntityException, EntityNotFoundException {

        Collection<IdentifiableEntity> oldChildren = relationalEntityManagerUtil.findAllBiDirChildren(oldParent);
        Collection<IdentifiableEntity> newChildren = relationalEntityManagerUtil.findAllBiDirChildren(parent);

        //find Children to unlink
        List<IdentifiableEntity> removedChildren = new ArrayList<>();
        for (IdentifiableEntity oldChild : oldChildren) {
            if (!newChildren.contains(oldChild)) {
                removedChildren.add(oldChild);
            }
        }

        //find added Children
        List<IdentifiableEntity> addedChildren = new ArrayList<>();
        for (IdentifiableEntity newChild : newChildren) {
            if (!oldChildren.contains(newChild)) {
                addedChildren.add(newChild);
            }else {
                // all children need to be merged, non added children can be merged here already
                entityManager.merge(newChild);
            }
        }

        adjustUpdatedEntities(addedChildren, removedChildren);

        //unlink removed Children from parent
        for (IdentifiableEntity removedChild : removedChildren) {
            log.debug("unlinking child: " + removedChild + " from parent: " + parent);
//            relationalEntityManagerUtil.unlinkBiDirParent(removedChild, oldParent);
            relationalEntityManagerUtil.unlinkBiDirParent(removedChild, parent); // somehow does not make a difference but makes more sense like that imo
        }

        //link added Children to parent
        for (IdentifiableEntity addedChild : addedChildren) {
            log.debug("linking child: " + addedChild + " to parent: " + parent);
            // illness gets set of pets updated, illness = child
            relationalEntityManagerUtil.linkBiDirParent(addedChild, parent);
            // new children may be detached, so merge them , must happen after linking!
            entityManager.merge(addedChild);
        }
        return newChildren;
//        entityManager.merge(parent); wont do no harm, maybe needed if newChild is detached?
    }

    protected <E> void adjustUpdatedEntities(List<E> added, List<E> removed) {
        removed.removeAll(added);
        added.removeAll(removed);
    }


//    protected void mergeChildrensParents(IdentifiableEntity biDirChild) {
//        for (IdentifiableEntity parent : relationalEntityManagerUtil.findAllBiDirParents(biDirChild)) {
//            entityManager.merge(parent);
//        }
//    }
//
//    protected void mergeParentsChildren(IdentifiableEntity biDirParent) {
//        for (IdentifiableEntity child : relationalEntityManagerUtil.findAllBiDirChildren(biDirParent)) {
//            entityManager.merge(child);
//        }
//    }
//
//    private void replaceParentsChildRefAndMerge(IdentifiableEntity child) {
//        //set backreferences
//
//        for (IdentifiableEntity parent : relationalEntityManagerUtil.findAllBiDirParents(child)) {
//            relationalEntityManagerUtil.linkBiDirChild(parent,child);
//            entityManager.merge(parent);
//        }
//    }
//
//    private void replaceChildrensParentRefAndMerge(IdentifiableEntity parent) {
//        //set backreferences
//        for (IdentifiableEntity child : relationalEntityManagerUtil.findAllBiDirChildren(parent)) {
//            relationalEntityManagerUtil.linkBiDirParent(child,parent);
//            entityManager.merge(child);
//        }
//    }

//    private void updateAddedChildren(IdentifiableEntity parent, List<IdentifiableEntity> addedChildren) {
////        for (IdentifiableEntity child : relationalEntityManagerUtil.findAllBiDirChildren(parent)) {
////            System.err.println(child);
////        }
//
//        //set backreferences
//        for (IdentifiableEntity child : addedChildren) {
//            System.err.println(child);
//            relationalEntityManagerUtil.linkBiDirParent(child,parent);
//            entityManager.merge(child);
//        }
//    }
//
//    private void updateAddedParents(IdentifiableEntity child, List<IdentifiableEntity> addedParents) {
//        //set backreferences
//        for (IdentifiableEntity parent : addedParents) {
//            System.err.println(parent);
//            relationalEntityManagerUtil.linkBiDirChild(parent,child);
//            entityManager.merge(parent);
//        }
//    }


//    private void linkChildrensParent(IdentifiableEntity biDirParent) {
//        Set<? extends IdentifiableEntity> children = relationalEntityManagerUtil.findSingleBiDirChildren(biDirParent);
//        for (IdentifiableEntity child : children) {
//            relationalEntityManagerUtil.linkBiDirParent(child, biDirParent);
//        }
//        Collection<Collection<IdentifiableEntity>> childCollections = relationalEntityManagerUtil.findBiDirChildCollections(biDirParent).values();
//        for (Collection<IdentifiableEntity> childCollection : childCollections) {
//            for (IdentifiableEntity child : childCollection) {
//                relationalEntityManagerUtil.linkBiDirParent(child, biDirParent);
//            }
//        }

//    }

//    private void linkParentsChild(IdentifiableEntity biDirChild) {
//        //set backreferences
//
//        Collection<Collection<IdentifiableEntity>> parentCollections = relationalEntityManagerUtil.findBiDirParentCollections(biDirChild).values();
//        for (Collection<IdentifiableEntity> parentCollection : parentCollections) {
//            for (IdentifiableEntity biDirParent : parentCollection) {
//                relationalEntityManagerUtil.linkBiDirChild(biDirParent,biDirChild);
//            }
//        }
//
//        for (IdentifiableEntity parent : relationalEntityManagerUtil.findSingleBiDirParents(biDirChild)) {
//            relationalEntityManagerUtil.linkBiDirChild(parent,biDirChild);
//        }
//
//    }


    @Autowired
    public void setRelationalEntityManagerUtil(RelationalEntityManagerUtil relationalEntityManagerUtil) {
        this.relationalEntityManagerUtil = relationalEntityManagerUtil;
    }

}

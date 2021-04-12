package com.github.vincemann.springrapid.coredemo.service.jpa;

import com.github.vincemann.springrapid.core.service.exception.BadEntityException;
import com.github.vincemann.springrapid.core.service.exception.EntityNotFoundException;
import com.github.vincemann.springrapid.core.util.Lists;
import com.github.vincemann.springrapid.coredemo.model.Owner;
import com.github.vincemann.springrapid.coredemo.model.Pet;
import com.github.vincemann.springrapid.coredemo.model.PetType;
import com.github.vincemann.springrapid.coredemo.repo.OwnerRepository;
import com.github.vincemann.springrapid.coredemo.repo.PetRepository;
import com.github.vincemann.springrapid.coredemo.repo.PetTypeRepository;
import com.github.vincemann.springrapid.coredemo.service.OwnerService;
import com.github.vincemann.springrapid.coredemo.service.PetService;
import com.github.vincemann.springrapid.coredemo.service.PetTypeService;
import com.github.vincemann.springrapid.coredemo.service.plugin.OwnerOfTheYearExtension;
import com.github.vincemann.springrapid.coretest.service.AbstractCrudServiceIntegrationTest;
import com.github.vincemann.springrapid.coretest.service.result.ServiceResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;

import static com.github.vincemann.ezcompare.Comparator.compare;
import static com.github.vincemann.springrapid.coredemo.service.jpa.JpaOwnerService.OWNER_OF_THE_YEARS_NAME;
import static com.github.vincemann.springrapid.coretest.service.ExceptionMatchers.noException;
import static com.github.vincemann.springrapid.coretest.service.ExistenceMatchers.notPresentInDatabase;
import static com.github.vincemann.springrapid.coretest.service.PropertyMatchers.propertyAssert;
import static com.github.vincemann.springrapid.coretest.service.request.CrudServiceRequestBuilders.*;
import static com.github.vincemann.springrapid.coretest.service.resolve.EntityPlaceholder.*;


//@EnableProjectComponentScan
//@ImportRapidEntityRelServiceConfig

/**
 * Test to showcase that auto management of bidir-relationships work
 */
class OwnerServiceIntegrationTest
        extends MyCrudServiceIntegrationTest<OwnerService, Owner, Long> {


    @Test
    public void canSaveOwnerWithoutPets() {
        ServiceResult serviceResult =
                test(save(meier))
                        .andExpect(() -> compare(meier)
                                // resolve db entity makes sure entity is actually saved in repo
                                .with(resolve(DB_ENTITY))
                                .properties()
                                //per instance or Type is both possible Owner::getAnything wont work
                                .include(meier::getTelephone)
                                .include(OwnerType::getAddress)
                                .assertEqual())
                        .andExpect(()-> propertyAssert(resolve(SERVICE_RETURNED_ENTITY))
                                .assertEmpty(OwnerType::getPets)
                        )
                        .andReturn();

        Assertions.assertTrue(ownerRepository.findByLastName(MEIER).isPresent());
        Assertions.assertTrue(ownerRepository.findByLastName(MEIER).get().getPets().isEmpty());
        Assertions.assertEquals(0, petRepository.count());
    }



    @Test
    public void canSavePetAlongsideWithOwner() throws BadEntityException {
        kahn.setPets(new HashSet<>(Lists.newArrayList(bello)));
        test(save(kahn))
                .andDo(() -> compare(resolve(SERVICE_INPUT_ENTITY))
                        // resolve db entity makes sure entity is actually saved in repo
                        .with(resolve(DB_ENTITY))
                        .properties()
                        .all()
                        //ignoring/adding properties via their name as a string is also possible but not recommended
                        .ignore("id")
                        .assertEqual());


        Assertions.assertTrue(petRepository.findByName(BELLO).isPresent());
        Assertions.assertEquals(1, petRepository.count());

        // verify bidir rel management
        Pet savedBello = petRepository.findByName(BELLO).get();
        Owner savedKahn = ownerRepository.findByLastName(KAHN).get();
        // check if bidir relation ships were managed
        Assertions.assertEquals(savedKahn,savedBello.getOwner());
        Assertions.assertEquals(savedBello,savedKahn.getPets().stream().findFirst().get());
    }


    @Test
    public void canLinkSavedPetToOwner_viaSave() throws BadEntityException {
        Pet savedBello = petService.save(bello);


        kahn.setPets(new HashSet<>(Lists.newArrayList(savedBello)));

        test(save(kahn))
                .andExpect(() -> compare(kahn)
                        // comparing service returned entity does not make sure entity is saved in db
                        .with(resolve(SERVICE_RETURNED_ENTITY))
                        .properties()
                        .all()
                        .ignore(OwnerType::getId)
                        .assertEqual()
                );

        Assertions.assertTrue(petRepository.findByName(BELLO).isPresent());
        Assertions.assertEquals(1, petRepository.count());

        // verify bidir rel management
        Pet dbBello = petRepository.findByName(BELLO).get();
        Owner savedKahn = ownerRepository.findByLastName(KAHN).get();
        // check if bidir relation ships were managed
        Assertions.assertEquals(savedKahn,dbBello.getOwner());
        Assertions.assertEquals(dbBello,savedKahn.getPets().stream().findFirst().get());
    }

    @Test
    public void canLinkSavedPetsToOwner_viaSave() throws BadEntityException {
        Pet savedBello = petService.save(bello);
        Pet savedKitty = petService.save(kitty);

        kahn.setPets(new HashSet<>(Lists.newArrayList(savedBello,savedKitty)));

        test(save(kahn))
                .andExpect(() -> compare(kahn)
                        .with(resolve(SERVICE_RETURNED_ENTITY))
                        .properties()
                        .all()
                        .ignore(OwnerType::getId)
                        .assertEqual()
                );

        Assertions.assertEquals(2, petRepository.count());
        Assertions.assertTrue(petRepository.findByName(BELLO).isPresent());
        Assertions.assertTrue(petRepository.findByName(KITTY).isPresent());

        // verify bidir rel management
        Pet dbBello = petRepository.findByName(BELLO).get();
        Pet dbKitty = petRepository.findByName(KITTY).get();
        Owner savedKahn = ownerRepository.findByLastName(KAHN).get();
        // check if bidir relation ships were managed
        Assertions.assertEquals(savedKahn,dbBello.getOwner());
        Assertions.assertEquals(savedKahn,dbKitty.getOwner());
        Assertions.assertEquals(dbBello,savedKahn.getPets().stream().filter(p -> p.getName().equals(BELLO)).findFirst().get());
        Assertions.assertEquals(dbKitty,savedKahn.getPets().stream().filter(p -> p.getName().equals(KITTY)).findFirst().get());
    }



    @Test
    public void canAddAnotherSavedPetToOwner_viaPartialUpdate() throws BadEntityException, EntityNotFoundException, BadEntityException {
        //given
        Pet savedBello = petService.save(bello);
        Pet savedKitty = petService.save(kitty);

        kahn.setPets(new HashSet<>(Lists.newArrayList(savedBello)));

        Owner savedKahn = getServiceUnderTest().save(kahn);

        Owner ownerUpdateRequest = new Owner();
        ownerUpdateRequest.getPets().addAll(savedKahn.getPets());
        //here comes the new pet
        ownerUpdateRequest.getPets().add(savedKitty);

        //when
        ownerUpdateRequest.setId(savedKahn.getId());

        test(partialUpdate(ownerUpdateRequest))
                .andExpect(() -> propertyAssert(resolve(DB_ENTITY))
                        .assertMatchSize(OwnerType::getPets, 2)
                );

        // verify bidir rel management
        Pet dbBello = petRepository.findByName(BELLO).get();
        Pet dbKitty = petRepository.findByName(KITTY).get();
        Owner dbKahn = ownerRepository.findByLastName(KAHN).get();
        // check if bidir relation ships were managed
        Assertions.assertEquals(dbKahn,dbBello.getOwner());
        Assertions.assertEquals(dbKahn,dbKitty.getOwner());
        Assertions.assertEquals(dbBello,dbKahn.getPets().stream().filter(p -> p.getName().equals(BELLO)).findFirst().get());
        Assertions.assertEquals(dbKitty,dbKahn.getPets().stream().filter(p -> p.getName().equals(KITTY)).findFirst().get());
    }

    @Test
    public void canAddMultipleSavedPetsToOwner_viaPartialUpdate() throws BadEntityException, EntityNotFoundException, BadEntityException {
        //given
        Pet savedBello = petService.save(bello);
        Pet savedKitty = petService.save(kitty);

        Owner savedKahn = getServiceUnderTest().save(kahn);

        Owner addPetsUpdate = new Owner();
        addPetsUpdate.getPets().add(savedKitty);
        addPetsUpdate.getPets().add(savedBello);
        addPetsUpdate.setId(savedKahn.getId());

        //when
        test(partialUpdate(addPetsUpdate))
                .andExpect(() -> propertyAssert(resolve(DB_ENTITY))
                        .assertMatchSize(OwnerType::getPets, 2)
                );

        // verify bidir rel management
        Pet dbBello = petRepository.findByName(BELLO).get();
        Pet dbKitty = petRepository.findByName(KITTY).get();
        Owner dbKahn = ownerRepository.findByLastName(KAHN).get();
        // check if bidir relation ships were managed
        Assertions.assertEquals(dbKahn,dbBello.getOwner());
        Assertions.assertEquals(dbKahn,dbKitty.getOwner());
        Assertions.assertEquals(dbBello,dbKahn.getPets().stream().filter(p -> p.getName().equals(BELLO)).findFirst().get());
        Assertions.assertEquals(dbKitty,dbKahn.getPets().stream().filter(p -> p.getName().equals(KITTY)).findFirst().get());
    }

    @Test
    public void canDeleteOnlyPetFromOwner_viaPartialUpdate() throws BadEntityException, EntityNotFoundException, BadEntityException {
        //given
        Pet savedBello = petService.save(bello);

        Owner savedKahn = getServiceUnderTest().save(kahn);
        kahn.setPets(new HashSet<>(Lists.newArrayList(savedBello)));

        Owner removePetUpdate = new Owner();
        // explicitly set empty list instead of null
        removePetUpdate.setPets(new HashSet<>());
        removePetUpdate.setId(savedKahn.getId());

        //when
        test(partialUpdate(removePetUpdate))
                .andExpect(() -> propertyAssert(resolve(DB_ENTITY))
                        .assertMatchSize(OwnerType::getPets, 0)
                );

        // verify bidir rel management
        Pet dbBello = petRepository.findByName(BELLO).get();
        Owner dbKahn = ownerRepository.findByLastName(KAHN).get();
        // check if bidir relation ships were managed
        Assertions.assertNull(dbBello.getOwner());
//        Assertions.assertTrue(dbKahn.getPets().isEmpty());

    }

    @Test
    public void canDeleteOneOfMultiplePetsFromOwner_viaPartialUpdate() throws BadEntityException, EntityNotFoundException, BadEntityException {
        //given
        Pet savedBello = petService.save(bello);
        Pet savedKitty = petService.save(kitty);

        Owner savedKahn = getServiceUnderTest().save(kahn);
        kahn.setPets(new HashSet<>(Lists.newArrayList(savedBello,savedKitty)));

        Owner removePetUpdate = new Owner();
        // explicitly set empty list instead of null
        removePetUpdate.setPets(new HashSet<>(Lists.newArrayList(savedBello)));
        removePetUpdate.setId(savedKahn.getId());

        //when
        test(partialUpdate(removePetUpdate))
                .andExpect(() -> propertyAssert(resolve(DB_ENTITY))
                        .assertMatchSize(OwnerType::getPets, 1)
                );

        // verify bidir rel management
        Pet dbBello = petRepository.findByName(BELLO).get();
        Pet dbKitty = petRepository.findByName(KITTY).get();
        Owner dbKahn = ownerRepository.findByLastName(KAHN).get();
        // check if bidir relation ships were managed
        Assertions.assertNull(dbKitty.getOwner());
        Assertions.assertEquals(dbKahn,dbBello.getOwner());
        Assertions.assertEquals(dbBello,dbKahn.getPets().stream().filter(p -> p.getName().equals(BELLO)).findFirst().get());

    }

    @Test
    public void canDeleteMultiplePetsFromOwner_viaPartialUpdate() throws BadEntityException, EntityNotFoundException, BadEntityException {
        //given
        Pet savedBello = petService.save(bello);
        Pet savedKitty = petService.save(kitty);
        Pet savedBella = petService.save(bella);

        Owner savedKahn = getServiceUnderTest().save(kahn);
        kahn.setPets(new HashSet<>(Lists.newArrayList(savedBello,savedKitty,savedBella)));

        Owner removePetUpdate = new Owner();
        // explicitly set empty list instead of null
        removePetUpdate.setPets(new HashSet<>(Lists.newArrayList(savedBello)));
        removePetUpdate.setId(savedKahn.getId());

        //when
        test(partialUpdate(removePetUpdate))
                .andExpect(() -> propertyAssert(resolve(DB_ENTITY))
                        .assertMatchSize(OwnerType::getPets, 1)
                );

        // verify bidir rel management
        Pet dbBello = petRepository.findByName(BELLO).get();
        Pet dbKitty = petRepository.findByName(KITTY).get();
        Pet dbBella = petRepository.findByName(BELLA).get();
        Owner dbKahn = ownerRepository.findByLastName(KAHN).get();
        // check if bidir relation ships were managed
        Assertions.assertNull(dbKitty.getOwner());
        Assertions.assertNull(dbBella.getOwner());
        Assertions.assertEquals(dbKahn,dbBello.getOwner());
        Assertions.assertEquals(dbBello,dbKahn.getPets().stream().filter(p -> p.getName().equals(BELLO)).findFirst().get());

    }

    @Test
    public void canDeleteAllPetsFromOwner_viaPartialUpdate() throws BadEntityException, EntityNotFoundException, BadEntityException {
        //given
        Pet savedBello = petService.save(bello);
        Pet savedKitty = petService.save(kitty);

        Owner savedKahn = getServiceUnderTest().save(kahn);
        kahn.setPets(new HashSet<>(Lists.newArrayList(savedBello,savedKitty)));

        Owner removePetUpdate = new Owner();
        // explicitly set empty list instead of null
        removePetUpdate.setPets(new HashSet<>());
        removePetUpdate.setId(savedKahn.getId());

        //when
        test(partialUpdate(removePetUpdate))
                .andExpect(() -> propertyAssert(resolve(DB_ENTITY))
                        .assertMatchSize(OwnerType::getPets, 0)
                );

        // verify bidir rel management
        Pet dbBello = petRepository.findByName(BELLO).get();
        Pet dbKitty = petRepository.findByName(KITTY).get();
        Owner dbKahn = ownerRepository.findByLastName(KAHN).get();
        // check if bidir relation ships were managed
        Assertions.assertNull(dbKitty.getOwner());
        Assertions.assertNull(dbBello.getOwner());
    }



    @Test
    public void canUpdateOwnersTelephoneNumber() throws BadEntityException, EntityNotFoundException, BadEntityException {
        String newNumber = meier.getTelephone() + "123";
        Owner diffTelephoneNumberUpdate = Owner.builder()
                .telephone(newNumber)
                .build();
        Owner toUpdate = getRepository().save(meier);
        diffTelephoneNumberUpdate.setId(toUpdate.getId());

        test(partialUpdate(diffTelephoneNumberUpdate))
                .andExpect(() ->
                        propertyAssert(resolve(DB_ENTITY))
                                .assertMatch(OwnerType::getTelephone, newNumber)
                );
    }


    @Test
    public void canFindOwnerOfTheYear_triggersExtension() throws BadEntityException {
        kahn.setFirstName(OWNER_OF_THE_YEARS_NAME);
        Owner savedOwner = getServiceUnderTest().save(kahn);
        Optional<Owner> ownerOfTheYear = getServiceUnderTest().findOwnerOfTheYear();
        Assertions.assertTrue(ownerOfTheYear.isPresent());
        Mockito.verify(ownerOfTheYearExtension).findOwnerOfTheYear();
    }

    @Test
    public void canFindOwnerByLastName() throws BadEntityException {
        Owner savedOwner = getServiceUnderTest().save(kahn);
        Optional<Owner> byLastName = getServiceUnderTest().findByLastName(kahn.getLastName());
        Assertions.assertTrue(byLastName.isPresent());
        compare(savedOwner)
                .with(byLastName.get())
                .properties()
                .all()
                .assertEqual();
    }

    @Test
    public void canDeleteOwner() throws BadEntityException {
        Owner savedOwner = getServiceUnderTest().save(kahn);
        test(deleteById(savedOwner.getId()))
                .andExpect(noException())
                .andExpect(notPresentInDatabase(savedOwner.getId()));
    }

    @Test
    public void canDeleteOwnerWithPet_thusUnlinkPet() throws BadEntityException {
        kahn.setPets(new HashSet<>(Lists.newArrayList(bello)));
        Owner savedOwner = getServiceUnderTest().save(kahn);

        test(deleteById(savedOwner.getId()))
                .andExpect(noException())
                .andExpect(notPresentInDatabase(savedOwner.getId()));

        // verify bidir rel management
        Assertions.assertTrue(petRepository.findByName(BELLO).isPresent());
        Pet dbBello = petRepository.findByName(BELLO).get();
        Assertions.assertNull(dbBello.getOwner());
    }




}
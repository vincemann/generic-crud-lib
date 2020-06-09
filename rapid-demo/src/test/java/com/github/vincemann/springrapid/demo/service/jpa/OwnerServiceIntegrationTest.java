package com.github.vincemann.springrapid.demo.service.jpa;

import com.github.vincemann.springrapid.commons.Lists;
import com.github.vincemann.springrapid.core.service.exception.BadEntityException;
import com.github.vincemann.springrapid.core.service.exception.EntityNotFoundException;
import com.github.vincemann.springrapid.coretest.service.CrudServiceIntegrationTest;
import com.github.vincemann.springrapid.coretest.service.result.ServiceResult;
import com.github.vincemann.springrapid.demo.EnableProjectComponentScan;
import com.github.vincemann.springrapid.demo.model.Owner;
import com.github.vincemann.springrapid.demo.model.Pet;
import com.github.vincemann.springrapid.demo.model.PetType;
import com.github.vincemann.springrapid.demo.service.OwnerService;
import com.github.vincemann.springrapid.demo.service.PetService;
import com.github.vincemann.springrapid.demo.service.PetTypeService;
import com.github.vincemann.springrapid.demo.service.plugin.OwnerOfTheYearPlugin;
import com.github.vincemann.springrapid.entityrelationship.slicing.test.ImportRapidEntityRelServiceConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;

import static com.github.vincemann.ezcompare.Comparison.compare;
import static com.github.vincemann.springrapid.coretest.config.GlobalEntityPlaceholderResolver.resolve;
import static com.github.vincemann.springrapid.coretest.service.request.CrudServiceRequestBuilders.*;
import static com.github.vincemann.springrapid.coretest.service.resolve.EntityPlaceholder.*;
import static com.github.vincemann.springrapid.coretest.service.result.matcher.ExceptionMatchers.noException;
import static com.github.vincemann.springrapid.coretest.service.result.matcher.ExistenceMatchers.notPresentInDatabase;
import static com.github.vincemann.springrapid.coretest.service.result.matcher.PropertyMatchers.propertyAssert;


@EnableProjectComponentScan
@ImportRapidEntityRelServiceConfig
class OwnerServiceIntegrationTest
        extends CrudServiceIntegrationTest<OwnerService, Owner, Long> {

    Owner ownerWithoutPets;
    Owner ownerWithOnePet;
    Pet testPet;
    PetType savedDogPetType;

    @SpyBean
    OwnerOfTheYearPlugin ownerOfTheYearPlugin;

    @Autowired
    PetService petService;

    @Autowired
    PetTypeService petTypeService;

    @BeforeEach
    public void setUp() throws Exception {
        super.setup();
        //proxyfy service
        savedDogPetType = petTypeService.save(new PetType("Dog"));

        testPet = Pet.builder()
                .petType(savedDogPetType)
                .name("Bello")
                .birthDate(LocalDate.now())
                .build();

        ownerWithoutPets = Owner.builder()
                .firstName("owner without pets")
                .lastName("owner without pets lastName")
                .address("asljnflksamfslkmf")
                .city("n1 city")
                .telephone("12843723847324")
                .build();

        ownerWithOnePet = Owner.builder()
                .firstName("owner with one pet")
                .lastName("owner with one pet lastName")
                .address("asljnflksamfslkmf")
                .city("n1 city")
                .telephone("12843723847324")
                .pets(new HashSet<>(Lists.newArrayList(testPet)))
                .build();
    }

    @Test
    public void saveOwnerWithoutPets_ShouldSucceed() {
        ServiceResult serviceResult = getTestTemplate()
                .perform(save(ownerWithoutPets))
                .andExpect(() -> compare(ownerWithoutPets)
                        .with(resolve(DB_ENTITY))
                        .properties()
                        .include(ownerWithoutPets::getTelephone)
                        .include(ownerWithoutPets::getAddress)
                        .assertEqual())
                .andReturn();
        Assertions.assertEquals(0, ((Owner) serviceResult.getResult()).getPets().size());
    }


    @Test
    public void saveOwnerWithPet_ShouldSucceed() throws BadEntityException {
        getTestTemplate()
                .perform(save(ownerWithOnePet))
                .andDo(() -> compare(resolve(SERVICE_INPUT_ENTITY))
                        .with(resolve(DB_ENTITY))
                        .properties()
                        .all()
                        .ignore("id")
                        .assertEqual());
        Assertions.assertTrue(getRepository().existsById(ownerWithOnePet.getId()));
    }

    @Test
    public void saveOwnerWithPersistedPet_shouldSucceed() throws BadEntityException {
        Pet savedPet = petService.save(testPet);

        Owner owner = Owner.builder()
                .firstName("owner with one already persisted pet")
                .lastName("owner with one already persisted pet lastName")
                .address("asljnflksamfslkmf")
                .city("n1 city")
                .telephone("12843723847324")
                .pets(new HashSet<>(Lists.newArrayList(savedPet)))
                .build();

        getTestTemplate()
                .perform(save(owner))
                .andExpect(() -> compare(owner)
                        .with(resolve(SERVICE_RETURNED_ENTITY))
                        .properties()
                        .all()
                        .ignore(owner::getId)
                        .assertEqual()
                );
    }


    @Test
    public void updateOwner_changeTelephoneNumber_shouldSucceed() throws BadEntityException, EntityNotFoundException, BadEntityException {
        String newNumber = ownerWithoutPets.getTelephone() + "123";
        Owner diffTelephoneNumberUpdate = Owner.builder()
                .telephone(newNumber)
                .build();
        Owner toUpdate = getRepository().save(ownerWithoutPets);
        diffTelephoneNumberUpdate.setId(toUpdate.getId());

        getTestTemplate()
                .perform(partialUpdate(diffTelephoneNumberUpdate))
                .andExpect(() ->
                        propertyAssert(resolve(DB_ENTITY))
                                .shouldMatch(toUpdate::getTelephone, newNumber)
                );
    }

    @Test
    public void updateOwner_addAnotherPet_shouldSucceed() throws BadEntityException, EntityNotFoundException, BadEntityException {
        //given
        Pet savedPet = petService.save(testPet);
        String newPetName = "petToAdd";
        Pet newPet = Pet.builder()
                .name(newPetName)
                .petType(savedDogPetType)
                .birthDate(LocalDate.now())
                .build();
        Pet savedPetToAdd = petService.save(newPet);

        Owner owner = Owner.builder()
                .firstName("owner with one already persisted pet")
                .lastName("owner with one already persisted pet lastName")
                .address("asljnflksamfslkmf")
                .city("n1 city")
                .telephone("12843723847324")
                .pets(new HashSet<>(Lists.newArrayList(savedPet)))
                .build();

        Owner ownerUpdateRequest = new Owner();
        ownerUpdateRequest.getPets().addAll(owner.getPets());
        //here comes the new pet
        ownerUpdateRequest.getPets().add(savedPetToAdd);

        //when
        Owner saved = getRepository().save(owner);
        ownerUpdateRequest.setId(saved.getId());

        getTestTemplate()
                .perform(partialUpdate(ownerUpdateRequest))
                .andExpect(() -> propertyAssert(resolve(DB_ENTITY))
                        .shouldMatchSize(ownerUpdateRequest::getPets, 2)
                );
    }

    @Test
    public void findOwnerOfTheYear_shouldSucceed_andTriggerPluginCallback() {
        //owner of the years name is 42
        ownerWithOnePet.setFirstName("42");
        Owner savedOwner = getRepository().save(ownerWithOnePet);
        Optional<Owner> ownerOfTheYear = getServiceUnderTest().findOwnerOfTheYear();
        Assertions.assertTrue(ownerOfTheYear.isPresent());
        Mockito.verify(ownerOfTheYearPlugin).onAfterFindOwnerOfTheYear(ownerOfTheYear);
    }

    @Test
    public void findByLastName_shouldSucceed() {
        Owner savedOwner = getRepository().save(ownerWithOnePet);
        Optional<Owner> byLastName = getServiceUnderTest().findByLastName(ownerWithOnePet.getLastName());
        Assertions.assertTrue(byLastName.isPresent());
        compare(savedOwner)
                .with(byLastName.get())
                .properties()
                .all()
                .assertEqual();
    }

    @Test
    public void deleteOwner_shouldSucceed() {
        Owner savedOwner = getRepository().save(ownerWithOnePet);
        getTestTemplate()
                .perform(deleteById(savedOwner.getId()))
                .andExpect(noException())
                .andExpect(notPresentInDatabase(savedOwner.getId()));
    }


}
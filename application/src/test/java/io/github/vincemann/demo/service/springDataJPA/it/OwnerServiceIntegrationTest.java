package io.github.vincemann.demo.service.springDataJPA.it;

import io.github.vincemann.demo.model.Owner;
import io.github.vincemann.demo.model.Pet;
import io.github.vincemann.demo.model.PetType;
import io.github.vincemann.demo.service.OwnerService;
import io.github.vincemann.demo.service.PetService;
import io.github.vincemann.demo.service.PetTypeService;
import io.github.vincemann.demo.service.plugin.OwnerOfTheYearPlugin;
import io.github.vincemann.generic.crud.lib.service.exception.BadEntityException;
import io.github.vincemann.generic.crud.lib.service.exception.EntityNotFoundException;
import io.github.vincemann.generic.crud.lib.service.exception.NoIdException;
import io.github.vincemann.generic.crud.lib.test.compare.ReflectionComparator;
import io.github.vincemann.generic.crud.lib.test.service.CrudServiceIntegrationTest;
import io.github.vincemann.generic.crud.lib.test.service.result.ServiceResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

import static io.github.vincemann.generic.crud.lib.test.util.CopyNonNullValuesEntityMerger.merge;
import static io.github.vincemann.generic.crud.lib.test.service.request.CrudServiceRequestBuilders.partialUpdate;
import static io.github.vincemann.generic.crud.lib.test.service.request.CrudServiceRequestBuilders.save;
import static io.github.vincemann.generic.crud.lib.test.service.result.matcher.compare.PropertyCompareResultMatchers.compare;
import static io.github.vincemann.generic.crud.lib.test.service.result.matcher.compare.ReflectionCompareResultMatchers.fullCompare;

//only load service "slice" of application context via service profile and Springs DataJpa Slice Test

class OwnerServiceIntegrationTest
        extends CrudServiceIntegrationTest<OwnerService,Owner, Long> {

    @Autowired
    ReflectionComparator<Owner> reflectionComparator;

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
                .pets(new HashSet<>(Arrays.asList(testPet)))
                .build();
    }

    @Test
    public void saveOwnerWithoutPets_ShouldSucceed() {
        ServiceResult entityServiceResult = getTestTemplate()
                .perform(save(ownerWithoutPets))
                .andExpect(compare(ownerWithoutPets)
                        .withDbEntity()
                        .property(ownerWithoutPets::getTelephone)
                        .property(ownerWithoutPets::getAddress)
                        .isEqual())
                .andReturn();
        Assertions.assertEquals(0,((Owner) entityServiceResult.getResult()).getPets().size());
    }


    @Test
    public void saveOwnerWithPet_ShouldSucceed() throws BadEntityException {
        getTestTemplate()
                .perform(save(ownerWithOnePet))
                .andExpect(fullCompare(ownerWithOnePet)
                        .withDbEntity()
                        .isEqual());
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
                .pets(new HashSet<>(Arrays.asList(savedPet)))
                .build();

        getTestTemplate()
                .perform(save(owner))
                .andExpect(fullCompare(owner)
                        .withDbEntity()
                        .isEqual()
                );
    }


    @Test
    public void updateOwner_changeTelephoneNumber_shouldSucceed() throws BadEntityException, EntityNotFoundException, NoIdException {
        Owner diffTelephoneNumberUpdate = Owner.builder()
                .telephone(ownerWithoutPets.getTelephone() + "123")
                .build();
        Owner toUpdate = getRepository().save(ownerWithoutPets);
        diffTelephoneNumberUpdate.setId(toUpdate.getId());

        getTestTemplate().perform(partialUpdate(diffTelephoneNumberUpdate))
                .andExpect(
                        fullCompare(merge(diffTelephoneNumberUpdate,toUpdate))
                        .withDbEntity()
                                .isEqual()
                );
    }

    @Test
    public void updateOwner_addAnotherPet_shouldSucceed() throws BadEntityException, EntityNotFoundException,  NoIdException {
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
                .pets(new HashSet<>(Arrays.asList(savedPet)))
                .build();

        Owner ownerUpdateRequest = new Owner();
        ownerUpdateRequest.getPets().addAll(owner.getPets());
        //here comes the new pet
        ownerUpdateRequest.getPets().add(savedPetToAdd);

        //when
        Owner saved = getRepository().save(owner);
        ownerUpdateRequest.setId(saved.getId());

        getTestTemplate().perform(partialUpdate(ownerUpdateRequest))
                .andExpect(
                        compare(merge(ownerUpdateRequest,owner))
                        .property(ownerUpdateRequest::getPets)
                        .sizeIs(2)
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
        Assertions.assertTrue(reflectionComparator.isEqual(savedOwner, byLastName.get()));
    }


}
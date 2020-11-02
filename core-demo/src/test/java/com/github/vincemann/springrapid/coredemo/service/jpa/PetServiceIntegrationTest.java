package com.github.vincemann.springrapid.coredemo.service.jpa;

import com.github.vincemann.springrapid.core.service.exception.BadEntityException;
import com.github.vincemann.springrapid.coretest.service.AbstractCrudServiceIntegrationTest;
import com.github.vincemann.springrapid.coretest.service.resolve.EntityPlaceholder;
import com.github.vincemann.springrapid.coredemo.EnableProjectComponentScan;
import com.github.vincemann.springrapid.coredemo.model.Pet;
import com.github.vincemann.springrapid.coredemo.model.PetType;
import com.github.vincemann.springrapid.coredemo.service.PetService;
import com.github.vincemann.springrapid.coredemo.service.PetTypeService;
import com.github.vincemann.springrapid.entityrelationship.slicing.test.ImportRapidEntityRelServiceConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static com.github.vincemann.ezcompare.Comparator.compare;
import static com.github.vincemann.springrapid.coretest.service.request.CrudServiceRequestBuilders.save;

@EnableProjectComponentScan
@ImportRapidEntityRelServiceConfig
class PetServiceIntegrationTest
        extends AbstractCrudServiceIntegrationTest<PetService, Pet, Long> {

    @Autowired
    private PetTypeService petTypeService;
    private PetType dogPetType;


    @BeforeEach
    protected void setupTestData() throws Exception {
//        super.setup();
        this.dogPetType = petTypeService.save(new PetType("Dog"));
    }

    @Test
    public void save_withPersistedPetType_shouldSucceed() throws BadEntityException {
        Pet dogWithDogType = Pet.builder()
                .petType(dogPetType)
                .birthDate(LocalDate.now())
                .name("bello")
                .build();

        test(save(dogWithDogType))
                .andExpect(() -> compare(dogWithDogType)
                        .with(resolve(EntityPlaceholder.DB_ENTITY))
                        .properties()
                        .all()
                        .ignore("id")
                        .assertEqual());
    }
}
package io.github.vincemann.demo.dtoCrudControllers.springAdapter;


import io.github.vincemann.demo.dtoCrudControllers.EntityInitializerControllerIT;
import io.github.vincemann.demo.dtoCrudControllers.PetController;
import io.github.vincemann.demo.dtos.PetDto;
import io.github.vincemann.demo.model.Pet;
import io.github.vincemann.demo.service.PetService;
import io.github.vincemann.generic.crud.lib.test.controller.springAdapter.plugins.CheckIfDbDeletedPlugin;
import io.github.vincemann.generic.crud.lib.test.controller.springAdapter.plugins.ServiceDeepEqualPlugin;
import io.github.vincemann.generic.crud.lib.test.controller.springAdapter.testBundles.successfulTestBundles.UpdatableSucceedingTestEntityBundle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment =
        SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(value = {"test", "springdatajpa"})
public class PetControllerIT extends EntityInitializerControllerIT<Pet, PetDto, PetService, PetController> {


    public PetControllerIT(@Autowired PetController crudController,
                           @Autowired CheckIfDbDeletedPlugin checkIfDbDeletedPlugin,
                           @Autowired ServiceDeepEqualPlugin serviceDeepEqualPlugin)
    {
        super(crudController,checkIfDbDeletedPlugin,serviceDeepEqualPlugin);
    }


    @Override
    protected List<UpdatableSucceedingTestEntityBundle<PetDto>> provideSucceedingTestBundles() {
        //Pet with persisted PetType
        PetDto petWithPersistedPetType = PetDto.builder()
                .name("esta")
                .petTypeId(getTestPetType().getId())
                .build();
        //update pets name
        PetDto diffPetsNameUpdate = PetDto.builder()
                .name("MODIFIED NAME")
                .petTypeId(getTestPetType().getId())
                .build();

        //Pet with persisted PetType and persisted Owner
        PetDto petWithOwner = PetDto.builder()
                .ownerId(getTestOwner().getId())
                .petTypeId(getTestPetType().getId())
                .name("esta")
                .build();
        //remove pets owner in update
        PetDto removePetsOwnerUpdate = PetDto.builder()
                .ownerId(null)
                .petTypeId(getTestPetType().getId())
                .name("esta")
                .build();

        return Arrays.asList(
                new UpdatableSucceedingTestEntityBundle<>(petWithPersistedPetType,diffPetsNameUpdate),
                new UpdatableSucceedingTestEntityBundle<>(petWithOwner, removePetsOwnerUpdate)
        );
    }

    @Override
    protected List<PetDto> provideInvalidTestDtos() {
        PetDto petWithAlreadySetId = PetDto.builder()
                .name("bello")
                .petTypeId(getTestPetType().getId())
                .ownerId(-1L)
                .build();
        petWithAlreadySetId.setId(42L);

        return Arrays.asList(
                //no name
                PetDto.builder()
                        .name(null)
                        .petTypeId(getTestPetType().getId())
                        .build(),
                //no pettype
                PetDto.builder()
                        .name("bello")
                        .petTypeId(null)
                        .build(),
                //invalid OwnerId
                PetDto.builder()
                        .name("bello")
                        .petTypeId(getTestPetType().getId())
                        .ownerId(-1L)
                        .build(),
                //already set id
                petWithAlreadySetId
        );
    }

    @Override
    protected List<UpdatableSucceedingTestEntityBundle<PetDto>> provideInvalidEntityTestBundles() {
        PetDto validPet = PetDto.builder()
                .name("esta")
                .petTypeId(getTestPetType().getId())
                .build();
        //remove name =  invalid Update
        PetDto removePetsNameUpdate = PetDto.builder()
                .name(null)
                .petTypeId(getTestPetType().getId())
                .build();

        return Arrays.asList(
                new UpdatableSucceedingTestEntityBundle<>(validPet,removePetsNameUpdate));
    }
}

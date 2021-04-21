package com.github.vincemann.springrapid.coredemo.controller;

import com.github.vincemann.springrapid.core.controller.GenericCrudController;
import com.github.vincemann.springrapid.core.security.RapidAuthenticatedPrincipal;
import com.github.vincemann.springrapid.core.security.RapidSecurityContext;
import com.github.vincemann.springrapid.coredemo.model.*;
import com.github.vincemann.springrapid.core.service.CrudService;
import com.github.vincemann.springrapid.coredemo.repo.OwnerRepository;
import com.github.vincemann.springrapid.coredemo.repo.PetRepository;
import com.github.vincemann.springrapid.coredemo.repo.PetTypeRepository;
import com.github.vincemann.springrapid.coredemo.repo.ToyRepository;
import com.github.vincemann.springrapid.coredemo.service.OwnerService;
import com.github.vincemann.springrapid.coredemo.service.PetService;
import com.github.vincemann.springrapid.coredemo.service.PetTypeService;
import com.github.vincemann.springrapid.coredemo.service.ToyService;
import com.github.vincemann.springrapid.coredemo.service.plugin.OwnerOfTheYearExtension;
import com.github.vincemann.springrapid.coretest.controller.urlparamid.AutoMockUrlParamIdControllerTest;
import com.github.vincemann.springrapid.coretest.controller.urlparamid.IntegrationUrlParamIdControllerTest;
import com.github.vincemann.springrapid.coretest.util.RapidTestUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.time.LocalDate;

@Getter
@Setter
@Slf4j
// currently unused, keep for future integration controller demo tests
public abstract class OneToManyControllerIntegrationTest<C extends GenericCrudController<?,Long,S,?,?>,S extends CrudService<?,Long>>
        extends IntegrationUrlParamIdControllerTest<C,Long,S>

{

    //Types
    final Owner OwnerType = new Owner();
    final Pet PetType = new Pet();
    final Toy ToyType = new Toy();

    protected static final String MEIER = "Meier";
    protected static final String KAHN = "Kahn";

    protected static final String BELLO = "Bello";
    protected static final String BELLA = "Bella";
    protected static final String KITTY = "Kitty";

    Owner meier;
    Owner kahn;

    Pet bello;
    Pet kitty;
    Pet bella;

    PetType savedDogPetType;
    PetType savedCatPetType;

    Toy rubberDuck;
    Toy bone;
    Toy ball;


    @Autowired
    ToyService toyService;
    @Autowired
    ToyRepository toyRepository;

    @Autowired
    PetService petService;
    @Autowired
    PetRepository petRepository;



    @Autowired
    PetTypeService petTypeService;
    @Autowired
    PetTypeRepository petTypeRepository;



    @SpyBean
    OwnerOfTheYearExtension ownerOfTheYearExtension;

    @Autowired
    OwnerRepository ownerRepository;
    @Autowired
    OwnerService ownerService;

    @Autowired
    RapidSecurityContext<RapidAuthenticatedPrincipal> securityContext;

    @BeforeEach
    public void setupTestData() throws Exception {

        savedDogPetType = petTypeService.save(new PetType("Dog"));
        savedCatPetType = petTypeService.save(new PetType("Cat"));

        bello = Pet.builder()
                .petType(savedDogPetType)
                .name(BELLO)
                .birthDate(LocalDate.now())
                .build();

        bella = Pet.builder()
                .petType(savedDogPetType)
                .name(BELLA)
                .birthDate(LocalDate.now())
                .build();

        kitty = Pet.builder()
                .petType(savedCatPetType)
                .name(KITTY)
                .birthDate(LocalDate.now())
                .build();

        meier = Owner.builder()
                .firstName("Max")
                .lastName(MEIER)
                .address("asljnflksamfslkmf")
                .city("n1 city")
                .telephone("0123456789")
                .build();

        kahn = Owner.builder()
                .firstName("Olli")
                .lastName(KAHN)
                .address("asljnflksamfslkmf")
                .city("n1 city")
                .telephone("1234567890")
                .build();
    }

    @AfterEach
    void tearDown() {
        RapidTestUtil.clear(petService);
        RapidTestUtil.clear(toyService);
        RapidTestUtil.clear(ownerService);
        RapidTestUtil.clear(petTypeService);
    }


}

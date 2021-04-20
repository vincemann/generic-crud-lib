package com.github.vincemann.springrapid.coredemo.controller;

import com.github.vincemann.springrapid.core.model.IdentifiableEntityImpl;
import com.github.vincemann.springrapid.core.util.Lists;
import com.github.vincemann.springrapid.coredemo.dtos.VetDto;
import com.github.vincemann.springrapid.coredemo.model.Specialty;
import com.github.vincemann.springrapid.coredemo.model.Vet;
import com.github.vincemann.springrapid.coredemo.service.VetService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

import static com.github.vincemann.ezcompare.Comparator.compare;
import static com.github.vincemann.springrapid.coretest.util.RapidTestUtil.createUpdateJsonLine;
import static com.github.vincemann.springrapid.coretest.util.RapidTestUtil.createUpdateJsonRequest;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class VetControllerIntegrationTest extends ManyToManyControllerIntegrationTest<VetController,VetService> {


    @Test
    public void canSaveVet_getLinkedToSpecialties() throws Exception {
        Specialty savedDentism = specialtyService.save(dentism);
        Specialty savedGastro = specialtyService.save(gastro);

        VetDto createKahnDto = new VetDto(kahn);
        createKahnDto.setSpecialtyIds(new HashSet<>(Lists.newArrayList(savedDentism.getId(),savedGastro.getId())));



        MvcResult result = getMockMvc().perform(create(createKahnDto))
                .andExpect(status().isOk())
                .andReturn();
        String json = result.getResponse().getContentAsString();
        System.err.println(json);


        VetDto responseDto = deserialize(json, VetDto.class);
        compare(createKahnDto).with(responseDto)
                .properties()
                .all()
                .ignore(VetType::getId)
                .assertEqual();
        Assertions.assertTrue(vetRepository.findByLastName(KAHN).isPresent());
        Vet dbKahn = vetRepository.findByLastName(KAHN).get();

        compare(createKahnDto).with(dbKahn)
                .properties()
                .all()
                .ignore(VetType::getId)
                .ignore(createKahnDto::getSpecialtyIds)
                .assertEqual();

        Assertions.assertEquals(responseDto.getId(),dbKahn.getId());

        assertVetHasSpecialties(KAHN,DENTISM,GASTRO);
        assertSpecialtyHasVets(DENTISM,KAHN);
        assertSpecialtyHasVets(GASTRO,KAHN);
    }

    @Test
    public void canRemoveMultipleSpecialtiesFromVet_viaUpdate() throws Exception {
        // kahn -> dentism, gastro, heart
        // meier -> dentism, heart
        Specialty savedDentism = specialtyService.save(dentism);
        Specialty savedGastro = specialtyService.save(gastro);
        Specialty savedHeart = specialtyService.save(heart);
        VetDto createdKahnDto = createVetLinkedToSpecialties(kahn,savedDentism,savedGastro,savedHeart);
        VetDto createdMeierDto = createVetLinkedToSpecialties(meier,savedDentism,savedHeart);

        // remove kahns dentism and gastro
        String removeDentismJson = createUpdateJsonLine("remove", "/specialtyIds",savedDentism.getId().toString());
        String removeGastroJson = createUpdateJsonLine("remove", "/specialtyIds",savedGastro.getId().toString());
        String updateJson = createUpdateJsonRequest(removeDentismJson, removeGastroJson);
        VetDto responseDto = deserialize(getMockMvc().perform(update(updateJson, createdKahnDto.getId())).andReturn().getResponse().getContentAsString(),VetDto.class);
        Assertions.assertTrue(responseDto.getSpecialtyIds().contains(savedHeart.getId()));
        Assertions.assertEquals(1,responseDto.getSpecialtyIds().size());

        assertVetHasSpecialties(KAHN,HEART);
        assertVetHasSpecialties(MEIER,DENTISM, HEART);

        assertSpecialtyHasVets(DENTISM,MEIER);
        assertSpecialtyHasVets(GASTRO);
        assertSpecialtyHasVets(HEART,KAHN,MEIER);
    }

    @Test
    public void canAddSingleSpecialtyToVet_viaUpdate() throws Exception {
        // kahn -> dentism
        // meier -> dentism, heart
        Specialty savedDentism = specialtyService.save(dentism);
        Specialty savedGastro = specialtyService.save(gastro);
        Specialty savedHeart = specialtyService.save(heart);
        VetDto createdKahnDto = createVetLinkedToSpecialties(kahn,savedDentism);
        VetDto createdMeierDto = createVetLinkedToSpecialties(meier,savedDentism,savedHeart);

        // add gastro to kahn
        String addDentismJson = createUpdateJsonLine("add", "/specialtyIds/-",savedGastro.getId().toString());
        String updateJson = createUpdateJsonRequest(addDentismJson);
        VetDto responseDto = deserialize(getMockMvc().perform(update(updateJson, createdKahnDto.getId())).andReturn().getResponse().getContentAsString(),VetDto.class);
        Assertions.assertTrue(responseDto.getSpecialtyIds().contains(savedGastro.getId()));
        Assertions.assertTrue(responseDto.getSpecialtyIds().contains(savedDentism.getId()));
        Assertions.assertEquals(2,responseDto.getSpecialtyIds().size());

        assertVetHasSpecialties(KAHN,DENTISM,GASTRO);
        assertVetHasSpecialties(MEIER,DENTISM, HEART);

        assertSpecialtyHasVets(DENTISM,MEIER,KAHN);
        assertSpecialtyHasVets(GASTRO,KAHN);
        assertSpecialtyHasVets(HEART,MEIER);
    }

    @Test
    public void canAddMultipleSpecialtiesToVet_viaUpdate() throws Exception {
        // kahn -> dentism
        // meier -> dentism, heart
        Specialty savedDentism = specialtyService.save(dentism);
        Specialty savedGastro = specialtyService.save(gastro);
        Specialty savedHeart = specialtyService.save(heart);
        VetDto createdKahnDto = createVetLinkedToSpecialties(kahn,savedDentism);
        VetDto createdMeierDto = createVetLinkedToSpecialties(meier,savedDentism,savedHeart);

        // add gastro and heart to kahn
        String addDentismJson = createUpdateJsonLine("add", "/specialtyIds/-",savedGastro.getId().toString());
        String addGastroJson = createUpdateJsonLine("add", "/specialtyIds/-",savedHeart.getId().toString());
        String updateJson = createUpdateJsonRequest(addDentismJson, addGastroJson);
        VetDto responseDto = deserialize(getMockMvc().perform(update(updateJson, createdKahnDto.getId())).andReturn().getResponse().getContentAsString(),VetDto.class);
        Assertions.assertTrue(responseDto.getSpecialtyIds().contains(savedHeart.getId()));
        Assertions.assertTrue(responseDto.getSpecialtyIds().contains(savedGastro.getId()));
        Assertions.assertTrue(responseDto.getSpecialtyIds().contains(savedDentism.getId()));
        Assertions.assertEquals(3,responseDto.getSpecialtyIds().size());

        assertVetHasSpecialties(KAHN,HEART,DENTISM,GASTRO);
        assertVetHasSpecialties(MEIER,DENTISM, HEART);

        assertSpecialtyHasVets(DENTISM,MEIER,KAHN);
        assertSpecialtyHasVets(GASTRO,KAHN);
        assertSpecialtyHasVets(HEART,KAHN,MEIER);
    }


    @Test
    public void canRemoveAllSpecialtiesFromVet_viaUpdate() throws Exception {
        // kahn -> dentism, heart
        // meier -> dentism, heart
        Specialty savedDentism = specialtyService.save(dentism);
        Specialty savedGastro = specialtyService.save(gastro);
        Specialty savedHeart = specialtyService.save(heart);
        VetDto createdKahnDto = createVetLinkedToSpecialties(kahn,savedDentism,savedHeart);
        VetDto createdMeierDto = createVetLinkedToSpecialties(meier,savedDentism,savedHeart);

        // remove all from kahn
        String removeAllJson = createUpdateJsonLine("remove", "/specialtyIds");
        String updateJson = createUpdateJsonRequest(removeAllJson);
        VetDto responseDto = deserialize(getMockMvc().perform(update(updateJson, createdKahnDto.getId())).andReturn().getResponse().getContentAsString(),VetDto.class);
        Assertions.assertEquals(0,responseDto.getSpecialtyIds().size());

        assertVetHasSpecialties(KAHN);
        assertVetHasSpecialties(MEIER,DENTISM, HEART);

        assertSpecialtyHasVets(DENTISM,MEIER);
        assertSpecialtyHasVets(GASTRO);
        assertSpecialtyHasVets(HEART,MEIER);
    }

    @Test
    public void canRemoveAllAndAddOneSpecialtyToVet_viaUpdate() throws Exception {
        // kahn -> dentism, heart
        // meier -> dentism, heart
        Specialty savedDentism = specialtyService.save(dentism);
        Specialty savedGastro = specialtyService.save(gastro);
        Specialty savedHeart = specialtyService.save(heart);
        VetDto createdKahnDto = createVetLinkedToSpecialties(kahn,savedDentism,savedHeart);
        VetDto createdMeierDto = createVetLinkedToSpecialties(meier,savedDentism,savedHeart);

        // remove all from kahn then add gastro
        String removeAllJson = createUpdateJsonLine("remove", "/specialtyIds");
        String addGastroJson = createUpdateJsonLine("add", "/specialtyIds",savedGastro.getId().toString());
        String updateJson = createUpdateJsonRequest(removeAllJson,addGastroJson);
        VetDto responseDto = deserialize(getMockMvc().perform(update(updateJson, createdKahnDto.getId())).andReturn().getResponse().getContentAsString(),VetDto.class);
        Assertions.assertTrue(responseDto.getSpecialtyIds().contains(savedGastro.getId()));
        Assertions.assertEquals(1,responseDto.getSpecialtyIds().size());

        assertVetHasSpecialties(KAHN,GASTRO);
        assertVetHasSpecialties(MEIER,DENTISM, HEART);

        assertSpecialtyHasVets(DENTISM,MEIER);
        assertSpecialtyHasVets(GASTRO,KAHN);
        assertSpecialtyHasVets(HEART,MEIER);
    }

    @Test
    public void canRemoveSpecificSpeciality_andAddDiffSpecialtyToVet_viaUpdate() throws Exception {
        // kahn -> dentism, heart
        // meier -> dentism, heart
        Specialty savedDentism = specialtyService.save(dentism);
        Specialty savedGastro = specialtyService.save(gastro);
        Specialty savedHeart = specialtyService.save(heart);
        VetDto createdKahnDto = createVetLinkedToSpecialties(kahn,savedDentism,savedHeart);
        VetDto createdMeierDto = createVetLinkedToSpecialties(meier,savedDentism,savedHeart);

        // remove dentism from kahn then add gastro
        String removeDentismJson = createUpdateJsonLine("remove", "/specialtyIds",savedDentism.getId().toString());
        String addGastroJson = createUpdateJsonLine("add", "/specialtyIds/-",savedGastro.getId().toString());
        String updateJson = createUpdateJsonRequest(removeDentismJson,addGastroJson);
        VetDto responseDto = deserialize(getMockMvc().perform(update(updateJson, createdKahnDto.getId())).andReturn().getResponse().getContentAsString(),VetDto.class);
        Assertions.assertTrue(responseDto.getSpecialtyIds().contains(savedGastro.getId()));
        Assertions.assertTrue(responseDto.getSpecialtyIds().contains(savedHeart.getId()));
        Assertions.assertEquals(2,responseDto.getSpecialtyIds().size());

        assertVetHasSpecialties(KAHN,GASTRO,HEART);
        assertVetHasSpecialties(MEIER,DENTISM, HEART);

        assertSpecialtyHasVets(DENTISM,MEIER);
        assertSpecialtyHasVets(GASTRO,KAHN);
        assertSpecialtyHasVets(HEART,MEIER,KAHN);
    }



    @Test
    public void canRemoveAllAndAddMultipleSpecialtiesInNextRequestToVet_viaUpdate() throws Exception {
        // kahn -> dentism, heart
        // meier -> dentism, heart
        Specialty savedDentism = specialtyService.save(dentism);
        Specialty savedGastro = specialtyService.save(gastro);
        Specialty savedHeart = specialtyService.save(heart);
        Specialty savedMuscle = specialtyService.save(muscle);
        VetDto createdKahnDto = createVetLinkedToSpecialties(kahn,savedDentism,savedHeart);
        VetDto createdMeierDto = createVetLinkedToSpecialties(meier,savedDentism,savedHeart);

        // remove all from kahn then add gastro and muscle
        String removeAllJson = createUpdateJsonLine("remove", "/specialtyIds");
        String updateJson = createUpdateJsonRequest(removeAllJson);
        VetDto responseDto = deserialize(getMockMvc().perform(update(updateJson, createdKahnDto.getId())).andReturn().getResponse().getContentAsString(),VetDto.class);
        Assertions.assertEquals(0,responseDto.getSpecialtyIds().size());

        String addGastroJson = createUpdateJsonLine("add", "/specialtyIds/-",savedGastro.getId().toString());
        String addMuscleJson = createUpdateJsonLine("add", "/specialtyIds/-",savedMuscle.getId().toString());
        updateJson = createUpdateJsonRequest(addGastroJson,addMuscleJson);
        responseDto = deserialize(getMockMvc().perform(update(updateJson, createdKahnDto.getId())).andReturn().getResponse().getContentAsString(),VetDto.class);
        Assertions.assertTrue(responseDto.getSpecialtyIds().contains(savedGastro.getId()));
        Assertions.assertTrue(responseDto.getSpecialtyIds().contains(savedMuscle.getId()));
        Assertions.assertEquals(2,responseDto.getSpecialtyIds().size());

        assertVetHasSpecialties(KAHN,GASTRO, MUSCLE);
        assertVetHasSpecialties(MEIER,DENTISM, HEART);

        assertSpecialtyHasVets(DENTISM,MEIER);
        assertSpecialtyHasVets(GASTRO,KAHN);
        assertSpecialtyHasVets(MUSCLE,KAHN);
        assertSpecialtyHasVets(HEART,MEIER);
    }

    private VetDto createVetLinkedToSpecialties(Vet vet, Specialty... specialties) throws Exception {
        VetDto createVetDto = new VetDto(vet);
        createVetDto.setSpecialtyIds(new HashSet<>(
                Arrays.stream(specialties)
                        .map(IdentifiableEntityImpl::getId)
                        .collect(Collectors.toList())));
        String json = getMockMvc().perform(create(createVetDto))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return deserialize(json,VetDto.class);
    }

}
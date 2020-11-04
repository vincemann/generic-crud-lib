package com.github.vincemann.springrapid.core.controller;

import com.github.vincemann.springrapid.core.controller.dto.mapper.context.DtoMappingContext;
import com.github.vincemann.springrapid.core.controller.dto.mapper.context.DtoRequestInfo;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class RoleFallbackDtoClassLocator implements DtoClassLocator {

    /**
     * Ignores role if no role was configured for this MappingContext.
     * Otherwise role and all other properties of {@link DtoRequestInfo} must match for a match.
     * If no match was found with a role, it falls back on searching for dtoClass without Role Information.
     *
     * @param info
     * @return
     */
    @Override
    //@LogInteraction
    public Class<?> find(DtoRequestInfo info, DtoMappingContext context) {
        Map<DtoRequestInfo, Class<?>> mappingEntries = context.getMappingEntries();
        Set<DtoRequestInfo> endpointMatches = findEndpointMatches(info,context);
        MatchSet roleMatchSet = findRoleMatchSet(info, endpointMatches);
        Set<DtoRequestInfo> roleFilteredEntries = roleMatchSet.matches.isEmpty() ? roleMatchSet.criteriaIndifferent : roleMatchSet.matches;
        MatchSet principalMatchSet = findPrincipalMatches(info, roleFilteredEntries);
        Set<DtoRequestInfo> matches = principalMatchSet.matches;
        Set<DtoRequestInfo> inDifferentPrincipalMatches = principalMatchSet.criteriaIndifferent;
        if (!matches.isEmpty()) {
            Assert.isTrue(matches.size() == 1, "Ambigious Mapping, found multiple Dto Matches: " + matches);
            DtoRequestInfo match = matches.stream().findFirst().get();
            log.debug("Matching DtoMappingEntry: " + match);
            return mappingEntries.get(match);
        } else {
            if (inDifferentPrincipalMatches.isEmpty()) {
//                throw new IllegalArgumentException("No DtoClass mapped for info: " + info);
                return null;
            }
            Assert.isTrue(inDifferentPrincipalMatches.size() == 1, "Ambigious Mapping, found multiple Dto Matches: " + inDifferentPrincipalMatches);
            DtoRequestInfo match = inDifferentPrincipalMatches.stream().findFirst().get();
            log.debug("Matching DtoMappingEntry: " + match);
            return mappingEntries.get(match);
        }
    }

    private MatchSet findPrincipalMatches(DtoRequestInfo userMappingInfo, Set<DtoRequestInfo> entries) {
        MatchSet principalMatchSet = new MatchSet();
        entries.stream().forEach(info -> {
            if (info.getPrincipal().equals(DtoRequestInfo.Principal.ALL)) {
                principalMatchSet.criteriaIndifferent.add(info);
            } else if (userMappingInfo.getPrincipal().equals(info.getPrincipal())) {
                principalMatchSet.matches.add(info);
            }
        });
        return principalMatchSet;
    }


    private Set<DtoRequestInfo> findEndpointMatches(DtoRequestInfo userMappingInfo, DtoMappingContext context) {
        return context.getMappingEntries().keySet().stream()
                .filter(info -> info.getDirection().equals(userMappingInfo.getDirection())
                        && info.getEndpoint().equals(userMappingInfo.getEndpoint()))
                .collect(Collectors.toSet());
    }


    private MatchSet findRoleMatchSet(DtoRequestInfo userMappingInfo, Set<DtoRequestInfo> entries) {
        MatchSet roleMatchSet = new MatchSet();
        entries.stream()
                .forEach(info -> {
                    boolean hasNeededRoles = true;
                    for (String neededRole : info.getAuthorities()) {
                        boolean hasRole = userMappingInfo.getAuthorities().contains(neededRole);
                        if (!hasRole) {
                            hasNeededRoles = false;
                        }
                    }
                    if (hasNeededRoles && !info.getAuthorities().isEmpty()) {
                        roleMatchSet.matches.add(info);
                    } else if (info.getAuthorities().isEmpty()) {
                        roleMatchSet.criteriaIndifferent.add(info);
                    }
                });
        return roleMatchSet;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Setter
    static class MatchSet {
        Set<DtoRequestInfo> matches = new HashSet<>();
        Set<DtoRequestInfo> criteriaIndifferent = new HashSet<>();
    }
}

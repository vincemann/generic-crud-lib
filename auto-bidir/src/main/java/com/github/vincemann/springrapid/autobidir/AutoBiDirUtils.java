package com.github.vincemann.springrapid.autobidir;

import com.github.vincemann.springrapid.core.proxy.CrudServiceExtension;
import com.github.vincemann.springrapid.core.service.CrudService;
import com.github.vincemann.springrapid.core.util.RepositoryUtil;
import org.aspectj.lang.JoinPoint;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.test.util.AopTestUtils;

import java.util.ArrayList;
import java.util.List;

public class AutoBiDirUtils {

//    public static List<Object> bannedBeans = new ArrayList<>();
    public static List<Class> bannedRepoEntityTypes = new ArrayList<>();

    public static Boolean isDisabled(JoinPoint joinPoint){
        Object proxied = AopTestUtils.getUltimateTargetObject(joinPoint.getTarget());
        if (proxied instanceof SimpleJpaRepository){
            Class<?> typeToCheck = RepositoryUtil.getRepoType((SimpleJpaRepository<?, ?>) proxied);
            if (bannedRepoEntityTypes.contains(typeToCheck)){
                return Boolean.TRUE;
            }
        }
        if (proxied instanceof CrudServiceExtension){
            return Boolean.TRUE;
        }
        if (proxied instanceof CrudService){
            Class<?> typeToCheck = ((CrudService<?, ?>) proxied).getEntityClass();
            if (bannedRepoEntityTypes.contains(typeToCheck)){
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }
}

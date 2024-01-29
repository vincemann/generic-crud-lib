package com.github.vincemann.springrapid.auth.service.token;

import org.springframework.stereotype.Component;

/**
 * signs jwt's and verifies jwt's signatures
 */
@Component
public interface JwsTokenService extends JwtService {
}

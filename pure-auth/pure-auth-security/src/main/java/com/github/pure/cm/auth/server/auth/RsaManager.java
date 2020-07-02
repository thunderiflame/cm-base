package com.github.pure.cm.auth.server.auth;

import com.github.pure.cm.common.core.util.encry.RsaUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.vote.AffirmativeBased;

/**
 * rsa 秘钥对管理器
 *
 * @author bairitan
 * @since 2019/11/18
 */
@Configuration
@RefreshScope
public class RsaManager implements ApplicationContextAware {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Value("${custom.config.auth-server.jwt-token-signer.public-key-redis}")
    private String publicKeyRedis;
    @Value("${custom.config.auth-server.jwt-token-signer.private-key-redis}")
    private String privateKeyRedis;

    public RsaUtil.RsaKey getRsaKey() {
        if (redisTemplate.hasKey(publicKeyRedis) && redisTemplate.hasKey(privateKeyRedis)) {
            RsaUtil.RsaKey rsaKey = new RsaUtil.RsaKey();
            rsaKey.setPrivateKey(RsaUtil.getPrivateKey(RsaUtil.decodeBase64(redisTemplate.opsForValue().get(privateKeyRedis))));

            rsaKey.setPublicKey(RsaUtil.getPublicKey(RsaUtil.decodeBase64(redisTemplate.opsForValue().get(publicKeyRedis))));
            return rsaKey;
        } else {
            RsaUtil.RsaKey key = RsaUtil.getKey(1024);
            redisTemplate.opsForValue().set(privateKeyRedis, RsaUtil.encodeBase64(key.getPrivateKey()));
            redisTemplate.opsForValue().set(publicKeyRedis, RsaUtil.encodeBase64(key.getPublicKey()));
            return key;
        }
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    }
}
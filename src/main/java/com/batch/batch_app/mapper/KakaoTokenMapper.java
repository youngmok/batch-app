package com.batch.batch_app.mapper;

import com.batch.batch_app.domain.KakaoToken;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface KakaoTokenMapper {

    Optional<KakaoToken> findLatestToken();

    void insertToken(KakaoToken token);

    void updateToken(KakaoToken token);
}

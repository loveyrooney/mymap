package com.mymap.domain.user;

import com.mymap.exception.BusinessException;
import com.mymap.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public long login(UserDTO userDTO) {
        Optional<User> getUser = userRepository.findByUserId(userDTO.getUserId());
        long result = getUser
                .map(user -> {
                    if(userDTO.getPassword().equals(user.getPassword()))
                    //if (passwordEncoder.matches(userDTO.getPassword(),user.getPassword()))
                        return user.getNo();
                    else
                        throw new BusinessException(ErrorCode.NOT_AUTHENTICATED);
                })
                .orElseThrow(()-> new BusinessException(ErrorCode.NOT_REGISTERED));
        return result;
    }

    @Override
    public void storeRefreshToken(String token, long userNo, Date expiration) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .userNo(userNo)
                .expiryDate(expiration)
                .build();
        refreshTokenRepository.save(refreshToken);
    }

    @Override
    public boolean isValid(long userNo) {
        return refreshTokenRepository.findByUserNo(userNo).isPresent();
    }

    @Override
    @Transactional
    public void deleteToken(long userNo) {
        refreshTokenRepository.deleteAllByUserNo(userNo);
    }
}

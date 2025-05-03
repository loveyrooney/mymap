package com.mymap.domain.user;

import com.mymap.exception.BusinessException;
import com.mymap.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
}

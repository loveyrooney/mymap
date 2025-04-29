package com.mymap.mymap;

import com.mymap.mymap.domain.user.User;
import com.mymap.mymap.domain.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

@SpringBootTest
public class DBtest {
    @Autowired
    private UserRepository userRepository;
    @Test
    public void test(){
        Optional<User> rooney = userRepository.findByUserId("rooney");
        System.out.println(rooney.get().getPassword());
    }
}

package com.ruppyrup.credentialmanager.dao;

import com.ruppyrup.credentialmanager.model.DAOUser;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.transaction.AfterTransaction;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
class UserDaoTest {

    @Autowired
    UserDao repo;

    @BeforeEach
    public void showCountBefore() {
        System.err.println("before: " + repo.count());
    }

    @AfterEach
    public void showCountAfter() {
        System.err.println("after: " + repo.count());
    }

    @AfterTransaction
    public void showCountAfterTransaction() {
        System.err.println("after tx: " + repo.count());
    }

    @Test
    public void testRollback() {
        repo.save(new DAOUser(1,"Rueprt", "fishy"));
        System.err.println("saved: " + repo.count());
    }


}
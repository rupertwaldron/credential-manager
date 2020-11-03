package com.ruppyrup.credentialmanager.service.impl;

import com.ruppyrup.credentialmanager.config.JwtContextManagerTestImpl;
import com.ruppyrup.credentialmanager.dao.CredentialDao;
import com.ruppyrup.credentialmanager.encryption.IEncryptionService;
import com.ruppyrup.credentialmanager.exceptions.CredentialNotFoundException;
import com.ruppyrup.credentialmanager.exceptions.ExistingUserException;
import com.ruppyrup.credentialmanager.exceptions.RequestMadeByNonOwner;
import com.ruppyrup.credentialmanager.model.Credential;
import com.ruppyrup.credentialmanager.model.CredentialDTO;
import com.ruppyrup.credentialmanager.model.UserDTO;
import com.ruppyrup.credentialmanager.service.CredentialService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DirtiesContext
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CredentialServiceImplTest {

    private UserDTO user1 = new UserDTO("javainuse", "javainuse");
    private UserDTO user2 = new UserDTO("ruppyrup", "ruppyrup");
    private UserDTO user3 = new UserDTO("bob", "bob");
    private CredentialDTO amazonUser1DTO = new CredentialDTO( "Amazon", "www.amazon.com", "pete", "football");
    private CredentialDTO pondUser1DTO = new CredentialDTO("PondPlanet", "www.pondplanet.com", "ruppyrup", "feelsick");
    private CredentialDTO jlUser1DTO = new CredentialDTO("John Lewis", "www.johnlewis.com", "rupert.waldron@yahoo.co.uk", "polly");
    private CredentialDTO ttDTO = new CredentialDTO("Tops tiles", "www.topstiles.com", "rupert.waldron@yahoo.co.uk", "tilly");
    private CredentialDTO pp2User1DTO = new CredentialDTO("PondPlanet", "www.pondplanet2.com", "Lee", "monster");
    private CredentialDTO prattUser1DTO = new CredentialDTO("Pratts Pods", "www.pp.com", "Simon", "gobsmack");
    private CredentialDTO amazonUser2DTO = new CredentialDTO("Amazon", "www.amazon.com", "rupert", "sweetpea");
    private CredentialDTO jlUser2DTO = new CredentialDTO("John Lewis", "www.johnlewis.com", "ruppyruyp@yahoo.co.uk", "deadsea");
    private CredentialDTO jlDTO = new CredentialDTO("John Lewis2", "www.johnlewis2.com", "ruppyruyp2@yahoo.co.uk", "deadsea2");

    private Credential amazonUser1;
    private Credential pondUser1;
    private Credential jlUser1;
    private Credential amazonUser2;
    private Credential jlUser2;


    @Autowired
    CredentialService credentialService;

    @Autowired
    JwtContextManagerTestImpl jwtContextManager;

    @Autowired
    CredentialDao credentialDao;

    @Autowired
    private JwtUserDetailsService userDetailsService;

    @Autowired
    IEncryptionService<Credential> encryptionService;

    @BeforeEach
    void setUp() throws ExistingUserException {
        userDetailsService.save(user1);
        userDetailsService.save(user2);
        userDetailsService.save(user3);
        jwtContextManager.setUser(user1.getUsername());
        amazonUser1 = credentialService.createCredential(amazonUser1DTO);
        amazonUser1.setUser(userDetailsService.getUser(user1.getUsername()));
        pondUser1 = credentialService.createCredential(pondUser1DTO);
        jlUser1 = credentialService.createCredential(jlUser1DTO);
        jwtContextManager.setUser(user2.getUsername());
        amazonUser2 = credentialService.createCredential(amazonUser2DTO);
        amazonUser2.setUser(userDetailsService.getUser(user2.getUsername()));
        jlUser2 = credentialService.createCredential(jlUser2DTO);
        jlUser2.setUser(userDetailsService.getUser(user2.getUsername()));
        credentialService.getAllCredentials().forEach(System.out::println);
    }

//    @AfterEach
//    void cleanUp() {
//        credentialDao.deleteAll();
//        userDetailsService.deleteUser(user1);
//        userDetailsService.deleteUser(user2);
//        userDetailsService.deleteUser(user3);
//    }

    @Test
    void getAllCredentials() {
        jwtContextManager.setUser(user1.getUsername());
        List<String> credentialNames1 = credentialService.getAllCredentials().stream().map(Credential::getCredentialName).collect(Collectors.toList());
        assertThat(credentialNames1).containsExactlyInAnyOrder(jlUser1.getCredentialName(), pondUser1.getCredentialName(), amazonUser1.getCredentialName());

        jwtContextManager.setUser(user2.getUsername());
        List<String> credentialNames2 = credentialService.getAllCredentials().stream().map(Credential::getCredentialName).collect(Collectors.toList());
        assertThat(credentialNames2).containsExactlyInAnyOrder(amazonUser2.getCredentialName(), jlUser2.getCredentialName());
    }

    @Test
    void getCredential() throws Exception {
        jwtContextManager.setUser(user1.getUsername());
        assertThat(credentialService.getCredential(amazonUser1.getUuid())).isEqualTo(encryptionService.decrypt(amazonUser1));

        jwtContextManager.setUser(user2.getUsername());
        assertThat(credentialService.getCredential(amazonUser2.getUuid())).isEqualTo(encryptionService.decrypt(amazonUser2));
    }

    @Test
    void createCredential() throws Exception {
        //when
        jwtContextManager.setUser(user1.getUsername());
        Credential created1 = credentialService.createCredential(ttDTO);

        jwtContextManager.setUser(user2.getUsername());
        Credential created2 = credentialService.createCredential(jlDTO);

        //then
        jwtContextManager.setUser(user1.getUsername());
        assertThat(new CredentialDTO(credentialService.getCredential(created1.getUuid()))).isEqualTo(ttDTO);
        jwtContextManager.setUser(user2.getUsername());
        assertThat(new CredentialDTO(credentialService.getCredential(created2.getUuid()))).isEqualTo(jlDTO);
    }

    @Test
    void updateCredential() throws Exception {
        //when
        jwtContextManager.setUser(user1.getUsername());
        Credential updated1 = credentialService.updateCredential(pondUser1.getUuid(), pp2User1DTO);

        //then
        assertThat(new CredentialDTO(encryptionService.decrypt(updated1))).isEqualTo(pp2User1DTO);
    }

    @Test
    void updateNonExistingCredential(){
        //when
        jwtContextManager.setUser(user1.getUsername());

        //then
        assertThrows(CredentialNotFoundException.class, () -> credentialService.updateCredential("a", prattUser1DTO));
    }

    @Test
    void deleteCredential() throws CredentialNotFoundException, RequestMadeByNonOwner {
        //when
        jwtContextManager.setUser(user1.getUsername());
        Credential credential = credentialService.deleteCredential(jlUser1.getUuid());

        //then
        assertThat(credential).isEqualTo(jlUser1);
        assertThrows(CredentialNotFoundException.class, () -> credentialService.updateCredential(jlUser1.getUuid(), jlUser1DTO));
    }

    @Test
    void getCredentialWillFailIfUserNotMatchCredentialUser() {
        jwtContextManager.setUser(user2.getUsername());
        assertThrows(CredentialNotFoundException.class, () -> credentialService.getCredential(amazonUser1.getUuid()));

        jwtContextManager.setUser(user1.getUsername());
        assertThrows(CredentialNotFoundException.class, () -> credentialService.getCredential(amazonUser2.getUuid()));
    }

    @Test
    void getAllCredentials_shouldReturnNothingForInvalidUser() {
        jwtContextManager.setUser(user3.getUsername());
        List<String> credentialNames1 = credentialService.getAllCredentials().stream().map(Credential::getCredentialName).collect(Collectors.toList());
        assertThat(credentialNames1).isEmpty();
    }

    @Test
    void updateCredential_shouldFailIfUserNotMatchCredentialUser() {
        //when
        jwtContextManager.setUser(user1.getUsername());

        //then
        assertThrows(CredentialNotFoundException.class, () -> credentialService.updateCredential(amazonUser2.getUuid(), amazonUser2DTO));
    }

    @Test
    void deleteCredential_shouldFailIfUserNotMatchCredentialUser() {
        //when
        jwtContextManager.setUser(user1.getUsername());

        //then
        assertThrows(CredentialNotFoundException.class, () -> credentialService.deleteCredential(amazonUser2.getUuid()));
    }
}
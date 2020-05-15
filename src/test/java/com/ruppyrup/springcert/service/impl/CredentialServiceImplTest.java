package com.ruppyrup.springcert.service.impl;

import com.ruppyrup.springcert.config.JwtContextManagerTestImpl;
import com.ruppyrup.springcert.dao.CredentialDao;
import com.ruppyrup.springcert.exceptions.CredentialNotFoundException;
import com.ruppyrup.springcert.exceptions.RequestMadeByNonOwner;
import com.ruppyrup.springcert.model.Credential;
import com.ruppyrup.springcert.model.CredentialDTO;
import com.ruppyrup.springcert.service.CredentialService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DirtiesContext
@SpringBootTest
@ActiveProfiles("test")
class CredentialServiceImplTest {

    private String user1 = "javainuse";
    private String user2 = "ruppyrup";
    private String user3 = "bob";
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

    @BeforeEach
    void setUp() {

        jwtContextManager.setUser(user1);
        amazonUser1 = credentialService.createCredential(amazonUser1DTO);
        pondUser1 = credentialService.createCredential(pondUser1DTO);
        jlUser1 = credentialService.createCredential(jlUser1DTO);
        jwtContextManager.setUser(user2);
        amazonUser2 = credentialService.createCredential(amazonUser2DTO);
        jlUser2 = credentialService.createCredential(jlUser2DTO);
        credentialService.getAllCredentials().forEach(System.out::println);
    }

    @AfterEach
    void cleanUp() {
        credentialDao.deleteAll();
    }

    @Test
    void getAllCredentials() {
        jwtContextManager.setUser(user1);
        List<String> credentialNames1 = credentialService.getAllCredentials().stream().map(Credential::getCredentialName).collect(Collectors.toList());
        assertThat(credentialNames1).containsExactlyInAnyOrder(jlUser1.getCredentialName(), pondUser1.getCredentialName(), amazonUser1.getCredentialName());

        jwtContextManager.setUser(user2);
        List<String> credentialNames2 = credentialService.getAllCredentials().stream().map(Credential::getCredentialName).collect(Collectors.toList());
        assertThat(credentialNames2).containsExactlyInAnyOrder(amazonUser2.getCredentialName(), jlUser2.getCredentialName());
    }

    @Test
    void getCredential() throws Exception {
        jwtContextManager.setUser(user1);
        assertThat(credentialService.getCredential(amazonUser1.getUuid())).isEqualTo(amazonUser1);

        jwtContextManager.setUser(user2);
        assertThat(credentialService.getCredential(amazonUser2.getUuid())).isEqualTo(amazonUser2);
    }

    @Test
    void createCredential() throws Exception {
        //when
        jwtContextManager.setUser(user1);
        Credential created1 = credentialService.createCredential(ttDTO);

        jwtContextManager.setUser(user2);
        Credential created2 = credentialService.createCredential(jlDTO);

        //then
        jwtContextManager.setUser(user1);
        assertThat(new CredentialDTO(credentialService.getCredential(created1.getUuid()))).isEqualTo(ttDTO);
        jwtContextManager.setUser(user2);
        assertThat(new CredentialDTO(credentialService.getCredential(created2.getUuid()))).isEqualTo(jlDTO);
    }

    @Test
    void updateCredential() throws Exception {
        //when
        jwtContextManager.setUser(user1);
        Credential updated1 = credentialService.updateCredential(pondUser1.getUuid(), pp2User1DTO);

        //then
        assertThat(new CredentialDTO(updated1)).isEqualTo(pp2User1DTO);
    }

    @Test
    void updateNonExistingCredential(){
        //when
        jwtContextManager.setUser(user1);

        //then
        assertThrows(CredentialNotFoundException.class, () -> credentialService.updateCredential("a", prattUser1DTO));
    }

    @Test
    void deleteCredential() throws CredentialNotFoundException, RequestMadeByNonOwner {
        //when
        jwtContextManager.setUser(user1);
        Credential credential = credentialService.deleteCredential(jlUser1.getUuid());

        //then
        assertThat(credential).isEqualTo(jlUser1);
        assertThrows(CredentialNotFoundException.class, () -> credentialService.updateCredential(jlUser1.getUuid(), jlUser1DTO));
    }

    @Test
    void getCredentialWillFailIfUserNotMatchCredentialUser() {
        jwtContextManager.setUser(user2);
        assertThrows(RequestMadeByNonOwner.class, () -> credentialService.getCredential(amazonUser1.getUuid()));

        jwtContextManager.setUser(user1);
        assertThrows(RequestMadeByNonOwner.class, () -> credentialService.getCredential(amazonUser2.getUuid()));
    }

    @Test
    void getAllCredentials_shouldReturnNothingForInvalidUser() {
        jwtContextManager.setUser(user3);
        List<String> credentialNames1 = credentialService.getAllCredentials().stream().map(Credential::getCredentialName).collect(Collectors.toList());
        assertThat(credentialNames1).isEmpty();
    }

    @Test
    void updateCredential_shouldFailIfUserNotMatchCredentialUser() {
        //when
        jwtContextManager.setUser(user1);

        //then
        assertThrows(RequestMadeByNonOwner.class, () -> credentialService.updateCredential(amazonUser2.getUuid(), amazonUser2DTO));
    }

    @Test
    void deleteCredential_shouldFailIfUserNotMatchCredentialUser() {
        //when
        jwtContextManager.setUser(user1);

        //then
        assertThrows(RequestMadeByNonOwner.class, () -> credentialService.deleteCredential(amazonUser2.getUuid()));
    }
}
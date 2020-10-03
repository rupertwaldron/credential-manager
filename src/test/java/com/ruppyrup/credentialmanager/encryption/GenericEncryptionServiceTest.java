package com.ruppyrup.credentialmanager.encryption;

import com.ruppyrup.credentialmanager.model.Credential;
import com.ruppyrup.credentialmanager.model.CredentialDTO;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;


class GenericEncryptionServiceTest {

    IEncryptionService<Credential> genericEncryptionService = new GenericEncryptionService<>("secretKey", "Blowfish");

    CredentialDTO credentialDTO1 = new CredentialDTO("Amazon", "www.amazon.com", "ruppyrup", "monkey");
    CredentialDTO credentialDTO2 = new CredentialDTO("Amazon", "www.amazon.com", "ruppyrup", "monkey");
    Credential credential1 = new Credential(credentialDTO1);


    @Test
    void encryptShouldEncryptObjectFields() {
        Credential encryptedObject = genericEncryptionService.encrypt(credential1);
        Assertions.assertThat(encryptedObject.getPassword()).isNotEqualTo("monkey");
        Assertions.assertThat(encryptedObject.getLogin()).isNotEqualTo("ruppyrup");
        Assertions.assertThat(encryptedObject.getCredentialName()).isEqualTo("Amazon");
        Assertions.assertThat(encryptedObject.getUrl()).isEqualTo("www.amazon.com");
        Credential decryptedObject = genericEncryptionService.decrypt(encryptedObject);
        Assertions.assertThat(credentialDTO2).isEqualTo(new CredentialDTO(decryptedObject));
    }
}
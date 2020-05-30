package com.ruppyrup.springcert.service.impl;

import com.ruppyrup.springcert.dao.CredentialDao;
import com.ruppyrup.springcert.exceptions.CredentialNotFoundException;
import com.ruppyrup.springcert.exceptions.RequestMadeByNonOwner;
import com.ruppyrup.springcert.jwt.JwtContextManager;
import com.ruppyrup.springcert.model.Credential;
import com.ruppyrup.springcert.model.CredentialDTO;
import com.ruppyrup.springcert.service.CredentialService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class CredentialServiceImpl implements CredentialService {

    private Counter credentialCount;

    @Autowired
    CredentialDao credentialDao;

    @Autowired
    JwtContextManager jwtContextManager;

    public CredentialServiceImpl(MeterRegistry meterRegistry) {
        credentialCount = meterRegistry.counter("storage.credential.count", "type", "credential");
    }

    @PostConstruct
    public void countCredentials() {
        credentialDao.findAll().forEach(cred -> credentialCount.increment());
    }

    @Override
    public List<Credential> getAllCredentials() {
        return credentialDao.findAllByUser(jwtContextManager.getAuthorizedUser());
    }

    @Override
    public Credential getCredential(String uuid) throws CredentialNotFoundException, RequestMadeByNonOwner {
        Credential foundCredential = getAuthorizedCredential(uuid);
        return foundCredential;
    }

    @Override
    public Credential createCredential(CredentialDTO credentialDto) {
        Credential credential = new Credential(credentialDto);
        credential.setUser(jwtContextManager.getAuthorizedUser());
        credentialCount.increment();
        return credentialDao.save(credential);
    }

    @Override
    public Credential updateCredential(String uuid, CredentialDTO credentialDTO) throws CredentialNotFoundException, RequestMadeByNonOwner {
        Credential credentialToUpdate = getAuthorizedCredential(uuid);
        credentialToUpdate.setUrl(credentialDTO.getUrl());
        credentialToUpdate.setLogin(credentialDTO.getLogin());
        credentialToUpdate.setCredentialName(credentialDTO.getCredentialName());
        credentialToUpdate.setPassword(credentialDTO.getPassword());
        return credentialDao.save(credentialToUpdate);
    }

    @Override
    public Credential deleteCredential(String uuid) throws CredentialNotFoundException, RequestMadeByNonOwner {
        Credential credentialToDelete = getAuthorizedCredential(uuid);
        if (credentialToDelete == null) return null;
        credentialDao.delete(credentialToDelete);
        credentialCount.increment(-1);
        return credentialToDelete;
    }

    private Credential getAuthorizedCredential(String uuid) throws CredentialNotFoundException, RequestMadeByNonOwner {
        Credential credentialToUpdate = credentialDao.findByUuid(uuid).orElseThrow(CredentialNotFoundException::new);
        if (!credentialToUpdate.getUser().equals(jwtContextManager.getAuthorizedUser()))
            throw new RequestMadeByNonOwner();
        return credentialToUpdate;
    }
}

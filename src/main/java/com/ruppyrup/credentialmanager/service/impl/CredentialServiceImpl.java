package com.ruppyrup.credentialmanager.service.impl;

import com.ruppyrup.credentialmanager.dao.CredentialDao;
import com.ruppyrup.credentialmanager.encryption.IEncryptionService;
import com.ruppyrup.credentialmanager.exceptions.CredentialNotFoundException;
import com.ruppyrup.credentialmanager.jwt.JwtContextManager;
import com.ruppyrup.credentialmanager.model.Credential;
import com.ruppyrup.credentialmanager.model.CredentialDTO;
import com.ruppyrup.credentialmanager.model.DAOUser;
import com.ruppyrup.credentialmanager.service.CredentialService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CredentialServiceImpl implements CredentialService {

    private Counter credentialCount;

    @Autowired
    private IEncryptionService<Credential> encryptionService;

    @Autowired
    CredentialDao credentialDao;

    @Autowired
    JwtContextManager jwtContextManager;

    @Autowired
    JwtUserDetailsService userService;

    public CredentialServiceImpl(MeterRegistry meterRegistry) {
        credentialCount = meterRegistry.counter("storage.credential.count", "type", "credential");
    }

    @PostConstruct
    public void countCredentials() {
        credentialDao.findAll().forEach(cred -> credentialCount.increment());
    }

    @Override
    public List<Credential> getAllCredentials() {
        DAOUser authorizedUser = userService.getUser(jwtContextManager.getAuthorizedUser());
        List<Credential> allByUser = credentialDao.findAllByUser(authorizedUser);
        return allByUser.stream()
                .map(credential -> encryptionService.decrypt(credential))
                .collect(Collectors.toList());
    }

    @Override
    public Credential getCredential(String uuid) throws CredentialNotFoundException {
        Credential foundCredential = getAuthorizedCredential(uuid);
        return encryptionService.decrypt(foundCredential);
    }

    @Override
    @Transactional
    public Credential createCredential(CredentialDTO credentialDto) {
        DAOUser authorizedUser = userService.getUser(jwtContextManager.getAuthorizedUser());
        Credential credential = new Credential(credentialDto);
        credential.setUser(authorizedUser);
        credentialCount.increment();
        return credentialDao.save(encryptionService.encrypt(credential));
    }

    @Override
    @Transactional
    public Credential updateCredential(String uuid, CredentialDTO credentialDTO) throws CredentialNotFoundException {
        Credential credentialToUpdate = encryptionService.decrypt(getAuthorizedCredential(uuid));
        credentialToUpdate.setUrl(credentialDTO.getUrl());
        credentialToUpdate.setLogin(credentialDTO.getLogin());
        credentialToUpdate.setCredentialName(credentialDTO.getCredentialName());
        credentialToUpdate.setPassword(credentialDTO.getPassword());
        return encryptionService.encrypt(credentialToUpdate);
//        return credentialDao.save(encryptionService.encrypt(credentialToUpdate));
    }

    @Override
    @Transactional
    public Credential deleteCredential(String uuid) throws CredentialNotFoundException {
        Credential credentialToDelete = getAuthorizedCredential(uuid);
        if (credentialToDelete == null) return null;
        credentialDao.delete(credentialToDelete);
        credentialCount.increment(-1);
        return credentialToDelete;
    }

    private Credential getAuthorizedCredential(String uuid) throws CredentialNotFoundException {
        DAOUser authorizedUser = userService.getUser(jwtContextManager.getAuthorizedUser());
        Credential credential = credentialDao.findByUuidAndUser(uuid, authorizedUser).orElseThrow(CredentialNotFoundException::new);
        return credential;
    }
}

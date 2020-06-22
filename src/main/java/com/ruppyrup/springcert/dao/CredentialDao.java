package com.ruppyrup.springcert.dao;

import com.ruppyrup.springcert.model.Credential;
import com.ruppyrup.springcert.model.DAOUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CredentialDao extends CrudRepository<Credential, Long> {
    List<Credential> findAllByUser(DAOUser user);
    Optional<Credential> findByUuidAndUser(String uuid, DAOUser user);

    //Credential findByCredentialNameAndUser(String CredentialName, String user);
}

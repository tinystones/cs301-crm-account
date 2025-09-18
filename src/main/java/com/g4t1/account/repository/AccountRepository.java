package com.g4t1.account.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.g4t1.account.entity.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, String>{

}

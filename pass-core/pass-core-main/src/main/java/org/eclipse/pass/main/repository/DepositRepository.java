package org.eclipse.pass.main.repository;

import org.eclipse.pass.object.model.Deposit;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface DepositRepository extends CrudRepository<Deposit, String> {

    @Query("select d.version from Deposit d where d.id = ?1")
    Long findDepositVersionById(Long depositId);
}

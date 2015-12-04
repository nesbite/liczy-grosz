package pl.edu.agh.iisg.to.to2project.service.impl;

import org.springframework.stereotype.Service;
import pl.edu.agh.iisg.to.to2project.domain.Account;
import pl.edu.agh.iisg.to.to2project.service.AccountService;
import pl.edu.agh.iisg.to.to2project.service.generic.CRUDServiceGeneric;

/**
 * @author Wojciech Pachuta.
 */
@Service
public class AccountServiceImpl extends CRUDServiceGeneric<Account, Long> implements AccountService {

//    @Autowired
//    protected void setDao(AccountDAO dao) {
//        this.dao = dao;
//    }

}

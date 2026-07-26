/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package startup;

import dao.RoleDao;
import entity.Role;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 *
 * @author acefonfo
 */

@Singleton
@Startup
public class RoleSeeder {

    private static final String[] DEFAULT_ROLES = {
        "ADMIN", 
        "DOCTOR", 
        "RECEPTIONIST", 
        "PATIENT"
    };

    @EJB
    private RoleDao roleDao;

    @PostConstruct
    public void seed() {
        for (String roleName : DEFAULT_ROLES) {
            if (roleDao.findByName(roleName) == null) {
                Role role = new Role();
                role.setName(roleName);
                roleDao.create(role);
            }
        }
    }
}
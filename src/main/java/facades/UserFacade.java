package facades;

import com.mysql.jdbc.StringUtils;
import security.IUserFacade;
import entity.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import org.jasypt.util.password.StrongPasswordEncryptor;
import security.IUser;

public class UserFacade implements IUserFacade {

    /*When implementing your own database for this seed, you should NOT touch any of the classes in the security folder
    Make sure your new facade implements IUserFacade and keeps the name UserFacade, and that your Entity User class implements 
    IUser interface, then security should work "out of the box" with users and roles stored in your database */
    private final Map<String, IUser> users = new HashMap<>();
    private EntityManagerFactory emf;
    StrongPasswordEncryptor spe = new StrongPasswordEncryptor();

    public UserFacade() {

    }

    public UserFacade(EntityManagerFactory emf) {
        this.emf = emf;
    }

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public User createUser(User user) {
        EntityManager em = getEntityManager();
        String EncryptedUserPassWord = spe.encryptPassword(user.getPassword());
        user.setPassword(EncryptedUserPassWord);
        try {
            em.getTransaction().begin();
            em.persist(user);
            em.persist(user);
            em.getTransaction().commit();
        } finally {
            em.close();

        }
        return user;
    }

    @Override
    public IUser getUserByUserId(String id) {
        EntityManager em = getEntityManager();
        User user = null;

        try {
            TypedQuery<User> q = em.createNamedQuery("User.findById", User.class);
            q.setParameter("userName", id);
            List<User> users = q.getResultList();
            System.out.println(users.get(0).getRolesAsStrings());
            return users.get(0);
        } finally {
            em.close();
        }

    }

    /*
  Return the Roles if users could be authenticated, otherwise null
     */
    @Override
    public List<String> authenticateUser(String userName, String password) {
        EntityManager em = getEntityManager();
        IUser user = null;
        try {
            Query q = em.createNamedQuery("User.findById", IUser.class);
            q.setParameter("userName", userName);
            user = (IUser) q.getSingleResult();
            System.out.println("test: " + user.getRolesAsStrings());
        } finally {
            em.close();
        }
        if (user != null && spe.checkPassword(password, user.getPassword())) {
            System.out.println(user.getPassword());
            return user.getRolesAsStrings();
        } else {
            return null;
        }
        //   return user != null && user.getPassword().equals(password) ? user.getRolesAsStrings() : null;
    }

}

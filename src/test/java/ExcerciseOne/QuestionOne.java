package ExcerciseOne;

import bank.dao.AccountDAOJPAImpl;
import bank.domain.Account;
import org.junit.*;
import org.junit.rules.ExpectedException;
import util.DatabaseCleaner;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Nekkyou on 25-4-2016.
 */
public class QuestionOne {
    /*
    Voor elke test moet je in ieder geval de volgende vragen beantwoorden:
        Wat is de waarde van asserties en printstatements?
            Corrigeer verkeerde asserties zodat de test ‘groen’ wordt.
        Welke SQL statements worden gegenereerd?
        Wat is het eindresultaat in de database?
        Verklaring van bovenstaande drie observaties.
    De antwoorden op de vragen kun je als commentaar bij de testen vastleggen.
     */

    EntityManagerFactory emf;
    EntityManager em;

    AccountDAOJPAImpl accountDAOJPAImpl;

    @Before
    public void setup() {
        emf = Persistence.createEntityManagerFactory("bankPU");
        em = emf.createEntityManager();

        DatabaseCleaner databaseCleaner = new DatabaseCleaner(em);
        try {
            databaseCleaner.clean();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            em = emf.createEntityManager();
        }

        accountDAOJPAImpl = new AccountDAOJPAImpl(em);
    }

    @After
    public void after() {

    }

    @Test
    public void PersistAndCommitTest() {
        Account account = new Account(111L);
        em.getTransaction().begin();
        em.persist(account);

        //DONE: verklaar en pas eventueel aan
        /*Hier wordt alleen een account aangemaakt,
         maar nog niet gecommit, dus er zijn nog geen accounts op dit moment.
         */
        assertNull(account.getId());
        em.getTransaction().commit();
        System.out.println("AccountId: " + account.getId());
        //DONE: verklaar en pas eventueel aan
        /*Op dit moment is het account gecommit.
         Deze staat nu dus wel in de database en kan dus opgehaald worden.
         */
        assertTrue(account.getId() > 0L);
    }

    @Test
    public void RollbackTest() {
        Account account = new Account(111L);
        em.getTransaction().begin();
        em.persist(account);
        assertNull(account.getId());
        em.getTransaction().rollback();
        // DONE code om te testen dat table account geen records bevat. Hint: bestudeer/gebruik AccountDAOJPAImpl
        // Code om te testen dat table account geen records bevat:
        // Eerst dachten we dat AssertNull ook werkte, maar het bleek dat er een lege list van accounts
        // uit findAll kwam
        Assert.assertEquals("Some accounts found", new ArrayList<Account>(), accountDAOJPAImpl.findAll());
    }

    @Test
    public void FlushTest() {
        Long expected = -100L;
        Account account = new Account(111L);
        account.setId(expected);
        em.getTransaction().begin();
        em.persist(account);
        //DONE: verklaar en pas eventueel aan
        /* AssertEquals en AssertNotEquals zijn omgewisseld
        Bij deze test is expected gelijk aan id, omdat deze al van te voren wordt ingesteld.
         */
        Assert.assertEquals(expected, account.getId());
        em.flush();
        //DONE: verklaar en pas eventueel aan
        /* AssertEquals en AssertNotEquals zijn omgewisseld
        In dit geval zijn ze niet gelijk, aangezien de data is geflushed en dus teruggezet naar
        een eerdere situatie
         */
        Assert.assertNotEquals(expected, account.getId());
        em.getTransaction().commit();
        //DONE: verklaar en pas eventueel aan
        /*Het account is nu gecommit en staat dus in de database.
         */
        Assert.assertNotNull("None accounts found", accountDAOJPAImpl.findAll());
    }

    @Test
    public void ChangeAfterPersistTest() {
        Long expectedBalance = 400L;
        Account account = new Account(114L);
        em.getTransaction().begin();
        em.persist(account);
        account.setBalance(expectedBalance);
        em.getTransaction().commit();
        Assert.assertEquals(expectedBalance, account.getBalance());
        //DONE: verklaar de waarde van account.getBalance
        /*Het nieuwe balans van het gemaakte account is gecommit.
         Daarom geeft de methode getBalance() 400 terug.
         */
        Long acId = account.getId();
        account = null;
        EntityManager em2 = emf.createEntityManager();
        em2.getTransaction().begin();
        Account found = em2.find(Account.class, acId);
        //DONE: verklaar de waarde van found.getBalance
        /*Er wordt in de database gezocht naar een account met het id dat gelijk is aan het eerder toegevoegde account.
         Het gevonden account krijgt de variabele naam "found".
         Het balans is in de tussentijd nog niet veranderd dus geeft found.getBalance() 400 terug.
         */
        Assert.assertEquals(expectedBalance, found.getBalance());
    }

    @Test
    public void RefreshTest() {
        /*
        In de vorige opdracht verwijzen de objecten account en found naar dezelfde rij in de database.
        Pas een van de objecten aan, persisteer naar de database.
        Refresh vervolgens het andere object om de veranderde state uit de database te halen.
        Test met asserties dat dit gelukt is.
         */

        Long expectedBalance = 400L;
        Account account = new Account(114L);
        em.getTransaction().begin();
        em.persist(account);
        account.setBalance(expectedBalance);
        em.getTransaction().commit();
        Long acId = account.getId();
        EntityManager em2 = emf.createEntityManager();
        em2.getTransaction().begin();
        Account found = em2.find(Account.class, acId);
        /*
        hier wordt de balans veranderd bij 'account', dit wordt gepersist en gecommit
        met de refresh op de tweede entitymanager wordt voor found de waarde opnieuw ingesteld
        omdat account en found naar hetzelfde record verwijzen, wordt de balans van account en found weer gelijk
         */
        account.setBalance(12L);
        em.getTransaction().begin();
        em.persist(account);
        em.getTransaction().commit();
        Assert.assertNotEquals(account.getBalance(), found.getBalance());
        em2.refresh(found);
        Assert.assertEquals(account.getBalance(), found.getBalance());
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void MergeTest() {
        Account acc = new Account(1L);
        Account acc2 = new Account(2L);
        Account acc9 = new Account(9L);

        // scenario 1
        Long balance1 = 100L;
        em.getTransaction().begin();
        em.persist(acc);
        acc.setBalance(balance1);
        em.getTransaction().commit();
        //TODO: voeg asserties toe om je verwachte waarde van de attributen te verifieren.
        /*Account 1 is gecreëerd en het banksaldo is vervolgens aangepast.
         Daarna is het gelijk gecommit dus is de verandering opgeslagen in de databse.
         Alleen account 1 is gecommit dus de andere accounts zullen niet aanwezig zijn in de database.
         */
        Assert.assertSame("Balance not set correctly", 100L, acc.getBalance());
        assertTrue(acc.getId() > 0L);
        assertNull(acc2.getId());
        assertNull(acc9.getId());
        //TODO: doe dit zowel voor de bovenstaande java objecten als voor opnieuw bij de entitymanager opgevraagde objecten met overeenkomstig Id.
        Assert.assertNotNull("Account not found", accountDAOJPAImpl.findByAccountNr(1L));
        try {
            Assert.assertNull(accountDAOJPAImpl.findByAccountNr(2L));
        } catch (NoResultException e) {
            Assert.assertNotNull(e.getMessage());
        }
        try {
            Assert.assertNull("Account found", accountDAOJPAImpl.findByAccountNr(9L));
        } catch(NoResultException e){
            Assert.assertNotNull(e.getMessage());
        }

        // scenario 2
        Long balance2a = 211L;
        acc = new Account(2L);
        em.getTransaction().begin();
        acc9 = em.merge(acc);
        acc.setBalance(balance2a);
        acc9.setBalance(balance2a + balance2a);
        em.getTransaction().commit();
        //TODO: voeg asserties toe om je verwachte waarde van de attributen te verifiëren.
        /*Account 1 is opnieuw geinitialiseerd en het banksaldo is vervolgens aangepast.
         acc9 is gemerged met acc1 waardoor deze dus ook in de database is gezet.
         Het saldo van acc9 is ook aangepast en zal 422 moeten bedragen (2 * 211)
         */
        Assert.assertEquals("Balance not set correctly", new Long(211L), acc.getBalance());
        Assert.assertEquals("Balance not set correctly", new Long(422L), acc9.getBalance());
        assertNull(acc.getId());
        assertTrue(acc9.getId() > 0L);
        //TODO: doe dit zowel voor de bovenstaande java objecten als voor opnieuw bij de entitymanager opgevraagde objecten met overeenkomstig Id.
        // HINT: gebruik acccountDAO.findByAccountNr
        Assert.assertNotNull("Account not found", accountDAOJPAImpl.findByAccountNr(2L));
        try {
            Assert.assertNull("Account found", accountDAOJPAImpl.findByAccountNr(9L));
        } catch(NoResultException e){
            Assert.assertNotNull(e.getMessage());
        }


        // scenario 3
        Long balance3b = 322L;
        Long balance3c = 333L;
        acc = new Account(3L);
        em.getTransaction().begin();
        acc2 = em.merge(acc);
        assertTrue(em.contains(acc)); // verklaar
        assertTrue(em.contains(acc2)); // verklaar
        Assert.assertEquals(acc, acc2); //verklaar
        acc2.setBalance(balance3b);
        acc.setBalance(balance3c);
        em.getTransaction().commit();
        //TODO: voeg asserties toe om je verwachte waarde van de attributen te verifiëren.
        //TODO: doe dit zowel voor de bovenstaande java objecten als voor opnieuw bij de entitymanager opgevraagde objecten met overeenkomstig Id.


        // scenario 4
        Account account = new Account(114L);
        account.setBalance(450L);
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(account);
        em.getTransaction().commit();

        Account account2 = new Account(114L);
        Account tweedeAccountObject = account2;
        tweedeAccountObject.setBalance(650l);
        Assert.assertEquals((Long) 650L, account2.getBalance());  //verklaar
        account2.setId(account.getId());
        em.getTransaction().begin();
        account2 = em.merge(account2);
        Assert.assertSame(account, account2);  //verklaar
        assertTrue(em.contains(account2));  //verklaar
        Assert.assertFalse(em.contains(tweedeAccountObject));  //verklaar
        tweedeAccountObject.setBalance(850l);
        Assert.assertEquals((Long) 650L, account.getBalance());  //verklaar
        Assert.assertEquals((Long) 650L, account2.getBalance());  //verklaar
        em.getTransaction().commit();
        em.close();
    }

    @Test
    public void FindAndClearTest() {
        Account acc1 = new Account(77L);
        em.getTransaction().begin();
        em.persist(acc1);
        em.getTransaction().commit();
        //Database bevat nu een account.

        // scenario 1
        Account accF1;
        Account accF2;
        accF1 = em.find(Account.class, acc1.getId());
        accF2 = em.find(Account.class, acc1.getId());
        Assert.assertSame(accF1, accF2);

        // scenario 2
        accF1 = em.find(Account.class, acc1.getId());
        em.clear();
        accF2 = em.find(Account.class, acc1.getId());
        Assert.assertSame(accF1, accF2);
        //TODO verklaar verschil tussen beide scenario's
    }

    @Test
    public void RemoveTest() {
        Account acc1 = new Account(88L);
        em.getTransaction().begin();
        em.persist(acc1);
        em.getTransaction().commit();
        Long id = acc1.getId();
        //Database bevat nu een account.

        em.remove(acc1);
        Assert.assertEquals(id, acc1.getId());
        Account accFound = em.find(Account.class, id);
        assertNull(accFound);
        //TODO: verklaar bovenstaande asserts
    }

    @Test
    public void GenerationTypeTest() {

    }
}

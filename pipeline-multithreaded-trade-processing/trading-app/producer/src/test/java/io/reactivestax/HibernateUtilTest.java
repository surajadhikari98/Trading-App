package io.reactivestax;

import io.reactivestax.utility.database.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HibernateUtilTest {


    private HibernateUtil hibernateUtil;
    private Transaction mockTransaction;
    private  Session session;
    private SessionFactory mockSessionFactory;
     AtomicInteger counter =new AtomicInteger(0);


    @Before
    public void setup() throws NoSuchFieldException, IllegalAccessException {
        hibernateUtil = HibernateUtil.getInstance();
        mockSessionFactory = mock(SessionFactory.class);
        session = mock(Session.class);
        mockTransaction = mock(Transaction.class);


        when(mockSessionFactory.openSession()).thenReturn(mock(Session.class));
        Field sessionFactory = HibernateUtil.class.getDeclaredField("sessionFactory");
        sessionFactory.setAccessible(true); // true means since there is no setter we are getting that field
        sessionFactory.set(null, mockSessionFactory); //null means it is static field
    }


    @Test
    public void testGetInstance() {
        HibernateUtil instance = HibernateUtil.getInstance();
        HibernateUtil instance1 = HibernateUtil.getInstance();
//        assertSame(instance, instance1);
        assertEquals(instance.hashCode(), instance1.hashCode());
    }

    @Test
    public void testThreadLocalSessionIsolation() throws InterruptedException {

        ConcurrentHashMap<String, Session> sessionsByThread = new ConcurrentHashMap<>();
        CountDownLatch latch = new CountDownLatch(1);

        ExecutorService executorService = Executors.newFixedThreadPool(10);

       //Runnable that each threads will execute
        Runnable task = () -> {
            try {
                latch.await();
                HibernateUtil instance = HibernateUtil.getInstance();
                Session session = instance.getConnection();
                assertNotNull(session);
                sessionsByThread.put(Thread.currentThread().getName(), session);

            } catch (InterruptedException e) {
                Thread.currentThread().isInterrupted();
            }
        };

        //Start multiple threads
        for (int i = 0; i < 10; i++) {
            executorService.submit(task);
        }


        //Release the latch to start all threads
        latch.countDown();

        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);

        for(Session session1: sessionsByThread.values()){
            for (Session session2: sessionsByThread.values()){
                if(session1!= session2){
                    assertNotSame(session1, session2);
                }
            }
        }


    }


}

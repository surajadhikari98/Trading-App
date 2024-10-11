package io.reactivestax.hibernate;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import io.reactivestax.hibernate.entity.Address;
import io.reactivestax.hibernate.entity.Order;
import io.reactivestax.hibernate.entity.Product;
import io.reactivestax.hibernate.entity.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;


public class HibernateCRUD {
    public static void main(String[] args) {
        // option-1
        SessionFactory factory = new Configuration()
                .configure("hibernate.cfg.xml")
                .addAnnotatedClass(User.class)
                .addAnnotatedClass(Address.class)
                .addAnnotatedClass(Order.class)
                .addAnnotatedClass(Product.class)
                .buildSessionFactory();

        // option-2
        // SessionFactory factory = new Configuration()
        // .configure("hibernate.cfg.xml")
        // .buildSessionFactory();

        Session session = factory.openSession();
        // Session session = factory.getCurrentSession();

        try {
            User user = persistUserWithOrdersAndProducts(session);

            System.out.println("Saved User ID=" + user.getId());

            readEntities(factory, user);

            updateEntities(factory, user);

            // deleteEntities(factory, user);

        } finally {
            factory.close();
        }
    }

    private static User persistUserWithOrdersAndProducts(Session session) { //session is similar to the connection
        // Create objects
        User user = new User();
        user.setUsername("john_doe");

        Address address = new Address();
        address.setStreet("123 Main St");
        address.setCity("Metropolis");
        user.setAddress(address);

        Order order1 = new Order();
        order1.setOrderDate(LocalDate.now());

        Order order2 = new Order();
        order2.setOrderDate(LocalDate.now().plusDays(1));

        Product product1 = new Product();
        product1.setName("Laptop");
        product1.setPrice(800.0);

        Product product2 = new Product();
        product2.setName("Phone");
        product2.setPrice(500.0);

        // Set relationships
        order1.setUser(user);
        order2.setUser(user);

        // order1.setProducts(Arrays.asList(product1, product2));
        // order2.setProducts(Collections.singletonList(product1));

        user.setOrders(Arrays.asList(order1, order2));

        // Start transaction and save data
        Transaction transaction = session.beginTransaction();

        session.persist(user); // This will cascade save address, orders, and products

        transaction.commit();
        return user;
    }

    // FOR REFERENCE PURPOSES - @Transactional(Transactional.TxType.REQUIRED)
    // @Transactional
    private static void readEntities(SessionFactory factory, User user) {
        Session session = null;
        try {
            /// read
            session = factory.openSession();
            session.beginTransaction();

            // one way to load the user object using the HQL
            List<User> users = session.createQuery("from User u where u.id=" + user.getId(),
                    User.class).getResultList();
            for (User userVar : users) {
                System.out.println(userVar.toString());
            }

            // second way to load the user object
            User user2 = session.get(User.class, user.getId()); // Fetch user by ID
            System.out.println("User Details: " + user2.getUsername());
            System.out.println("Address: " + user2.getAddress().getStreet());

            // for (Order order : user.getOrders()) {
            // System.out.println("Order Date: " + order.getOrderDate());
            // for (Product product : order.getProducts()) {
            // System.out.println("Product: " + product.getName() + ", Price: " +
            // product.getPrice());
            // }
            // }
            session.getTransaction().commit();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    private static void updateEntities(SessionFactory factory, User user) {
        Session session = null;
        try {
            session = factory.openSession();
            session.beginTransaction();

            User user3 = session.get(User.class, user.getId());
            user3.setUsername("jane_doe" + user.getId()); // Update username
            user3.getAddress().setCity("Gotham" + user.getId()); // Update address

            session.getTransaction().commit();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    private static void deleteEntities(SessionFactory factory, User user) {
        Session session = null;
        try {
            session = factory.openSession();
            session.beginTransaction();

            User user4 = session.get(User.class, user.getId());
            if (user4 != null) {
                session.remove(user4); // This will delete the user and associated address, orders (cascade)
            } else {
                System.out.println("User not found!");
            }
            session.getTransaction().commit();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

}
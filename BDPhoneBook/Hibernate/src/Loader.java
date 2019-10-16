import entities.PhoneBook;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Loader {

    private static Pattern patternFIO = Pattern.compile("([А-ЯЁ][а-яё]++)\\s([А-ЯЁ][а-яё]++)\\s([А-ЯЁ][а-яё]++)");
    private static Pattern patternPhone = Pattern.compile("\\s*+\\+?(\\d{1})\\s*+\\(?(\\d{3})\\)?\\s*+(\\d{3})\\s*+\\-*+(\\d{2})\\s*+\\-*+(\\d{2})\\s*+");
    private static SessionFactory sessionFactory;

    public static void main(String[] args) {

        setUp();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        for (; ; ) {

            System.out.println("\r\nВведите номер телефона или ФИО (полностью), либо команду: \r\n" +
                    "wr - для создания контактов;\r\n" +
                    "del - для удаления контакта по номеру телефона или ФИО\r\n" +
                    "LIST - для распечатки полного списка;\r\n" +
                    "exit - для выхода из программы.");
            try {
                String command = reader.readLine().trim();

                if(command.equals("exit")) {
                    Session session = beginSession();
                    PhoneBook phoneBook = (PhoneBook) session.createQuery("FROM PhoneBook WHERE fio=Ли").list();

//        Department dept = (Department) session.createQuery("FROM Department WHERE name=Отдел производства").list().get(0);
//        session.delete(dept);
                    System.out.println("Найден контакт с id = " + phoneBook.getId() + " " + phoneBook.getFio() + " " + phoneBook.getPhone());
                    endSession(session);
                    System.out.println("До новых встреч!");
                    if (sessionFactory != null) {
                        sessionFactory.close();
                    }
                    break;
                }else if (command.equals("LIST")) {
                    contactList();
                }else if (command.equals("wr")) {
                    createList();
                }else if (command.equals("del")) {
                    System.out.println("Для удаления контакта введите номер телефона в формате 8XXXXXXXXXX (только цифры)\r\n" +
                            " или ФИО полностью через пробелы");
                    String phoneOrFIO = reader.readLine().trim();

                    if (textIncorrectInput(phoneOrFIO, patternPhone)) {
                        search(command, phoneOrFIO,"phone");
                    }else if (textIncorrectInput(phoneOrFIO, patternFIO)){
                        search(command, phoneOrFIO,"fio");
                    }
                }else if (textIncorrectInput(command, patternFIO)){
                    search(command, command,"fio");
                }else if (textIncorrectInput(command, patternPhone)){
                    search(command, command, "phone");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
        //==================================================================
//        if (sessionFactory != null) {
//            sessionFactory.close();
//        }

    public static Boolean textIncorrectInput(String text, Pattern pattern) {

        Matcher matcher = pattern.matcher(text);
        Boolean result = matcher.matches();
        if (result){
            return true;
        }else return false;
    }

    private static void endSession(Session session) {
        session.getTransaction().commit();
        session.close();
    }

    private static Session beginSession() {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        return session;
    }

    private static void contactList() {
        Session session = beginSession();
        List<PhoneBook> phoneBook = getPhoneBook(session);
        System.out.println("Список контактов:");
        for (PhoneBook element : phoneBook) {
            System.out.println(element.getFio() + " " + element.getPhone());
        }
        endSession(session);
    }

    private static void search(String command, String phoneOrFIO, String element) {

        Session session = beginSession();
        try {
            PhoneBook phoneBook = (PhoneBook) session.createQuery("FROM PhoneBook WHERE " + element + "=" + phoneOrFIO).list().get(0);

//        Department dept = (Department) session.createQuery("FROM Department WHERE name=Отдел производства").list().get(0);
//        session.delete(dept);
            System.out.println("Найден контакт с id = " + phoneBook.getId() + " " + phoneBook.getFio() + " " + phoneBook.getPhone());

            if (command.equals("del")){
            del(session, phoneBook);
            }

            endSession(session);
        }catch (Exception e){
            System.out.println("Контакт не найден. Для добавления контакта введите: wr");
            session.getTransaction().rollback();
            session.close();
            e.getStackTrace();
        }
    }

    private static void del(Session session, PhoneBook phoneBook) {
        session.delete(phoneBook);
        System.out.println("Удален контакт с id = " + phoneBook.getId());
    }

    private static void createList() {
        System.out.println("Ведите полностью ФИО и нажмите enter:\r\n");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        Session session = null;
        try {
            String fio = reader.readLine().trim();
            System.out.println("Введите номер телефона в формате 8XXXXXXXXXX");
            String phone = reader.readLine().trim();
            
             if ((textIncorrectInput(fio, patternFIO))&&(textIncorrectInput(phone, patternPhone))) {
                 phone = phone.replaceAll("[^0-9]","");
                 session = beginSession();
                 PhoneBook phoneBook = new PhoneBook(fio, phone);
                 session.save(phoneBook);
                 System.out.println("Сохранены контакты с id = " + phoneBook.getId());
                 endSession(session);
             }else {
                 System.out.println("Неверно введены данные:\r\n" +
                         "ФИО необходимо полностью вводить через 1 пробел.\r\n" +
                         "В телефоне должны быть только цифры.");
             }
        } catch (IOException e) {
            session.getTransaction().rollback();
            session.close();
            e.printStackTrace();
        }
    }

    private static List<PhoneBook> getPhoneBook(Session session) {
        return (List<PhoneBook>) session.createQuery("FROM PhoneBook").list();
    }

    //=====================================================================

    private static void setUp() {

        final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure(new File("src/config/hibernate.cfg.xml")) // configures settings from hibernate.config.xml
                .build();

        try {
            sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
        } catch (Exception e) {
            // The registry would be destroyed by the SessionFactory, but we had trouble building the SessionFactory
            // so destroy it manually.
            StandardServiceRegistryBuilder.destroy(registry);
            throw e;
        }
    }
}

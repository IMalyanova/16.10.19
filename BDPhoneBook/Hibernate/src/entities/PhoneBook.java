package entities;


public class PhoneBook {

    private Integer id;
    private String fio;
    private String phone;


    public PhoneBook() {
        //Used by Hibernate
    }


    public PhoneBook(String fio, String phone) {
        this.fio = fio;
        this.phone = phone;
    }

    public PhoneBook(String fio) {
        this.fio = fio;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFio() {
        return fio;
    }

    public void setFio(String fio) {
        this.fio = fio;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

}

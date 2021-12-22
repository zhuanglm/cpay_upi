package sdk.models;

public class Consumer {
    private String first_name;
    private String last_name;
    private String phone;
    private String email;
    private String reference;

    public String getFirstName() {
        return first_name;
    }

    public String getLastName() {
        return last_name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getReference() {
        return reference;
    }


    public Consumer(String first_name, String last_name, String phone, String email, String reference) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.phone = phone;
        this.email = email;
        this.reference = reference;
    }
}

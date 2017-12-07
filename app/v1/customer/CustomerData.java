package v1.customer;

import javax.persistence.*;

/**
 * Data returned from the database
 */
@Entity
@Table(name = "customer")
public class CustomerData {

    public CustomerData() {
    }

    public CustomerData(String name, String location) {
        this.name = name;
        this.location = location;
    }

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    public Long id;
    public String name;
    public String location;
}
